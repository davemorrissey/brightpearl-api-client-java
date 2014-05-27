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

package uk.co.visalia.brightpearl.apiclient;

import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.github.restdriver.clientdriver.RestClientDriver;
import uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient.ApiSession;
import uk.co.visalia.brightpearl.apiclient.account.Account;
import uk.co.visalia.brightpearl.apiclient.account.Datacenter;
import uk.co.visalia.brightpearl.apiclient.account.UserCredentials;
import uk.co.visalia.brightpearl.apiclient.auth.PrivateAppAuthorisation;
import uk.co.visalia.brightpearl.apiclient.auth.PrivateAppIdentity;
import uk.co.visalia.brightpearl.apiclient.http.ClientFactory;
import uk.co.visalia.brightpearl.apiclient.http.httpclient4.HttpClient4ClientFactoryBuilder;

import static uk.co.visalia.brightpearl.apiclient.BrightpearlLegacyApiSessionFactory.newApiSessionFactory;

public class ClientDriverTestSupport {

    public static int CLIENT_DRIVER_PORT = 8091;
    public static Datacenter DATACENTER = new Datacenter("CLIENT_DRIVER", "http://localhost:" + CLIENT_DRIVER_PORT);
    public static String JSON_CONTENT_TYPE = "application/json";

    public static final String APP_REFERENCE = "visalia_unittest";
    public static final String ACCOUNT_TOKEN = "abcd1234";

    public static final Account ACCOUNT = new Account(DATACENTER, "visalia");
    public static final UserCredentials CREDENTIALS = new UserCredentials("user@visalia.co.uk", "sesame");
    public static final PrivateAppIdentity APP_IDENTITY = PrivateAppIdentity.create(ACCOUNT, APP_REFERENCE);
    public static final PrivateAppAuthorisation APP_AUTHORISATION = PrivateAppAuthorisation.system(APP_IDENTITY, ACCOUNT_TOKEN);

    public BrightpearlApiClient basicBrightpearlApiClient() {
        ClientFactory clientFactory = HttpClient4ClientFactoryBuilder.httpClient4ClientFactory().withConnectionTimeoutMs(500).withSocketTimeoutMs(500).build();
        return BrightpearlApiClientFactory.brightpearlApiClient().withClientFactory(clientFactory).build();
    }

    public ApiSession basicApiSession() {
        return basicBrightpearlApiClient().createSession(APP_AUTHORISATION);
    }

    public BrightpearlLegacyApiSession basicLegacyApiSession() {
        ClientFactory clientFactory = HttpClient4ClientFactoryBuilder.httpClient4ClientFactory().withConnectionTimeoutMs(500).withSocketTimeoutMs(500).build();
        BrightpearlApiClient client = BrightpearlApiClientFactory.brightpearlApiClient().withClientFactory(clientFactory).build();

        return newApiSessionFactory().withBrightpearlApiClient(client).withAccount(ACCOUNT).withUserCredentials(CREDENTIALS).newApiSession();
    }

    public BrightpearlLegacyApiSession basicAuthenticatedLegacyApiSession(ClientDriverRule driver) {

        driver.addExpectation(
                RestClientDriver.onRequestTo("/visalia/authorise")
                        .withMethod(Method.POST),
                RestClientDriver.giveResponse("{response:\"4dc2d23f-2354-54dc-2d23-f2392a4dc2d2\"}", JSON_CONTENT_TYPE)
                        .withStatus(200)
        ).times(1);

        BrightpearlLegacyApiSession session = basicLegacyApiSession();
        session.authenticate();
        return session;

    }

}
