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
 * Contains details of the columns available from a search resource, and the default sorting applied. This is the response
 * from a search meta data request.
 *
 * <a href="http://brightpearl.com/developer/latest/tutorial/working-with-resource-search.html">http://brightpearl.com/developer/latest/tutorial/working-with-resource-search.html</a>
 */
public class SearchMetaData implements Serializable {

    private List<SearchColumn> columns;
    private List<SearchSort> sorting;

    /**
     * Details of all the columns available from a search resource.
     */
    public List<SearchColumn> getColumns() {
        return columns;
    }

    /**
     * Describes the default sorting applied.
     */
    public List<SearchSort> getSorting() {
        return sorting;
    }

}