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

package uk.co.visalia.brightpearl.apiclient.common;

import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IdSetTest {

    @Test
    public void testEmpty() {
        assertThat(new IdSet().toString(), is(""));
    }

    @Test
    public void testIndividualIds() {
        IdSet idSet = IdSet.ids(1, 4, 6, 8, 33);
        assertThat(idSet.toString(), is("1,4,6,8,33"));

        IdSet parsed = IdSet.parse(idSet.toString());
        assertThat(parsed.equals(idSet), is(true));
    }

    @Test
    public void testIndividualIdsOutOfOrder() {
        IdSet idSet = IdSet.ids(33, 8, 4, 6, 1);
        assertThat(idSet.toString(), is("1,4,6,8,33"));

        IdSet parsed = IdSet.parse(idSet.toString());
        assertThat(parsed.equals(idSet), is(true));
    }

    @Test
    public void testAddChain() {
        IdSet idSet = IdSet.ids(8, 1, 6, 22, 7, 23).add(6, 1, 22, 5, 99, 103).add(Arrays.asList(22, 1, 6, 5, 90));
        assertThat(idSet.toString(), is("1,5-8,22-23,90,99,103"));

        IdSet parsed = IdSet.parse(idSet.toString());
        assertThat(parsed.equals(idSet), is(true));
    }

    @Test
    public void testSingleRange() {
        IdSet idSet = IdSet.range(20, 40);
        assertThat(idSet.toString(), is("20-40"));

        IdSet parsed = IdSet.parse(idSet.toString());
        assertThat(parsed.equals(idSet), is(true));
    }

    @Test
    public void testMultipleRanges() {
        IdSet idSet = IdSet.range(20, 40).addRange(10, 20).addRange(100, 150).addRange(99, 98);
        assertThat(idSet.toString(), is("10-40,100-150"));

        IdSet parsed = IdSet.parse(idSet.toString());
        assertThat(parsed.equals(idSet), is(true));
    }

    @Test
    public void testParseDotsAndCommas() {
        IdSet expected = IdSet.range(10, 20).add(5).addRange(50, 60).add(104);
        assertThat(IdSet.parse("10-20.5,50-60.104").equals(expected), is(true));
    }

    @Test
    public void testIteration() {
        IdSet idSet = IdSet.ids(8, 1, 6, 22, 7, 23);
        Integer[] expected = new Integer[] { 1, 6, 7, 8, 22, 23 };
        int pos = 0;
        for (Integer id : idSet) {
            assertThat(id, is(expected[pos++]));
        }
    }

    @Test
    public void testSplit() {
        IdSet idSet = IdSet.range(20, 40).addRange(10, 20).addRange(100, 150).addRange(99, 98);
        Set<IdSet> split = idSet.split(15);
        assertThat(split.size(), is(6));
        int idx = 0;
        for (IdSet splitSet : split) {
            if (idx == 0) {
                assertThat(splitSet.toString(), is("10-24"));
            } else if (idx == 1) {
                assertThat(splitSet.toString(), is("25-39"));
            } else if (idx == 2) {
                assertThat(splitSet.toString(), is("40,100-113"));
            } else if (idx == 3) {
                assertThat(splitSet.toString(), is("114-128"));
            } else if (idx == 4) {
                assertThat(splitSet.toString(), is("129-143"));
            } else if (idx == 5) {
                assertThat(splitSet.toString(), is("144-150"));
            }
            if (idx < 5) {
                assertThat(splitSet.getSize(), is(15));
            }
            idx++;
        }
    }

    @Test
    public void testSplit1() {
        IdSet idSet = IdSet.range(20, 40).addRange(10, 20).addRange(100, 150).addRange(99, 98);
        Set<IdSet> split = idSet.split(1);
        assertThat(split.size(), is(82));
        int idx = 0;
        for (IdSet splitSet : split) {
            if (idx == 0) {
                assertThat(splitSet.toString(), is("10"));
            } else if (idx == 1) {
                assertThat(splitSet.toString(), is("11"));
            } else if (idx == 2) {
                assertThat(splitSet.toString(), is("12"));
            } else if (idx == 3) {
                assertThat(splitSet.toString(), is("13"));
            } else if (idx == 4) {
                assertThat(splitSet.toString(), is("14"));
            } else if (idx == 5) {
                assertThat(splitSet.toString(), is("15"));
            }
            idx++;
        }
    }

}