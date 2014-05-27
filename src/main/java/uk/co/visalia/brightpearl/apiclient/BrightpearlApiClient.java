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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import uk.co.visalia.brightpearl.apiclient.account.Account;
import uk.co.visalia.brightpearl.apiclient.account.UserCredentials;
import uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation;
import uk.co.visalia.brightpearl.apiclient.auth.PrivateAppIdentity;
import uk.co.visalia.brightpearl.apiclient.auth.PublicAppIdentity;
import uk.co.visalia.brightpearl.apiclient.client.UserCredentialsWrapper;
import uk.co.visalia.brightpearl.apiclient.client.adaptors.CalendarAdaptor;
import uk.co.visalia.brightpearl.apiclient.client.adaptors.DateTimeAdaptor;
import uk.co.visalia.brightpearl.apiclient.client.multimessage.MultiMessage;
import uk.co.visalia.brightpearl.apiclient.client.multimessage.MultiMessageItem;
import uk.co.visalia.brightpearl.apiclient.client.multimessage.MultiMessageResponse;
import uk.co.visalia.brightpearl.apiclient.client.multimessage.MultiMessageResponseItem;
import uk.co.visalia.brightpearl.apiclient.client.parsing.JsonWrapper;
import uk.co.visalia.brightpearl.apiclient.client.parsing.PartialSearchResponse;
import uk.co.visalia.brightpearl.apiclient.exception.*;
import uk.co.visalia.brightpearl.apiclient.http.*;
import uk.co.visalia.brightpearl.apiclient.http.httpclient4.HttpClient4ClientFactoryBuilder;
import uk.co.visalia.brightpearl.apiclient.multimessage.*;
import uk.co.visalia.brightpearl.apiclient.ratelimit.NoOpRateLimiter;
import uk.co.visalia.brightpearl.apiclient.ratelimit.RateLimiter;
import uk.co.visalia.brightpearl.apiclient.request.*;
import uk.co.visalia.brightpearl.apiclient.search.SearchColumn;
import uk.co.visalia.brightpearl.apiclient.search.SearchResults;
import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * <p>
 * Main client class for the Brightpearl API. This supports public and private apps of both types, system and staff. An
 * instance of this class is designed to be shared amongst many apps and accounts if necessary.
 * </p><p>
 * An instance is thread-safe assuming the instance of {@link uk.co.visalia.brightpearl.apiclient.http.ClientFactory} provided is
 * thread-safe, as all implementations provided with this library are. Instances are not specific to any account and can
 * be safely reused for multiple accounts.
 * </p>
 */
public final class BrightpearlApiClient {

    private static final String MULTIMESSAGE_TEMPLATE = "%s/public-api/%s/multi-message";
    private static final String MULTIMESSAGE_SERVICE_TEMPLATE = "/%s/%s";
    private static final String SERVICE_TEMPLATE = "%s/public-api/%s/%s/%s";
    private static final String AUTH_TEMPLATE = "%s/%s/authorise";

    private final ClientFactory clientFactory;
    private final RateLimiter rateLimiter;
    private final Gson gson;

    /**
     * Package private constructor for internal use only. {@link BrightpearlApiClientFactory} must be used to create new instances.
     */
    BrightpearlApiClient(ClientFactory clientFactory, RateLimiter rateLimiter, Gson gson) {
        this.clientFactory = clientFactory == null ? new HttpClient4ClientFactoryBuilder().build() : clientFactory;
        this.rateLimiter = rateLimiter == null ? new NoOpRateLimiter() : rateLimiter;
        this.gson = gson == null ? defaultGson() : gson;
    }

    /**
     * Using Brightpearl account details and user credentials, attempts authentication of the user and returns an auth
     * token if the request was successful. If the credentials are invalid, an {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlServiceException} will
     * be thrown, with an error code that, in most cases, starts with "GWY".
     * @param account Details of customer account to authenticate with.
     * @param userCredentials Credentials of an API-enabled user in the account.
     * @return an auth token for the user.
     */

    public String fetchLegacyAuthToken(Account account, UserCredentials userCredentials) {

        String url = buildAuthUrl(account);
        String jsonBody = gson.toJson(new UserCredentialsWrapper(userCredentials));

        Request request = RequestBuilder.newRequest().withMethod(Method.POST).withUrl(url).withBody(jsonBody).build();
        Response response = getClient(account).execute(request);
        sendRateLimitHeaders(account, response);

        return parseBasicEntity(response, stringType(), true);

    }

    /**
     * Using private app details and user credentials, attempts authentication of the user and returns their staff token
     * if they are authorised to use the app. If the credentials are invalid, an {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlServiceException}
     * will be thrown, with an error code that, in most cases, starts with "GWY".
     * @param appIdentity Details of a private app, including the Brightpearl customer account.
     * @param userCredentials Credentials of user in the account.
     * @return a staff token for the user.
     */
    public String fetchStaffToken(PrivateAppIdentity appIdentity, UserCredentials userCredentials) {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put(AppAuthorisation.APP_HEADER, appIdentity.getAppReference());
        return fetchStaffToken(appIdentity.getAccount(), headerMap, userCredentials);
    }

