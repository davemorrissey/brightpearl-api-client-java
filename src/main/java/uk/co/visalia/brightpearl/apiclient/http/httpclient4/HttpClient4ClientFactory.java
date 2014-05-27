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

import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import uk.co.visalia.brightpearl.apiclient.account.Account;
import uk.co.visalia.brightpearl.apiclient.http.Client;
import uk.co.visalia.brightpearl.apiclient.http.ClientFactory;

/**
 * An implementation of {@link uk.co.visalia.brightpearl.apiclient.http.ClientFactory} that creates a single Apache HTTP Components {@link DefaultHttpClient}
 * instance using a {@link PoolingClientConnectionManager} to provide pooling and thread safety, and returns this
 * wrapped in a {@link HttpClient4Client} instance in response to all requests tp {@link #getClient(Account)}.
 */
public class HttpClient4ClientFactory implements ClientFactory {

    public static final int DEFAULT_MAX_CONNECTIONS = 20;
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 10;
    public static final int DEFAULT_CONNECTION_MANAGER_TIMEOUT_MS = 10000;
    public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 15000;
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 60000;
    public static final boolean DEFAULT_ALLOW_REDIRECTS = false;

    private int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
    private int connectionManagerTimeoutMs = DEFAULT_CONNECTION_MANAGER_TIMEOUT_MS;
    private int connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT_MS;
    private int socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;
    private boolean allowRedirects = DEFAULT_ALLOW_REDIRECTS;

    private final Object clientLock = new Object();
    private HttpClient4Client client;

    HttpClient4ClientFactory(
            int maxConnections,
            int maxConnectionsPerRoute,
            int connectionManagerTimeoutMs,
            int connectionTimeoutMs,
            int socketTimeoutMs,
            boolean allowRedirects) {
        this.maxConnections = maxConnections;
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        this.connectionManagerTimeoutMs = connectionManagerTimeoutMs;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.socketTimeoutMs = socketTimeoutMs;
        this.allowRedirects = allowRedirects;
    }

    /**
     * Returns the same instance of {@link HttpClient4Client}, backed by a {@link DefaultHttpClient} instance using a
     * thread-safe pooled connection manager, in response to every request.
     * @param account The Brightpearl customer account a client is required for.
     * @return a {@link HttpClient4Client} instance.
     */
    @Override
    public Client getClient(Account account) {
        if (client == null) {
            synchronized (clientLock) {
                if (client == null) {
                    createClient();
                }
            }
        }
        return client;
    }

    private void createClient() {
        BasicHttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeoutMs);
        httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMs);
        HttpClientParams.setRedirecting(httpParams, allowRedirects);
        HttpClientParams.setConnectionManagerTimeout(httpParams, connectionManagerTimeoutMs);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);

        this.client = new HttpClient4Client(httpClient);
    }


}
