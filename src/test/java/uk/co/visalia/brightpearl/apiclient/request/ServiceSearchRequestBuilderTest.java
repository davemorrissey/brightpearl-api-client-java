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

import org.junit.Test;
import uk.co.visalia.brightpearl.apiclient.ServiceName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ServiceSearchRequestBuilderTest {

    /**
     * Ensure that each ruid is only used once.
     */
    @Test
    public void testUniqueRandomRuids() {

        ServiceSearchRequestBuilder<Void> builder = ServiceSearchRequestBuilder.newSearchRequest(ServiceName.CONTACT, "/contact", Void.class);

        String ruid1 = builder.build().getRuid();
        String ruid2 = builder.build().getRuid();

        assertThat(ruid1, is(notNullValue()));
        assertThat(ruid2, is(notNullValue()));
        assertThat(ruid1, is(not(ruid2)));


    }

    /**
     * Ensure that each ruid is only used once.
     */
    @Test
    public void testUniqueManualRuids() {

        ServiceSearchRequestBuilder<Void> builder = ServiceSearchRequestBuilder.newSearchRequest(ServiceName.CONTACT, "/contact", Void.class).withRuid("a");

        String ruid1 = builder.build().getRuid();
        String ruid2 = builder.build().getRuid();

        assertThat(ruid1, is("a"));
        assertThat(ruid2, is(notNullValue()));
        assertThat(ruid1, is(not(ruid2)));

    }

}
