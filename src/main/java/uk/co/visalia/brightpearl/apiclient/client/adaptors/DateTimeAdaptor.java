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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * A custom GSON serialiser and deserialiser implementation that supports {@link DateTime}s represented as ISO date times.
 * Joda is not required by this library - this adaptor will only be registered with the default GSON parser if Joda is
 * available in the classpath.
 */
public class DateTimeAdaptor extends TypeAdapter<DateTime> {

    @Override
    public DateTime read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        String string = jsonReader.nextString();
        return ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(string);
    }

    @Override
    public void write(JsonWriter jsonWriter, DateTime dateTime) throws IOException {
        if (dateTime == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(ISODateTimeFormat.dateTime().print(dateTime));
    }

}
