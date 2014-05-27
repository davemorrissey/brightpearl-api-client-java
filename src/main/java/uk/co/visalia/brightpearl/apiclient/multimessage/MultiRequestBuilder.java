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
import uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequestBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder used to create immutable instances of {@link MultiRequest}.
 * @see <a href="http://www.brightpearl.com/developer/latest/concept/multi-message.html">http://www.brightpearl.com/developer/latest/concept/multi-message.html</a>
 */
public final class MultiRequestBuilder {

    private OnFailOption onFailOption;

    private ProcessingMode processingMode;

    private List<ServiceWriteRequest> requests;

    private MultiRequestBuilder() {

    }

    /**
     * Returns a new builder instance used to create immutable {@link MultiRequest} instances
     * @return new builder instance.
     */
    public static MultiRequestBuilder newMultiRequest() {
        return new MultiRequestBuilder();
    }

    /**
     * Constructs the immutable {@link MultiRequest} instance.
     * @return a {@link MultiRequest} instance.
     */
    public MultiRequest build() {
        return new MultiRequest(requests, onFailOption, processingMode);
    }

    /**
     * Set the on fail option (STOP or CONTINUE) for the request, defining whether remaining requests should still be
     * executed when one fails. {@link OnFailOption#STOP} cannot be used with {@link ProcessingMode#PARALLEL}.
     * @param onFailOption the {@link OnFailOption} to be used.
     * @return builder instance for method chaining.
     */
    public MultiRequestBuilder withOnFailOption(OnFailOption onFailOption) {
        this.onFailOption = onFailOption;
        return this;
    }

    /**
     * Set the processing mode option (SEQUENTIAL or PARALLEL) for the request, which defines whether requests should be
     * executed in serial or in parallel by the Brightpearl multimessage API. {@link ProcessingMode#PARALLEL} cannot be
     * used with {@link OnFailOption#STOP}.
     * @param processingMode the {@link ProcessingMode} to be used.
     * @return builder instance for method chaining.
     */
    public MultiRequestBuilder withProcessingMode(ProcessingMode processingMode) {
        this.processingMode = processingMode;
        return this;
    }

    /**
     * Set the requests to be added to the multimessage batch, replacing any previously set.
     * @param requests requests to be sent in the batch.
     * @return builder instance for method chaining.
     */
    public MultiRequestBuilder withRequests(List<ServiceWriteRequest> requests) {
        if (requests == null || requests.isEmpty() || requests.contains(null)) {
            throw new IllegalArgumentException("Request list must not be empty or contain nulls");
        }
        this.requests = new ArrayList<ServiceWriteRequest>();
        for (ServiceWriteRequest request : requests) {
            this.requests.add(request);
        }
        return this;
    }

    /**
     * Add a request to the multimessage batch.
     * @param request request to be added.
     * @return builder instance for method chaining.
     */
    public MultiRequestBuilder withAddedRequest(ServiceWriteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (this.requests == null) {
            this.requests = new ArrayList<ServiceWriteRequest>();
        }
        this.requests.add(request);
        return this;
    }

    /**
     * Add a request to the multimessage batch. This is a convenience method accepting a builder so its build method does
     * not need to be called.
     * @param requestBuilder builder for a request to be added.
     * @return builder instance for method chaining.
     */
    public MultiRequestBuilder withAddedRequest(ServiceWriteRequestBuilder requestBuilder) {
        if (requestBuilder == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        return withAddedRequest(requestBuilder.build());
    }

}
