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
import uk.co.visalia.brightpearl.apiclient.util.Base64;
import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the details of an app, a Brightpearl customer account and the tokens required to verify the app's permission
 * to make API calls to the account.
 */
public final class PublicAppAuthorisation implements AppAuthorisation {

    private final PublicAppIdentity appIdentity;

    private final Account account;

    private final String accountToken;

    private final String staffToken;

    private PublicAppAuthorisation(PublicAppIdentity appIdentity, Account account, String accountToken, String staffToken) {
        this.appIdentity = appIdentity;
        this.account = account;
        this.accountToken = accountToken;
        this.staffToken = staffToken;
    }

    /**
     * Create account authorisation details for a system-to-system call using an account token.
     * @param appIdentity Identifies the developer and app.
     * @param account Brightpearl customer account details.
     * @param accountToken Account token, signed or unsigned. If it is unsigned, the developer secret is required.
     * @return authorisation details.
     */
    public static PublicAppAuthorisation system(PublicAppIdentity appIdentity, Account account, String accountToken) {
        if (appIdentity == null) { throw new IllegalArgumentException("App identity is required"); }
        if (account == null) { throw new IllegalArgumentException("Account is required"); }
        if (StringUtils.isBlank(accountToken)) { throw new IllegalArgumentException("Account token is required"); }

        if (accountToken.matches("[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}")) {
            if (StringUtils.isBlank(appIdentity.getDeveloperSecret())) {
                throw new IllegalArgumentException("Cannot sign account token without developer secret");
            }
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec key = new SecretKeySpec(appIdentity.getDeveloperSecret().getBytes(), "HmacSHA256");
                mac.init(key);
                accountToken = Base64.encode(new String(mac.doFinal(accountToken.getBytes())));
            } catch (Exception e) {
                throw new RuntimeException("Account token signing failed", e);
            }
        }

        return new PublicAppAuthorisation(appIdentity, account, accountToken, null);
    }

    /**
     * Create account authorisation details for a staff authorised API call.
     * @param appIdentity Identifies the developer and app.
     * @param account Brightpearl customer account details.
     * @param staffToken Account token, signed or unsigned. If it is unsigned, the developer secret is required.
     * @return authorisation details.
     */
    public static PublicAppAuthorisation staff(PublicAppIdentity appIdentity, Account account, String staffToken) {
        if (appIdentity == null) { throw new IllegalArgumentException("App identity is required"); }
        if (account == null) { throw new IllegalArgumentException("Account is required"); }
        if (StringUtils.isBlank(staffToken)) { throw new IllegalArgumentException("Staff token is required"); }
        return new PublicAppAuthorisation(appIdentity, account, null, staffToken);
    }

    public PublicAppIdentity getAppIdentity() {
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
        return account;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put(DEV_HEADER, appIdentity.getDeveloperReference());
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
        return "PublicAppAuthorisation{" +
                "appIdentity=" + appIdentity +
                ", account=" + account +
                ", accountToken='" + (accountToken == null ? "null" : "[redacted]") + '\'' +
                ", staffToken='" + (staffToken == null ? "null" : "[redacted]") + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicAppAuthorisation that = (PublicAppAuthorisation) o;
        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (accountToken != null ? !accountToken.equals(that.accountToken) : that.accountToken != null) return false;
        if (appIdentity != null ? !appIdentity.equals(that.appIdentity) : that.appIdentity != null) return false;
        if (staffToken != null ? !staffToken.equals(that.staffToken) : that.staffToken != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = appIdentity != null ? appIdentity.hashCode() : 0;
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (accountToken != null ? accountToken.hashCode() : 0);
        result = 31 * result + (staffToken != null ? staffToken.hashCode() : 0);
        return result;
    }
}
