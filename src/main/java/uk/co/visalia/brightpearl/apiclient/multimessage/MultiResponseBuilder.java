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

package uk.co.visalia.brightpearl.apiclient.multimessage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A builder used to create immutable instances of {@link MultiResponse}. Intended for internal use only.
 */
public class MultiResponseBuilder {

    private int status;

    private Map<String, ServiceResponse> serviceResponseMap;

    private List<String> unprocessedRequestIds;

    private MultiResponseBuilder() {

    }

    /**
     * Returns a new builder instance used to create immutable {@link MultiResponse} instances
     * @return new builder instance.
     */
    public static MultiResponseBuilder newMultiResponse() {
        return new MultiResponseBuilder();
    }

    /**
     * Constructs the immutable {@link MultiResponse} instance.
     * @return a {@link MultiResponse} instance.
     */
    public MultiResponse build() {
        return new MultiResponse(status, serviceResponseMap, unprocessedRequestIds);
    }

    /**
     * Set the HTTP response code for the multimessage request.
     * @param status response code for the multimessage request.
     * @return builder instance for method chaining.
     */
    public MultiResponseBuilder withStatus(int status) {
        this.status = status;
        return this;
    }

    /**
     * Set the results of individual executed requests as a map of request unique ID to response object.
     * @param serviceResponseMap map of responses for executed requests.
     * @return builder instance for method chaining.
     */
    public MultiResponseBuilder withServiceResponseMap(Map<String, ServiceResponse> serviceResponseMap) {
        this.serviceResponseMap = serviceResponseMap == null ? null : Collections.unmodifiableMap(serviceResponseMap);
        return this;
    }

    /**
     * Set the list of unique request IDs that were not executed due to a failured.
     * @param unprocessedRequestIds unique IDs of requests that were not executed.
     * @return builder instance for method chaining.
     */
    public MultiResponseBuilder withUnprocessedRequestIds(List<String> unprocessedRequestIds) {
        this.unprocessedRequestIds = unprocessedRequestIds == null ? null : Collections.unmodifiableList(unprocessedRequestIds);
        return this;
    }

}
