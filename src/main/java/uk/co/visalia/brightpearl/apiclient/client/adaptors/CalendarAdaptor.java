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

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import uk.co.visalia.brightpearl.apiclient.util.StringUtils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.Calendar;

/**
 * A custom GSON serialiser and deserialiser implementation that supports {@link Calendar}s represented as ISO date times.
 */
public class CalendarAdaptor extends TypeAdapter<Calendar> {

    private static DatatypeFactory datatypeFactory;
    private static Exception storedInitException;

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (Exception e) {
            storedInitException = e;
        }
    }

    @Override
    public Calendar read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        String string = jsonReader.nextString();
        if (StringUtils.isNotBlank(string)) {
            if (storedInitException != null) {
                throw new JsonParseException("Could not parse '" + string + "' as an ISO date.", storedInitException);
            }
            try {
                return datatypeFactory.newXMLGregorianCalendar(string).toGregorianCalendar();
            } catch (Exception e) {
                throw new JsonParseException("Could not parse '" + string + "' as an ISO date.", e);
            }
        }
        return null;
    }

    @Override
    public void write(JsonWriter jsonWriter, Calendar calendar) throws IOException {
        if (calendar == null) {
            jsonWriter.nullValue();
            return;
        }
        if (storedInitException != null) {
            throw new JsonParseException("Unexpected error serializing calendar", storedInitException);
        }
        try {
            DatatypeFactory dtf = datatypeFactory;
            XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar();
            xgc.setYear(calendar.get(Calendar.YEAR));
            xgc.setDay(calendar.get(Calendar.DAY_OF_MONTH));
            xgc.setMonth(calendar.get(Calendar.MONTH) + 1);
            xgc.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            xgc.setMinute(calendar.get(Calendar.MINUTE));
            xgc.setSecond(calendar.get(Calendar.SECOND));
            xgc.setMillisecond(calendar.get(Calendar.MILLISECOND));
            int offsetInMinutes = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000);
            xgc.setTimezone(offsetInMinutes);
            jsonWriter.value(xgc.toXMLFormat());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error serializing calendar", e);
        }
    }

}
