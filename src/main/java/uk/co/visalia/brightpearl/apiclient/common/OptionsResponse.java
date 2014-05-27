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

package uk.co.visalia.brightpearl.apiclient.common;

import java.io.Serializable;
import java.util.List;

/**
 * The standard response from a Brightpearl OPTIONs API. This contains a list of URIs for the corresponding GET API that
 * may be used to fetch a large set of data.
 */
public class OptionsResponse implements Serializable {

    private List<String> getUris;

    /**
     * Returns the set of URIs.
     * @return the set of URIs.
     */
    public List<String> getGetUris() {
        return getUris;
    }

    /**
     * Returns a string representation of this class including all its fields and those of its nested objects. This may
     * generate long log messages but may be useful in testing and debugging.
     */
    @Override
    public String toString() {
        return "OptionsResponse{" +
                ", getUris='" + getUris + '\'' +
                '}';
    }

    /**
     * Considers two objects to be equal if they have the same class and all their fields are equal. This should be used
     * with caution in production code; business logic using object identifer comparison is usually preferable.
     * @param o the reference object with which to compare.
     * @return if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionsResponse optionsResponse = (OptionsResponse) o;
        if (getUris != null ? !getUris.equals(optionsResponse.getUris) : optionsResponse.getUris != null) return false;
        return true;
    }

    /**
     * Returns a hashCode calculated from all fields.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = getUris != null ? getUris.hashCode() : 0;
        return result;
    }
}
