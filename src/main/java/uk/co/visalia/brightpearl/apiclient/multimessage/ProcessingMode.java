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
 * <p>
 * A hint to the Brightpearl API whether the requests in a multi-message are safe to be executed concurrently. The
 * {@link #PARALLEL} option should only be used when the requests are independent and isolated, and unlikely to compete
 * for the same resources. For example, when POSTing rows to the same order, the {@link #PARALLEL} option is not suitable,
 * whereas when POSTing order status updates to separate orders, it should be safe.
 * </p><p>
 * Neither this client library nor Brightpearl provide guidance or restriction on the use of parallel mode, so
 * care should be taken when using it. At the time of writing, parallel mode has little impact on response times so
 * there may be no compelling reason to use it.
 * </p>
 */
public enum ProcessingMode {

    /**
     * Process the individual requests of a multi-message in parallel. This option cannot be used with {@link OnFailOption#STOP}.
     */
    PARALLEL,

    /**
     * Process the individual requests of a multi-message in sequence.
     */
    SEQUENTIAL

}
