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

import uk.co.visalia.brightpearl.apiclient.common.ServiceError;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlServiceException;

import java.util.List;

/**
 * Contains the response for an individual request executed in a multimessage batch.
 * @param <T> placeholder for the response type.
 */
public final class ServiceResponse<T> {

    /**
     * The unique ID of the request sent.
     */
    private final String ruid;

    /**
     * HTTP status code received.
     */
    private final int status;

    /**
     * The parsed response entity of the type expected for the request.
     */
    private final T response;

    /**
     * Errors returned by the Brightpearl API.
     */
    private final List<ServiceError> errors;

    /**
     * Exception encountered processing the response for this individual item.
     */
    private final RuntimeException exception;

    ServiceResponse(String ruid, int status, T response, RuntimeException exception) {
        this.ruid = ruid;
        this.status = status;
        this.response = response;
        this.exception = exception;
        if (exception instanceof BrightpearlServiceException) {
            this.errors = ((BrightpearlServiceException)exception).getServiceErrors();
        } else {
            this.errors = null;
        }
    }

    /**
     * Returns the unique ID of the request sent.
     */
    public String getRuid() {
        return ruid;
    }

    /**
     * Returns the HTTP status code received.
     * @return HTTP status code of the individual response.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns false if there were errors in the response body, regardless of the status code, or there was an error parsing
     * the response for this individual request.
     * @return true if the request was successful.
     */
    public boolean isSuccess() {
        return exception == null;
    }

    /**
     * Returns any errors included in the response body.
     * @return a list of {@link ServiceError}s from the response.
     */
    public List<ServiceError> getErrors() {
        return errors;
    }

    /**
     * Returns the parsed entity parsed from the response element of the JSON, in the type expected for the request sent.
     * This method will throw an exception if the Brightpearl API returned error messages or the response for the individual
     * request could not be parsed, mimicking the behaviour of the
     * {@link uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient} and {@link uk.co.visalia.brightpearl.apiclient.BrightpearlLegacyApiSession}
     * classes. To avoid exceptions, call {@link #isSuccess()} before this method.
     * @return The entity parsed from the response element of the JSON body received.
     */
    public T getResponse() {
        if (exception != null) {
            throw exception;
        }
        return response;
    }

}
