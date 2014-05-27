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
 * An instruction to the Brightpearl Multi-Message API that determines what the behaviour should be when one request in
 * a multi-message fails. The default behaviour for this client is to {@link #STOP} the processing, however when all the requests
 * in a multi-message are independent and one failure does not impact the others, the {@link #CONTINUE} option may be used.
 */
public enum OnFailOption {

    /**
     * Cancel the processing of subsequent requests in a multi-message batch when one fails. This option cannot be used
     * with the {@link ProcessingMode#PARALLEL} processing option.
     */
    STOP,

    /**
     * Continue processing all requests in a multi-message regardless of failures.
     */
    CONTINUE

}
