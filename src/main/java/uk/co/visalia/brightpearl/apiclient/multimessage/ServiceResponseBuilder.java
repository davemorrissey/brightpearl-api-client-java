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

/**
 * A builder used to create immutable instances of {@link ServiceResponse}. Intended for internal use only.
 */
public class ServiceResponseBuilder<T> {

    /**
     * The unique ID of the request sent.
     */
    private String ruid;

    /**
     * HTTP status code received.
     */
    private int status;

    /**
     * The parsed response entity of the type expected for the request.
     */
    private T response;

    /**
     * Exception encountered processing the response for this individual item.
     */
    private RuntimeException exception;

    private ServiceResponseBuilder() {

    }

    /**
     * Returns a new builder instance used to create immutable {@link MultiResponse} instances
     * @return new builder instance.
     */
    public static <T> ServiceResponseBuilder<T> newServiceResponse() {
        return new ServiceResponseBuilder<T>();
    }

    /**
     * Constructs the immutable {@link MultiResponse} instance.
     * @return a {@link MultiResponse} instance.
     */
    public ServiceResponse<T> build() {
        return new ServiceResponse<T>(ruid, status, response, exception);
    }

    /**
     * Set the unique ID of the request this is a response to.
     * @param ruid unique ID of a request.
     * @return builder instance for method chaining.
     */
    public ServiceResponseBuilder<T> withRuid(String ruid) {
        this.ruid = ruid;
        return this;
    }

    /**
     * Set the HTTP status code returned for this individual response.
     * @param status HTTP status code of the individual request.
     * @return builder instance for method chaining.
     */
    public ServiceResponseBuilder<T> withStatus(int status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the entity received in the response element of the JSON response body.
     * @param response response entity.
     * @return builder instance for method chaining.
     */
    public ServiceResponseBuilder<T> withResponse(T response) {
        this.response = response;
        return this;
    }

    /**
     * Set the exception thrown based on service errors in the response or an error parsing the JSON.
     * @param exception exception thrown processing the response.
     * @return builder instance for method chaining.
     */
    public ServiceResponseBuilder<T> withException(RuntimeException exception) {
        this.exception = exception;
        return this;
    }

}