    /**
     * Using private app details and user credentials, attempts authentication of the user and returns their staff token
     * if they are authorised to use the app. If the credentials are invalid, an {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlServiceException}
     * will be thrown, with an error code that, in most cases, starts with "GWY".
     * @param appIdentity Details of a public app.
     * @param account A Brightpearl customer's account details.
     * @param userCredentials Credentials of user in the account.
     * @return a staff token for the user.
     */
    public String fetchStaffToken(PublicAppIdentity appIdentity, Account account, UserCredentials userCredentials) {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put(AppAuthorisation.DEV_HEADER, appIdentity.getDeveloperReference());
        headerMap.put(AppAuthorisation.APP_HEADER, appIdentity.getAppReference());
        return fetchStaffToken(account, headerMap, userCredentials);
    }

    /**
     * Internal method for fetching a staff token, supports both public and private.
     * @param account Brightpearl account.
     * @param headers App identification headers.
     * @param userCredentials User's credentials.
     * @return a staff token for the user.
     */
    private String fetchStaffToken(Account account, Map<String, String> headers, UserCredentials userCredentials) {
        String url = buildAuthUrl(account);
        String jsonBody = gson.toJson(new UserCredentialsWrapper(userCredentials));

        Request request = RequestBuilder.newRequest()
                .withMethod(Method.POST)
                .withUrl(url)
                .withHeaders(headers)
                .withBody(jsonBody)
                .build();

        Response response = getClient(account).execute(request);
        sendRateLimitHeaders(account, response);

        return parseBasicEntity(response, stringType(), true);

    }

    /**
     * Creates a session an app and account.
     * @return an {@link ApiSession} instance. This contains a reference to the {@link BrightpearlApiClient instance}.
     */
    public ApiSession createSession(AppAuthorisation authorisation) {
        if (authorisation == null) {
            throw new IllegalArgumentException("Authorisation details must be supplied");
        }
        return new ApiSession(authorisation);
    }

    /**
     * An authenticated API session configured with a Brightpearl customer account and headers needed to authenticate calls
     * for a single app of any type. This provides a shortcut to using the methods on the parent {@link BrightpearlApiClient}
     * class, each of which requires identity and credentials to be passed to every call.
     */
    public class ApiSession {

        private final AppAuthorisation authorisation;

        /**
         * Creates an API session authorised for calls to a single account from a single app. Supports all types.
         * @param authorisation Private app authorisation details.
         */
        private ApiSession(AppAuthorisation authorisation) {
            this.authorisation = authorisation;
        }

        /**
         * Convenience shortcut for {@link #get(ServiceReadRequest)}. Calls build on the
         * supplied builder and makes the call to the API.
         * @param serviceRequestBuilder Builder for read request to be executed.
         * @param <T> Type of expected response.
         * @return If the request was successful, a response of the expected type.
         */
        public <T> T get(ServiceReadRequestBuilder<T> serviceRequestBuilder) {
            return get(serviceRequestBuilder.build());
        }

        /**
         * Make a GET or OPTIONS call to the API, and extract the expected response entity from the JSON returned. If a valid
         * cached response is available this will be returned. Subclasses of {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException} are thrown if
         * there is a failure, and other runtime exceptions may be thrown by custom implementations of {@link uk.co.visalia.brightpearl.apiclient.http.ClientFactory}.
         * @param serviceRequest A read request to be made.
         * @param <T> Type of expected response.
         * @return If the request was successful, a response of the expected type.
         */
        public <T> T get(ServiceReadRequest<T> serviceRequest) {
            return BrightpearlApiClient.this.get(authorisation, serviceRequest);
        }

        /**
         * Convenience shortcut for {@link #search(uk.co.visalia.brightpearl.apiclient.request.ServiceSearchRequest)}. Calls build on the
         * supplied builder and makes the call to the API.
         * @param serviceRequestBuilder Builder for search request to be executed.
         * @param <T> Type of expected response.
         * @return If the request was successful, a {@link SearchResults} object containing parsed results and meta information.
         */
        public <T> SearchResults<T> search(SearchRequestBuilder<T> serviceRequestBuilder) {
            return search(serviceRequestBuilder.build());
        }

        /**
         * <p>
         * Makes a GET call to a search API, and deserialises the response into a {@link SearchResults} object that contains
         * meta information, reference data and a list of results.
         * </p><p>
         * If a valid cached response is available this will be returned. Transport errors and other request failures are
         * thrown as subclasses of {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException} unless a custom HTTP client or rate limiter implementation
         * throws a different type.
         * </p><p>
         * This method uses the column names returned in the search metadata to populate objects of the requested type with
         * values from each array in the results. Therefore the expected response type declared in the search request should
         * be a flat object (i.e. only primitives, Strings and Dates) with fields either named or annotated with
         * {@link com.google.gson.annotations.SerializedName} according to the column names expected. In many cases, the
         * more complex type used for resource GETs will be unsuitable for use with the corresponding resource search.
         * </p>
         * @param serviceRequest The search request to be executed.
         * @param <T> Type of expected response.
         * @return If the request was successful, a {@link SearchResults} object containing parsed results and meta information.
         */
        public <T> SearchResults<T> search(ServiceSearchRequest<T> serviceRequest) {
            return BrightpearlApiClient.this.search(authorisation, serviceRequest);
        }

        /**
         * Convenience shortcut for {@link #execute(uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequest)}. Calls build on the supplied builder and makes
         * the call to the API.
         * @param serviceRequestBuilder Builder for write request to be executed.
         * @param <T> Type of expected response. Use {@link Void}.class if no response body is expected.
         * @return If the request was successful, a response of the expected type, null if the declared type was {@link Void}.
         */
        public <T> T execute(ServiceWriteRequestBuilder<T> serviceRequestBuilder) {
            return execute(serviceRequestBuilder.build());
        }

