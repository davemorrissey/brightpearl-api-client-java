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

package uk.co.visalia.brightpearl.apiclient.client.multimessage;

/**
 * For internal use only. Each element in the processedResponses array of a multimessage response is deserialised into this
 * type, before the body is processed according to the expected response type.
 */
public class MultiMessageResponseItem {

    private String label;

    private Integer statusCode;

    private MultiMessageResponseItemBody body;

    public String getLabel() {
        return label;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public MultiMessageResponseItemBody getBody() {
        return body;
    }
}
