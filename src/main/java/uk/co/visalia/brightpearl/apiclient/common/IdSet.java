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

import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * Many of Brightpearl's GET APIs, and some searches and destructive resources, allow multiple IDs to be fetched or
 * modified in one request by specifying comma or dot separated ID ranges (e.g. &quot;1-3,6-15,20&quot;). This class
 * represents a set of ID ranges and includes a {@link #toString()} method that generates a valid ID set string for
 * inclusion in URLs.
 * </p><p>
 * Internally, an ordered {@link Set} implementation is used, so duplicates are automatically filtered and the ID string
 * is generated in ascending order, therefore period (.) separation, which indicates unordered IDs, is not used.
 * </p><p>
 * This client does not place any limit on the size of an ID set, however Brightpearl does have limits that are not always
 * documented. Bear in mind that requesting a large number of non-sequential IDs will produce an excessively long URL,
 * and that any large number of IDs may result in the request being rejected by Brightpearl or returning a very large
 * response. Some GET resources have corresponding OPTIONs resources that provide a list of individual requests to make
 * in order to retrieve a large set of entities. Where no OPTIONs resource is available, consider limiting ID sets to
 * fewer than 100 IDs and test whether Brightpearl accepts these requests.
 * </p>
 * @see <a href="http://www.brightpearl.com/developer/latest/concept/id-set.html">http://www.brightpearl.com/developer/latest/concept/id-set.html</a>
 */
public final class IdSet implements Iterable<Integer>, Serializable {

    private final Set<Integer> ids;

    /**
     * Initialise a new empty ID set.
     */
    public IdSet() {
        ids = new TreeSet<Integer>();
    }

    /**
     * Initialise an ID set containing IDs from an array or varargs argument. Additional IDs may be added.
     * @param ids Initial set of IDs to add to the ID set.
     */
    public IdSet(Integer... ids) {
        this();
        add(ids);
    }

    /**
     * Initialise an ID set containing a collection of IDs. Additional IDs may be added.
     * @param ids Initial set of IDs to add to the ID set.
     */
    public IdSet(Collection<Integer> ids) {
        this();
        add(ids);
    }

    /**
     * Initialise an empty ID set. The naming of this method reflects its intended use with methods in the accompanying
     * services package, which supports generation of URLs with wildcards or omitted final path components when an empty
     * ID set is used. For example, the URL /order/[ORDER-ID-SET]/goods-note/goods-out/[GOODS-OUT-NOTE-ID-SET] will become
     * /order/{@literal *}/goods-note/goods-out if any() and all() are passed as arguments to the request builder. Passing
     * null achieves the same result but is less expressive.
     */
    public static IdSet any() {
        return new IdSet();
    }

    /**
     * Initialise an empty ID set. The naming of this method reflects its intended use with methods in the accompanying
     * services package, which supports generation of URLs with wildcards or omitted final path components when an empty
     * ID set is used. For example, the URL /order/[ORDER-ID-SET]/goods-note/goods-out/[GOODS-OUT-NOTE-ID-SET] will become
     * /order/{@literal *}/goods-note/goods-out if any() and all() are passed as arguments to the request builder. Passing
     * null achieves the same result but is less expressive.
     */
    public static IdSet all() {
        return new IdSet();
    }

    /**
     * Initialise an ID set containing IDs from an array or varargs argument. Additional IDs may be added.
     * @param ids Initial set of IDs to add to the ID set.
     * @return A new ID set containing the given IDs.
     */
    public static IdSet ids(Integer... ids) {
        return new IdSet(ids);
    }

    /**
     * Initialise an ID set containing a collection of IDs. Additional IDs may be added.
     * @param ids Initial set of IDs to add to the ID set.
     * @return A new ID set containing the given IDs.
     */
    public static IdSet ids(Collection<Integer> ids) {
        return new IdSet(ids);
    }

    /**
     * Initialise an ID set with a single range of IDs. Additional IDs may be added.
     * @param from first ID (inclusive)
     * @param to last ID (inclusive)
     * @return A new ID set for the given range of IDs.
     */
    public static IdSet range(int from, int to) {
        IdSet idSet = new IdSet();
        return idSet.addRange(from, to);
    }

    /**
     * Parse the string representation of an ID set.
     * @param str A string ID set.
     * @return
     */
    public static IdSet parse(String str) {
        if (StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException("Cannot parse IdSet: String is empty");
        }
        IdSet set = new IdSet();
        String[] parts = str.split("[,.]");
        for (String part : parts) {
            if (part.contains("-")) {
                String start = part.substring(0, part.indexOf("-"));
                String end = part.substring(part.indexOf("-") + 1);
                try {
                    set.addRange(Integer.parseInt(start), Integer.parseInt(end));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse IdSet: " + part + " is not a valid range");
                }
            } else {
                try {
                    set.add(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse IdSet: " + part + " is not a valid ID");
                }
            }
        }
        return set;
    }

    /**
     * Add IDs from an array or varargs argument. Duplicates are filtered and ranges automatically calculated.
     * @param ids a set of IDs to add to the ID set.
     * @return this instance for method chaining.
     */
    public IdSet add(Integer... ids) {
        add(Arrays.asList(ids));
        return this;
    }

    /**
     * Add IDs from a collection. Duplicates are filtered and ranges automatically calculated.
     * @param ids a collection of IDs to add to the ID set.
     * @return this instance for method chaining.
     */
    public IdSet add(Collection<Integer> ids) {
        for (Integer id : ids) {
            if (id != null) {
                this.ids.add(id);
            }
        }
        return this;
    }

    /**
     * Add a range of IDs. Duplicate IDs are filtered.
     * @param from first ID of the range (inclusive)
     * @param to last ID of the range (inclusive)
     * @return this instance for method chaining.
     */
    public IdSet addRange(int from, int to) {
        if (to >= from) {
            for (int id = from; id <= to; id++) {
                this.ids.add(id);
            }
        }
        return this;
    }

    /**
     * Returns the number of IDs in the set.
     * @return number of IDs in the set.
     */
    public int getSize() {
        return ids.size();
    }

    /**
     * Returns true if this set contains no IDs.
     * @return true if the set is empty.
     */
    public boolean isEmpty() {
        return ids.isEmpty();
    }

    /**
     * Returns the IDs as a {@link Set}.
     * @return the set of IDs.
     */
    public Set<Integer> getIds() {
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Returns an iterator over the set of IDs. Uses {@link java.util.Set#iterator()} internally. Iteration will be in
     * ascending order.
     * @return an iterator for the IDs.
     */
    @Override
    public Iterator<Integer> iterator() {
        return ids.iterator();
    }

    /**
     * Splits this IdSet into batches of the given size. All Brightpearl APIs, with the possible exception of some search
     * APIs, have limits on the size of the IdSet they will accept. Some have accompanying OPTIONS APIs to help you
     * fetch large sets. For others, use this method to split the IdSet and send a request for each batch.
     * <p>
     * If this IdSet is empty, the returned set will be empty.
     *
     * @param batchSize Size of batch.
     * @return A {@link Set} of IDSets.
     */
    public Set<IdSet> split(int batchSize) {
        if (batchSize < 0) {
            throw new IllegalArgumentException("Batch size must be a positive integer");
        }
        Set<IdSet> result = new LinkedHashSet<IdSet>();
        IdSet current = new IdSet();
        for (Integer id : ids) {
            current.add(id);
            if (current.getSize() >= batchSize) {
                result.add(current);
                current = new IdSet();
            }
        }
        if (current.getSize() > 0) {
            result.add(current);
        }
        return result;
    }

    /**
     * Creates a string representation of the ID set for inclusion in URLs.
     * @return an ID set string.
     */
    public String toString() {

        StringBuilder builder = new StringBuilder();
        int from = Integer.MIN_VALUE;
        int to = Integer.MIN_VALUE;
        for (Integer id : ids) {
            if (id != null) {
                if (from == Integer.MIN_VALUE || to == Integer.MIN_VALUE) {
                    from = id;
                    to = id;
                } else if (id == to + 1) {
                    to = id;
                } else {
                    toStringAppend(builder, from, to);
                    from = id;
                    to = id;
                }
            }
        }
        if (from != Integer.MIN_VALUE && to != Integer.MIN_VALUE) {
            toStringAppend(builder, from, to);
        }
        return builder.toString();

    }

    private void toStringAppend(StringBuilder builder, int from, int to) {
        if (to == from) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(Integer.toString(to));
        } else {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(Integer.toString(from));
            builder.append('-');
            builder.append(Integer.toString(to));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdSet idSet = (IdSet) o;
        if (ids != null ? !ids.equals(idSet.ids) : idSet.ids != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return ids != null ? ids.hashCode() : 0;
    }
}