        /**
         * Make a POST, PUT or DELETE call to the API. Subclasses of {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException} are thrown if there
         * is a failure, and other runtime exceptions may be thrown by custom implementations of {@link uk.co.visalia.brightpearl.apiclient.http.ClientFactory}.
         * @param serviceRequest A write request to be made.
         * @param <T> Type of expected response. Use {@link Void}.class if no response body is expected.
         * @return If the request was successful, a response of the expected type, null if the declared type was {@link Void}.
         */
        public <T> T execute(ServiceWriteRequest<T> serviceRequest) {
            return BrightpearlApiClient.this.execute(authorisation, serviceRequest);
        }

        /**
         * Convenience method equivalent to {@link #execute(MultiRequest)}, this calls {@link MultiRequestBuilder#build()}
         * on the supplied builder for executing the request.
         * @param multiRequestBuilder Multimessage request builder to be executed.
         * @return a {@link MultiResponse} object containing the results.
         */
        public MultiResponse execute(MultiRequestBuilder multiRequestBuilder) {
            return execute(multiRequestBuilder.build());
        }

        /**
         * <p>
         * Executes a multi message request. Multi-messages can decrease latency when sending multiple write requests, and
         * count as only one request for rate limiting purposes. Use {@link MultiRequestBuilder} to assemble a multi message.
         * </p><p>
         * Errors executing the multi message request itself are thrown as exceptions from this method and generally indicate
         * that none of the individual requests have been executed by Brightpearl, though this cannot be guaranteed. For
         * example if the connection was broken after the request was sent, the result cannot be recovered. Errors in the
         * responses for individual requests are stored in the {@link uk.co.visalia.brightpearl.apiclient.multimessage.ServiceResponse} corresponding to the request, so they
         * can be handled individually.
         * </p><p>
         * The Brightpearl multi message API supports sending messages containing between two and ten and individual requests.
         * This client simplifies the use of this API by supporting any number of individual requests. If the supplied multi
         * message wrapper contains only one request it will be sent direct to the relevant API, and if the wrapper contains
         * more than ten requests it will be split into batches of ten and executed in serial. In both cases this method
         * attempts to mimic the behaviour that would be expected if all the requests had been sent in one multi-message.
         * However there are limitations to be aware of.
         * </p><p>
         * <b>Single request</b>: If the {@link MultiRequest} supplied contains only one request, it is sent direct to the
         * relevant API. HTTP transport errors are thrown by this method, and the response from the Brightpearl API is
         * returned in a mocked up {@link MultiResponse} wrapper with the wrapper status set to 200 if the individual
         * response had status 200, and 207 otherwise.
         * </p><p>
         * <b>More than ten requests</b>: The requests are split up into batches of between two and ten (batches of one are
         * avoided for consistency) and sent to the multi message API in sequence. HTTP transport exceptions will only be
         * thrown if the first batch fails with these errors; if a later batch fails with a transport error that batch and
         * all following ones will be aborted (regardless of the {@link OnFailOption}) and their request IDs included in
         * the unprocessedRequestIds field of the response. This is done so that the results of any successful batches can
         * be returned and inspected, and the list of unprocessed IDs handled, but it doesn't match the behaviour of the
         * Brightpearl API. If the status code of a batch is not 200, and the {@link OnFailOption} is {@link OnFailOption#STOP},
         * later batches are not sent, otherwise all batches will be run barring transport errors.
         * </p><p>
         * A batch of between two and ten requests is guaranteed to be sent as a single multi message, and therefore will
         * avoid behaviour that may not exactly match that provided by Brightpearl. Clients that have complex error recovery
         * requirements should avoid sending less than two or more than ten requests. The {@link uk.co.visalia.brightpearl.apiclient.multimessage.MultiMessageUtils#split(MultiRequest)}
         * utility may be used to split a request into batches.
         * </p>
         * @param multiRequest Multimessage request to be executed.
         * @return a {@link MultiResponse} object containing the results.
         */
        public MultiResponse execute(MultiRequest multiRequest) {
            return BrightpearlApiClient.this.execute(authorisation, multiRequest);
        }

        /**
         * <p>
         * Executes a request and returns the response as an unparsed string of JSON together with the HTTP status
         * code and headers, exactly as returned by the {@link uk.co.visalia.brightpearl.apiclient.http.Client} implementation.
         * This method bypasses the detection of failure response codes and error messages in the response body provided
         * by other methods, so does not throw specific exceptions such as {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlAuthException}.
         * Rate limiting is supported.
         * </p><p>
         * This method may be used to execute all types of request.
         * </p><p>
         * An exception will only be thrown for HTTP transport errors, which will usually result in a {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException}
         * unless a custom HTTP client implementation throws a different type.
         * </p>
         * @param serviceRequest Request to be sent.
         * @return Unprocessed HTTP response.
         */
        public Response getHttpResponse(AbstractServiceRequest serviceRequest) {
            return BrightpearlApiClient.this.getHttpResponse(authorisation, serviceRequest);
        }

    }

    /**
     * Convenience method equivalent to {@link #execute(AppAuthorisation, MultiRequest)}, this calls {@link MultiRequestBuilder#build()}
     * on the supplied builder for executing the request.
     * @param authorisation App and account authorisation details.
     * @param multiRequestBuilder Multimessage request builder to be executed.
     * @return a {@link MultiResponse} object containing the results.
     */
    public MultiResponse execute(AppAuthorisation authorisation, MultiRequestBuilder multiRequestBuilder) {
        return execute(authorisation, multiRequestBuilder.build());
    }

