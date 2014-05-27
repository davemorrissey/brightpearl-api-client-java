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

package uk.co.visalia.brightpearl.apiclient.exception;

import uk.co.visalia.brightpearl.apiclient.common.ServiceError;

import java.util.Collections;
import java.util.List;
/**
 * This exception is thrown by the simple get, search and execute APIs in {@link uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient}
 * when the Brightpearl API returns a collection of error messages in the JSON response body. The status code of the
 * response and the error messages returned are provided.
 */
public class BrightpearlServiceException extends BrightpearlClientException {

    private final int status;

    private final List<ServiceError> serviceErrors;

    public BrightpearlServiceException(int status, List<ServiceError> serviceErrors) {
        super(buildMessage(status, serviceErrors));
        this.status = status;
        this.serviceErrors = serviceErrors == null ? null : Collections.unmodifiableList(serviceErrors);
    }

    /**
     * Returns the HTTP status of the response received.
     * @return HTTP response status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns service errors from the response.
     * @return service errors.
     */
    public List<ServiceError> getServiceErrors() {
        return serviceErrors;
    }

    private static String buildMessage(int status, List<ServiceError> serviceErrors) {
        if (serviceErrors != null) {
            StringBuilder builder = new StringBuilder();
            for (ServiceError serviceError : serviceErrors) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(serviceError.getCode());
            }
            return "Status code " + status + " and " + serviceErrors.size() + " errors returned from Brightpearl API (" + builder.toString() + ")";
        }
        return "Status code " + status + " and 0 errors returned from Brightpearl API";
    }

}
