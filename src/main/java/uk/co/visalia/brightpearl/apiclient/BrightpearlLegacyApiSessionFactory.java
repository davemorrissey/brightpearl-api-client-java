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
import uk.co.visalia.brightpearl.apiclient.config.ExpiredAuthTokenStrategy;
import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

/**
 * <p>
 * A factory class used to create {@link BrightpearlLegacyApiSession}s, which are specific to a Brightpearl account. Sessions
 * may optionally be provided with user credentials; if user credentials are provided, automatic re-authentication is
 * supported.
 * </p><p>
 * Each factory instance is specific to a Brightpearl customer account because it requires the account details (datacenter
 * and customer code) to create a session instance for that account. At minimum, the factory must be configured with the
 * account details by calling {@link #setAccount(Account)}, and if configured credentials are to be used, {@link #setUserCredentials(uk.co.visalia.brightpearl.apiclient.account.UserCredentials)}
 * must also be called.
 * </p><p>
 * Factory instances are intended to be persistent and used to create multiple sessions for an account during the lifetime
 * of an application as authentication tokens expire, but when automatic reauth is enabled and sessions are created using
 * credentials, one session may last indefinitely. In any case, only one active session for an account should be used at
 * any time to avoid unexpected behaviour, for example exceeding the expected connection limit, or hitting the request cap
 * despite using a strategy to avoid it. This factory may be wrapped in a facade that caches the active session until
 * it expires to provide this behaviour.
 * </p><p>
 * Instances can be constructed in code, and this class can also be used as a Spring bean factory provided that credentials
 * authentication is being used. For example:
 * </p>
 * <pre>
 * &lt;bean id="brightpearlApiSessionFactory" class="uk.co.visalia.brightpearl.apiclient.BrightpearlLegacyApiSessionFactory"&gt;
 *     &lt;property name="brightpearlApiClient" ref="..."/&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="brightpearlApiSession"
 *     factory-bean="brightpearlApiSessionFactory"
 *     factory-method="newApiSession"/&gt;
 * </pre>
 * <p>
 * </p>
 *
 */
public final class BrightpearlLegacyApiSessionFactory {

    private BrightpearlApiClient brightpearlApiClient;

    private Account account;

    private UserCredentials userCredentials;

    private ExpiredAuthTokenStrategy expiredAuthTokenStrategy;

    /**
     * Creates a new session factory instance.
     */
    public static BrightpearlLegacyApiSessionFactory newApiSessionFactory() {
        return new BrightpearlLegacyApiSessionFactory();
    }

    /**
     * Set the underlying {@link BrightpearlApiClient} that requests are sent to for execution. If this method is not
     * called, an instance of the class using all its default settings will be used.
     * @param brightpearlApiClient custom configured {@link BrightpearlApiClient} instance.
     */
    public void setBrightpearlApiClient(BrightpearlApiClient brightpearlApiClient) {
        this.brightpearlApiClient = brightpearlApiClient;
    }

    /**
     * Set the target Brightpearl customer account for sessions created by this factory.
     * @param account target account.
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * Set the credentials of an API-enabled user account associated with the customer account. Credentials are not
     * required if sessions will be created with tokens generated externally.
     * @param userCredentials credentials of an API user.
     */
    public void setUserCredentials(UserCredentials userCredentials) {
        this.userCredentials = userCredentials;
    }

    /**
     * Set the strategy to use when credentials authentication is configured and the current auth token expires.
     * @param expiredAuthTokenStrategy auth token expiry strategy.
     */
    public void setExpiredAuthTokenStrategy(ExpiredAuthTokenStrategy expiredAuthTokenStrategy) {
        this.expiredAuthTokenStrategy = expiredAuthTokenStrategy;
    }

    /**
     * Set the underlying {@link BrightpearlApiClient} that requests are sent to for execution. If this method is not
     * called, an instance of the class using all its default settings will be used.
     * @param brightpearlApiClient custom configured {@link BrightpearlApiClient} instance.
     * @return builder instance for method chaining.
     */
    public BrightpearlLegacyApiSessionFactory withBrightpearlApiClient(BrightpearlApiClient brightpearlApiClient) {
        setBrightpearlApiClient(brightpearlApiClient);
        return this;
    }

    /**
     * Set the target Brightpearl customer account for sessions created by this factory.
     * @param account target account.
     * @return builder instance for method chaining.
     */
    public BrightpearlLegacyApiSessionFactory withAccount(Account account) {
        setAccount(account);
        return this;
    }

    /**
     * Set the credentials of an API-enabled user account associated with the customer account. Credentials are not
     * required if sessions will be created with tokens generated externally.
     * @param userCredentials credentials of an API user.
     * @return builder instance for method chaining.
     */
    public BrightpearlLegacyApiSessionFactory withUserCredentials(UserCredentials userCredentials) {
        setUserCredentials(userCredentials);
        return this;
    }

