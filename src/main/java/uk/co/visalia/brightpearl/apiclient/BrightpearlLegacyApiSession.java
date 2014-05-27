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

import uk.co.visalia.brightpearl.apiclient.account.Account;
import uk.co.visalia.brightpearl.apiclient.account.UserCredentials;
import uk.co.visalia.brightpearl.apiclient.auth.LegacyAuthorisation;
import uk.co.visalia.brightpearl.apiclient.config.ExpiredAuthTokenStrategy;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlAuthException;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException;
import uk.co.visalia.brightpearl.apiclient.multimessage.MultiRequest;
import uk.co.visalia.brightpearl.apiclient.multimessage.MultiRequestBuilder;
import uk.co.visalia.brightpearl.apiclient.multimessage.MultiResponse;
import uk.co.visalia.brightpearl.apiclient.request.*;
import uk.co.visalia.brightpearl.apiclient.search.SearchResults;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static uk.co.visalia.brightpearl.apiclient.config.ExpiredAuthTokenStrategy.REAUTHENTICATE;

/**
 * <p>
 * For use with Brightpearl's old method of authentication - staff tokens access using staff email address and password.
 * These tokens are not specific to any developer or app, so the new method of using app credentials should be used in
 * preference.
 * </p><p>
 * This class provides a simple interface for calls to a single account, and management of API sessions including automatic
 * reauthentication if required. A session may be created with email and password credentials, which supports automatic
 * reauth, or with a pre-fetched authentication token, which gives a session that is valid until the token expires.
 * </p><p>
 * An instance is specific to an account and uses internal synchronization to delay requests for that account
 * from other threads while one thread is attempting authentication. If automatic reauth is enabled, the same applies
 * when a request fails due to the token expiring - the thread that first receives a 401 response from Brightpearl will
 * attempt to re-auth, while other threads block and wait for the result. This prevents further wasted calls with an
 * auth token known to be invalid.
 * </p><p>
 * If a user's password has been changed, the BrightpearlApiSession instance cannot be recovered without changing it back again. The
 * only recourse is to discard the session and start a new one.
 * </p><p>
 * Calls are passed to {@link BrightpearlApiClient} for execution and parsing.
 * </p>
 */
public final class BrightpearlLegacyApiSession {

    private final BrightpearlApiClient brightpearlApiClient;

    private final ExpiredAuthTokenStrategy expiredAuthTokenStrategy;

    private final Account account;

    private final UserCredentials userCredentials;

    private final AtomicReference<String> authTokenRef = new AtomicReference<String>();

    private final AtomicLong authRetryTime = new AtomicLong(0L);

    private final Lock authenticationLock = new ReentrantLock();

    private long authLockWait = 15000;
    private long authRetryInterval = 5000;

    BrightpearlLegacyApiSession(
            BrightpearlApiClient brightpearlApiClient,
            ExpiredAuthTokenStrategy expiredAuthTokenStrategy,
            Account account,
            UserCredentials userCredentials) {
        if (userCredentials == null) {
            throw new IllegalArgumentException("User credentials must be supplied");
        }
        this.brightpearlApiClient = brightpearlApiClient;
        this.expiredAuthTokenStrategy = expiredAuthTokenStrategy;
        this.account = account;
        this.userCredentials = userCredentials;
    }

    BrightpearlLegacyApiSession(
            BrightpearlApiClient brightpearlApiClient,
            ExpiredAuthTokenStrategy expiredAuthTokenStrategy,
            Account account,
            UserCredentials userCredentials,
            String authToken) {
        if (authToken == null) {
            throw new IllegalArgumentException("An authentication token must be supplied");
        }
        this.brightpearlApiClient = brightpearlApiClient;
        this.expiredAuthTokenStrategy = expiredAuthTokenStrategy;
        this.account = account;
        this.userCredentials = userCredentials;
        this.authTokenRef.set(authToken);
    }


    /**
     * <p>
     * Authenticates this session. For use in sessions using user credentials, and not pre-authenticated sessions that
     * were created with an auth token. The token is stored in the session and also returned.
     * </p><p>
     * It is not necessary to manually authenticate before using the session; the first request made will prompt
     * authentication if necessary. However, this will block requests from other threads so in systems that may experience
     * high load immediately once started - or once added to a load balancer - manually authenticating during startup is
     * recommended.
     * </p>
     * @return The new auth token.
     */
    public String authenticate() {
        if (userCredentials == null) {
            throw new IllegalStateException("Cannot authenticate session for account " + account.getAccountCode() + "; no credentials configured.");
        }
        String authToken = brightpearlApiClient.fetchLegacyAuthToken(account, userCredentials);
        authTokenRef.set(authToken);
        return authToken;
    }

