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

import java.util.Collections;
import java.util.Map;

/**
 * A builder used to create immutable {@link Response} instances.
 */
public class ResponseBuilder {

    private int status;

    private Map<String, String> headers;

    private String body;

    private ResponseBuilder() {
    }

    /**
     * Creates a new builder instance.
     * @return new builder instance.
     */
    public static ResponseBuilder newResponse() {
        return new ResponseBuilder();
    }

    /**
     * Builds and returns an immutable {@link Response} instance.
     * @return new response instance.
     */
    public Response build() {
        return new Response(status, headers, body);
    }

    /**
     * Set the HTTP response status.
     * @param status HTTP status.
     * @return builder instance for method chaining.
     */
    public ResponseBuilder withStatus(int status) {
        this.status = status;
        return this;
    }

    /**
     * Set the response headers received. All headers should be included.
     * @param headers Response headers received.
     * @return builder instance for method chaining.
     */
    public ResponseBuilder withHeaders(Map<String, String> headers) {
        this.headers = headers == null ? null : Collections.unmodifiableMap(headers);
        return this;
    }

    /**
     * Set the JSON body received.
     * @param body JSON received in the response body.
     * @return builder instance for method chaining.
     */
    public ResponseBuilder withBody(String body) {
        this.body = body;
        return this;
    }
    
}