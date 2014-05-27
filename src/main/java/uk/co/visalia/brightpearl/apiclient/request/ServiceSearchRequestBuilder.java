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

package uk.co.visalia.brightpearl.apiclient.request;

import uk.co.visalia.brightpearl.apiclient.ServiceName;
import uk.co.visalia.brightpearl.apiclient.http.Method;
import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * <p>
 * A builder used to construct generic {@link ServiceSearchRequest} instances that may be used against any search resource.
 * This provides methods for setting filters, pagination and sorting parameters, and column selection on the request. These
 * are all sent as querystring parameters, so be aware that using the {@link #withAddedParam(String, String)} method can
 * overwrite values set by other methods.
 * </p><p>
 * Resource-specific search request builders are included in the accompanying services package; these provide dedicated
 * methods for each column in that search and enums respresenting the columns.
 * </p>
 * @param <T> A placeholder for the type expected in the response, avoiding the requirement to cast responses. This is not
 *           linked to the {@link Type} set on a request so a {@link ClassCastException} is possible if the type is set
 *           incorrectly.
 */
public class ServiceSearchRequestBuilder<T> extends AbstractServiceRequestBuilder<ServiceSearchRequestBuilder<T>> implements SearchRequestBuilder<T> {

    public static final String COLUMNS_PARAM = "columns";
    public static final String PAGE_SIZE_COLUMN = "pageSize";
    public static final String FIRST_RESULT_COLUMN = "firstResult";
    public static final String SORT_PARAM = "sort";

    private Map<String, String> filters;
    private List<String> columns;
    private Map<String, SortDirection> sorts;
    private Integer pageSize = null;
    private Integer firstResult = null;

    ServiceSearchRequestBuilder() {
        super();
        this.filters = new LinkedHashMap<String, String>();
        this.columns = new ArrayList<String>();
        this.sorts = new LinkedHashMap<String, SortDirection>();
    }

    @Override
    protected ServiceSearchRequestBuilder<T> getThis() {
        return this;
    }

    /**
     * Creates a new search request builder configured with the URL to call and expected response type. Filters, column
     * selection, pagination and sorting parameters can be added if required.
     * @param service Brightpearl service that contains the resource.
     * @param path Path to the search resource, for example '/product-search'.
     * @param responseType Type of search result expected. In many cases this will be a different class to that used for
     *                     resource GET requests; it must have a flat structure with fields corresponding to column names.
     * @param <T> Placeholder for the response type, used to avoid casting of responses.
     * @return A search request builder instance.
     */
    public static <T> ServiceSearchRequestBuilder<T> newSearchRequest(ServiceName service, String path, Class<T> responseType) {
        ServiceSearchRequestBuilder<T> builder = new ServiceSearchRequestBuilder<T>();
        return builder.withMethod(Method.GET)
                .withService(service)
                .withPath(path)
                .withResponseType(responseType);
    }

    /**
     * Creates a new search request builder configured with the URL to call and expected response type. Filters, column
     * selection, pagination and sorting parameters can be added if required.
     * @param service Brightpearl service that contains the resource.
     * @param path Path to the search resource, for example '/product-search'.
     * @param responseType Type of search result expected. In many cases this will be a different class to that used for
     *                     resource GET requests; it must have a flat structure with fields corresponding to column names.
     * @param <T> Placeholder for the response type, used to avoid casting of responses. Responses are parsed into the type defined by responseType, not this type parameter.
     * @return A search request builder instance.
     */
    public static <T> ServiceSearchRequestBuilder<T> newSearchRequest(ServiceName service, String path, Type responseType) {
        ServiceSearchRequestBuilder<T> builder = new ServiceSearchRequestBuilder<T>();
        return builder.withMethod(Method.GET)
                .withService(service)
                .withPath(path)
                .withResponseType(responseType);
    }

    /**
     * Constructs the immutable request instance for execution.
     * @return an immutable search request.
     */
    public ServiceSearchRequest<T> build() {
        String ruid = StringUtils.isNotEmpty(getRuid()) ? getRuid() : UUID.randomUUID().toString();
        resetRuid();
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            withAddedParam(filter.getKey(), filter.getValue());
        }
        if (columns.size() > 0) {
            withAddedParam(COLUMNS_PARAM, StringUtils.join(columns, ","));
        }
        if (pageSize != null) {
            withAddedParam(PAGE_SIZE_COLUMN, Integer.toString(pageSize));
        }
        if (firstResult != null) {
            withAddedParam(FIRST_RESULT_COLUMN, Integer.toString(firstResult));
        }
        if (sorts.size() > 0) {
            List<String> sortsList = new ArrayList<String>();
            for (Map.Entry<String, SortDirection> sort : sorts.entrySet()) {
                sortsList.add(sort.getKey() + "|" + sort.getValue());
            }
            withAddedParam(SORT_PARAM, StringUtils.join(sortsList, ","));
        }
        return new ServiceSearchRequest<T>(ruid, getService(), getMethod(), getPath(), getResponseType(), getParams());
    }

    /**
     * Add an additional column value filter to be applied to the search. If the column name is the same as an existing filter,
     * the existing filter will be replaced.
     * @param column name of column to filter on.
     * @param value value of filter.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withAddedFilter(String column, String value) {
        this.filters.put(column, value);
        return this;
    }

    /**
     * Add additional column value filters to be applied to the search, preserving previous filters set on this builder.
     * If entries in the supplied map have the same key (column name) as an existing filter, the existing filter will be
     * replaced.
     * @param filters map of filters (column name to filter string) to be applied.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withAddedFilters(Map<String, String> filters) {
        this.filters.putAll(filters);
        return this;
    }

    /**
     * Set the column value filters to be applied to the search, replacing any previous filters set on this builder.
     * @param filters Map of filters (column name to filter string) to be applied.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withFilters(Map<String, String> filters) {
        this.filters = new LinkedHashMap<String, String>();
        if (filters != null) {
            this.filters.putAll(filters);
        }
        return this;
    }

    /**
     * Add an additional column sort to be applied to the search. If more than one sort column is specified, the sorting
     * is applied in order as with an SQL query.
     * @param column Name of the column to sort by.
     * @param direction Direction to sort in.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withAddedSort(String column, SortDirection direction) {
        this.sorts.put(column, direction);
        return this;
    }

    /**
     * Set the sort columns and directions, replacing any previously added. Be sure to use a {@link java.util.Map} implementation
     * that preserves insertion order, for example {@link java.util.LinkedHashMap}.
     * @param sorts Map of column name to sort direction.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withSorts(Map<String, SortDirection> sorts) {
        this.sorts = new LinkedHashMap<String, SortDirection>();
        if (sorts != null) {
            this.sorts.putAll(sorts);
        }
        return this;
    }

    /**
     * Set paging parameters in one call. Sets the start position and number of results to request, allowing paging
     * through large result sets.
     * @param pageSize Number of results per page.
     * @param firstResult Position of first result to return.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withPage(Integer pageSize, Integer firstResult) {
        this.pageSize = pageSize;
        this.firstResult = firstResult;
        return this;
    }

    /**
     * Sets the number of results to return per page, allowing paging through large result sets.
     * @param pageSize Number of results per page.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * Sets the first result to return, allowing paging through large result sets.
     * @param firstResult Position of the first result to return.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * Adds a column to be included in search results. Requesting fewer columns may improve response times. When
     * columns are omitted, each individual result is returned with the corresponding fields left null. By default, all
     * available columns are included.
     * @param column Columns to be included in the results.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withAddedColumn(String column) {
        if (StringUtils.isNotBlank(column)) {
            this.columns.add(column);
        }
        return this;
    }

    /**
     * Sets the columns to be included in search results, replacing any previously set. Requesting fewer columns may
     * improve response times. When columns are omitted, each individual result is returned with the corresponding fields
     * left null. By default, all available columns are included.
     * @param columns Columns to be included in the results.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withColumns(String... columns) {
        return withColumns(Arrays.asList(columns));
    }

    /**
     * Sets the columns to be included in search results, replacing any previously set. Requesting fewer columns may
     * improve response times. When columns are omitted, each individual result is returned with the corresponding fields
     * left null. By default, all available columns are included.
     * @param columns Columns to be included in the results.
     * @return builder instance for method chaining.
     */
    public ServiceSearchRequestBuilder<T> withColumns(List<String> columns) {
        this.columns = new ArrayList<String>();
        if (columns != null) {
            for (String column : columns) {
                if (StringUtils.isNotBlank(column)) {
                    this.columns.add(column);
                }
            }
        }
        return this;
    }

}
