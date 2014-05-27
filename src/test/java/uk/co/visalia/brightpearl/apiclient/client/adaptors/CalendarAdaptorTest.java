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
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CalendarAdaptorTest {

    @Test
    public void test() {

        Gson gson = new GsonBuilder().registerTypeAdapter(Calendar.class, new CalendarAdaptor()).registerTypeAdapter(GregorianCalendar.class, new CalendarAdaptor()).create();

        String inputJson = "{\"calendar\":\"2011-06-29T11:12:24.000+01:00\"}";

        Wrapper inputWrapper = gson.fromJson(inputJson, Wrapper.class);
        Calendar inputCalendar = inputWrapper.calendar;

        assertThat(inputCalendar.get(Calendar.YEAR), is(2011));
        assertThat(inputCalendar.get(Calendar.MONTH), is(Calendar.JUNE));
        assertThat(inputCalendar.get(Calendar.DAY_OF_MONTH), is(29));
        assertThat(inputCalendar.get(Calendar.HOUR_OF_DAY), is(11));
        assertThat(inputCalendar.get(Calendar.MINUTE), is(12));
        assertThat(inputCalendar.get(Calendar.SECOND), is(24));
        assertThat(inputCalendar.get(Calendar.MILLISECOND), is(0));
        assertThat(inputCalendar.get(Calendar.ZONE_OFFSET), is(3600000));

        String outputJson = gson.toJson(inputWrapper);

        assertThat(outputJson, is(inputJson));

    }

    public static class Wrapper {

        private Calendar calendar;

    }

}
