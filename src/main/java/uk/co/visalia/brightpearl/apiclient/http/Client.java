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

package uk.co.visalia.brightpearl.apiclient.http;

/**
 * <p>
 * A simple interface for an HTTP client, providing a generic API that can be implemented using any HTTP client library,
 * to make this client library fully portable.
 * </p><p>
 * {@link uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient} requests a Client instance from {@link ClientFactory} every
 * time it makes a request to the Brightpearl API. This allows support for HTTP clients that can only be used once or
 * cannot support concurrent requests.
 * </p><p>
 * Implementations should not attempt to handle unexpected response codes; if a response was received from
 * the remote server with any status code, this method should return without throwing an exception. {@link uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient}
 * handles interpretation of unexpected status codes. An exception should only be thrown if there is a failure to
 * connect to the remote server or an exception while processing the response. The recommended exception to throw in
 * these cases is {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlHttpException}.
 * </p>
 */
public interface Client {

    /**
     * Executes a request and returns the response. The status code, response body and headers should all be returned if
     * any response is received from the server, regardless of status code. If the request fails due to a transport error,
     * a {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlHttpException} should be thrown.
     * @param request The request to be executed.
     * @return Response from the server.
     */
    Response execute(Request request);

}
