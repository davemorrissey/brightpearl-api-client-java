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
 * Contains the email address and password of an account user with API access to be used for authentication.
 */
public final class UserCredentials implements Serializable {

    private final String emailAddress;

    private final String password;

    /**
     * Create an immutable credentials instance with a supplied email address and password.
     * @param emailAddress Email address of a user account within a Brightpearl customer account. Must be a non-empty string.
     * @param password The user's password. Must be a non-empty string.
     */
    public UserCredentials(String emailAddress, String password) {
        if (StringUtils.isBlank(emailAddress)) {
            throw new IllegalArgumentException("An email address must be supplied");
        }
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("A password must be supplied");
        }
        this.emailAddress = emailAddress.trim();
        this.password = password.trim();
    }

    /**
     * Returns the email address.
     * @return the email address.
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Returns the password.
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "UserCredentials{" +
                "emailAddress='" + emailAddress + '\'' +
                ", password='" + (password == null ? "null" : "[redacted]") + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCredentials that = (UserCredentials) o;
        if (emailAddress != null ? !emailAddress.equals(that.emailAddress) : that.emailAddress != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = emailAddress != null ? emailAddress.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
