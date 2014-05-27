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

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.co.visalia.brightpearl.apiclient.account.Account;
import uk.co.visalia.brightpearl.apiclient.account.Datacenter;
import uk.co.visalia.brightpearl.apiclient.account.UserCredentials;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlAuthException;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException;
import uk.co.visalia.brightpearl.apiclient.http.Method;
import uk.co.visalia.brightpearl.apiclient.http.Request;
import uk.co.visalia.brightpearl.apiclient.http.ResponseBuilder;
import uk.co.visalia.brightpearl.apiclient.request.ServiceReadRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static uk.co.visalia.brightpearl.apiclient.config.ExpiredAuthTokenStrategy.FAIL;
import static uk.co.visalia.brightpearl.apiclient.config.ExpiredAuthTokenStrategy.REAUTHENTICATE;

@RunWith(MockitoJUnitRunner.class)
public class BrightpearlApiSessionTest {

    @Mock private uk.co.visalia.brightpearl.apiclient.http.ClientFactory httpClientFactory;
    @Mock private uk.co.visalia.brightpearl.apiclient.http.Client httpClient;

    private BrightpearlApiClient client;

    private Account account;
    private UserCredentials creds;
    private String token1;

    private AtomicInteger getCounter = new AtomicInteger(0);
    private AtomicInteger postCounter = new AtomicInteger(0);

    @Before
    public void setup() {

        account = new Account(Datacenter.EU1, "visalia");
        creds = new UserCredentials("example@domain.com", "password");
        token1 = "AAAAA11111";

        getCounter.set(0);
        postCounter.set(0);

        when(httpClientFactory.getClient(account)).thenReturn(httpClient);

        client = BrightpearlApiClientFactory.brightpearlApiClient().withClientFactory(httpClientFactory).build();

    }

    @Test(expected=IllegalArgumentException.class)
    public void disallowNullAuthToken() {

        new BrightpearlLegacyApiSession(client, FAIL, account, null, null);

    }

    @Test(expected=IllegalArgumentException.class)
    public void disallowNullCredentials() {

        new BrightpearlLegacyApiSession(client, FAIL, account, null);

    }

    @Test(expected=IllegalStateException.class)
    public void disallowAuthenticateCallWhenTokenSupplied() {

        BrightpearlLegacyApiSession session = new BrightpearlLegacyApiSession(client, FAIL, account, null, token1);
        session.authenticate();

    }

    @Test @SuppressWarnings("unchecked")
    public void doNotAttemptReauthWhenUsingToken() {

        BrightpearlLegacyApiSession session = new BrightpearlLegacyApiSession(client, FAIL, account, null, token1);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.GET) {
                    assertThat(request.getHeaders().get("brightpearl-auth"), is("AAAAA11111"));
                    return ResponseBuilder.newResponse().withStatus(401).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        try {
            session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
            Assert.fail("Expected exception not thrown");
        } catch (BrightpearlAuthException e) {
            assertThat(postCounter.get(), is(0));
        }

    }

