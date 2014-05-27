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

import java.util.regex.Pattern;

/**
 * An extendable enum containing the known Brightpearl services and allowing a custom service name to be used. Used to
 * create requests.
 */
public final class ServiceName {

    private static final Pattern PATH_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-]+");

    public static final ServiceName ACCOUNTING = new ServiceName("accounting-service");
    public static final ServiceName CONTACT = new ServiceName("contact-service");
    public static final ServiceName INTEGRATION = new ServiceName("integration-service");
    public static final ServiceName ORDER = new ServiceName("order-service");
    public static final ServiceName PRODUCT = new ServiceName("product-service");
    public static final ServiceName WAREHOUSE = new ServiceName("warehouse-service");

    private final String path;

    /**
     * Constructs a custom service name instance, using the path, for example &quot;product-service&quot;.
     * @param path path component for the service.
     */
    public ServiceName(String path) {
        validatePath(path);
        this.path = path;
    }

    /**
     * Returns the URI component for the service, for example &quot;product-service&quot;.
     * @return path component for the service.
     */
    public String getPath() {
        return path;
    }

    private void validatePath(String path) {
        if (path == null || path.trim().length() == 0) {
            throw new IllegalArgumentException("Service path must be provided");
        } else if (!PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException("Service must contain alphanumeric characters, hyphen and underscore only");
        }
    }

    @Override
    public String toString() {
        return "ServiceName{" +
                "path='" + path + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceName that = (ServiceName) o;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
