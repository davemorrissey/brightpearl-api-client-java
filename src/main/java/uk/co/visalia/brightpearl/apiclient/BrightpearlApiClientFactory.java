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

package uk.co.visalia.brightpearl.apiclient;

import com.google.gson.Gson;
import uk.co.visalia.brightpearl.apiclient.http.ClientFactory;
import uk.co.visalia.brightpearl.apiclient.ratelimit.RateLimiter;

import java.util.Calendar;

/**
 * <p>
 * Constructs a configured {@link BrightpearlApiClient}. Supports construction in code using the builder pattern (starting
 * with {@link #brightpearlApiClient()}) and usage as a Spring bean factory. For example:
 * </p>
 * <pre>
 * &lt;bean id="brightpearlApiClientFactory" class="BrightpearlApiClientFactory"&gt;
 *     &lt;property name="clientFactory" ref="myClientFactory"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="brightpearlApiClient"
 *     factory-bean="brightpearlApiClientFactory"
 *     factory-method="build"/&gt;
 * </pre>
 * <p>
 * Defaults are available for all the configuration options so there is no requirement to set any options before building
 * the client instance.
 * </p>
 */
public final class BrightpearlApiClientFactory {

    private ClientFactory clientFactory;
    private RateLimiter rateLimiter;
    private Gson gson;

    /**
     * Static builder method for method chaining, fluent builder style.
     * @return a new {@link BrightpearlApiClientFactory} instance.
     */
    public static BrightpearlApiClientFactory brightpearlApiClient() {
        return new BrightpearlApiClientFactory();
    }

    /**
     * Constructs the immutable {@link BrightpearlApiClient} with configuration options provided, using defaults for
     * any not set.
     * @return an immutable {@link BrightpearlApiClient} instance.
     */
    public BrightpearlApiClient build() {
        return new BrightpearlApiClient(clientFactory, rateLimiter, gson);
    }

    /**
     * Set the {@link ClientFactory} that will supply {@link uk.co.visalia.brightpearl.apiclient.http.Client} instances for the execution of HTTP requests. By
     * default, an Apache HTTP Components implementation is used, with a dependency on org.apache.httpcomponents:httpclient:4.2.5.
     * This may be replaced with another implementation when required.
     * @param clientFactory custom client factory implementation.
     */
    public void setClientFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * Set the {@link RateLimiter} implementation to be used for monitoring and limiting request throughput. By default,
     * a no-op implementation is used, which places no restrictions on the number of requests made.
     * @param rateLimiter custom rate limiter implementation.
     */
    public void setRateLimiter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * Provide a custom GSON instance for serialising JSON request bodies and deserialising JSON responses. A custom
     * instance can support serialisation of types not used by the service package included in this client, for example
     * Joda dates. The default GSON instance used has the default configuration, except for the addition of support for
     * serialising {@link Calendar}s as ISO dates.
     * @param gson a custom configured GSON instance.
     */
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    /**
     * Set the {@link ClientFactory} that will supply {@link uk.co.visalia.brightpearl.apiclient.http.Client} instances for the execution of HTTP requests. By
     * default, an Apache HTTP Components implementation is used, with a dependency on org.apache.httpcomponents:httpclient:4.2.5.
     * This may be replaced with another implementation when required.
     * @param clientFactory custom client factory implementation.
     * @return builder instance for method chaining.
     */
    public BrightpearlApiClientFactory withClientFactory(ClientFactory clientFactory) {
        setClientFactory(clientFactory);
        return this;
    }

    /**
     * Set the {@link RateLimiter} implementation to be used for monitoring and limiting request throughput. By default,
     * a no-op implementation is used, which places no restrictions on the number of requests made.
     * @param rateLimiter custom rate limiter implementation.
     * @return builder instance for method chaining.
     */
    public BrightpearlApiClientFactory withRateLimiter(RateLimiter rateLimiter) {
        setRateLimiter(rateLimiter);
        return this;
    }

    /**
     * Provide a custom GSON instance for serialising JSON request bodies and deserialising JSON responses. A custom
     * instance can support serialisation of types not used by the service package included in this client, for example
     * Joda dates. The default GSON instance used has the default configuration, except for the addition of support for
     * serialising {@link Calendar}s as ISO dates.
     * @param gson a custom configured GSON instance.
     * @return builder instance for method chaining.
     */
    public BrightpearlApiClientFactory withGson(Gson gson) {
        setGson(gson);
        return this;
    }
}
