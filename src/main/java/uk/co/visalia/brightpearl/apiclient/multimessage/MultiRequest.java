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

import java.util.Collections;
import java.util.List;

/**
 * Wraps a set of write requests together with a {@link OnFailOption} and a {@link ProcessingMode} to define a multimessage
 * request for execution with the Brightpearl API. Instances are immutable and may be constructed using {@link MultiRequestBuilder}.
 * For further information please see the link below.
 * @see <a href="http://www.brightpearl.com/developer/latest/concept/multi-message.html">http://www.brightpearl.com/developer/latest/concept/multi-message.html</a>
 */
public final class MultiRequest {

    private final OnFailOption onFailOption;

    private final ProcessingMode processingMode;

    private final List<ServiceWriteRequest> requests;

    MultiRequest(List<ServiceWriteRequest> requests, OnFailOption onFailOption, ProcessingMode processingMode) {
        if (requests == null || requests.size() == 0) {
            throw new IllegalArgumentException("Multi request must have at least one request");
        }
        this.onFailOption = onFailOption == null ? OnFailOption.STOP : onFailOption;
        this.processingMode = processingMode == null ? ProcessingMode.SEQUENTIAL : processingMode;
        if (this.processingMode == ProcessingMode.PARALLEL && this.onFailOption == OnFailOption.STOP) {
            throw new IllegalArgumentException("Processing mode PARALLEL cannot be used with on-fail option STOP");
        }
        this.requests = Collections.unmodifiableList(requests);
    }

    /**
     * The on fail option (STOP or CONTINUE) set on the request, defining whether remaining requests should still be
     * executed when one fails.
     * @return the on fail option selected.
     */
    public OnFailOption getOnFailOption() {
        return onFailOption;
    }

    /**
     * The processing mode option (SEQUENTIAL or PARALLEL) set on the request, which defines whether requests should be
     * executed in serial or in parallel by the Brightpearl multimessage API.
     * @return the processing mode option selected.
     */
    public ProcessingMode getProcessingMode() {
        return processingMode;
    }

    /**
     * The list of requests included in the multimessage batch.
     * @return included requests for execution.
     */
    public List<ServiceWriteRequest> getRequests() {
        return requests;
    }

    /**
     * Returns an individual request by its unique ID.
     * @param id a request unique ID.
     * @return the request with the given ID, null if it is not found.
     */
    public ServiceWriteRequest getRequest(String id) {
        for (ServiceWriteRequest request : requests) {
            if (request.getRuid() != null && request.getRuid().equals(id)) {
                return request;
            }
        }
        return null;
    }
}