    /**
     * <p>
     * Executes a multi message request. Multi-messages can decrease latency when sending multiple write requests, and
     * count as only one request for rate limiting purposes. Use {@link MultiRequestBuilder} to assemble a multi message.
     * </p><p>
     * Errors executing the multi message request itself are thrown as exceptions from this method and generally indicate
     * that none of the individual requests have been executed by Brightpearl, though this cannot be guaranteed. For
     * example if the connection was broken after the request was sent, the result cannot be recovered. Errors in the
     * responses for individual requests are stored in the {@link uk.co.visalia.brightpearl.apiclient.multimessage.ServiceResponse} corresponding to the request, so they
     * can be handled individually.
     * </p><p>
     * The Brightpearl multi message API supports sending messages containing between two and ten and individual requests.
     * This client simplifies the use of this API by supporting any number of individual requests. If the supplied multi
     * message wrapper contains only one request it will be sent direct to the relevant API, and if the wrapper contains
     * more than ten requests it will be split into batches of ten and executed in serial. In both cases this method
     * attempts to mimic the behaviour that would be expected if all the requests had been sent in one multi-message.
     * However there are limitations to be aware of.
     * </p><p>
     * <b>Single request</b>: If the {@link MultiRequest} supplied contains only one request, it is sent direct to the
     * relevant API. HTTP transport errors are thrown by this method, and the response from the Brightpearl API is
     * returned in a mocked up {@link MultiResponse} wrapper with the wrapper status set to 200 if the individual
     * response had status 200, and 207 otherwise.
     * </p><p>
     * <b>More than ten requests</b>: The requests are split up into batches of between two and ten (batches of one are
     * avoided for consistency) and sent to the multi message API in sequence. HTTP transport exceptions will only be
     * thrown if the first batch fails with these errors; if a later batch fails with a transport error that batch and
     * all following ones will be aborted (regardless of the {@link OnFailOption}) and their request IDs included in
     * the unprocessedRequestIds field of the response. This is done so that the results of any successful batches can
     * be returned and inspected, and the list of unprocessed IDs handled, but it doesn't match the behaviour of the
     * Brightpearl API. If the status code of a batch is not 200, and the {@link OnFailOption} is {@link OnFailOption#STOP},
     * later batches are not sent, otherwise all batches will be run barring transport errors.
     * </p><p>
     * A batch of between two and ten requests is guaranteed to be sent as a single multi message, and therefore will
     * avoid behaviour that may not exactly match that provided by Brightpearl. Clients that have complex error recovery
     * requirements should avoid sending less than two or more than ten requests. The {@link uk.co.visalia.brightpearl.apiclient.multimessage.MultiMessageUtils#split(MultiRequest)}
     * utility may be used to split a request into batches.
     * </p>
     * @param authorisation App and account authorisation details.
     * @param multiRequest Multimessage request to be executed.
     * @return a {@link MultiResponse} object containing the results.
     */
    public MultiResponse execute(AppAuthorisation authorisation, MultiRequest multiRequest) {

        if (multiRequest.getRequests().size() == 0) {

            // Allow zero size batches in case clients forget to count before sending one.
            return MultiResponseBuilder.newMultiResponse().withStatus(200).build();

        } else if (multiRequest.getRequests().size() == 1) {

            // MM API doesn't support single requests so if the list contains only one request,
            // divert it to the direct API and wrap the response to a best approximation of how
            // it would have looked from the MM API.

            Map<String, ServiceResponse> serviceResponseMap = new HashMap<String, ServiceResponse>();

            ServiceWriteRequest itemRequest = multiRequest.getRequests().get(0);
            Response response = getHttpResponse(authorisation, itemRequest);

            try {
                JsonWrapper jsonWrapper = parseJsonWrapper(response, itemRequest.getResponseType(), false);
                if (jsonWrapper == null) {
                    serviceResponseMap.put(itemRequest.getRuid(), ServiceResponseBuilder.<Object>newServiceResponse().withRuid(itemRequest.getRuid()).withStatus(response.getStatus()).build());
                } else {
                    serviceResponseMap.put(itemRequest.getRuid(), ServiceResponseBuilder.<Object>newServiceResponse().withRuid(itemRequest.getRuid()).withStatus(response.getStatus()).withResponse(parseEntity(jsonWrapper, itemRequest.getResponseType())).withException(null).build());
                }
            } catch (BrightpearlAuthException e) {
                throw e;
            } catch (RuntimeException e) {
                serviceResponseMap.put(itemRequest.getRuid(), ServiceResponseBuilder.newServiceResponse().withRuid(itemRequest.getRuid()).withStatus(response.getStatus()).withException(e).build());
            }

            return MultiResponseBuilder.newMultiResponse().withStatus(response.getStatus() == 200 ? 200 : 207).withServiceResponseMap(serviceResponseMap).build();

        } else {

            // If response code is 207, continue only if onfail = continue, otherwise aggregate remaining request ids
            // in the unprocessed messages list. After first batch, if there's an auth error or a non-recoverable request
            // cap error, mark the batch and all remaining ones processed so that the result of the first can be returned.

            List<MultiRequest> splitMultiRequest = MultiMessageUtils.split(multiRequest);
            List<MultiResponse> multiResponses = new ArrayList<MultiResponse>();

            // First batch: Throw all exceptions.
            // Subsequent batches: Catch all exceptions and treat the messages as unprocessed. Auth exceptions are very
            // unlikely, request cap errors can be caught but transport errors could occur.
            // Any batch: Stop if status code is not 200 (most likely 207 as anything else will result in an exception) and OnFailMode==STOP.
            boolean stop = false;
            for (int i = 0; i < splitMultiRequest.size() && !stop; i++) {
                try {
                    MultiResponse multiResponse = getBatchResponse(authorisation, splitMultiRequest.get(i));
                    multiResponses.add(multiResponse);
                    stop = multiResponse.getStatus() != 200 && multiRequest.getOnFailOption() == OnFailOption.STOP;
                } catch (RuntimeException e) {
                    if (i == 0) {
                        throw e;
                    } else {
                        break;
                    }
                }
            }

            return mergeMultiResponses(splitMultiRequest.size(), multiResponses, multiRequest);
        }
    }

