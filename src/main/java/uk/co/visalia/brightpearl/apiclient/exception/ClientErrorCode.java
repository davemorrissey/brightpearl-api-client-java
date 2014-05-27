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
 * To aid in troubleshooting problems, these standardised error codes are included in {@link BrightpearlHttpException}s
 * whenever possible. There are two classes of error included - transport errors, where no response was received from
 * the Brightpearl API, and response errors, where the response from Brightpearl was either corrupt, unexpectedly empty
 * or could not be parsed into an object of the expected type.
 */
public enum ClientErrorCode {

    EMPTY_RESPONSE,
    INVALID_RESPONSE_FORMAT,
    INVALID_RESPONSE_TYPE,
    NO_RESPONSE,
    UNKNOWN_HOST,
    CONNECTION_TIMEOUT,
    SOCKET_ERROR,
    SOCKET_TIMEOUT,
    READ_TIMEOUT,
    OTHER_TRANSPORT_ERROR

}
