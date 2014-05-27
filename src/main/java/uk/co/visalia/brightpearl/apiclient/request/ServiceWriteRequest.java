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
 * <p>
 * Represents a write request to be made to the Brightpearl API. Extends {@link AbstractServiceRequest}, adding the attributes
 * unique to a write request. This is currently only the entity to be serialised as JSON and sent as the request body.
 * </p><p>
 * Request instances are intended to be immutable to avoid modification while a request is being constructed, however
 * the enclosed entity cannot be protected from external modification. Avoid modifying entities after including them in
 * a request.
 * </p>
 * @param <T> A placeholder for the type expected in the response, avoiding the requirement to cast responses. This is not
 *           linked to the {@link Type} set on a request so a {@link ClassCastException} is possible if the type is set
 *           incorrectly.
 */
public class ServiceWriteRequest<T> extends AbstractServiceRequest<T> {

    private final Object entity;

    ServiceWriteRequest(String ruid, ServiceName service, Method method, String path, Type responseType, Map<String, String> params, Object entity) {
        super(ruid, service, method, path, responseType, params);
        this.entity = entity;
    }

    /**
     * The entity to be serialised as JSON and sent as the request body.
     * @return the request entity object.
     */
    public Object getEntity() {
        return entity;
    }

}
