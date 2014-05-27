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
 * This exception is thrown when a 503 is received from Brightpearl but the message does not indicate the request cap
 * has been reached. This may be due to the datacenter being offline, the account being migrated or upgraded or a number
 * of other reasons.
 */
public class BrightpearlUnavailableException extends BrightpearlClientException {

    public BrightpearlUnavailableException(String message) {
        super(message);
    }

    public BrightpearlUnavailableException(String message, Exception cause) {
        super(message, cause);
    }

}