    /**
     * Convenience shortcut for {@link #get(AppAuthorisation, ServiceReadRequest)}. Calls build on the
     * supplied builder and makes the call to the API.
     * @param authorisation App and account authorisation details.
     * @param serviceRequestBuilder Builder for read request to be executed.
     * @param <T> Type of expected response.
     * @return If the request was successful, a response of the expected type.
     */
    public <T> T get(AppAuthorisation authorisation, ServiceReadRequestBuilder<T> serviceRequestBuilder) {
        return get(authorisation, serviceRequestBuilder.build());
    }

    /**
     * Make a GET or OPTIONS call to the API, and extract the expected response entity from the JSON returned. If a valid
     * cached response is available this will be returned. Subclasses of {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException} are thrown if
     * there is a failure, and other runtime exceptions may be thrown by custom implementations of {@link uk.co.visalia.brightpearl.apiclient.http.ClientFactory}.
     * @param authorisation App and account authorisation details.
     * @param serviceRequest A read request to be made.
     * @param <T> Type of expected response.
     * @return If the request was successful, a response of the expected type.
     */
    public <T> T get(AppAuthorisation authorisation, ServiceReadRequest<T> serviceRequest) {
        Response response = getHttpResponse(authorisation, serviceRequest);
        return parseBasicEntity(response, serviceRequest.getResponseType(), false);
    }

    /**
     * Convenience shortcut for {@link #search(AppAuthorisation, ServiceSearchRequest)}. Calls build on the
     * supplied builder and makes the call to the API.
     * @param authorisation App and account authorisation details.
     * @param serviceRequestBuilder Builder for search request to be executed.
     * @param <T> Type of expected response.
     * @return If the request was successful, a {@link SearchResults} object containing parsed results and meta information.
     */
    public <T> SearchResults<T> search(AppAuthorisation authorisation, SearchRequestBuilder<T> serviceRequestBuilder) {
        return search(authorisation, serviceRequestBuilder.build());
    }

    /**
     * <p>
     * Makes a GET call to a search API, and deserialises the response into a {@link SearchResults} object that contains
     * meta information, reference data and a list of results.
     * </p><p>
     * If a valid cached response is available this will be returned. Transport errors and other request failures are
     * thrown as subclasses of {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException} unless a custom HTTP client or rate limiter implementation
     * throws a different type.
     * </p><p>
     * This method uses the column names returned in the search metadata to populate objects of the requested type with
     * values from each array in the results. Therefore the expected response type declared in the search request should
     * be a flat object (i.e. only primitives, Strings and Dates) with fields either named or annotated with
     * {@link com.google.gson.annotations.SerializedName} according to the column names expected. In many cases, the
     * more complex type used for resource GETs will be unsuitable for use with the corresponding resource search.
     * </p>
     * @param authorisation App and account authorisation details.
     * @param serviceRequest The search request to be executed.
     * @param <T> Type of expected response.
     * @return If the request was successful, a {@link SearchResults} object containing parsed results and meta information.
     */
    public <T> SearchResults<T> search(AppAuthorisation authorisation, ServiceSearchRequest<T> serviceRequest) {
        Response response = getHttpResponse(authorisation, serviceRequest);
        return parseSearchEntity(response, serviceRequest.getResponseType());
    }

    /**
     * Convenience shortcut for {@link #execute(AppAuthorisation, ServiceWriteRequest)}. Calls build on the
     * supplied builder and makes the call to the API.
     * @param authorisation App and account authorisation details.
     * @param serviceRequestBuilder Builder for write request to be executed.
     * @param <T> Type of expected response. Use {@link Void}.class if no response body is expected.
     * @return If the request was successful, a response of the expected type, null if the declared type was {@link Void}.
     */
    public <T> T execute(AppAuthorisation authorisation, ServiceWriteRequestBuilder<T> serviceRequestBuilder) {
        return execute(authorisation, serviceRequestBuilder.build());
    }

    /**
     * Make a POST, PUT or DELETE call to the API. Subclasses of {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException} are thrown if there
     * is a failure, and other runtime exceptions may be thrown by custom implementations of {@link uk.co.visalia.brightpearl.apiclient.http.ClientFactory}.
     * @param authorisation App and account authorisation details.
     * @param serviceRequest A write request to be made.
     * @param <T> Type of expected response. Use {@link Void}.class if no response body is expected.
     * @return If the request was successful, a response of the expected type, null if the declared type was {@link Void}.
     */
    public <T> T execute(AppAuthorisation authorisation, ServiceWriteRequest<T> serviceRequest) {
        Response response = getHttpResponse(authorisation, serviceRequest);
        return parseBasicEntity(response, serviceRequest.getResponseType(), false);
    }

