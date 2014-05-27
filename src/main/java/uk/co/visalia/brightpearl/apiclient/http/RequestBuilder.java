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

import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * A builder used to create immutable {@link Request} instances.
 */
public class RequestBuilder {

    private Method method;

    private String url;

    private String body;

    private Map<String, String> parameters;

    private Map<String, String> headers;

    private RequestBuilder() {

    }

    /**
     * Creates a new builder instance. At minimum, a method and URL must be set.
     * @return a new builder instance.
     */
    public static RequestBuilder newRequest() {
        return new RequestBuilder();
    }

    /**
     * Constructs the immutable {@link Request} instance.
     * @return a request instance for execution.
     */
    public Request build() {
        if (method == null) {
            throw new IllegalArgumentException("Request method cannot be null");
        }
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Request URL must be a non-empty string");
        }
        return new Request(method, url, body, parameters, headers);
    }

    /**
     * Sets the HTTP method of the request.
     * @param method HTTP method.
     * @return builder instance for method chaining.
     */
    public RequestBuilder withMethod(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Request method cannot be null");
        }
        this.method = method;
        return this;
    }

    /**
     * Sets the full URL of the resource.
     * @param url full URL excluding querystring parameters.
     * @return builder instance for method chaining.
     */
    public RequestBuilder withUrl(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Request URL must be a non-empty string");
        }
        this.url = url;
        return this;
    }

    /**
     * Sets the map of unencoded querystring parameters to be added to the URL.
     * @param parameters querystring parameters.
     * @return builder instance for method chaining.
     */
    public RequestBuilder withParameters(Map<String, String> parameters) {
        this.parameters = parameters == null ? null : Collections.unmodifiableMap(parameters);
        return this;
    }

    /**
     * Sets the map of unencoded headers to be added to the request.
     * @param headers request headers.
     * @return builder instance for method chaining.
     */
    public RequestBuilder withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Add a JSON body to a PUT or POST request.
     * @param body JSON request body.
     * @return builder instance for method chaining.
     */
    public RequestBuilder withBody(String body) {
        this.body = body;
        return this;
    }

}
