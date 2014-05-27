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

/**
 * Details of a single error message returned in the errors element of a JSON response. There is no reference available
 * for error codes.
 * @see <a href="http://www.brightpearl.com/developer/latest/concept/response-structure.html">http://www.brightpearl.com/developer/latest/concept/response-structure.html</a>
 */
public final class ServiceError implements Serializable {

    private final String code;

    private final String message;

    public ServiceError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Returns the error code identifying the error.
     * @return the error code.
     */
    public String getCode() {
        return code;
    }

    /**
     * A human readable description of the error, sometimes with additional details of its cause.
     * @return a human readable message.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ServiceError{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceError that = (ServiceError) o;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

}