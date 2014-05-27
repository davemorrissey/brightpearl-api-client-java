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

package uk.co.visalia.brightpearl.apiclient.ratelimit;

import org.junit.Before;
import org.junit.Test;
import uk.co.visalia.brightpearl.apiclient.account.Account;
import uk.co.visalia.brightpearl.apiclient.account.Datacenter;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConstantWaitRateLimiterTest {

    private ConstantWaitRateLimiter limiter;
    private Account account1;
    private Account account2;

    @Before
    public void setup() {

        limiter = new ConstantWaitRateLimiter();
        account1 = new Account(Datacenter.EU1, "visalia1");
        account2 = new Account(Datacenter.EU1, "visalia2");

    }

    @Test
    public void test10Requests() {

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            limiter.rateLimit(account1);
        }
        long end = System.currentTimeMillis();

        assertThat((end - start), is(greaterThan(2600L)));
        assertThat((end - start), is(lessThan(3300L)));

    }

    @Test
    public void testGap() throws InterruptedException {

        limiter.rateLimit(account1);

        Thread.sleep(310L);

        long start = System.currentTimeMillis();
        limiter.rateLimit(account1);
        long end = System.currentTimeMillis();
        assertThat((end - start), is(lessThan(20L)));

    }

    @Test
    public void testThreaded10Requests() throws Exception {

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 2; j++) {
                        limiter.rateLimit(account1);
                    }
                }
            });
            threads.add(thread);
        }

        long start = System.currentTimeMillis();

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        long end = System.currentTimeMillis();

        assertThat((end - start), is(greaterThan(2600L)));
        assertThat((end - start), is(lessThan(3300L)));

    }

    @Test
    public void testThreaded50RequestsTwoAccounts() throws Exception {

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 10; i++) {
            final Account account = i < 5 ? account1 : account2;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 2; j++) {
                        limiter.rateLimit(account);
                    }
                }
            });
            threads.add(thread);
        }

        long start = System.currentTimeMillis();

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        long end = System.currentTimeMillis();

        assertThat((end - start), is(greaterThan(2600L)));
        assertThat((end - start), is(lessThan(3300L)));

    }

}
