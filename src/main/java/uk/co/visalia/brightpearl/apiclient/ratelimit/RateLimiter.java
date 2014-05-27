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

/**
 * An interface for rate limiters, which are called before any request is made to the Brightpearl API and notified when
 * a request cap error is received. Implementations can abort requests by throwing exceptions, or sleep before returning
 * to reduce the rate of requests.
 *
 * @see <a href="http://www.brightpearl.com/developer/latest/concept/request-throttling.html">http://www.brightpearl.com/developer/latest/concept/request-throttling.html</a>
 */
public interface RateLimiter {

    /**
     * Called immediately before a request is made to the Brightpearl API for the given account. Implementations may
     * sleep before returning to reduce throughput, or throw an exception to abort the request.
     * @param account The account a request will be sent to.
     */
    void rateLimit(Account account);

    /**
     * Called after every request is made to the Brightpearl API, with values from the brightpearl-requests-remaining
     * and brightpearl-next-throttle-period headers. These may be used to adjust the behaviour of the {@link #rateLimit(Account)}
     * method, but as other requests may have been made on other threads while this request was executing, the values
     * may be stale, and events may be received out of sequence.
     * @param account The account a response has been received from.
     * @param requestsRemaining The value of the brightpearl-requests-remaining response header.
     * @param nextThrottlePeriod The value of the brightpearl-next-throttle-period header.
     */
    void requestCompleted(Account account, int requestsRemaining, long nextThrottlePeriod);

    /**
     * Called when a request to an account has been rejected due to request throttling. This event may be used to adjust
     * the behaviour of the the {@link #rateLimit(Account)} method. Other concurrent requests that were already started
     * at the time this event is received are likely to also fail.
     * @param account An account for which a request cap error has been received.
     */
    void requestCapExceeded(Account account);

}