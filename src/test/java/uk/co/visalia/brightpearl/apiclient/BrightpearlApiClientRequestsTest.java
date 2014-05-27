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
import uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlRequestCapException;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlUnavailableException;
import uk.co.visalia.brightpearl.apiclient.request.ServiceReadRequestBuilder;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.github.restdriver.serverdriver.file.FileHelper.fromFile;

public class BrightpearlApiClientRequestsTest extends ClientDriverTestSupport {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule(CLIENT_DRIVER_PORT);

    @Test(expected=BrightpearlRequestCapException.class)
    public void testRequestCapExceeded() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/contact-service/contact/200")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/error/request_cap.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact/200", Object.class));
    }

    @Test(expected=BrightpearlUnavailableException.class)
    public void testOther503() {

        ApiSession session = basicApiSession();

        driver.addExpectation(
                onRequestTo("/public-api/visalia/contact-service/contact/200")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/error/string_response.json"), JSON_CONTENT_TYPE)
                        .withStatus(503)
        );

        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact/200", Object.class));
    }

}
