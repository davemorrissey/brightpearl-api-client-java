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

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient.ApiSession;
import uk.co.visalia.brightpearl.apiclient.account.Account;
import uk.co.visalia.brightpearl.apiclient.account.Datacenter;
import uk.co.visalia.brightpearl.apiclient.account.UserCredentials;
import uk.co.visalia.brightpearl.apiclient.auth.*;
import uk.co.visalia.brightpearl.apiclient.config.ExpiredAuthTokenStrategy;
import uk.co.visalia.brightpearl.apiclient.http.ClientFactory;
import uk.co.visalia.brightpearl.apiclient.http.httpclient4.HttpClient4ClientFactoryBuilder;
import uk.co.visalia.brightpearl.apiclient.ratelimit.ConstantWaitRateLimiter;
import uk.co.visalia.brightpearl.apiclient.request.*;
import uk.co.visalia.brightpearl.apiclient.search.SearchResults;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Simplified demonstration of functionality. This is not best practice!
 */
public class CodeSamples {

    public void makeSampleRequests() {

        // Create client instance
        BrightpearlApiClient client = BrightpearlApiClientFactory.brightpearlApiClient().build();

        // Swap this method call for the authentication type you want
        AppAuthorisation authorisation = publicSystemApp();

        // Make a GET request without creating a session. Note generic type inference isn't possible when deserializing generic types.
        ServiceReadRequestBuilder<List<Contact>> contactGetRequest = ServiceReadRequestBuilder.newGetRequest(
                ServiceName.CONTACT,
                "/contact/200-210",
                new TypeToken<List<Contact>>() { }.getType());

        List<Contact> contacts = client.get(authorisation, contactGetRequest);

        // Now create a session that wraps the authorisation so it doesn't need to be passed to every call.
        ApiSession session = client.createSession(authorisation);

        // Make the same GET request.
        contacts = session.get(contactGetRequest);

        // Make a POST request.

        Contact contact = new Contact();
        contact.firstName = "Monty";
        contact.lastName = "Python";

        Integer contactId = session.execute(ServiceWriteRequestBuilder.newPostRequest(
                ServiceName.CONTACT,
                "/contact",
                contact,
                Integer.class));

        // Make a SEARCH request.

        ServiceSearchRequest<ContactSearch> request = ServiceSearchRequestBuilder.newSearchRequest(ServiceName.CONTACT, "/contact-search", ContactSearch.class)
                .withAddedFilter("firstName", "Monty")
                .withAddedSort("lastName", SortDirection.DESC)
                .withPage(50, 1)
                .build();

        SearchResults<ContactSearch> contactResults = session.search(request);

    }

    /**
     * An example of custom client and session configuration.
     */
    public void customClientConfiguration() {

        ClientFactory clientFactory = HttpClient4ClientFactoryBuilder
                .httpClient4ClientFactory()
                .withConnectionManagerTimeoutMs(10000)
                .withConnectionTimeoutMs(15000)
                .withSocketTimeoutMs(5000)
                .withAllowRedirects(false)
                .withMaxConnections(60)
                .withMaxConnectionsPerRoute(20)
                .build();

        BrightpearlApiClient client = BrightpearlApiClientFactory
                .brightpearlApiClient()
                .withClientFactory(clientFactory)
                .withRateLimiter(new ConstantWaitRateLimiter(100, 1, TimeUnit.MINUTES))
                .withGson(new GsonBuilder().create())
                .build();

        // LEGACY AUTH ONLY
        BrightpearlLegacyApiSession session = BrightpearlLegacyApiSessionFactory
                .newApiSessionFactory()
                .withBrightpearlApiClient(client)
                .withExpiredAuthTokenStrategy(ExpiredAuthTokenStrategy.FAIL)
                .withAccount(new Account(Datacenter.EU1, "visalia"))
                .withUserCredentials(new UserCredentials("sarah@visalia.co.uk", "sesame"))
                .newApiSession();

    }

    // Demo contact class, representing response from the Contact GET API and input to Contact PUT.

    public static class Contact {
        private Integer contactId;
        @SerializedName("isPrimaryContact")
        private Boolean primaryContact;
        private String salutation;
        private String firstName;
        private String lastName;
    }

    // Demo contact search class, representing response from the Contact SEARCH API

    public class ContactSearch implements Serializable {
        private Integer contactId;
        private String primaryEmail;
        private String secondaryEmail;
        private String tertiaryEmail;
        private String firstName;
        private String lastName;
        @SerializedName("isSupplier")
        private Boolean supplier;
    }


    private AppAuthorisation publicSystemApp() {

        // Initial setup

        String developerRef = "";
        String developerSecret = ""; // Optional
        String appRef = "";

        PublicAppIdentity appIdentity = PublicAppIdentity.create(developerRef, developerSecret, appRef);

        // Define customer account
        Datacenter datacenter = null;
        String accountCode = "";
        String unsignedAccountToken = "";
        Account account = new Account(datacenter, accountCode);

        // Create system app authorisation

        return PublicAppAuthorisation.system(appIdentity, account, unsignedAccountToken);

    }

    private AppAuthorisation publicStaffApp(BrightpearlApiClient client) {

        // Initial setup

        String developerRef = "";
        String appRef = "";

        PublicAppIdentity appIdentity = PublicAppIdentity.create(developerRef, appRef);

        // Define customer account

        Datacenter datacenter = null;
        String accountCode = "";
        Account account = new Account(datacenter, accountCode);

        // Define staff member credentials and request staff token

        UserCredentials credentials = new UserCredentials("", "");
        String staffToken = client.fetchStaffToken(
                appIdentity,
                account,
                credentials);

        // Create authorisation using this token

        return PublicAppAuthorisation.staff(
                appIdentity,
                account,
                staffToken);

    }

    public static AppAuthorisation privateSystemApp() {

        // Initial setup

        Datacenter datacenter = null;
        String accountCode = "";
        String appRef = "";

        Account account = new Account(datacenter, accountCode);
        PrivateAppIdentity appIdentity = PrivateAppIdentity.create(account, appRef);

        // Create system app authorisation

        String accountToken = "";

        return PrivateAppAuthorisation.system(appIdentity, accountToken);

    }

    public static AppAuthorisation privateStaffApp(BrightpearlApiClient client) {

        // Initial setup

        Datacenter datacenter = null;
        String accountCode = "";
        String appRef = "";

        Account account = new Account(datacenter, accountCode);
        PrivateAppIdentity appIdentity = PrivateAppIdentity.create(account, appRef);

        // Define staff member credentials and request staff token

        UserCredentials credentials = new UserCredentials("", "");
        String staffToken = client.fetchStaffToken(
                appIdentity,
                credentials);

        // Create authorisation using this token

        return PrivateAppAuthorisation.staff(
                appIdentity,
                staffToken);

    }

}