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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilities for multimessage requests.
 */
public final class MultiMessageUtils {

    private MultiMessageUtils() { }

    /**
     * Splits a {@link MultiRequest} into as many requests as necessary to give no more than ten request, whilst also
     * avoiding batches of one request (except when the supplied request contains only one request). This is used
     * internally by {@link uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient} to split requests but may also be used externally to give greater
     * control over the execution of large batches in case a multimessage or an individual request fails.
     */
    public static List<MultiRequest> split(MultiRequest multiRequest) {
        if (multiRequest.getRequests().size() > 10) {
            List<MultiRequest> requests = new ArrayList<MultiRequest>();
            List<ServiceWriteRequest> batch = new ArrayList<ServiceWriteRequest>();

            // Iterate through leaving two that can either be appended to the last batch if it contains no more than eight,
            // or send in their own multimessage. This way all the requests are send via the multimessage API, never direct.
            for (int i = 0; i < multiRequest.getRequests().size() - 2; i++) {
                batch.add(multiRequest.getRequests().get(i));
                if (batch.size() == 10) {
                    requests.add(new MultiRequest(batch, multiRequest.getOnFailOption(), multiRequest.getProcessingMode()));
                    batch = new ArrayList<ServiceWriteRequest>();
                }
            }
            if (batch.size() > 8) {
                requests.add(new MultiRequest(batch, multiRequest.getOnFailOption(), multiRequest.getProcessingMode()));
                batch = new ArrayList<ServiceWriteRequest>();
            }

            batch.add(multiRequest.getRequests().get(multiRequest.getRequests().size() - 2));
            batch.add(multiRequest.getRequests().get(multiRequest.getRequests().size() - 1));
            requests.add(new MultiRequest(batch, multiRequest.getOnFailOption(), multiRequest.getProcessingMode()));
            return requests;
        } else {
            return Arrays.asList(multiRequest);
        }
    }

}
