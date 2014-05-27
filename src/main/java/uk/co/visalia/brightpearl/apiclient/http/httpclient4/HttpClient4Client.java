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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlHttpException;
import uk.co.visalia.brightpearl.apiclient.exception.ClientErrorCode;
import uk.co.visalia.brightpearl.apiclient.http.*;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link uk.co.visalia.brightpearl.apiclient.http.Client} using Apache HTTP Components {@link HttpClient}.
 */
public class HttpClient4Client implements Client {

    private HttpClient httpClient;

    HttpClient4Client(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * {@inheritDoc}
     * @param request The request to be executed.
     * @return Response from the Brightpearl API.
     */
    @Override
    public Response execute(Request request) {

        if (httpClient == null) {
            throw new IllegalStateException("HttpClient has been shut down");
        }

        HttpRequestBase clientRequest = buildRequest(request);

        try {
            HttpResponse clientResponse = httpClient.execute(clientRequest);

            int status = clientResponse.getStatusLine().getStatusCode();
            Map<String, String> headers = new HashMap<String, String>();
            for (Header header : clientResponse.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
            String body = EntityUtils.toString(clientResponse.getEntity());

            return ResponseBuilder.newResponse().withStatus(status).withHeaders(headers).withBody(body).build();

        } catch (Exception e) {
            throw resolveException(e);
        } finally {
            clientRequest.releaseConnection();
        }

    }

    private HttpRequestBase buildRequest(Request request) {

        Method method = request.getMethod();
        String url = request.getUrl();
        String body = request.getBody();

        if (request.getParameters() != null) {
            try {
                URIBuilder builder = new URIBuilder(url);
                for (Map.Entry<String, String> param : request.getParameters().entrySet()) {
                    builder.setParameter(param.getKey(), param.getValue());
                }
                url = builder.build().toString();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URL: \"" + request.getUrl() + "\"");
            }
        }

        HttpRequestBase clientRequest = createBaseRequest(method, url, body);

        if (request.getHeaders() != null) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                clientRequest.setHeader(header.getKey(), header.getValue());
            }
        }

        return clientRequest;

    }

    private HttpRequestBase createBaseRequest(Method method, String url, String body) {
        if (method == Method.GET) {
            return new HttpGet(url);
        } else if (method == Method.POST) {
            HttpPost post = new HttpPost(url);
            addBody(post, body);
            return post;
        } else if (method == Method.PUT) {
            HttpPut put = new HttpPut(url);
            addBody(put, body);
            return put;
        } else if (method == Method.DELETE) {
            return new HttpDelete(url);
        } else if (method == Method.OPTIONS) {
            return new HttpOptions(url);
        }
        throw new IllegalArgumentException("HTTP method " + method + " is not supported by this client");
    }

    private void addBody(HttpEntityEnclosingRequestBase requestBase, String body) {
        if (body != null) {
            requestBase.setEntity(new StringEntity(body, Charset.forName("UTF-8")));
        }
    }

    private BrightpearlHttpException resolveException(Exception e) {
        if (e instanceof ConnectTimeoutException) {
            throw new BrightpearlHttpException(ClientErrorCode.CONNECTION_TIMEOUT, e);
        } else if (e instanceof SocketTimeoutException) {
            throw new BrightpearlHttpException(ClientErrorCode.SOCKET_TIMEOUT, e);
        } else if (e instanceof SocketException) {
            throw new BrightpearlHttpException(ClientErrorCode.SOCKET_ERROR, e);
        } else if (e instanceof UnknownHostException) {
            throw new BrightpearlHttpException(ClientErrorCode.UNKNOWN_HOST, e);
        } else if (e instanceof NoHttpResponseException) {
            throw new BrightpearlHttpException(ClientErrorCode.NO_RESPONSE, e);
        } else {
            throw new BrightpearlHttpException(ClientErrorCode.OTHER_TRANSPORT_ERROR, e);
        }
    }

}