    /**
     * <p>
     * Executes a request and returns the response as an unparsed string of JSON together with the HTTP status
     * code and headers, exactly as returned by the {@link uk.co.visalia.brightpearl.apiclient.http.Client} implementation.
     * This method bypasses the detection of failure response codes and error messages in the response body provided
     * by other methods, so does not throw specific exceptions such as {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlAuthException}.
     * Rate limiting is supported.
     * </p><p>
     * This method may be used to execute all types of request.
     * </p><p>
     * An exception will only be thrown for HTTP transport errors, which will usually result in a {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException}
     * unless a custom HTTP client implementation throws a different type.
     * </p>
     * @param authorisation App and account authorisation details.
     * @param serviceRequest Read request to be sent.
     * @return Unprocessed HTTP response.
     */
    public Response getHttpResponse(AppAuthorisation authorisation, AbstractServiceRequest serviceRequest) {
        String url = buildServiceUrl(authorisation.getAccount(), serviceRequest);
        String jsonBody = null;
        if (serviceRequest instanceof ServiceWriteRequest) {
            jsonBody = "{}";
            Object entity = ((ServiceWriteRequest)serviceRequest).getEntity();
            if (entity != null && (serviceRequest.getMethod() == Method.POST || serviceRequest.getMethod() == Method.PUT)) {
                jsonBody = gson.toJson(entity);
            }
        }

        rateLimiter.rateLimit(authorisation.getAccount());
        Request request = RequestBuilder.newRequest()
                .withMethod(serviceRequest.getMethod())
                .withUrl(url)
                .withParameters(serviceRequest.getParams())
                .withHeaders(authorisation.getHeaders())
                .withBody(jsonBody)
                .build();
        Response response = getClient(authorisation.getAccount()).execute(request);
        sendRateLimitHeaders(authorisation.getAccount(), response);
        if (response.getStatus() == 503) {
            if (response.getBody() != null && response.getBody().contains("too many requests")) {
                rateLimiter.requestCapExceeded(authorisation.getAccount());
                throw new BrightpearlRequestCapException("Request limit exceeded");
            } else {
                throw new BrightpearlUnavailableException("Brightpearl API returned 503 Service Unavailable");
            }
        }

        return response;
    }

    private MultiResponse getBatchResponse(AppAuthorisation authorisation, MultiRequest multiRequest) {

        Map<String, ServiceResponse> serviceResponseMap = new HashMap<String, ServiceResponse>();

        List<MultiMessageItem> items = new ArrayList<MultiMessageItem>();
        for (ServiceWriteRequest request : multiRequest.getRequests()) {
            Object body = request.getEntity() == null ? new Object() : request.getEntity();
            items.add(new MultiMessageItem(request.getRuid(), buildMultiMessageServiceUrl(request), request.getMethod(), body));
        }

        MultiMessage message = new MultiMessage(multiRequest.getProcessingMode(), multiRequest.getOnFailOption(), items);

        String multiUrl = buildMultiMessageUrl(authorisation.getAccount());
        String jsonBody = gson.toJson(message);

        rateLimiter.rateLimit(authorisation.getAccount());
        Request request = RequestBuilder.newRequest()
                .withMethod(Method.POST)
                .withUrl(multiUrl)
                .withBody(jsonBody)
                .withHeaders(authorisation.getHeaders())
                .build();
        Response response = getClient(authorisation.getAccount()).execute(request);
        sendRateLimitHeaders(authorisation.getAccount(), response);

        if (response.getStatus() == 503) {
            if (response.getBody() != null && response.getBody().contains("too many requests")) {
                rateLimiter.requestCapExceeded(authorisation.getAccount());
                throw new BrightpearlRequestCapException("Request limit exceeded");
            } else {
                throw new BrightpearlUnavailableException("Brightpearl API returned 503 Service Unavailable");
            }
        }

        MultiMessageResponse multiResponse = parseBasicEntity(response, new TypeToken<MultiMessageResponse>() { }.getType(), false);

        for (MultiMessageResponseItem item : multiResponse.getProcessedMessages()) {
            ServiceWriteRequest itemRequest = multiRequest.getRequest(item.getLabel());
            if (itemRequest != null) {

                // TODO What if body is null?
                try {
                    JsonWrapper jsonWrapper = parseJsonWrapper(item.getStatusCode(), item.getBody().getContent(), itemRequest.getResponseType(), false);
                    if (jsonWrapper == null) {
                        serviceResponseMap.put(item.getLabel(), ServiceResponseBuilder.<Object>newServiceResponse().withRuid(itemRequest.getRuid()).withStatus(item.getStatusCode()).build());
                    } else {
                        serviceResponseMap.put(item.getLabel(), ServiceResponseBuilder.<Object>newServiceResponse().withRuid(itemRequest.getRuid()).withStatus(item.getStatusCode()).withResponse(parseEntity(jsonWrapper, itemRequest.getResponseType())).build());
                    }
                } catch (RuntimeException e) {
                    serviceResponseMap.put(item.getLabel(), ServiceResponseBuilder.newServiceResponse().withRuid(itemRequest.getRuid()).withStatus(item.getStatusCode()).withException(e).build());
                }

            }

        }
        return MultiResponseBuilder.newMultiResponse().withStatus(response.getStatus()).withServiceResponseMap(serviceResponseMap).withUnprocessedRequestIds(multiResponse.getUnprocessedMessages()).build();
    }

