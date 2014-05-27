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

package uk.co.visalia.brightpearl.apiclient.config;

/**
 * Defines the strategy {@link uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient} should use when a request cap error is
 * received from the Brightpearl API, in terms on the number of retries and wait between them. With a non-zero retryAttempts
 * value, all requests will be blocked while the first to fail is repeated until it succeeds. All waiting requests are then
 * sent.
 * @see <a href="http://www.brightpearl.com/developer/latest/concept/request-throttling.html">http://www.brightpearl.com/developer/latest/concept/request-throttling.html</a>
 */
public class RequestCapStrategy {

    /**
     * Default retry configuration: Make one attempt every 15 seconds and fail if 5 successive retries are blocked by the request cap.
     */
    public static RequestCapStrategy RETRY = new RequestCapStrategy(5, 15000);

    /**
     * Fail strategy: Do not attempt to repeat requests until they succeed; throw an exception for every failure.
     */
    public static RequestCapStrategy FAIL = new RequestCapStrategy(0, 0);

    private final int retryAttempts;
    private final long retryWait;

    private RequestCapStrategy(int retryAttempts, long retryWait) {
        this.retryAttempts = retryAttempts;
        this.retryWait = retryWait;
    }

    /**
     * Constructs a retry strategy with a custom number of retry attempts and wait period in ms. The product of the two
     * numbers should be at least 60,000 (60 seconds) to guarantee that retry will eventually succeed, subject to
     * Brightpearl's API following the stated behaviour.
     * @param retryAttempts
     * @param retryWait
     * @return
     */
    public static RequestCapStrategy retry(int retryAttempts, long retryWait) {
        return new RequestCapStrategy(retryAttempts, retryWait);
    }

    /**
     * For a retry strategy, returns the number of attempts that will be made to retry a request before aborting. For
     * a fail strategy this is zero.
     * @return the number of retry attempts allowed.
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * For a retry strategy, returns the wait period between attempts. For a fail strategy this is zero and has no meaning.
     * @return the wait between retries.
     */
    public long getRetryWait() {
        return retryWait;
    }

}
