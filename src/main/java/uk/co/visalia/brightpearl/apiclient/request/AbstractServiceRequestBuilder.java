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
import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Intended for internal use only. This is the superclass for request builders providing common functionality.
 * @param <B> Builder type. Used to support method chaining from superclass methods.
 */
abstract class AbstractServiceRequestBuilder<B extends AbstractServiceRequestBuilder<B>> {

    private String ruid;

    private ServiceName service;

    private Method method;

    private String path;

    private Type responseType;

    private Map<String, String> params;

    AbstractServiceRequestBuilder() {
    }

    abstract B getThis();

    void resetRuid() {
        ruid = null;
    }

    B withMethod(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }
        this.method = method;
        return getThis();
    }

    /**
     * Set a unique identifier for the request. All requests are given a unique identifier automatically, for use in
     * logging and as a label for a request contained in a multimessage, and this method may be used to override it with
     * a custom ID.
     * <p>
     * Note this RUID is applied only to the first request built by this builder; if you call {@link #build()} more than
     * once, the second and subsequent requests will have randomly generated IDs unless you also call this method
     * between each invocation.
     * @param ruid the custom unique identifier to use.
     * @return builder instance for method chaining.
     */
    public B withRuid(String ruid) {
        if (StringUtils.isBlank(ruid)) {
            throw new IllegalArgumentException("RUID must be a non-empty string");
        }
        this.ruid = ruid.trim();
        return getThis();
    }

    /**
     * Returns the name of the Brightpearl service that contains the resource to be called.
     * @param service service containing the target resource.
     * @return builder instance for method chaining.
     */
    public B withService(ServiceName service) {
        if (service == null) {
            throw new IllegalArgumentException("Service cannot be null");
        }
        this.service = service;
        return getThis();
    }

    /**
     * Set the path of the Brightpearl resource within the service provided to {@link #withService(ServiceName)}. For example, for
     * the product GET API, this would be '/product'.
     * @param path the path to be set.
     * @return builder instance for method chaining.
     */
    public B withPath(String path) {
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("Path must be a non-empty string");
        }
        this.path = path.trim();
        return getThis();
    }

    /**
     * Set the generic type token representing the expected type of the response element in the JSON response body. This is used
     * by the client to deserialise the response, so if it does not match the expected response an exception may be thrown
     * or an empty object returned. {@link Type} is used instead of {@link Class} to provide support for deserialising
     * generic types e.g. collections and maps, however this does mean the type is not guaranteed to match the type token
     * T, which is used to avoid casting responses.
     * @param responseType the type to set.
     * @return builder instance for method chaining.
     */
    public B withResponseType(Type responseType) {
        this.responseType = responseType == null ? Void.class : responseType;
        return getThis();
    }

    /**
     * Add an additional unencoded querystring parameter to be added to the request, preserving the existing set but
     * overwriting any param with the same name.
     * @param name parameter name.
     * @param value parameter value.
     * @return builder instance for method chaining.
     */
    public B withAddedParam(String name, String value) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Param name must be a non-empty string");
        }
        if (value == null) {
            throw new IllegalArgumentException("Param value must not be null");
        }
        if (this.params == null) {
            this.params = new HashMap<String, String>();
        }
        this.params.put(name.trim(), value.trim());
        return getThis();
    }

    /**
     * Add additional unencoded querystring parameters to be added to the request, preserving the existing set but
     * overwriting any param with the same names.
     * @param params map of querystring params.
     * @return builder instance for method chaining.
     */
    public B withAddedParams(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Cannot add null params");
        }
        for (Map.Entry<String, String> param : params.entrySet()) {
            withAddedParam(param.getKey(), param.getValue());
        }
        return getThis();
    }

    /**
     * Add querystring parameters to the request, replacing any previously set.
     * @param params map of querystring params.
     * @return builder instance for method chaining.
     */
    public B withParams(Map<String, String> params) {
        this.params = new HashMap<String, String>();
        withAddedParams(params);
        return getThis();
    }

    protected String getRuid() {
        return ruid;
    }

    protected ServiceName getService() {
        return service;
    }

    protected Method getMethod() {
        return method;
    }

    protected String getPath() {
        return path;
    }

    protected Type getResponseType() {
        return responseType;
    }

    protected Map<String, String> getParams() {
        return params == null ? null : Collections.unmodifiableMap(params);
    }

    /**
     * Build the immutable request instance.
     * @return the request instance.
     */
    public abstract AbstractServiceRequest build();
}
