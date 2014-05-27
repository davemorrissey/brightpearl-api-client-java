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
 * <p>
 * Contains the basic details of an HTTP response received from the Brightpearl API. {@link Client} implementations must
 * return instances with all properties set, except when the HTTP request fails with a transport error, in which case
 * a {@link uk.co.visalia.brightpearl.apiclient.exception.BrightpearlHttpException} should be thrown.
 * </p><p>
 * Instances are immutable and may be constructed using {@link ResponseBuilder}.
 * </p>
 */
public final class Response {

    private final int status;

    private final Map<String, String> headers;

    private final String body;

    private final long timestamp;

    Response(int status, Map<String, String> headers, String body) {
        this.status = status;
        this.headers = headers == null ? null : Collections.unmodifiableMap(headers);
        this.body = body;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Returns the HTTP status of the response.
     * @return HTTP response status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the response headers. All headers should be included.
     * @return response headers.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns the JSON response body.
     * @return unparsed JSON string.
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns the timestamp this response instance was created. This indicates the age of the response and may be used
     * for caching.
     * @return response timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * A utility method that looks up the value of a named header.
     * @param key Header name to look up.
     * @return value of the header if it was present in the response, null if not.
     */
    public String getHeader(String key) {
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (header.getKey().equalsIgnoreCase(key)) {
                    return header.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        if (status != response.status) return false;
        if (body != null ? !body.equals(response.body) : response.body != null) return false;
        if (headers != null ? !headers.equals(response.headers) : response.headers != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = status;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}
