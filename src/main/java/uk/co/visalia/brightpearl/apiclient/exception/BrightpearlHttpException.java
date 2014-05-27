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

/**
 * <p>
 * Subclass of {@link BrightpearlClientException} thrown when a request fails due to an HTTP transport error, and when a
 * response is received but cannot be parsed into the type expected. Instances contain a {@link ClientErrorCode}, which
 * gives further information about the cause of the error.
 * </p><p>
 * This exception is not thrown when a response was received from Brightpearl with an error status code and error messages
 * in a well-formed JSON body - in this case, {@link BrightpearlServiceException} is thrown.
 * </p>
 */
public class BrightpearlHttpException extends BrightpearlClientException {

    private final ClientErrorCode clientErrorCode;

    public BrightpearlHttpException(ClientErrorCode clientErrorCode) {
        super("Brightpearl request failed: " + clientErrorCode);
        this.clientErrorCode = clientErrorCode;
    }

    public BrightpearlHttpException(ClientErrorCode clientErrorCode, String message) {
        super("Brightpearl request failed: " + clientErrorCode + " (" + message + ")");
        this.clientErrorCode = clientErrorCode;
    }

    public BrightpearlHttpException(ClientErrorCode clientErrorCode, Exception e) {
        super("Brightpearl request failed: " + clientErrorCode + " (" + e.getClass().getSimpleName() + ")", e);
        this.clientErrorCode = clientErrorCode;
    }

    public ClientErrorCode getClientErrorCode() {
        return clientErrorCode;
    }

}
