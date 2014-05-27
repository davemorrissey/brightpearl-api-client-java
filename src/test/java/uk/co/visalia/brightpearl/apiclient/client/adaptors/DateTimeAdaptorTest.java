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

package uk.co.visalia.brightpearl.apiclient.client.adaptors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DateTimeAdaptorTest {

    @Test
    public void test() {

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeAdaptor()).registerTypeAdapter(DateTime.class, new DateTimeAdaptor()).create();

        String inputJson = "{\"dateTime\":\"2011-06-29T11:12:24.000+02:00\"}";

        Wrapper inputWrapper = gson.fromJson(inputJson, Wrapper.class);
        DateTime inputDateTime = inputWrapper.dateTime;

        System.out.println(inputDateTime);

        assertThat(inputDateTime.getYear(), is(2011));
        assertThat(inputDateTime.getMonthOfYear(), is(6));
        assertThat(inputDateTime.getDayOfMonth(), is(29));
        assertThat(inputDateTime.getHourOfDay(), is(11));
        assertThat(inputDateTime.getMinuteOfHour(), is(12));
        assertThat(inputDateTime.getSecondOfMinute(), is(24));
        assertThat(inputDateTime.getMillisOfSecond(), is(0));
        assertThat(inputDateTime.getZone().getOffset(inputDateTime), is(7200000));

        String outputJson = gson.toJson(inputWrapper);

        assertThat(outputJson, is(inputJson));

    }

    public static class Wrapper {

        private DateTime dateTime;

    }

}
