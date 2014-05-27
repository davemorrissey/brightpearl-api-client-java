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
import java.util.UUID;

/**
 * A factory for immutable {@link ServiceWriteRequest} instances. Static methods provide POST, PUT and DELETE requests configured
 * with the minimum fields needed to execute a request, and chaining methods are available to set additional attributes
 * on the request before an immutable instance is build with {@link #build()}.
 * @param <T> placeholder for the response type, used to avoid casting of responses. This is not
 *           linked to the {@link Type} set on a request so a {@link ClassCastException} is possible if the type is set
 *           incorrectly.
 */
public class ServiceWriteRequestBuilder<T> extends AbstractServiceRequestBuilder<ServiceWriteRequestBuilder<T>> {

    private ServiceWriteRequestBuilder() {
        super();
    }

    private Object entity;

    @Override
    protected ServiceWriteRequestBuilder<T> getThis() {
        return this;
    }

    /**
     * Creates a new POST request builder configured with the URL to call and expected response type. This method does
     * not support extraction of generic types but is able to infer the type parameter &lt;T&gt;.
     * @param service Brightpearl service that contains the resource.
     * @param path path to the search resource, for example '/product'.
     * @param entity an object to be serialised as JSON and included in the request body. Null is permitted, but rarely appropriate.
     * @param responseType type of result expected in the 'response' element of the JSON response body. Use Void or null if no response is expected, or the response is not required.
     * @param <T> placeholder for the response type, used to avoid casting of responses.
     * @return A POST request builder instance.
     */
    public static <T> ServiceWriteRequestBuilder<T> newPostRequest(ServiceName service, String path, Object entity, Class<T> responseType) {
        ServiceWriteRequestBuilder<T> builder = new ServiceWriteRequestBuilder<T>();
        return builder.withMethod(Method.POST)
                .withService(service)
                .withPath(path)
                .withEntity(entity)
                .withResponseType(responseType);
    }

    /**
     * Creates a new POST request builder configured with the URL to call and expected response type. This method supports
     * extraction of generic types (e.g. typed lists or maps) from the response element.
     * @param service Brightpearl service that contains the resource.
     * @param path path to the search resource, for example '/product'.
     * @param entity an object to be serialised as JSON and included in the request body. Null is permitted, but rarely appropriate.
     * @param responseType type of result expected in the 'response' element of the JSON response body. Use Void or null if no response is expected, or the response is not required.
     * @param <T> placeholder for the response type, used to avoid casting of responses.
     * @return A POST request builder instance.
     */
    public static <T> ServiceWriteRequestBuilder<T> newPostRequest(ServiceName service, String path, Object entity, Type responseType) {
        ServiceWriteRequestBuilder<T> builder = new ServiceWriteRequestBuilder<T>();
        return builder.withMethod(Method.POST)
                .withService(service)
                .withPath(path)
                .withEntity(entity)
                .withResponseType(responseType);
    }

    /**
     * Creates a new PUT request builder configured with the URL to call and expected response type. This method does
     * not support extraction of generic types but is able to infer the type parameter &lt;T&gt;.
     * @param service Brightpearl service that contains the resource.
     * @param path path to the search resource, for example '/product'.
     * @param entity an object to be serialised as JSON and included in the request body. Null is permitted, but rarely appropriate.
     * @param responseType type of result expected in the 'response' element of the JSON response body. Use Void or null if no response is expected, or the response is not required.
     * @param <T> placeholder for the response type, used to avoid casting of responses.
     * @return A PUT request builder instance.
     */
    public static <T> ServiceWriteRequestBuilder<T> newPutRequest(ServiceName service, String path, Object entity, Class<T> responseType) {
        ServiceWriteRequestBuilder<T> builder = new ServiceWriteRequestBuilder<T>();
        return builder.withMethod(Method.PUT)
                .withService(service)
                .withPath(path)
                .withEntity(entity)
                .withResponseType(responseType);
    }

    /**
     * Creates a new PUT request builder configured with the URL to call and expected response type. This method supports
     * extraction of generic types (e.g. typed lists or maps) from the response element.
     * @param service Brightpearl service that contains the resource.
     * @param path path to the search resource, for example '/product'.
     * @param entity an object to be serialised as JSON and included in the request body. Null is permitted, but rarely appropriate.
     * @param responseType type of result expected in the 'response' element of the JSON response body. Use Void or null if no response is expected, or the response is not required.
     * @param <T> placeholder for the response type, used to avoid casting of responses.
     * @return A PUT request builder instance.
     */
    public static <T> ServiceWriteRequestBuilder<T> newPutRequest(ServiceName service, String path, Object entity, Type responseType) {
        ServiceWriteRequestBuilder<T> builder = new ServiceWriteRequestBuilder<T>();
        return builder.withMethod(Method.PUT)
                .withService(service)
                .withPath(path)
                .withEntity(entity)
                .withResponseType(responseType);
    }

    /**
     * Creates a new DELETE request builder configured with the URL to call and expected response type. This method does
     * not support extraction of generic types but is able to infer the type parameter &lt;T&gt;.
     * @param service Brightpearl service that contains the resource.
     * @param path path to the search resource, for example '/product'.
     * @param responseType type of result expected in the 'response' element of the JSON response body. Use Void or null if no response is expected, or the response is not required.
     * @param <T> placeholder for the response type, used to avoid casting of responses.
     * @return A delete request builder instance.
     */
    public static <T> ServiceWriteRequestBuilder<T> newDeleteRequest(ServiceName service, String path, Class<T> responseType) {
        ServiceWriteRequestBuilder<T> builder = new ServiceWriteRequestBuilder<T>();
        return builder.withMethod(Method.DELETE)
                .withService(service)
                .withPath(path)
                .withResponseType(responseType);
    }

    /**
     * Creates a new DELETE request builder configured with the URL to call and expected response type. This method supports
     * extraction of generic types (e.g. typed lists or maps) from the response element.
     * @param service Brightpearl service that contains the resource.
     * @param path path to the search resource, for example '/product'.
     * @param responseType type of result expected in the 'response' element of the JSON response body. Use Void or null if no response is expected, or the response is not required.
     * @param <T> placeholder for the response type, used to avoid casting of responses.
     * @return A delete request builder instance.
     */
    public static <T> ServiceWriteRequestBuilder<T> newDeleteRequest(ServiceName service, String path, Type responseType) {
        ServiceWriteRequestBuilder<T> builder = new ServiceWriteRequestBuilder<T>();
        return builder.withMethod(Method.DELETE)
                .withService(service)
                .withPath(path)
                .withResponseType(responseType);
    }

    private ServiceWriteRequestBuilder<T> withEntity(Object entity) {
        this.entity = entity;
        return getThis();
    }

    /**
     * Builds an immutable request instance from values provided to this builder.
     * @return immutable request instance.
     */
    public ServiceWriteRequest<T> build() {
        String ruid = StringUtils.isNotEmpty(getRuid()) ? getRuid() : UUID.randomUUID().toString();
        resetRuid();
        return new ServiceWriteRequest<T>(ruid, getService(), getMethod(), getPath(), getResponseType(), getParams(), entity);
    }

}