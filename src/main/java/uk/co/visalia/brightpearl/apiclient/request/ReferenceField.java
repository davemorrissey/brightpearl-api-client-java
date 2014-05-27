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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * An annotation for search result domain object fields where the field is populated from a reference data map included
 * in the search result. The value of the annotation is the name of the reference data map, and the field in the domain
 * object that provides keys to the map must be annotated with @ReferenceKey for this annotation to work.
 * </p><p>
 * For example, the Goods Out Note search reference data looks like this: { warehouseNames: { 2: "Bristol", 3: "Boston" } }
 * so the warehouseId field in the domain object used to represent search results would be annotated with @ReferenceKey("warehouseNames")
 * indicating that the value of the warehouseId column provides a key to the warehouseNames map (in this case, 2 or 3).
 * </p><p>
 * The field of the domain object that should receive the value from the reference data map should be annotated with
 * (in this example) @ReferenceField("warehouseNames").
 * </p>
 * @see <a href="http://www.brightpearl.com/developer/latest/tutorial/working-with-resource-search.html">http://www.brightpearl.com/developer/latest/tutorial/working-with-resource-search.html</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ReferenceField {

    String value();

}
