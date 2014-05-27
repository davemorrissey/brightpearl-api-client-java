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
import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * This is the superclass for read and write request classes, providing the functionality common to both. It is only
 * intended for use internally so has a package private constructor.
 * </p><p>
 * Requests are split into read requests (including searches) and write requests because write requests can be included
 * in multimessages and read requests cannot, and because read requests can be cached and write requests cannot.
 * </p>
 * @param <T> A placeholder for the type expected in the response, avoiding the requirement to cast responses. This is not
 *           linked to the {@link Type} set on a request so a {@link ClassCastException} is possible if the type is set
 *           incorrectly.
 */
public abstract class AbstractServiceRequest<T> {

    private final String ruid;

    private final ServiceName service;

    private final Method method;

    private final String path;

    private final Type responseType;

    private final Map<String, String> params;

    AbstractServiceRequest(String ruid, ServiceName service, Method method, String path, Type responseType, Map<String, String> params) {
        this.ruid = ruid;
        this.service = service;
        this.method = method;
        this.path = path;
        this.responseType = responseType;
        this.params = params == null ? null : Collections.unmodifiableMap(params);
    }

    /**
     * A unique identifier for the request. This is used as the label for the request when it is sent in a multimessage,
     * and is also useful for logging. A unique ID is generated internally if a custom ID is not supplied.
     * @return unique ID of the request.
     */
    public String getRuid() {
        return ruid;
    }

    /**
     * The HTTP method of the request.
     * @return HTTP method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the name of the Brightpearl service that contains the resource to be called.
     * @return service name.
     */
    public ServiceName getService() {
        return service;
    }

    /**
     * Returns the path of the Brightpearl resource within the service given by {@link #getService()}. For example, for
     * the product GET API, this would be '/product'.
     * @return the resource path.
     */
    public String getPath() {
        return path;
    }

    /**
     * A generic type token representing the expected type of the response element in the JSON response body. This is used
     * by the client to deserialise the response, so if it does not match the expected response an exception may be thrown
     * or an empty object returned. {@link Type} is used instead of {@link Class} to provide support for deserialising
     * generic types e.g. collections and maps, however this does mean the type is not guaranteed to match the type token
     * T, which is used to avoid casting responses.
     * @return the response type expected.
     */
    public Type getResponseType() {
        return responseType;
    }

    /**
     * Querystring parameters to be added to the URL. Parameters are sometimes used to request additional data sections
     * in the response, and for filtering, sorting and column selection in resource searches.
     * @return unencoded querystring parameters.
     */
    public Map<String, String> getParams() {
        return params;
    }
}
