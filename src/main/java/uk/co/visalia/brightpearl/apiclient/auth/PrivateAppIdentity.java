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

/**
 * Contains the identity of a private app, which is specific to a single Brightpearl account. It contains the account
 * details as well, because all calls made for this app must go to the same account.
 */
public final class PrivateAppIdentity {

    private final String appReference;

    private final Account account;

    private PrivateAppIdentity(Account account, String appReference) {
        this.account = account;
        this.appReference = appReference;
    }

    /**
     * Create a private app identity.
     * @param account A Brightpearl customer account.
     * @param appReference The private app's unique reference.
     * @return An identity object.
     */
    public static PrivateAppIdentity create(Account account, String appReference) {
        if (account == null) { throw new IllegalArgumentException("Account must not be null"); }
        if (StringUtils.isBlank(appReference)) { throw new IllegalArgumentException("App reference must be supplied"); }
        return new PrivateAppIdentity(account, appReference);
    }

    public String getAppReference() {
        return appReference;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "PrivateAppIdentity{" +
                "appReference='" + appReference + '\'' +
                ", account=" + account +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateAppIdentity that = (PrivateAppIdentity) o;
        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (appReference != null ? !appReference.equals(that.appReference) : that.appReference != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = appReference != null ? appReference.hashCode() : 0;
        result = 31 * result + (account != null ? account.hashCode() : 0);
        return result;
    }
}
