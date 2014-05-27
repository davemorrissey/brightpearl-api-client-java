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

import uk.co.visalia.brightpearl.apiclient.account.Account;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A very basic implementation of {@link RateLimiter} that will reduce the
 * throughput of requests by pausing for the minimum period required to ensure the cap is not exceeded. By default this
 * is 300 ms. Each calling thread is blocked until the request may be sent, so this is not suitable for use as a queue.
 * </p><p>
 * If there is more than one server making requests to the same Brightpearl account, this approach will only be effective
 * if this class is configured with a 1/n share of the request cap, which increases latency for all requests, however a
 * distributed tracking approach may be more appropriate.
 * </p><p>
 * This class is intended to prevent hitting the request cap for an account, and its behaviour is not affected by an
 * account unexpectedly being throttled.
 * </p>
 */
public class ConstantWaitRateLimiter implements RateLimiter {

    private final int cap;

    private final int period;

    private final TimeUnit periodUnit;

    private final long minimumPeriod;

    private final ConcurrentHashMap<String, Long> lastRequestMap;

    /**
     * Construct a constant wait rate limiter with the default settings, limiting requests to 200 per minute. This is
     * suitable for a single node system.
     */
    public ConstantWaitRateLimiter() {
        this(200, 1, TimeUnit.MINUTES);
    }

    /**
     * Construct a constant wait limiter with a custom rate limit. Avoid low rates as this will cause high latency
     * during busy periods.
     * @param cap The number of requests allowed in the given period.
     * @param period The period of time.
     * @param periodUnit Unit of the period of time.
     */
    public ConstantWaitRateLimiter(int cap, int period, TimeUnit periodUnit) {
        this.cap = cap;
        this.period = period;
        this.periodUnit = periodUnit;
        this.lastRequestMap = new ConcurrentHashMap<String, Long>();
        this.minimumPeriod = minimumPeriod();
    }

    /**
     * Called before a request is made, this sleeps for the minimum amount of time that must be left to ensure no more
     * than the maximum number of requests are made in the configured period. With default configuration, the maximum
     * wait will be 300ms to give a rate of 200 per minute. If more than 300ms has passed since the previous request,
     * the wait will be zero.
     * @param account The account a request is about to be sent to.
     */
    @Override
    public void rateLimit(Account account) {
        long sleepTime = getSleepTime(account);
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // Continue with request.
            }
        }
    }

    /**
     * This implementation does nothing.
     * @param account The account a response has been received from.
     * @param requestsRemaining The value of the brightpearl-requests-remaining response header.
     * @param nextThrottlePeriod The value of the brightpearl-next-throttle-period header.
     */
    @Override
    public void requestCompleted(Account account, int requestsRemaining, long nextThrottlePeriod) {
    }

    /**
     * This implementation does nothing. Use of this class should prevent the request cap being reached, and it does
     * not modify its constant wait behaviour if the request cap is unexpectedly hit.
     * @param account The account that received a 503 request cap error.
     */
    @Override
    public void requestCapExceeded(Account account) {
    }

    private long minimumPeriod() {
        long periodMs = periodUnit.toMillis(period);
        return periodMs/cap;
    }

    private synchronized long getSleepTime(Account account) {
        long timeNow = System.currentTimeMillis();
        Long lastRequest = lastRequestMap.get(account.getAccountCode());
        if (lastRequest != null && (timeNow - lastRequest < minimumPeriod)) {
            long sleep = minimumPeriod - (timeNow - lastRequest);
            lastRequestMap.put(account.getAccountCode(), timeNow + sleep);
            return sleep;
        } else {
            lastRequestMap.put(account.getAccountCode(), timeNow);
            return 0;
        }
    }

}
