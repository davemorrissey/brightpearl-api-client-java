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

package uk.co.visalia.brightpearl.apiclient.request;

import uk.co.visalia.brightpearl.apiclient.ServiceName;
import uk.co.visalia.brightpearl.apiclient.http.Method;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * An extension of {@link ServiceReadRequest} used for resource searches. Although the two classes are the same, a separate
 * class is used to ensure an ordinary read request cannot be passed to a search API.
 * @param <T> A placeholder for the type expected in the response, avoiding the requirement to cast responses. This is not
 *           linked to the {@link Type} set on a request so a {@link ClassCastException} is possible if the type is set
 *           incorrectly.
 */
public class ServiceSearchRequest<T> extends ServiceReadRequest<T> {

    ServiceSearchRequest(String ruid, ServiceName service, Method method, String path, Type responseType, Map<String, String> params) {
        super(ruid, service, method, path, responseType, params);
    }

}
