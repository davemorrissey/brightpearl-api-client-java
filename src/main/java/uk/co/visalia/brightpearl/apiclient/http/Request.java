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
 * Contains the basic details of an HTTP request to be executed by a {@link Client} in a generic form that can be supported
 * by any implementation. Immutable instances are created using {@link RequestBuilder}.
 */
public final class Request {

    private final Method method;

    private final String url;

    private final String body;

    private final Map<String, String> parameters;

    private final Map<String, String> headers;

    Request(Method method, String url, String body, Map<String, String> parameters, Map<String, String> headers) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.parameters = parameters == null ? null : Collections.unmodifiableMap(parameters);
        this.headers = headers == null ? null : Collections.unmodifiableMap(headers);
    }

    /**
     * The HTTP method.
     * @return the HTTP method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * The full URL of a Brightpearl API resource.
     * @return URL to be called.
     */
    public String getUrl() {
        return url;
    }

    /**
     * JSON request body for inclusion in PUT and POST requests.
     * @return JSON request body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Parameters for addition to the URL querystring. The map will contain raw values, and the {@link Client} is
     * responsible for encoding them.
     * @return map of querystring parameters.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * HTTP headers for addition to the request. These will include the authentication token. The map will contain raw
     * values, and the {@link Client} is responsible for encoding them.
     * @return map of request headers.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method=" + method +
                ", url='" + url + '\'' +
                ", body='" + body + '\'' +
                ", parameters=" + parameters +
                ", headers=" + headers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        if (body != null ? !body.equals(request.body) : request.body != null) return false;
        if (headers != null ? !headers.equals(request.headers) : request.headers != null) return false;
        if (method != request.method) return false;
        if (parameters != null ? !parameters.equals(request.parameters) : request.parameters != null) return false;
        if (url != null ? !url.equals(request.url) : request.url != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }
}
