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

package uk.co.visalia.brightpearl.apiclient.client.parsing;

import com.google.gson.JsonElement;
import uk.co.visalia.brightpearl.apiclient.common.ServiceError;

import java.util.List;
import java.util.Map;

/**
 * For internal use only. Raw type used in the parsing of all BP API responses including searches and multimessage responses.
 * This represents the entirety of the JSON response and stores the response element as unparsed JSON so that it may be
 * parsed into the expected type only after the response is checked for errors and for the presence of the expected
 * response type. Using a generic type is not possible because certain errors are returned in the response element and
 * not the errors element, which would cause a parsing failure.
 */
public class JsonWrapper {

    private JsonElement response;

    private List<ServiceError> errors;

    private Map<String, Map<String, Object>> reference;

    public List<ServiceError> getErrors() {
        return errors;
    }

    public JsonElement getResponse() {
        return response;
    }

    public Map<String, Map<String, Object>> getReference() {
        return reference;
    }

}
