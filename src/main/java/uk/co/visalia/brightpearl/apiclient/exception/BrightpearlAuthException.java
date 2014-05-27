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
 * Thrown within {@link uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient} when a 401 response is received from the
 * Brightpearl API. This indicates the authentication details used are invalid. This is also thrown when
 * an authentication request fails, in which case the credentials supplied are invalid.
 * </p>
 */
public class BrightpearlAuthException extends BrightpearlClientException {

    public BrightpearlAuthException() {
        super("Invalid authentication details");
    }

    public BrightpearlAuthException(String message) {
        super(message);
    }

}
