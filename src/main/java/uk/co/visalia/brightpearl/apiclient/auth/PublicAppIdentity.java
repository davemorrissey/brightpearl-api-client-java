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

import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

/**
 * Contains the identity details of a public app.
 */
public final class PublicAppIdentity {

    private final String developerReference;

    private final String developerSecret;

    private final String appReference;

    private PublicAppIdentity(String developerReference, String developerSecret, String appReference) {
        this.developerReference = developerReference;
        this.developerSecret = developerSecret;
        this.appReference = appReference;
    }

    /**
     * Create a public app identity where the secret will not be required, either because all authentication will use
     * staff tokens or because account tokens will be supplied already signed.
     * @param developerReference Developer's unique reference.
     * @param appReference App's unique reference.
     * @return An identity object.
     */
    public static PublicAppIdentity create(String developerReference, String appReference) {
        if (StringUtils.isBlank(developerReference)) { throw new IllegalArgumentException("Developer reference must be supplied"); }
        if (StringUtils.isBlank(appReference)) { throw new IllegalArgumentException("App reference must be supplied"); }
        return new PublicAppIdentity(developerReference, null, appReference);
    }

    /**
     * Create a public app identity with an optional developer secret for signing account tokens.
     * @param developerReference Developer's unique reference.
     * @param appReference App's unique reference.
     * @return An identity object.
     */
    public static PublicAppIdentity create(String developerReference, String developerSecret, String appReference) {
        if (StringUtils.isBlank(developerReference)) { throw new IllegalArgumentException("Developer reference must be supplied"); }
        if (StringUtils.isBlank(appReference)) { throw new IllegalArgumentException("App reference must be supplied"); }
        return new PublicAppIdentity(developerReference, developerSecret, appReference);
    }

    public String getDeveloperReference() {
        return developerReference;
    }

    public String getDeveloperSecret() {
        return developerSecret;
    }

    public String getAppReference() {
        return appReference;
    }

    @Override
    public String toString() {
        return "PublicAppIdentity{" +
                "developerReference='" + developerReference + '\'' +
                ", developerSecret='" + (developerSecret == null ? "null" : "[redacted]") + '\'' +
                ", appReference='" + appReference + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicAppIdentity that = (PublicAppIdentity) o;
        if (appReference != null ? !appReference.equals(that.appReference) : that.appReference != null) return false;
        if (developerReference != null ? !developerReference.equals(that.developerReference) : that.developerReference != null)
            return false;
        if (developerSecret != null ? !developerSecret.equals(that.developerSecret) : that.developerSecret != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = developerReference != null ? developerReference.hashCode() : 0;
        result = 31 * result + (developerSecret != null ? developerSecret.hashCode() : 0);
        result = 31 * result + (appReference != null ? appReference.hashCode() : 0);
        return result;
    }
}