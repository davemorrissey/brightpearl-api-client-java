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

import uk.co.visalia.brightpearl.apiclient.http.Method;

/**
 * For internal use only. Represents a single message within a multimessage request.
 */
public class MultiMessageItem {

    private String label;

    private String uri;

    private Method httpMethod;

    private Object body;

    public MultiMessageItem(String label, String uri, Method httpMethod, Object body) {
        this.label = label;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.body = body;
    }

    public String getLabel() {
        return label;
    }

    public String getUri() {
        return uri;
    }

    public Method getHttpMethod() {
        return httpMethod;
    }

    public Object getBody() {
        return body;
    }
}