    private MultiResponse mergeMultiResponses(int batchCount, List<MultiResponse> multiResponses, MultiRequest multiRequest) {
        if (batchCount == 1 && multiResponses.size() == 1) {
            return multiResponses.get(0);
        }
        boolean fullySuccessful = true;
        Map<String, ServiceResponse> serviceResponseMap = new HashMap<String, ServiceResponse>();
        for (MultiResponse multiResponse : multiResponses) {
            for (ServiceWriteRequest request : multiRequest.getRequests()) {
                ServiceResponse serviceResponse = multiResponse.getServiceResponse(request.getRuid());
                if (serviceResponse != null) {
                    serviceResponseMap.put(request.getRuid(), serviceResponse);
                }
            }
            if (multiResponse.getStatus() != 200) {
                fullySuccessful = false;
            }
        }
        List<String> unprocessedIds = new ArrayList<String>();
        for (ServiceWriteRequest request : multiRequest.getRequests()) {
            if (!serviceResponseMap.containsKey(request.getRuid())) {
                unprocessedIds.add(request.getRuid());
            }
        }
        return MultiResponseBuilder.newMultiResponse().withStatus(fullySuccessful && unprocessedIds.isEmpty() ? 200 : 207).withServiceResponseMap(serviceResponseMap).withUnprocessedRequestIds(unprocessedIds).build();
    }

    /*
     * Extract typed entity from a raw HTTP response, including auth error handling. First parses a generic response to
     * check for errors, then parses the "response" element as the expected type.
     */
    private <T> T parseBasicEntity(Response response, Type type, boolean isAuthentication) {
        JsonWrapper jsonWrapper = parseJsonWrapper(response, type, isAuthentication);
        if (jsonWrapper == null) {
            return null;
        } else {
            return parseEntity(jsonWrapper, type);
        }
    }

    private <T> SearchResults<T> parseSearchEntity(Response response, Type type) {

        JsonWrapper jsonWrapper = parseJsonWrapper(response, type, false);

        PartialSearchResponse partialSearchResponse = parseEntity(jsonWrapper, partialSearchResponseType());
        List<SearchColumn> columns = partialSearchResponse.getMetaData().getColumns();

        // Map of column name to the set of reference data maps it provides keys for.
        Map<String, String[]> referenceLookup = new HashMap<String, String[]>();
        // Map of reference data map key to the name of the class field it populates.
        Map<String, String> referenceTargets = new HashMap<String, String>();

        if (type instanceof Class) {
            Field[] fields = ((Class)type).getDeclaredFields();
            for (Field field : fields) {
                ReferenceKey referenceKey = field.getAnnotation(ReferenceKey.class);
                if (referenceKey != null && referenceKey.value() != null && referenceKey.value().length > 0) {
                    referenceLookup.put(field.getName(), referenceKey.value());
                }
            }
            for (Field field : fields) {
                ReferenceField referenceField = field.getAnnotation(ReferenceField.class);
                if (referenceField != null && referenceField.value() != null) {
                    referenceTargets.put(referenceField.value(), field.getName());
                }
            }
        }

        // Turn each result into a map of column name to value, convert that to a json object then
        // finally into the expected type. Copes with any column order and missing columns.
        Map<String, Map<String, Object>> referenceData = jsonWrapper.getReference();
        List<T> results = new ArrayList<T>();
        for (JsonArray jsonArray : partialSearchResponse.getResults()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i).getName();
                Object value = jsonArray.get(i);
                map.put(column, value);

                // If the column contains a key into one or more reference maps, lookup the raw reference value from
                // reference data in the response, and populate the column to value map with this value, so that it gets
                // parsed into the expected type along with all the native fields.
                if (value != null && referenceLookup.containsKey(column)) {
                    for (String referenceKey : referenceLookup.get(column)) {
                        Map<String, Object> referenceMap = referenceData.get(referenceKey);
                        if (referenceMap != null && referenceMap.containsKey(value.toString()) && referenceTargets.containsKey(referenceKey)) {
                            map.put(referenceTargets.get(referenceKey), referenceMap.get(value.toString()));
                        }
                    }
                }

            }