    /**
     * Set the strategy to use when credentials authentication is configured and the current auth token expires.
     * @param expiredAuthTokenStrategy auth token expiry strategy.
     * @return builder instance for method chaining.
     */
    public BrightpearlLegacyApiSessionFactory withExpiredAuthTokenStrategy(ExpiredAuthTokenStrategy expiredAuthTokenStrategy) {
        setExpiredAuthTokenStrategy(expiredAuthTokenStrategy);
        return this;
    }

    /**
     * <p>
     * Creates a new session using configured credentials. An auth token will be generated either when {@link BrightpearlLegacyApiSession#authenticate()}
     * is called, or when the first request is made.
     * </p><p>
     * Automatic re-authentication is disabled by default and may be enabled by calling
     * {@link #setExpiredAuthTokenStrategy(ExpiredAuthTokenStrategy)} with {@link ExpiredAuthTokenStrategy#REAUTHENTICATE}.
     * With this configuration, the session will detect authentication failures and make one attempt at re-authenticating
     * with the same credentials before retrying the request that failed. Unless the API user's password is changed, which
     * will prevent re-authentication, this allows one ApiSession instance to last indefinitely.
     * </p>
     * @return a configured and immutable {@link BrightpearlLegacyApiSession}.
     */
    public BrightpearlLegacyApiSession newApiSession() {
        checkAccount();
        checkUserCredentials(false);
        return new BrightpearlLegacyApiSession(defaultedApiClient(), defaultedExpiredAuthTokenStrategy(), account, userCredentials);
    }

    /**
     * <p>
     * Creates a new session pre-authenticated with a token generated independently.
     * </p><p>
     * If credentials have been supplied and automatic re-authentication has been enabled by calling {@link #setExpiredAuthTokenStrategy(ExpiredAuthTokenStrategy)} with {@link ExpiredAuthTokenStrategy#REAUTHENTICATE},
     * a new token can be generated when the provided one expires. With this configuration, the session will detect
     * authentication failures and make one attempt at re-authenticating with the same credentials before retrying the
     * request that failed. Unless the API user's password is changed, which will prevent re-authentication, this allows
     * one ApiSession instance to last indefinitely.
     * </p><p>
     * If no credentials are supplied or the strategy is set to {@link ExpiredAuthTokenStrategy#FAIL}, the session will last until the
     * first time a request is rejected because the token has expired, which occurs after an undefined period of inactivity.
     * </p><p>
     * This method allows you to manually provide an auth token for a single use session, or prime a new long-lived session
     * with a stored token - useful for Android where keeping a session instance alive may be undesirable. Avoid priming
     * a new session with a token that has not been used for 30 minutes, because the first request is guaranteed to fail.
     * </p>
     * @param authToken the initial auth token to be used for this session.
     * @return an {@link BrightpearlLegacyApiSession} instance.
     */
    public BrightpearlLegacyApiSession newPreauthenticatedApiSession(String authToken) {
        if (StringUtils.isBlank(authToken)) {
            throw new IllegalArgumentException("Cannot create API session: An auth token must be supplied");
        }
        checkAccount();
        checkUserCredentials(true);
        return new BrightpearlLegacyApiSession(defaultedApiClient(), defaultedExpiredAuthTokenStrategy(), account, userCredentials, authToken);
    }

    /**
     * Check account preconditions, all fields must be set but no validation included.
     */
    private void checkAccount() {
        if (account == null ||
                account.getDatacenter() == null ||
                StringUtils.isBlank(account.getDatacenter().getHost()) ||
                StringUtils.isBlank(account.getAccountCode())) {
            throw new IllegalStateException("Cannot create API session: Account must be supplied with a datacenter and account code");
        }
    }

    /**
     * Check user preconditions, all fields must be set but no validation included.
     */
    private void checkUserCredentials(boolean optional) {
        if (optional && userCredentials == null) {
            return;
        }
        if (userCredentials == null ||
                StringUtils.isBlank(userCredentials.getEmailAddress()) ||
                StringUtils.isBlank(userCredentials.getPassword())) {
            throw new IllegalStateException("Cannot create API session: User credentials must be supplied including email address and password");
        }
    }

    /**
     * If a client has not been specified, use the default settings including HttpClient 4.
     * @return An API client.
     */
    private BrightpearlApiClient defaultedApiClient() {
        if (brightpearlApiClient == null) {
            return BrightpearlApiClientFactory.brightpearlApiClient().build();
        } else {
            return brightpearlApiClient;
        }
    }

    private ExpiredAuthTokenStrategy defaultedExpiredAuthTokenStrategy() {
        if (expiredAuthTokenStrategy == null || userCredentials == null) {
            return ExpiredAuthTokenStrategy.FAIL;
        } else {
            return expiredAuthTokenStrategy;
        }
    }

}
