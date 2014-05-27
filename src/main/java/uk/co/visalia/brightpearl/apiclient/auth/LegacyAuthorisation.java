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
 * Contains a legacy authentication token related to a staff member and not any developer or app.
 */
public class LegacyAuthorisation implements AppAuthorisation {

    private final Account account;

    private final String authToken;

    private LegacyAuthorisation(Account account, String authToken) {
        this.account = account;
        this.authToken = authToken;
    }

    /**
     * Create legacy authentication details using an account and staff token.
     * @param account Brightpearl customer account details.
     * @param authToken Staff authentication token.
     */
    public static LegacyAuthorisation staff(Account account, String authToken) {
        if (account == null) { throw new IllegalArgumentException("Account is required"); }
        if (StringUtils.isBlank(authToken)) { throw new IllegalArgumentException("Auth token is required"); }
        return new LegacyAuthorisation(account, authToken);
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put(LEGACY_AUTH_HEADER, authToken);
        return Collections.unmodifiableMap(headerMap);
    }

    @Override
    public String toString() {
        return "LegacyAuthorisation{" +
                "account=" + account +
                ", authToken='" + (authToken == null ? "null" : "[redacted]") + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LegacyAuthorisation that = (LegacyAuthorisation) o;
        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (authToken != null ? !authToken.equals(that.authToken) : that.authToken != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (authToken != null ? authToken.hashCode() : 0);
        return result;
    }
}