    /**
     * <p>
     * Executes a GET or OPTIONs request and returns the parsed response if the request was successful. Success is
     * defined as a 2xx response code with a response element that could be successfully deserialised to the expected
     * type. If the server returned an error or the response element could not be parsed, a {@link BrightpearlClientException}
     * subclass will be thrown.
     * </p><p>
     * This is a convenience method that accepts a builder so you don't need to call its {@link ServiceReadRequestBuilder#build()}
     * method.
     * </p>
     * @param requestBuilder a {@link ServiceReadRequestBuilder} with all required fields set.
     * @param <T> Type of the expected response.
     * @return a response of the desired type, if the request was successful and the response was deserialised.
     * @see BrightpearlApiClient#get(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, uk.co.visalia.brightpearl.apiclient.request.ServiceReadRequest)
     */
    public <T> T get(ServiceReadRequestBuilder<T> requestBuilder) {
        return get(requestBuilder.build());
    }

    /**
     * Executes a GET or OPTIONs request and returns the parsed response if the request was successful. Success is
     * defined as a 2xx response code with a response element that could be successfully deserialised to the expected
     * type. If the server returned an error or the response element could not be parsed, a {@link BrightpearlClientException}
     * subclass will be thrown.
     *
     * @param request a {@link uk.co.visalia.brightpearl.apiclient.request.ServiceReadRequest} with all required fields set.
     * @param <T> Type of the expected response.
     * @return a response of the desired type, if the request was successful and the response was deserialised.
     * @see BrightpearlApiClient#get(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, uk.co.visalia.brightpearl.apiclient.request.ServiceReadRequest)
     */
    public <T> T get(ServiceReadRequest<T> request) {
        String authToken = authTokenRef.get();
        if (userCredentials != null) {
            authToken = authenticateIfRequired();
        }
        try {
            return brightpearlApiClient.get(LegacyAuthorisation.staff(account, authToken), request);
        } catch (BrightpearlAuthException e) {
            if (expiredAuthTokenStrategy == REAUTHENTICATE) {
                authTokenRef.set(null);
                authToken = authenticateIfRequired();
                return brightpearlApiClient.get(LegacyAuthorisation.staff(account, authToken), request);
            } else {
                throw e;
            }
        }
    }

    /**
     * <p>
     * Executes a search request and returns the parsed response if the request was successful. Success is
     * defined as a 2xx response code with search results that could be successfully deserialised to the expected
     * type. If the server returned an error or the response element could not be parsed, a {@link BrightpearlClientException}
     * subclass will be thrown.
     * </p><p>
     * This is a convenience method that accepts a builder so you don't need to call its {@link uk.co.visalia.brightpearl.apiclient.request.SearchRequestBuilder#build()}
     * method.
     * </p>
     * @param requestBuilder a {@link uk.co.visalia.brightpearl.apiclient.request.SearchRequestBuilder} with all required fields set.
     * @param <T> Type of the expected search result.
     * @return a search response containing results of the desired type, if the request was successful and the response was deserialised.
     * @see BrightpearlApiClient#search(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, uk.co.visalia.brightpearl.apiclient.request.ServiceSearchRequest)
     */
    public <T> SearchResults<T> search(SearchRequestBuilder<T> requestBuilder) {
        return search(requestBuilder.build());
    }

    /**
     * Executes a search request and returns the parsed response if the request was successful. Success is
     * defined as a 2xx response code with search results that could be successfully deserialised to the expected
     * type. If the server returned an error or the response element could not be parsed, a {@link BrightpearlClientException}
     * subclass will be thrown.
     * @param request a {@link uk.co.visalia.brightpearl.apiclient.request.ServiceSearchRequest} with all required fields set.
     * @param <T> Type of the expected search result.
     * @return a search response containing results of the desired type, if the request was successful and the response was deserialised.
     * @see BrightpearlApiClient#search(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, uk.co.visalia.brightpearl.apiclient.request.ServiceSearchRequest)
     */
    public <T> SearchResults<T> search(ServiceSearchRequest<T> request) {
        String authToken = authTokenRef.get();
        if (userCredentials != null) {
            authToken = authenticateIfRequired();
        }
        try {
            return brightpearlApiClient.search(LegacyAuthorisation.staff(account, authToken), request);
        } catch (BrightpearlAuthException e) {
            if (expiredAuthTokenStrategy == REAUTHENTICATE) {
                authTokenRef.set(null);
                authToken = authenticateIfRequired();
                return brightpearlApiClient.search(LegacyAuthorisation.staff(account, authToken), request);
            } else {
                throw e;
            }
        }
    }

    /**
     * <p>
     * Executes a write (PUT, POST, DELETE) request and returns the parsed response if the request was successful. Success is
     * defined as a 2xx response code with a response that could be successfully deserialised to the expected
     * type. If the server returned an error or the response element could not be parsed, a {@link BrightpearlClientException}
     * subclass will be thrown.
     * </p><p>
     * This is a convenience method that accepts a builder so you don't need to call its {@link uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequestBuilder#build()}
     * method.
     * </p>
     * @param requestBuilder a {@link uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequestBuilder} with all required fields set.
     * @param <T> Type of the expected result.
     * @return a response of the desired type, if the request was successful and the response was deserialised.
     * @see BrightpearlApiClient#execute(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequest)
     */
    public <T> T execute(ServiceWriteRequestBuilder<T> requestBuilder) {
        return execute(requestBuilder.build());
    }

