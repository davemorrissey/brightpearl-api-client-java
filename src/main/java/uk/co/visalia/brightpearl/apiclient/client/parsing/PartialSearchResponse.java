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

package uk.co.visalia.brightpearl.apiclient.client.parsing;

import com.google.gson.JsonArray;
import uk.co.visalia.brightpearl.apiclient.search.SearchResultsMetaData;

import java.util.List;

/**
 * For internal use only. Used to represent the response element of a search response at an intermediate stage of search
 * result parsing, once it has been determined that the search has returned a result but before the metadata has been
 * used to build a list of search result objects of the desired type.
 */
public class PartialSearchResponse {

    private SearchResultsMetaData metaData;

    private List<JsonArray> results;

    public SearchResultsMetaData getMetaData() {
        return metaData;
    }

    public List<JsonArray> getResults() {
        return results;
    }
}
