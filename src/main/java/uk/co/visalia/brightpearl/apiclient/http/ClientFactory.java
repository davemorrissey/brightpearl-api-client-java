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

package uk.co.visalia.brightpearl.apiclient.http;

import uk.co.visalia.brightpearl.apiclient.account.Account;

/**
 * Interface for classes that create {@link Client}s. In most cases, implementations should cache the client they return
 * from {@link #getClient(uk.co.visalia.brightpearl.apiclient.account.Account)} and support returning the same instance in response
 * to subsequent calls. If the underlying HTTP client does not support connection pooling or any other form of cached
 * resources for improved performance, implementations may instead return a new client for each request.
 */
public interface ClientFactory {

    /**
     * <p>
     * Get a client to be used for a given {@link uk.co.visalia.brightpearl.apiclient.account.Account}. This method will be called
     * for every request to be made to the Brightpearl API, so that client instances that support only a single request
     * are supported. Implementations may return the same instance in response to multiple calls to this method, and
     * should do so provided the client is thread-safe and supports multiple concurrent requests.
     * </p><p>
     * Creating a client for each customer account is not recommended but can be used as a method of throttling the
     * resources available to each account, so the account is included as a parameter to allow this.
     * </p>
     * @param account The Brightpearl customer account a client is required for.
     * @return A client instance appropriate for the account.
     */
    Client getClient(Account account);

}
