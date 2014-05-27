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
 * Details of a column available for retrieving, filtering or sorting in a search API.
 * @see <a href="http://brightpearl.com/developer/latest/tutorial/working-with-resource-search.html">http://brightpearl.com/developer/latest/tutorial/working-with-resource-search.html</a>
 */
public class SearchColumn implements Serializable {

    private String name;
    private Boolean sortable;
    private Boolean filterable;
    private String reportDataType;
    private List<String> referenceData;
    private Boolean required;

    public String getName() {
        return name;
    }

    public Boolean getSortable() {
        return sortable;
    }

    public Boolean getFilterable() {
        return filterable;
    }

    public String getReportDataType() {
        return reportDataType;
    }

    public List<String> getReferenceData() {
        return referenceData;
    }

    public Boolean getRequired() {
        return required;
    }

}
