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

package uk.co.visalia.brightpearl.apiclient.account;

import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import java.io.Serializable;

/**
 * Defines a Brightpearl customer account. There is no way to look up the datacenter that contains a unique account code,
 * so it must be configured.
 */
public final class Account implements Serializable {

    private final Datacenter datacenter;

    private final String accountCode;

    /**
     * Construct an account instance.
     * @param datacenter the datacenter that hosts the account.
     * @param accountCode the customer account code.
     */
    public Account(Datacenter datacenter, String accountCode) {
        if (datacenter == null || StringUtils.isBlank(datacenter.getHost()) || StringUtils.isBlank(datacenter.getName())) {
            throw new IllegalArgumentException("A datacenter name and host must be provided");
        }
        if (StringUtils.isBlank(accountCode)) {
            throw new IllegalArgumentException("An account code must be provided");
        }
        this.datacenter = datacenter;
        this.accountCode = accountCode;
    }

    /**
     * Returns the datacenter
     * @return the datacenter that hosts the account.
     */
    public Datacenter getDatacenter() {
        return datacenter;
    }

    public String getAccountCode() {
        return accountCode;
    }

    @Override
    public String toString() {
        return "Account{" +
                "datacenter=" + datacenter +
                ", accountCode='" + accountCode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        if (accountCode != null ? !accountCode.equals(account.accountCode) : account.accountCode != null) return false;
        if (datacenter != null ? !datacenter.equals(account.datacenter) : account.datacenter != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = datacenter != null ? datacenter.hashCode() : 0;
        result = 31 * result + (accountCode != null ? accountCode.hashCode() : 0);
        return result;
    }
}
