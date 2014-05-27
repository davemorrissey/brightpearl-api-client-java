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

/**
 * A common interface for builders that create {@link ServiceSearchRequest}s. This serves no other purpose than allowing
 * the builder itself to be passed to {@link uk.co.visalia.brightpearl.apiclient.BrightpearlLegacyApiSession#search(SearchRequestBuilder)},
 * removing the need to call build() and increasing readability.
 * @param <T> Placeholder for the search domain object type to be extracted from the JSON response.
 */
public interface SearchRequestBuilder<T> {

    /**
     * Build an immutable request instance for execution.
     * @return a search request.
     */
    ServiceSearchRequest<T> build();

}
