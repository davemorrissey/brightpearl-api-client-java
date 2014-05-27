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

import uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequest;

import java.util.*;

/**
 * Contains the results of a multimessage request. This consists of the status code from the multimessage API, results of
 * each request executed, and a list of unique IDs for the requests that were not executed.
 * @see <a href="http://www.brightpearl.com/developer/latest/concept/multi-message.html">http://www.brightpearl.com/developer/latest/concept/multi-message.html</a>
 */
public final class MultiResponse {

    /**
     * HTTP status code for the multimessage wrapper.
     */
    private int status;

    /**
     * Map of request ID to result received for the individual request.
     */
    private Map<String, ServiceResponse> serviceResponseMap;

    /**
     * List of IDs of requests that were not executed.
     */
    private List<String> unprocessedRequestRuids;

    MultiResponse(int status, Map<String, ServiceResponse> serviceResponseMap, List<String> unprocessedRequestRuids) {
        this.status = status;
        this.serviceResponseMap = serviceResponseMap == null ? Collections.unmodifiableMap(new HashMap<String, ServiceResponse>()) : Collections.unmodifiableMap(serviceResponseMap);
        this.unprocessedRequestRuids = unprocessedRequestRuids == null ? Collections.unmodifiableList(new ArrayList<String>()) : Collections.unmodifiableList(unprocessedRequestRuids);
    }

    /**
     * Returns the status code for the multimessage container request. If the request succeeded, the response code will
     * be 200 if all individual messages received 200 responses, and 207 if any did not. Regardless of this status code,
     * the {@link #getUnprocessedRequestRuids()} collection and the results of each individual request from {@link #getServiceResponse(String)}
     * should be inspected to confirm the requests were all successfully executed.
     * @return status code of the multimessage request.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Fetches the response for an individual request included in the multimessage, using the request's unique ID. This
     * is equivalent to passing the ID to {@link #getServiceResponse(String)} but hides casting of the response to the
     * correct generic type.
     * @param request A request sent in the multi-message.
     * @param <T> Type of response entity expected for the request.
     * @return The response received for the individual request, or null if the ID is either not valid or the request was not processed due to an earlier failure.
     */
    @SuppressWarnings("unchecked")
    public <T> ServiceResponse<T> getServiceResponse(ServiceWriteRequest<T> request) {
        return serviceResponseMap.get(request.getRuid());
    }

    /**
     * Fetches the response for an individual request included in the multimessage, using the request's unique ID. The
     * response is untyped and the entity within may be casted to the expected type.
     * @param ruid ID of a request sent in the multi-message.
     * @return The response received for the individual request, or null if the ID is either not valid or the request was not processed due to an earlier failure.
     */
    public ServiceResponse getServiceResponse(String ruid) {
        return serviceResponseMap.get(ruid);
    }

    /**
     * Returns the list of unique request IDs that were not processed due to earlier failures.
     * @return unprocessed request IDs.
     */
    public List<String> getUnprocessedRequestRuids() {
        return unprocessedRequestRuids;
    }

}
