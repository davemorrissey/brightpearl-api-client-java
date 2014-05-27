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

import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * A factory for immutable {@link ServiceReadRequestSet} instances. These requests are used to load large sets of data
 * easily, by creating individual {@link ServiceReadRequest}s to the same API each with a subset of a large ID set.
 * <p>
 * This class is returned from static request builder methods in the services package, and provides a simple method for
 * setting parameters on all the requests before they are built.
 * <p>
 * This builder does not attempt to check that all requests added are to the same API.
 *
 * @param <T> placeholder for the response type, used to avoid casting of responses. This is not
 *           linked to the {@link Type} set on each request so a {@link ClassCastException} is possible if the type is set
 *           incorrectly.
 */
public class ServiceReadRequestSetBuilder<T> {

    // The set of requests to be executed. These should all be for the same API but there is no check.
    private Set<ServiceReadRequest<T>> requests = new LinkedHashSet<ServiceReadRequest<T>>();

    // Parameters for addition to every request.
    private Map<String, String> params;

    // Set to true once build() has been called. Indicates that ruids should be refreshed if it is called again.
    private boolean regenerateRuids = false;

    /**
     * Creates a new paged GET request builder configured with the URL to call and expected response type. This method does
     * not support extraction of generic types but is able to infer the type parameter &lt;T&gt;.
     * @param responseType type of result expected in the 'response' element of the JSON response body. This argument is not used for parsing, only to provide
     * @param <T> placeholder for the response type, used to avoid casting of responses.
     * @return an OPTIONS request builder instance.
     */
    public static <T> ServiceReadRequestSetBuilder<T> newReadRequestSet(Class<T> responseType) {
        return new ServiceReadRequestSetBuilder<T>();
    }

    /**
     * Creates a new paged GET request builder configured with the URL to call and expected response type. This method supports
     * extraction of generic types (e.g. typed lists or maps) from the response element.
     * @param <T> placeholder for the response type, used to avoid casting of responses.
     * @return A GET request builder instance.
     */
    public static <T> ServiceReadRequestSetBuilder<T> newReadRequestSet() {
        return new ServiceReadRequestSetBuilder<T>();
    }

    /**
     * Add an additional unencoded querystring parameter to be added to every request, preserving the existing set but
     * overwriting any param with the same name. The parameter is applied to every request when {@link #build()} is called,
     * so includes those not yet added.
     * @param name parameter name.
     * @param value parameter value.
     * @return builder instance for method chaining.
     */
    public ServiceReadRequestSetBuilder<T> withAddedParam(String name, String value) {
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
        return this;
    }

    /**
     * Add additional unencoded querystring parameters to be added to every request, preserving the existing set but
     * overwriting any param with the same names. The parameters are applied to every request when {@link #build()} is called,
     * so includes those not yet added.
     * @param params map of querystring params.
     * @return builder instance for method chaining.
     */
    public ServiceReadRequestSetBuilder<T> withAddedParams(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Cannot add null params");
        }
        for (Map.Entry<String, String> param : params.entrySet()) {
            withAddedParam(param.getKey(), param.getValue());
        }
        return this;
    }

    /**
     * Add querystring parameters to every request, replacing any previously set. The parameters are applied to every
     * request when {@link #build()} is called, so includes those not yet added.
     * @param params map of querystring params.
     * @return builder instance for method chaining.
     */
    public ServiceReadRequestSetBuilder<T> withParams(Map<String, String> params) {
        this.params = new HashMap<String, String>();
        withAddedParams(params);
        return this;
    }

    /**
     * Adds a request to the batch. This is a convenience method that accepts a builder so you can skip calling its
     * {@link ServiceReadRequestBuilder#build()} method.
     * @param requestBuilder A request to be added to the batch.
     * @return builder instance for method chaining.
     */
    public ServiceReadRequestSetBuilder<T> withAddedRequest(ServiceReadRequestBuilder<T> requestBuilder) {
        if (requestBuilder == null) {
            throw new NullPointerException("Request must not be null");
        }
        requests.add(requestBuilder.build());
        return this;
    }

    /**
     * Adds a request to the batch.
     * @param request A request to be added to the batch.
     * @return builder instance for method chaining.
     */
    public ServiceReadRequestSetBuilder<T> withAddedRequest(ServiceReadRequest<T> request) {
        if (request == null) {
            throw new NullPointerException("Request must not be null");
        }
        requests.add(request);
        return this;
    }

    /**
     * Adds a set of requests to the batch.
     * @param requests Requests to be added to the batch.
     * @return builder instance for method chaining.
     */
    public ServiceReadRequestSetBuilder<T> withAddedRequests(Set<ServiceReadRequest<T>> requests) {
        if (requests == null) {
            throw new NullPointerException("Request set must not be null");
        }
        for (ServiceReadRequest<T> addRequest : requests) {
            withAddedRequest(addRequest);
        }
        return this;
    }

    /**
     * Builds an immutable request set instance from values provided to this builder.
     * @return immutable request set instance.
     */
    public ServiceReadRequestSet<T> build() {
        Set<ServiceReadRequest<T>> buildRequests = new LinkedHashSet<ServiceReadRequest<T>>();
        for (ServiceReadRequest<T> templateRequest : requests) {
            buildRequests.add(ServiceReadRequestBuilder.<T>newGetRequest(templateRequest.getService(), templateRequest.getPath(), templateRequest.getResponseType())
                    .withRuid(regenerateRuids ? UUID.randomUUID().toString() : templateRequest.getRuid())
                    .withAddedParams(templateRequest.getParams())
                    .withAddedParams(params)
                    .build());
        }
        regenerateRuids = true;
        return new ServiceReadRequestSet<T>(buildRequests);
    }

}
