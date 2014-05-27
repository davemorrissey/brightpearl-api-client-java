/*
 * Copyright 2014 David Morrissey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.visalia.brightpearl.apiclient;

import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.junit.Rule;
import org.junit.Test;
import uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient.ApiSession;
import uk.co.visalia.brightpearl.apiclient.exception.*;
import uk.co.visalia.brightpearl.apiclient.multimessage.MultiRequestBuilder;
import uk.co.visalia.brightpearl.apiclient.multimessage.MultiResponse;
import uk.co.visalia.brightpearl.apiclient.multimessage.OnFailOption;
import uk.co.visalia.brightpearl.apiclient.multimessage.ServiceResponse;
import uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequest;
import uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequestBuilder;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.github.restdriver.serverdriver.file.FileHelper.fromFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class BrightpearlApiClientMultiMessageTest extends ClientDriverTestSupport {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule(CLIENT_DRIVER_PORT);

    // Internal server error from MM API
    @Test(expected=BrightpearlServiceException.class)
    public void multi_mmApiErrorCode() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/internal_server_error.json"), JSON_CONTENT_TYPE)
                        .withStatus(500)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")))
                .withAddedRequest(postBrand(Brand.brand().withName("Samsung")));

        session.execute(request);

    }

    // Request cap error from MM API
    @Test(expected=BrightpearlRequestCapException.class)
    public void multi_mmRequestCapError() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/request_cap.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")))
                .withAddedRequest(postBrand(Brand.brand().withName("Samsung")));

        session.execute(request);

    }

    // Request cap error from MM API
    @Test(expected=BrightpearlUnavailableException.class)
    public void multi_mmOther503Error() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/string_response.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")))
                .withAddedRequest(postBrand(Brand.brand().withName("Samsung")));

        session.execute(request);

    }

    // MM API returns not authenticated error
    @Test(expected=BrightpearlAuthException.class)
    public void multi_mmNotAuthenticated() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/not_authenticated.json"), JSON_CONTENT_TYPE)
                        .withStatus(401)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")))
                .withAddedRequest(postBrand(Brand.brand().withName("Samsung")));

        session.execute(request);

    }

    // Response from MM is corrupt JSON
    @Test(expected=BrightpearlClientException.class)
    public void multi_mmCorruptContainer() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/corrupt.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")))
                .withAddedRequest(postBrand(Brand.brand().withName("Samsung")));

        session.execute(request);

    }

    // Response from MM API doesn't contain an MM response (processed messages details)
    @Test(expected=BrightpearlClientException.class)
    public void multi_mmInvalidContainer() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/order/order_status_get.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")))
                .withAddedRequest(postBrand(Brand.brand().withName("Samsung")));

        session.execute(request);

    }

    // All requests successful
    @Test
    public void multi_mmSuccess() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/multi/brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();
        ServiceWriteRequest<Integer> request2 = postBrand(Brand.brand().withName("Samsung")).withRuid("at2").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1)
                .withAddedRequest(request2);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(200));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(200));
        assertThat(response1.getResponse(), is(111));

        ServiceResponse<Integer> response2 = multiResponse.getServiceResponse(request2);
        assertThat(response2.getStatus(), is(200));
        assertThat(response2.getResponse(), is(222));

    }

    // One success one failure
    @Test
    public void multi_mmPartSuccess() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/multi/brand_post_part_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(207)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();
        ServiceWriteRequest<Integer> request2 = postBrand(Brand.brand().withName("Samsung")).withRuid("at2").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1)
                .withAddedRequest(request2);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(207));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(200));
        assertThat(response1.isSuccess(), is(true));
        assertThat(response1.getResponse(), is(111));

        ServiceResponse<Integer> response2 = multiResponse.getServiceResponse(request2);
        assertThat(response2.getStatus(), is(400));
        assertThat(response2.isSuccess(), is(false));
        try {
            response2.getResponse();
        } catch (BrightpearlServiceException e) {
            assertThat(e.getServiceErrors().get(0).getCode(), is("PRDC-024"));
        }

    }

    // One of the response bodies is corrupt json
    @Test
    public void multi_mmPartCorrupt() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/multi/brand_post_part_corrupt.json"), JSON_CONTENT_TYPE)
                        .withStatus(207)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();
        ServiceWriteRequest<Integer> request2 = postBrand(Brand.brand().withName("Samsung")).withRuid("at2").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1)
                .withAddedRequest(request2);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(207));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(200));
        assertThat(response1.isSuccess(), is(true));
        assertThat(response1.getResponse(), is(111));

        ServiceResponse<Integer> response2 = multiResponse.getServiceResponse(request2);
        assertThat(response2.getStatus(), is(200));
        assertThat(response2.isSuccess(), is(false));
        try {
            response2.getResponse();
        } catch (BrightpearlClientException e) {
            assertThat(e.getMessage().contains("INVALID_RESPONSE_FORMAT"), is(true));
        }

    }

    // One of the response bodies has a response element of the wrong type
    @Test
    public void multi_mmPartUnexpected() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/multi/brand_post_part_unexpected.json"), JSON_CONTENT_TYPE)
                        .withStatus(207)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();
        ServiceWriteRequest<Integer> request2 = postBrand(Brand.brand().withName("Samsung")).withRuid("at2").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1)
                .withAddedRequest(request2);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(207));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(200));
        assertThat(response1.isSuccess(), is(true));
        assertThat(response1.getResponse(), is(111));

        ServiceResponse<Integer> response2 = multiResponse.getServiceResponse(request2);
        assertThat(response2.getStatus(), is(200));
        assertThat(response2.isSuccess(), is(false));
        try {
            response2.getResponse();
        } catch (BrightpearlClientException e) {
            assertThat(e.getMessage().contains("INVALID_RESPONSE_TYPE"), is(true));
        }

    }

    // One of the response bodies is unexpectedly blank
    @Test
    public void multi_mmPartBlank() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/multi/brand_post_part_blank.json"), JSON_CONTENT_TYPE)
                        .withStatus(207)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();
        ServiceWriteRequest<Integer> request2 = postBrand(Brand.brand().withName("Samsung")).withRuid("at2").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1)
                .withAddedRequest(request2);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(207));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(200));
        assertThat(response1.isSuccess(), is(true));
        assertThat(response1.getResponse(), is(111));

        ServiceResponse<Integer> response2 = multiResponse.getServiceResponse(request2);
        assertThat(response2.getStatus(), is(200));
        assertThat(response2.isSuccess(), is(false));
        try {
            response2.getResponse();
        } catch (BrightpearlClientException e) {
            assertThat(e.getMessage().contains("EMPTY_RESPONSE"), is(true));
        }

    }

    // One request fails and the subsequent one is not processed
    @Test
    public void multi_mmPartUnprocessed() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/multi/brand_post_part_unprocessed.json"), JSON_CONTENT_TYPE)
                        .withStatus(207)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();
        ServiceWriteRequest<Integer> request2 = postBrand(Brand.brand().withName("Samsung")).withRuid("at2").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1)
                .withAddedRequest(request2);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(207));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(400));
        assertThat(response1.isSuccess(), is(false));

        ServiceResponse<Integer> response2 = multiResponse.getServiceResponse(request2);
        assertThat(response2, is(nullValue()));

        assertThat(multiResponse.getUnprocessedRequestRuids().size(), is(1));
        assertThat(multiResponse.getUnprocessedRequestRuids().get(0), is("at2"));

    }

    // Single request is sent direct, authentication error looks the same as it would from MM API
    @Test(expected=BrightpearlAuthException.class)
    public void single_notAuthenticated() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/brand")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/not_authenticated.json"), JSON_CONTENT_TYPE)
                        .withStatus(401)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")));

        session.execute(request);

    }

    // Single request is sent direct, transport error looks the same as it would from MM API
    @Test(expected=BrightpearlHttpException.class)
    public void single_transportError() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/brand")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/not_authenticated.json"), JSON_CONTENT_TYPE)
                        .withStatus(401)
                        .after(2, TimeUnit.SECONDS)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")));

        session.execute(request);

    }

    // Single request is sent direct, rejected due to request cap
    @Test(expected=BrightpearlRequestCapException.class)
    public void single_requestCap() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/brand")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/request_cap.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")));

        session.execute(request);

    }

    // Single request is sent direct, rejected due to other 503
    @Test(expected=BrightpearlUnavailableException.class)
    public void single_other503() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/brand")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/string_response.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(postBrand(Brand.brand().withName("Nokia")));

        session.execute(request);

    }

    // Single request is sent direct, and succeeds, response is wrapped as a multi response
    @Test
    public void single_success() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/brand")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/product/brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(200));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(200));
        assertThat(response1.isSuccess(), is(true));
        assertThat(response1.getResponse(), is(123));

    }

    // Single request is sent direct, and gets a service exception, response is wrapped as a multi response
    @Test
    public void single_serviceException() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/brand")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/product/brand_post_errors.json"), JSON_CONTENT_TYPE)
                        .withStatus(400)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(207));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(400));
        assertThat(response1.isSuccess(), is(false));
        try {
            response1.getResponse();
        } catch (BrightpearlServiceException e) {
            assertThat(e.getServiceErrors().get(0).getCode(), is("PRDC-024"));
        }

    }

    // Single request is sent direct, and gets a corrupt response, which is wrapped as a multi response
    @Test
    public void single_corrupt() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/brand")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/corrupt.json"), JSON_CONTENT_TYPE)
                        .withStatus(400)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(207));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(400));
        assertThat(response1.isSuccess(), is(false));
        try {
            response1.getResponse();
        } catch (BrightpearlClientException e) {
            assertThat(e.getMessage().contains("INVALID_RESPONSE_FORMAT"), is(true));
        }

    }

    // One of the response bodies has a response element of the wrong type
    @Test
    public void single_unexpected() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/brand")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/contact/contact_get.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        ServiceWriteRequest<Integer> request1 = postBrand(Brand.brand().withName("Nokia")).withRuid("at1").build();

        MultiRequestBuilder request = MultiRequestBuilder.newMultiRequest()
                .withAddedRequest(request1);

        MultiResponse multiResponse = session.execute(request);
        assertThat(multiResponse.getStatus(), is(200));

        ServiceResponse<Integer> response1 = multiResponse.getServiceResponse(request1);
        assertThat(response1.getStatus(), is(200));
        assertThat(response1.isSuccess(), is(false));
        try {
            response1.getResponse();
        } catch (BrightpearlClientException e) {
            assertThat(e.getMessage().contains("INVALID_RESPONSE_TYPE"), is(true));
        }

    }

    // Two batches are successfully processed
    @Test @SuppressWarnings("unchecked")
    public void batches_success() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand10.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_1_10_brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand15.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_11_15_brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        MultiResponse multiResponse = session.execute(createMultiRequest(15));
        assertThat(multiResponse.getStatus(), is(200));
        assertThat(multiResponse.getUnprocessedRequestRuids().size(), is(0));

        for (int i = 1; i < 15; i++) {
            ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
            assertThat(response.getStatus(), is(200));
            assertThat(response.getResponse(), is(i * 1000));
        }

    }

    // First batch gets a 207 and OnFail=STOP so second batch is not processed.
    @Test @SuppressWarnings("unchecked")
    public void batches_firstBatchFail_stop() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand10.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_1_10_brand_post_part_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(207)
        );

        MultiResponse multiResponse = session.execute(createMultiRequest(15).withOnFailOption(OnFailOption.STOP));
        assertThat(multiResponse.getStatus(), is(207));
        assertThat(multiResponse.getUnprocessedRequestRuids().size(), is(6));

        for (int i = 1; i < 15; i++) {
            if (i == 9) {
                ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
                assertThat(response.getStatus(), is(400));
                assertThat(response.isSuccess(), is(false));
                try {
                    response.getResponse();
                } catch (BrightpearlServiceException e) {
                    assertThat(e.getServiceErrors().get(0).getCode(), is("PRDC-024"));
                }
            } else if (i < 9) {
                ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
                assertThat(response.getStatus(), is(200));
                assertThat(response.getResponse(), is(i * 1000));
            } else {
                assertThat(multiResponse.getUnprocessedRequestRuids().contains(Integer.toString(i)), is(true));
            }
        }

    }

    // First batch gets a 207 but OnFail=CONTINUE so carry on.
    @Test @SuppressWarnings("unchecked")
    public void batches_firstBatchFail_continue() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand10.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_1_10_brand_post_part_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(207)
        );
        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand15.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_11_15_brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        MultiResponse multiResponse = session.execute(createMultiRequest(15).withOnFailOption(OnFailOption.CONTINUE));
        assertThat(multiResponse.getStatus(), is(207));
        assertThat(multiResponse.getUnprocessedRequestRuids().size(), is(1));

        for (int i = 1; i < 15; i++) {
            if (i == 9) {
                ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
                assertThat(response.getStatus(), is(400));
                assertThat(response.isSuccess(), is(false));
                try {
                    response.getResponse();
                } catch (BrightpearlServiceException e) {
                    assertThat(e.getServiceErrors().get(0).getCode(), is("PRDC-024"));
                }
            } else if (i == 10) {
                assertThat(multiResponse.getUnprocessedRequestRuids().contains(Integer.toString(i)), is(true));
            } else {
                ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
                assertThat(response.getStatus(), is(200));
                assertThat(response.getResponse(), is(i * 1000));
            }
        }

    }

    // Internal server error from MM API on first batch
    @Test(expected=BrightpearlServiceException.class)
    public void batches_batch1_ApiErrorCode() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/internal_server_error.json"), JSON_CONTENT_TYPE)
                        .withStatus(500)
        );

        session.execute(createMultiRequest(15));

    }

    // Request cap server error from MM API on first batch
    @Test(expected=BrightpearlRequestCapException.class)
    public void batches_batch1_RequestCapError() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/request_cap.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        session.execute(createMultiRequest(15));

    }

    // Other 503 server error from MM API on first batch
    @Test(expected=BrightpearlUnavailableException.class)
    public void batches_batch1_Other503Error() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/string_response.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        session.execute(createMultiRequest(15));

    }

    // MM API returns not authenticated error on first batch
    @Test(expected=BrightpearlAuthException.class)
    public void batches_batch1_notAuthenticated() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/not_authenticated.json"), JSON_CONTENT_TYPE)
                        .withStatus(401)
        );

        session.execute(createMultiRequest(15));

    }

    // Response from MM is corrupt JSON for first batch
    @Test(expected=BrightpearlClientException.class)
    public void batches_batch1_corruptContainer() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/error/corrupt.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        session.execute(createMultiRequest(15));

    }

    // Response from MM API doesn't contain an MM response (processed messages details)
    @Test(expected=BrightpearlClientException.class)
    public void batches_batch1_invalidContainer() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST),
                giveResponse(fromFile("json/order/order_status_get.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );

        session.execute(createMultiRequest(15));

    }

    // Second batch gets an auth exception, which is treated as a part failure so the results of the first can be returned
    @Test @SuppressWarnings("unchecked")
    public void batches_batch2_authException() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand10.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_1_10_brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand15.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/error/not_authenticated.json"), JSON_CONTENT_TYPE)
                        .withStatus(401)
        );

        MultiResponse multiResponse = session.execute(createMultiRequest(15));
        assertThat(multiResponse.getStatus(), is(207));
        assertThat(multiResponse.getUnprocessedRequestRuids().size(), is(5));

        for (int i = 1; i < 15; i++) {
            if (i <= 10) {
                ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
                assertThat(response.getStatus(), is(200));
                assertThat(response.getResponse(), is(i * 1000));
            } else {
                assertThat(multiResponse.getUnprocessedRequestRuids().contains(Integer.toString(i)), is(true));
            }
        }

    }

    // Second batch gets a service exception, which is treated as a part failure so the results of the first can be returned
    @Test @SuppressWarnings("unchecked")
    public void batches_batch2_serviceException() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand10.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_1_10_brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand15.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/error/internal_server_error.json"), JSON_CONTENT_TYPE)
                        .withStatus(500)
        );

        MultiResponse multiResponse = session.execute(createMultiRequest(15));
        assertThat(multiResponse.getStatus(), is(207));
        assertThat(multiResponse.getUnprocessedRequestRuids().size(), is(5));

        for (int i = 1; i < 15; i++) {
            if (i <= 10) {
                ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
                assertThat(response.getStatus(), is(200));
                assertThat(response.getResponse(), is(i * 1000));
            } else {
                assertThat(multiResponse.getUnprocessedRequestRuids().contains(Integer.toString(i)), is(true));
            }
        }

    }

    // Second batch gets a request cap error, which is treated as a part failure so the results of the first can be returned
    @Test @SuppressWarnings("unchecked")
    public void batches_batch2_RequestCapError() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand10.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_1_10_brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand15.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/error/request_cap.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        MultiResponse multiResponse = session.execute(createMultiRequest(15));
        assertThat(multiResponse.getStatus(), is(207));
        assertThat(multiResponse.getUnprocessedRequestRuids().size(), is(5));

        for (int i = 1; i < 15; i++) {
            if (i <= 10) {
                ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
                assertThat(response.getStatus(), is(200));
                assertThat(response.getResponse(), is(i * 1000));
            } else {
                assertThat(multiResponse.getUnprocessedRequestRuids().contains(Integer.toString(i)), is(true));
            }
        }

    }

    // Second batch gets a service exception, which is treated as a part failure so the results of the first can be returned
    @Test @SuppressWarnings("unchecked")
    public void batches_batch2_transportError() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand10.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_1_10_brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        driver.addExpectation(
                onRequestTo("/public-api/visalia/multi-message")
                        .withMethod(Method.POST)
                        .withBody(Pattern.compile(".*brand15.*"), Pattern.compile(".*")),
                giveResponse(fromFile("json/multi/batch_11_15_brand_post_success.json"), JSON_CONTENT_TYPE)
                        .withStatus(500)
                        .after(2, TimeUnit.SECONDS)
        );

        MultiResponse multiResponse = session.execute(createMultiRequest(15));
        assertThat(multiResponse.getStatus(), is(207));
        assertThat(multiResponse.getUnprocessedRequestRuids().size(), is(5));

        for (int i = 1; i < 15; i++) {
            if (i <= 10) {
                ServiceResponse<Integer> response = multiResponse.getServiceResponse(Integer.toString(i));
                assertThat(response.getStatus(), is(200));
                assertThat(response.getResponse(), is(i * 1000));
            } else {
                assertThat(multiResponse.getUnprocessedRequestRuids().contains(Integer.toString(i)), is(true));
            }
        }

    }

    private MultiRequestBuilder createMultiRequest(int count) {
        MultiRequestBuilder builder = MultiRequestBuilder.newMultiRequest();
        for (int i = 1; i <= count; i++) {
            Brand brand = Brand.brand().withName("brand" + i);
            builder.withAddedRequest(ServiceWriteRequestBuilder.newPostRequest(ServiceName.PRODUCT, "/brand", brand, Integer.class).withRuid(Integer.toString(i)));
        }
        return builder;
    }

    private static ServiceWriteRequestBuilder<Integer> postBrand(Brand brand) {
        return ServiceWriteRequestBuilder.newPostRequest(ServiceName.PRODUCT, "/brand", brand, Integer.class);
    }

    private static class Brand {

        private String name;

        private static Brand brand() {
            return new Brand();
        }

        private Brand withName(String name) {
            this.name = name;
            return this;
        }

    }

}
