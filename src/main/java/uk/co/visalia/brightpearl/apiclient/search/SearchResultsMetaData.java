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

package uk.co.visalia.brightpearl.apiclient.search;

import java.io.Serializable;
import java.util.List;

/**
 * Contains the metadata of a response from a search resource, describing which search columns were returned, the sort
 * order applied, and the number of available results.
 * <a href="http://brightpearl.com/developer/latest/tutorial/working-with-resource-search.html">http://brightpearl.com/developer/latest/tutorial/working-with-resource-search.html</a>
 */
public class SearchResultsMetaData implements Serializable {

    private List<SearchColumn> columns;
    private List<SearchSort> sorting;
    private Integer resultsAvailable;
    private Integer resultsReturned;
    private Integer firstResult;
    private Integer lastResult;

    /**
     * The columns included in the response.
     */
    public List<SearchColumn> getColumns() {
        return columns;
    }

    /**
     * The sorting applied to the results.
     */
    public List<SearchSort> getSorting() {
        return sorting;
    }

    /**
     * Returns the total number of results that matched the filters supplied.
     */
    public Integer getResultsAvailable() {
        return resultsAvailable;
    }

    /**
     * Returns the number of results included on the current page.
     */
    public Integer getResultsReturned() {
        return resultsReturned;
    }

    /**
     * Returns the position of the first result on the current page.
     */
    public Integer getFirstResult() {
        return firstResult;
    }

    /**
     * Returns the position of the last result on the current page.
     */
    public Integer getLastResult() {
        return lastResult;
    }

}