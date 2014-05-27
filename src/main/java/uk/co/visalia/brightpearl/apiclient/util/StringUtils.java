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

package uk.co.visalia.brightpearl.apiclient.util;

import java.util.Collection;

/**
 * A set of functions used internally, mostly replicated from commons-lang to avoid unwanted dependencies. Not
 * intended for use by clients of the API library.
 */
public final class StringUtils {

    private StringUtils() { }

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public static boolean isBlank(String string) {
        return string == null || string.trim().length() == 0;
    }

    public static boolean isNotBlank(String string) {
        return !isBlank(string);
    }

    public static boolean equals(String lhs, String rhs) {
        if ((lhs == null) != (rhs == null)) {
            return false;
        } else if (lhs == null && rhs == null) {
            return true;
        } else {
            return lhs.equals(rhs);
        }
    }

    public static String join(Collection<String> strings, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(string);
        }
        return builder.toString();
    }

}
