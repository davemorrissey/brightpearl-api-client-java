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
import java.util.Map;

/**
 * Contains the full details of a search result.
 *
 * @see <a href="http://brightpearl.com/developer/latest/tutorial/working-with-resource-search.html">http://brightpearl.com/developer/latest/tutorial/working-with-resource-search.html</a>
 * @param <T> Type of class representing each result of the search.
 */
public class SearchResults<T> implements Serializable {

    private SearchResultsMetaData metaData;
    private List<T> results;
    private Map<String, Map<String, Object>> reference;

    public SearchResults(SearchResultsMetaData metaData, List<T> results, Map<String, Map<String, Object>> reference) {
        this.metaData = metaData;
        this.results = results;
        this.reference = reference;
    }

    public SearchResultsMetaData getMetaData() {
        return metaData;
    }

    public List<T> getResults() {
        return results;
    }

    public Map<String, Map<String, Object>> getReference() {
        return reference;
    }

}
