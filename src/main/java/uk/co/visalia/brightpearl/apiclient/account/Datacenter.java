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
 * Represents the datacenter on which an account is hosted. Static instances are included representing the known datacenters,
 * and a customer datacenter can be created if required. To find the datacenter for an account, see the link below.
 *
 * @see <a href="http://www.brightpearl.com/developer/latest/concept/uri-syntax.html">http://www.brightpearl.com/developer/latest/concept/uri-syntax.html</a>
 */
public final class Datacenter implements Serializable {

    /**
     * Datacenter EU1 (GMT, CET).
     */
    public static final Datacenter EU1 = new Datacenter("EU1", "https://ws-eu1.brightpearl.com");

    /**
     * Datacenter EU2 (for accounts where admin domain is https://secure2.thisispearl.com).
     */
    public static final Datacenter EU2 = new Datacenter("EU2", "https://ws-eu2.brightpearl.com");

    /**
     * Datacenter USE (EST, CST).
     */
    public static final Datacenter USE = new Datacenter("USE", "https://ws-use.brightpearl.com");

    /**
     * Datacenter USW (PST, MST).
     */
    public static final Datacenter USW = new Datacenter("USW", "https://ws-usw.brightpearl.com");

    private static final Datacenter[] DATACENTERS = { EU1, EU2, USE, USW };

    private final String name;

    private final String host;

    /**
     * Create a custom datacenter.
     * @param name User friendly name of the datacenter. This is used for logging only; any non-null value is valid.
     * @param host Scheme and host name of the datacenter, e.g. https://ws-usw.brightpearl.com. Trailing slashes should be omitted.
     */
    public Datacenter(String name, String host) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Datacenter name must be a non-empty string");
        }
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("Datacenter host must be a non-empty string");
        }
        this.name = name;
        this.host = host;
    }

    /**
     * Returns the name of the datacenter. This is an internal name only, useful for logging.
     * @return the name of the datacenter.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the datacenter host, including its scheme.
     * @return the datacenter host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Looks up a known datacenter by name, similar to looking up an enum by name. If a custom datacenter is being used
     * this method cannot be used; no static references to custom instances are kept.
     * @param name A datacenter name, corresponding to one of the static instances.
     * @return The datacenter matching the supplied name, or null if the name is invalid.
     */
    public static Datacenter forName(String name) {
        for (Datacenter datacenter : DATACENTERS) {
            if (datacenter.getName().equals(name)) {
                return datacenter;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Datacenter{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Datacenter that = (Datacenter) o;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        return result;
    }
}