            try {
                T result = gson.fromJson(gson.toJsonTree(map), type);
                results.add(result);
            } catch (JsonParseException e) {
                throw new BrightpearlHttpException(ClientErrorCode.INVALID_RESPONSE_FORMAT, e);
            }
        }

        return new SearchResults<T>(partialSearchResponse.getMetaData(), results, jsonWrapper.getReference());
    }

    private JsonWrapper parseJsonWrapper(Response response, Type type, boolean isAuthentication) {
        String responseJson = null;
        if (isJsonResponse(response)) {
            responseJson = response.getBody();
        }
        return parseJsonWrapper(response.getStatus(), responseJson, type, isAuthentication);
    }

    private JsonWrapper parseJsonWrapper(int status, String responseJson, Type type, boolean isAuthentication) {

        try {
            if (StringUtils.isNotEmpty(responseJson)) {
                JsonWrapper jsonWrapper = gson.fromJson(responseJson, JsonWrapper.class);

                if (status == 401 && !isAuthentication) {
                    // Invalid credentials.
                    if (jsonWrapper.getErrors() != null && !jsonWrapper.getErrors().isEmpty()) {
                        throw new BrightpearlAuthException(jsonWrapper.getErrors().get(0).getMessage());
                    } else if (jsonWrapper.getResponse() != null) {
                        if (jsonWrapper.getResponse().isJsonPrimitive()) {
                            throw new BrightpearlAuthException(jsonWrapper.getResponse().getAsString());
                        }
                    }
                    throw new BrightpearlAuthException();
                } else if (jsonWrapper.getErrors() != null && !jsonWrapper.getErrors().isEmpty()) {
                    // Presence of errors always interpreted as an error.
                    throw new BrightpearlServiceException(status, jsonWrapper.getErrors());
                } else if (status >= 200 && status < 300) {
                    // 2xx status and no errors should always be a success.
                    if (jsonWrapper.getResponse() != null) {
                        return jsonWrapper;
                    }
                } else {
                    // Non-2xx status code and no errors. The response should instead contain a string.
                    String responseString = parseEntity(jsonWrapper, stringType());
                    throw new BrightpearlHttpException(ClientErrorCode.INVALID_RESPONSE_TYPE, responseString);
                }
            }

            if (type != null && !type.equals(voidType())) {
                // A null response has been received unexpectedly with a 2xx status.
                throw new BrightpearlHttpException(ClientErrorCode.EMPTY_RESPONSE);
            } else {
                // Null response was received as expected.
                return null;
            }
        } catch (JsonParseException e) {
            throw new BrightpearlHttpException(ClientErrorCode.INVALID_RESPONSE_FORMAT, e);
        }

    }

    private <T> T parseEntity(JsonWrapper jsonWrapper, Type type) {
        if (type == null || voidType().equals(type)) {
            return null;
        }
        try {
            return gson.fromJson(jsonWrapper.getResponse(), type);
        } catch (JsonParseException e) {
            throw new BrightpearlHttpException(ClientErrorCode.INVALID_RESPONSE_TYPE, e);
        }
    }

    private String buildAuthUrl(Account account) {
        return String.format(AUTH_TEMPLATE, account.getDatacenter().getHost(), account.getAccountCode());
    }

    private String buildServiceUrl(Account account, AbstractServiceRequest serviceRequest) {
        String service = serviceRequest.getService().getPath();
        String path = serviceRequest.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return String.format(SERVICE_TEMPLATE, account.getDatacenter().getHost(), account.getAccountCode(), service, path);
    }

    private String buildMultiMessageUrl(Account account) {
        return String.format(MULTIMESSAGE_TEMPLATE, account.getDatacenter().getHost(), account.getAccountCode());
    }

    private String buildMultiMessageServiceUrl(AbstractServiceRequest serviceRequest) {
        String service = serviceRequest.getService().getPath();
        String path = serviceRequest.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return String.format(MULTIMESSAGE_SERVICE_TEMPLATE, service, path);
    }

    private boolean isJsonResponse(Response response) {
        String contentTypeHeader = response.getHeader("content-type");
        return contentTypeHeader != null && contentTypeHeader.startsWith("application/json");
    }

    private Type voidType() {
        return new TypeToken<Void>() { }.getType();
    }

    private Type stringType() {
        return new TypeToken<String>() { }.getType();
    }

    private Type partialSearchResponseType() {
        return new TypeToken<PartialSearchResponse>() { }.getType();
    }

    private Client getClient(Account account) {
        Client client = clientFactory.getClient(account);
        if (client == null) {
            throw new BrightpearlClientException("Client factory did not return a client for account " + account.getAccountCode());
        }
        return client;
    }

    private void sendRateLimitHeaders(Account account, Response response) {
        if (response == null) { return; }
        String remainingStr = response.getHeader("brightpearl-requests-remaining");
        String periodStr = response.getHeader("brightpearl-next-throttle-period");
        if (rateLimiter != null && StringUtils.isNotBlank(remainingStr) && StringUtils.isNotBlank(periodStr)) {
            try {
                int remaining = Integer.parseInt(remainingStr.trim());
                long period = Long.parseLong(periodStr.trim());
                rateLimiter.requestCompleted(account, remaining, period);
            } catch (Exception e) {
                // No action if headers not returned
            }
        }
    }

    private Gson defaultGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        try {
            // Register Calendar adaptor only if required classes are available - these are not present on Android
            Class.forName("javax.xml.datatype.DatatypeFactory");
            Class.forName("javax.xml.datatype.XMLGregorianCalendar");
            CalendarAdaptor calendarAdaptor = new CalendarAdaptor();
            gsonBuilder.registerTypeAdapter(Calendar.class, calendarAdaptor);
            gsonBuilder.registerTypeAdapter(GregorianCalendar.class, calendarAdaptor);
        } catch (Exception e) {
            // Calendar parsing classes not available
        }

        try {
            // Register Joda DateTime adaptor only if joda is on the classpath.
            Class<?> dateTimeClass = Class.forName("org.joda.time.DateTime");
            DateTimeAdaptor dateTimeAdaptor = new DateTimeAdaptor();
            gsonBuilder.registerTypeAdapter(dateTimeClass, dateTimeAdaptor);
            gsonBuilder.registerTypeAdapter(dateTimeClass, dateTimeAdaptor);
        } catch (Exception e) {
            // Joda is not available
        }

        return gsonBuilder.create();
    }

}