    /**
     * Executes a write (PUT, POST, DELETE) request and returns the parsed response if the request was successful. Success is
     * defined as a 2xx response code with a response that could be successfully deserialised to the expected
     * type. If the server returned an error or the response element could not be parsed, a {@link BrightpearlClientException}
     * subclass will be thrown.
     * @param request a {@link uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequest} with all required fields set.
     * @param <T> Type of the expected result.
     * @return a response of the desired type, if the request was successful and the response was deserialised.
     * @see BrightpearlApiClient#execute(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequest)
     */
    public <T> T execute(ServiceWriteRequest<T> request) {
        String authToken = authTokenRef.get();
        if (userCredentials != null) {
            authToken = authenticateIfRequired();
        }
        try {
            return brightpearlApiClient.execute(LegacyAuthorisation.staff(account, authToken), request);
        } catch (BrightpearlAuthException e) {
            if (expiredAuthTokenStrategy == REAUTHENTICATE) {
                authTokenRef.set(null);
                authToken = authenticateIfRequired();
                return brightpearlApiClient.execute(LegacyAuthorisation.staff(account, authToken), request);
            } else {
                throw e;
            }
        }
    }

    /**
     * <p>
     * Executes a multimessage and returns the parsed responses and error messages. See the documentation for
     * {@link BrightpearlApiClient#execute(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, MultiRequest)} for a full description. This method adds
     * automatic reauthentication and retry when a multimessage is rejected because the auth token is expired.
     * </p><p>
     * This is a convenience method that accepts a builder so you don't need to call its {@link MultiRequestBuilder#build()}
     * method.
     * </p>
     * @param multiRequestBuilder a {@link MultiRequestBuilder} to be executed.
     * @return results of the request execution.
     * @see BrightpearlApiClient#execute(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, MultiRequest)
     */
    public MultiResponse execute(MultiRequestBuilder multiRequestBuilder) {
        return execute(multiRequestBuilder.build());
    }

    /**
     * Executes a multimessage and returns the parsed responses and error messages. See the documentation for
     * {@link BrightpearlApiClient#execute(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, MultiRequest)} for a full description. This method adds
     * automatic reauthentication and retry when a multimessage is rejected because the auth token is expired.
     *
     * @param multiRequest a {@link MultiRequest} to be executed.
     * @return results of the request execution.
     * @see BrightpearlApiClient#execute(uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation, MultiRequest)
     */
    public MultiResponse execute(MultiRequest multiRequest) {
        String authToken = authTokenRef.get();
        if (userCredentials != null) {
            authToken = authenticateIfRequired();
        }
        try {
            return brightpearlApiClient.execute(LegacyAuthorisation.staff(account, authToken), multiRequest);
        } catch (BrightpearlAuthException e) {
            if (expiredAuthTokenStrategy == REAUTHENTICATE) {
                authTokenRef.set(null);
                authToken = authenticateIfRequired();
                return brightpearlApiClient.execute(LegacyAuthorisation.staff(account, authToken), multiRequest);
            } else {
                throw e;
            }
        }
    }

    /**
     * For credentials sessions, handles first-time authentication, using double checked locking to ensure no two threads
     * will simultaneously attempt authentication. If an attempt within the last 5s threw an exception, pass it on.
     *
     * The first thread to find the auth token is null will kick off authentication.
     *
     * This method is unaware of re-authentication, though in theory it could be blocked by re-auth's hold on authenticationLock.
     */
    private String authenticateIfRequired() {
        String authToken = authTokenRef.get();
        if (authToken == null) {
            try {
                if (authenticationLock.tryLock(authLockWait, TimeUnit.MILLISECONDS)) {
                    try {
                        authToken = authTokenRef.get();
                        if (authToken != null) {
                            // While this thread waited, another completed authentication successfully.
                            return authToken;
                        } else if (authRetryTime.get() > System.currentTimeMillis()) {
                            // While this thread waited, another failed authentication, so it's too soon to try again.
                            throw new BrightpearlClientException("Request could not be executed; authentication has failed and is retried every 5 seconds");
                        } else {
                            // Still no token. This thread holds the lock and can start auth.
                            try {
                                return authenticate();
                            } catch (RuntimeException e) {
                                authRetryTime.set(System.currentTimeMillis() + authRetryInterval);
                                throw e;
                            }
                        }
                    } finally {
                        authenticationLock.unlock();
                    }
                } else {
                    throw new BrightpearlClientException("Request could not be executed; thread timed out waiting for a lock on authentication");
                }
            } catch (InterruptedException e) {
                throw new BrightpearlClientException("Request could not be executed; thread was interrupted while waiting for a lock on authentication");
            }
        } else {
            return authToken;
        }
    }

    void setAuthLockWait(long authLockWait) {
        this.authLockWait = authLockWait;
    }

    void setAuthRetryInterval(long authRetryInterval) {
        this.authRetryInterval = authRetryInterval;
    }
}