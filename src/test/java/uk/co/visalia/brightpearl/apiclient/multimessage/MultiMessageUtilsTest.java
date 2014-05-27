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

package uk.co.visalia.brightpearl.apiclient.multimessage;

import org.junit.Test;
import uk.co.visalia.brightpearl.apiclient.ServiceName;
import uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequest;
import uk.co.visalia.brightpearl.apiclient.request.ServiceWriteRequestBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MultiMessageUtilsTest {

    @Test
    public void _10() {

        List<MultiRequest> requests = MultiMessageUtils.split(createMultiRequest(10));
        assertThat(requests.size(), is(1));

    }

    @Test
    public void _11() {

        List<MultiRequest> requests = MultiMessageUtils.split(createMultiRequest(11));
        assertThat(requests.size(), is(2));

        assertThat(requests.get(0).getRequests().size(), is(9));
        assertThat(requests.get(1).getRequests().size(), is(2));

    }

    @Test
    public void _17() {

        List<MultiRequest> requests = MultiMessageUtils.split(createMultiRequest(17));
        assertThat(requests.size(), is(2));

        assertThat(requests.get(0).getRequests().size(), is(10));
        assertThat(requests.get(1).getRequests().size(), is(7));

    }

    @Test
    public void _18() {

        List<MultiRequest> requests = MultiMessageUtils.split(createMultiRequest(18));
        assertThat(requests.size(), is(2));

        assertThat(requests.get(0).getRequests().size(), is(10));
        assertThat(requests.get(1).getRequests().size(), is(8));

    }

    @Test
    public void _19() {

        List<MultiRequest> requests = MultiMessageUtils.split(createMultiRequest(19));
        assertThat(requests.size(), is(2));

        assertThat(requests.get(0).getRequests().size(), is(10));
        assertThat(requests.get(1).getRequests().size(), is(9));

    }

    @Test
    public void _20() {

        List<MultiRequest> requests = MultiMessageUtils.split(createMultiRequest(20));
        assertThat(requests.size(), is(2));

        assertThat(requests.get(0).getRequests().size(), is(10));
        assertThat(requests.get(1).getRequests().size(), is(10));

    }

    @Test
    public void _21() {

        List<MultiRequest> requests = MultiMessageUtils.split(createMultiRequest(21));
        assertThat(requests.size(), is(3));

        assertThat(requests.get(0).getRequests().size(), is(10));
        assertThat(mergeIds(requests.get(0)), is(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));
        assertThat(requests.get(1).getRequests().size(), is(9));
        assertThat(mergeIds(requests.get(1)), is(Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "18")));
        assertThat(requests.get(2).getRequests().size(), is(2));
        assertThat(mergeIds(requests.get(2)), is(Arrays.asList("19", "20")));


    }

    @Test
    public void _22() {

        List<MultiRequest> requests = MultiMessageUtils.split(createMultiRequest(22));
        assertThat(requests.size(), is(3));

        assertThat(requests.get(0).getRequests().size(), is(10));
        assertThat(mergeIds(requests.get(0)), is(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));
        assertThat(requests.get(1).getRequests().size(), is(10));
        assertThat(mergeIds(requests.get(1)), is(Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "18", "19")));
        assertThat(requests.get(2).getRequests().size(), is(2));
        assertThat(mergeIds(requests.get(2)), is(Arrays.asList("20", "21")));

    }

    private MultiRequest createMultiRequest(int count) {
        MultiRequestBuilder builder = MultiRequestBuilder.newMultiRequest();
        for (int i = 0; i < count; i++) {
            builder.withAddedRequest(ServiceWriteRequestBuilder.newPostRequest(ServiceName.CONTACT, "/contact", null, Void.class).withRuid(Integer.toString(i)));
        }
        return builder.build();
    }

    private List<String> mergeIds(MultiRequest multiRequest) {
        List<String> ids = new ArrayList<String>();
        for (ServiceWriteRequest request : multiRequest.getRequests()) {
            ids.add(request.getRuid());
        }
        return ids;
    }

}
