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

package uk.co.visalia.brightpearl.apiclient.http.httpclient4;

/**
 * <p>
 * Constructs a configured {@link HttpClient4ClientFactory}. Supports construction in
 * code using the builder pattern (starting with {@link #httpClient4ClientFactory()})
 * and usage as a Spring bean factory. For example:
 * </p>
 * <pre>
 * &lt;bean id="httpClient4ClientFactoryBuilder" class="HttpClient4ClientFactoryBuilder"&gt;
 *     &lt;property name="maxConnections"&gt;20&lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="httpClient4ClientFactory"
 *     factory-bean="httpClient4ClientFactoryBuilder"
 *     factory-method="build"/&gt;
 * </pre>
 * <p>
 * See static fields of {@link HttpClient4ClientFactory} for the default settings applied if methods in this builder are
 * not used to override them.
 * </p>
 */
public final class HttpClient4ClientFactoryBuilder {

    private int maxConnections = HttpClient4ClientFactory.DEFAULT_MAX_CONNECTIONS;
    private int maxConnectionsPerRoute = HttpClient4ClientFactory.DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
    private int connectionManagerTimeoutMs = HttpClient4ClientFactory.DEFAULT_CONNECTION_MANAGER_TIMEOUT_MS;
    private int connectionTimeoutMs = HttpClient4ClientFactory.DEFAULT_CONNECTION_TIMEOUT_MS;
    private int socketTimeoutMs = HttpClient4ClientFactory.DEFAULT_SOCKET_TIMEOUT_MS;
    private boolean allowRedirects = HttpClient4ClientFactory.DEFAULT_ALLOW_REDIRECTS;

    /**
     * Static builder method for method chaining, fluent builder style.
     * @return a new {@link HttpClient4ClientFactoryBuilder} instance.
     */
    public static HttpClient4ClientFactoryBuilder httpClient4ClientFactory() {
        return new HttpClient4ClientFactoryBuilder();
    }

    /**
     * Set the maximum total connections allowed across all routes. If creating a client for use with multiple Brightpearl
     * customer accounts there will be one route per datacenter.
     * @param maxConnections maximum total connections allowed for all routes.
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Set the maximum connections allowed per route. All Brightpearl customer accounts on a datacenter will share the
     * same route, so this is not a per-account limit.
     * @param maxConnectionsPerRoute maximum connections allowed per route.
     */
    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    /**
     * Sets the connection manager timeout in milliseconds. This is the time a thread will wait for a connection to become
     * free when the connection pool is exhausted. Use zero for indefinite blocking.
     * @param connectionManagerTimeoutMs connection manager timeout in milliseconds.
     */
    public void setConnectionManagerTimeoutMs(int connectionManagerTimeoutMs) {
        this.connectionManagerTimeoutMs = connectionManagerTimeoutMs;
    }

    /**
     * Set the time in milliseconds to wait for a connection to be established before aborting the request. A value of
     * zero is interpreted as infinite timeout.
     * @param connectionTimeoutMs connection timeout in milliseconds.
     */
    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    /**
     * Maximum time of inactivity to allow between two consecutive data packets from the server before aborting the
     * request. A value of zero is interpreted as an infinite timeout.
     * @param socketTimeoutMs socket timeout in milliseconds.
     */
    public void setSocketTimeoutMs(int socketTimeoutMs) {
        this.socketTimeoutMs = socketTimeoutMs;
    }

    /**
     * Set whether redirects should be followed. Redirects are not expected from the Brightpearl API according to the
     * documentation provided, and this setting is untested.
     * @param allowRedirects whether redirects should be followed.
     */
    public void setAllowRedirects(boolean allowRedirects) {
        this.allowRedirects = allowRedirects;
    }

    /**
     * Set the maximum total connections allowed across all routes. If creating a client for use with multiple Brightpearl
     * customer accounts there will be one route per datacenter.
     * @param maxConnections maximum total connections allowed for all routes.
     * @return builder instance for method chaining.
     */
    public HttpClient4ClientFactoryBuilder withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    /**
     * Set the maximum connections allowed per route. All Brightpearl customer accounts on a datacenter will share the
     * same route, so this is not a per-account limit.
     * @param maxConnectionsPerRoute maximum connections allowed per route.
     * @return builder instance for method chaining.
     */
    public HttpClient4ClientFactoryBuilder withMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        return this;
    }

    /**
     * Sets the connection manager timeout in milliseconds. This is the time a thread will wait for a connection to become
     * free when the connection pool is exhausted.
     * @param connectionManagerTimeoutMs connection manager timeout in milliseconds.
     * @return builder instance for method chaining.
     */
    public HttpClient4ClientFactoryBuilder withConnectionManagerTimeoutMs(int connectionManagerTimeoutMs) {
        this.connectionManagerTimeoutMs = connectionManagerTimeoutMs;
        return this;
    }

    /**
     * Set the time in milliseconds to wait for a connection to be established before aborting the request. A value of
     * zero is interpreted as infinite timeout.
     * @param connectionTimeoutMs connection timeout in milliseconds.
     * @return builder instance for method chaining.
     */
    public HttpClient4ClientFactoryBuilder withConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
        return this;
    }

    /**
     * Maximum time of inactivity to allow between two consecutive data packets from the server before aborting the
     * request. A value of zero is interpreted as an infinite timeout.
     * @param socketTimeoutMs socket timeout in milliseconds.
     * @return builder instance for method chaining.
     */
    public HttpClient4ClientFactoryBuilder withSocketTimeoutMs(int socketTimeoutMs) {
        this.socketTimeoutMs = socketTimeoutMs;
        return this;
    }

    /**
     * Set whether redirects should be followed. Redirects are not expected from the Brightpearl API according to the
     * documentation provided, and this setting is untested.
     * @param allowRedirects whether redirects should be followed.
     * @return builder instance for method chaining.
     */
    public HttpClient4ClientFactoryBuilder withAllowRedirects(boolean allowRedirects) {
        this.allowRedirects = allowRedirects;
        return this;
    }

    /**
     * Builds an immutable {@link HttpClient4ClientFactory} instance with the configuration supplied.
     * @return a {@link HttpClient4ClientFactory} instance.
     */
    public HttpClient4ClientFactory build() {
        return new HttpClient4ClientFactory(
                maxConnections,
                maxConnectionsPerRoute,
                connectionManagerTimeoutMs,
                connectionTimeoutMs,
                socketTimeoutMs,
                allowRedirects);
    }

}
