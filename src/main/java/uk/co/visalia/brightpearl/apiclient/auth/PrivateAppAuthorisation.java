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

package uk.co.visalia.brightpearl.apiclient.auth;

import uk.co.visalia.brightpearl.apiclient.account.Account;
import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the details of a private app, the Brightpearl customer account to which it belongs, and either an account token
 * or a staff token for authorisation of calls to this account.
 */
public final class PrivateAppAuthorisation implements AppAuthorisation {

    private final PrivateAppIdentity appIdentity;

    private final String accountToken;

    private final String staffToken;

    private PrivateAppAuthorisation(PrivateAppIdentity appIdentity, String accountToken, String staffToken) {
        this.appIdentity = appIdentity;
        this.accountToken = accountToken;
        this.staffToken = staffToken;
    }

    /**
     * Create account authorisation details for a private (own account) system-to-system call using the account token
     * generated for a private app.
     * @param appIdentity Details of the private app including the Brightpearl customer account it belongs to.
     * @param accountToken Account token generated for the private app.
     * @return authorisation details.
     */
    public static PrivateAppAuthorisation system(PrivateAppIdentity appIdentity, String accountToken) {
        if (appIdentity == null) { throw new IllegalArgumentException("App identity is required"); }
        if (StringUtils.isBlank(accountToken)) { throw new IllegalArgumentException("Account token is required"); }
        return new PrivateAppAuthorisation(appIdentity, accountToken, null);
    }

    /**
     * Create account authorisation details for a staff authorised API call for a private app.
     * @param appIdentity Details of the private app including the Brightpearl customer account it belongs to.
     * @param staffToken Account token, signed or unsigned. If it is unsigned, the developer secret is required.
     * @return authorisation details.
     */
    public static PrivateAppAuthorisation staff(PrivateAppIdentity appIdentity, String staffToken) {
        if (appIdentity == null) { throw new IllegalArgumentException("App identity is required"); }
        if (StringUtils.isBlank(staffToken)) { throw new IllegalArgumentException("Staff token is required"); }
        return new PrivateAppAuthorisation(appIdentity, null, staffToken);
    }

    public PrivateAppIdentity getAppIdentity() {
        return appIdentity;
    }

    public String getAccountToken() {
        return accountToken;
    }

    public String getStaffToken() {
        return staffToken;
    }

    @Override
    public Account getAccount() {
        return appIdentity.getAccount();
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put(APP_HEADER, appIdentity.getAppReference());
        if (accountToken != null) {
            headerMap.put(ACCOUNT_TOKEN_HEADER, accountToken);
        } else {
            headerMap.put(STAFF_TOKEN_HEADER, staffToken);
        }
        return Collections.unmodifiableMap(headerMap);
    }

    @Override
    public String toString() {
        return "PrivateAppAuthorisation{" +
                "appIdentity=" + appIdentity +
                ", accountToken='" + (accountToken == null ? "null" : "[redacted]") + '\'' +
                ", staffToken='" + (staffToken == null ? "null" : "[redacted]") + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateAppAuthorisation that = (PrivateAppAuthorisation) o;
        if (accountToken != null ? !accountToken.equals(that.accountToken) : that.accountToken != null) return false;
        if (appIdentity != null ? !appIdentity.equals(that.appIdentity) : that.appIdentity != null) return false;
        if (staffToken != null ? !staffToken.equals(that.staffToken) : that.staffToken != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = appIdentity != null ? appIdentity.hashCode() : 0;
        result = 31 * result + (accountToken != null ? accountToken.hashCode() : 0);
        result = 31 * result + (staffToken != null ? staffToken.hashCode() : 0);
        return result;
    }
}