    @Test @SuppressWarnings("unchecked")
    public void firstRequestTriggersAuthentication() {

        BrightpearlLegacyApiSession session = new BrightpearlLegacyApiSession(client, FAIL, account, creds);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":\"AAAAA11111\"}").build();
                } else if (request.getMethod() == Method.GET) {
                    getCounter.incrementAndGet();
                    assertThat(request.getHeaders().get("brightpearl-auth"), is("AAAAA11111"));
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));

        assertThat(postCounter.get(), is(1));
        assertThat(getCounter.get(), is(1));

    }

    @Test @SuppressWarnings("unchecked")
    public void doNotAttemptReauthInFailMode() {

        BrightpearlLegacyApiSession session = new BrightpearlLegacyApiSession(client, FAIL, account, creds);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":\"AAAAA11111\"}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        session.authenticate();

        assertThat(postCounter.get(), is(1));
        reset(httpClient);
        postCounter.set(0);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.GET) {
                    getCounter.incrementAndGet();
                    assertThat(request.getHeaders().get("brightpearl-auth"), is("AAAAA11111"));
                    return ResponseBuilder.newResponse().withStatus(401).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        try {
            session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
            Assert.fail("Expected exception not thrown");
        } catch (BrightpearlAuthException e) {
            assertThat(postCounter.get(), is(0));
        }

    }

    @Test(expected=BrightpearlClientException.class) @SuppressWarnings("unchecked")
    public void otherExceptionsDoNotTriggerAuth() {

        BrightpearlLegacyApiSession session = authenticatedReauthSession();

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.GET) {
                    getCounter.incrementAndGet();
                    assertThat(request.getHeaders().get("brightpearl-auth"), is("AAAAA11111"));
                    throw new BrightpearlClientException("other");
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));

        assertThat(getCounter.get(), is(1));
        assertThat(postCounter.get(), is(0));

    }

    @Test @SuppressWarnings("unchecked")
    public void reauthSuccessfullyAndRepeatRequest() {

        BrightpearlLegacyApiSession session = authenticatedReauthSession();

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":\"BBBBB22222\"}").build();
                } else if (request.getMethod() == Method.GET && request.getHeaders().get("brightpearl-auth").equals("AAAAA11111")) {
                    getCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(401).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else if (request.getMethod() == Method.GET && request.getHeaders().get("brightpearl-auth").equals("BBBBB22222")) {
                    getCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));

        assertThat(postCounter.get(), is(1));
        assertThat(getCounter.get(), is(2));

    }

    @Test @SuppressWarnings("unchecked")
    public void reauthFailsDoNotRepeatRequest_RethrowWithin10Seconds_RetryAfter10Seconds() throws Exception {

        BrightpearlLegacyApiSession session = authenticatedReauthSession();

        // Token expires so a request gets an auth token exception. Attempt to re-auth fails.

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(400).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"errors\":[]}").build();
                } else if (request.getMethod() == Method.GET && request.getHeaders().get("brightpearl-auth").equals("AAAAA11111")) {
                    getCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(401).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        try {
            session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
            Assert.fail("Expected exception not thrown");
        } catch (Exception e) {
            assertThat(getCounter.get(), is(1));
            assertThat(postCounter.get(), is(1));
        }

        reset(httpClient);
        getCounter.set(0);
        postCounter.set(0);

        // Immediate following request gets the same exception back without an attempt to auth or make the request.

        try {
            session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
            Assert.fail("Expected exception not thrown");
        } catch (Exception e) {
            assertThat(getCounter.get(), is(0));
            assertThat(postCounter.get(), is(0));
        }

        reset(httpClient);

        // After the re-auth retry period, another request is made, re-auth succeeds and the request proceeds as normal.

        Thread.sleep(6000);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":\"BBBBB22222\"}").build();
                } else if (request.getMethod() == Method.GET && request.getHeaders().get("brightpearl-auth").equals("BBBBB22222")) {
                    getCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));

        assertThat(getCounter.get(), is(1));
        assertThat(postCounter.get(), is(1));

    }

    @Test @SuppressWarnings("unchecked")
    public void reauthenticationTakesTooLong() throws Exception {

        final BrightpearlLegacyApiSession session = authenticatedReauthSession();
        session.setAuthLockWait(3000);

        // One thread makes a request and the auth token is expired. It starts auth, which takes 20 seconds, then completes
        // the retry successfully. Meanwhile another thread gives up waiting and throws an exception.

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    Thread.sleep(4000);
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":\"BBBBB22222\"}").build();
                } else if (request.getMethod() == Method.GET && request.getHeaders().get("brightpearl-auth").equals("AAAAA11111")) {
                    getCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(401).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else if (request.getMethod() == Method.GET && request.getHeaders().get("brightpearl-auth").equals("BBBBB22222")) {
                    getCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        final AtomicBoolean resultReceived = new AtomicBoolean(false);
        Thread authThread = new Thread(new Runnable() {
            @Override
            public void run() {
                session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
                resultReceived.set(true);
            }
        });

        final AtomicBoolean exceptionReceived = new AtomicBoolean(false);
        Thread waitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
                } catch (BrightpearlClientException e) {
                    e.printStackTrace();
                    exceptionReceived.set(true);
                }
            }
        });

        authThread.start();
        Thread.sleep(200);
        waitThread.start();

        authThread.join();
        waitThread.join();

        assertThat(resultReceived.get(), is(true));
        assertThat(exceptionReceived.get(), is(true));

    }

    @Test @SuppressWarnings("unchecked")
    public void threadsWaitForInitialAuth_SuccessfulAuthTokenUsed() throws Exception {

        final BrightpearlLegacyApiSession session = new BrightpearlLegacyApiSession(client, FAIL, account, creds);

        final AtomicBoolean expectRequests = new AtomicBoolean(false);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    Thread.sleep(2000);
                    expectRequests.set(true);
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":\"AAAAA11111\"}").build();
                } else if (request.getMethod() == Method.GET && request.getHeaders().get("brightpearl-auth").equals("AAAAA11111")) {
                    if (!expectRequests.get()) {
                        Assert.fail("Request made before authentication completed");
                    }
                    getCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 30; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
                    }
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // Despite 30 threads competing only one should authenticate
        assertThat(postCounter.get(), is(1));

    }

    @Test @SuppressWarnings("unchecked")
    public void threadsWaitForInitialAuth_FailedAuth_AllSubsequentRequestsFail() throws Exception {

        final BrightpearlLegacyApiSession session = new BrightpearlLegacyApiSession(client, FAIL, account, creds);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    Thread.sleep(2000);
                    return ResponseBuilder.newResponse().withStatus(400).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"errors\":[{\"code\":\"ABC\"}]}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 30; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
                    }
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // Despite 30 threads competing only one should authenticate
        assertThat(postCounter.get(), is(1));

    }

    @Test @SuppressWarnings("unchecked")
    public void threadsWaitForReauth_SuccessfulReauthTokenUsed() throws Exception {

        final BrightpearlLegacyApiSession session = authenticatedReauthSession();

        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean expectToken2 = new AtomicBoolean(false);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    Thread.sleep(2000);
                    expectToken2.set(true);
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":\"BBBBB22222\"}").build();
                } else if (request.getMethod() == Method.GET) {
                    getCounter.incrementAndGet();
                    int count = counter.incrementAndGet();
                    String token = request.getHeaders().get("brightpearl-auth");
                    if (expectToken2.get()) {
                        assertThat(token, is("BBBBB22222"));
                    } else {
                        assertThat(token, is("AAAAA11111"));
                    }
                    if (count == 1000) {
                        return ResponseBuilder.newResponse().withStatus(401).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                    } else {
                        return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                    }
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 30; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
                    }
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // Despite 30 threads competing only one should authenticate
        assertThat(postCounter.get(), is(1));

    }

    @Test @SuppressWarnings("unchecked")
    public void threadsWaitForReauth_FailedReauth_AllSubsequentRequestsFail() throws Exception {

        final BrightpearlLegacyApiSession session = authenticatedReauthSession();

        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean expectFurtherRequests = new AtomicBoolean(true);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    Thread.sleep(2000);
                    expectFurtherRequests.set(false);
                    return ResponseBuilder.newResponse().withStatus(401).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"errors\":[]}").build();
                } else if (request.getMethod() == Method.GET) {
                    getCounter.incrementAndGet();
                    int count = counter.incrementAndGet();
                    if (!expectFurtherRequests.get()) {
                        Assert.fail("Request to client made after auth failure");
                    }
                    if (count == 1000) {
                        return ResponseBuilder.newResponse().withStatus(401).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                    } else {
                        return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":{}}").build();
                    }
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 30; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        session.get(ServiceReadRequestBuilder.newGetRequest(ServiceName.CONTACT, "/contact", Object.class));
                    }
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // Despite 30 threads competing only one should authenticate
        assertThat(postCounter.get(), is(1));

    }

    private BrightpearlLegacyApiSession authenticatedReauthSession() {

        BrightpearlLegacyApiSession session = new BrightpearlLegacyApiSession(client, REAUTHENTICATE, account, creds);

        when(httpClient.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = (Request)invocationOnMock.getArguments()[0];
                if (request.getMethod() == Method.POST) {
                    postCounter.incrementAndGet();
                    return ResponseBuilder.newResponse().withStatus(200).withHeaders(ImmutableMap.of("content-type", "application/json")).withBody("{\"response\":\"AAAAA11111\"}").build();
                } else {
                    throw new IllegalStateException("Unexpected " + request.getMethod() + " request");
                }
            }
        });

        session.authenticate();

        assertThat(postCounter.get(), is(1));
        reset(httpClient);
        postCounter.set(0);

        return session;

    }
}
