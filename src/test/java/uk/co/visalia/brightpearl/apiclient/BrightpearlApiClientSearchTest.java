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
import com.google.gson.annotations.SerializedName;
import org.junit.Rule;
import org.junit.Test;
import uk.co.visalia.brightpearl.apiclient.BrightpearlApiClient.ApiSession;
import uk.co.visalia.brightpearl.apiclient.auth.AppAuthorisation;
import uk.co.visalia.brightpearl.apiclient.exception.BrightpearlClientException;
import uk.co.visalia.brightpearl.apiclient.request.ReferenceField;
import uk.co.visalia.brightpearl.apiclient.request.ReferenceKey;
import uk.co.visalia.brightpearl.apiclient.request.ServiceSearchRequestBuilder;
import uk.co.visalia.brightpearl.apiclient.search.SearchResults;

import java.util.Arrays;
import java.util.List;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.github.restdriver.serverdriver.file.FileHelper.fromFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class BrightpearlApiClientSearchTest extends ClientDriverTestSupport {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule(CLIENT_DRIVER_PORT);

    @Test @SuppressWarnings("unchecked")
    public void testSimpleSearch() {
        ApiSession session = basicApiSession();
        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/product-search")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/product/product_search.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        SearchResults<SimpleProduct> results = session.search(ServiceSearchRequestBuilder.newSearchRequest(ServiceName.PRODUCT, "/product-search", SimpleProduct.class));
        assertThat(results.getMetaData().getResultsAvailable(), is(1));
        assertThat(results.getMetaData().getResultsReturned(), is(1));
        assertThat(results.getMetaData().getLastResult(), is(1));
        assertThat(results.getMetaData().getFirstResult(), is(1));
        assertThat(results.getMetaData().getColumns().size(), is(12));
        assertThat(results.getMetaData().getColumns().get(0).getName(), is("productId"));
        assertThat(results.getMetaData().getColumns().get(0).getSortable(), is(true));
        assertThat(results.getMetaData().getColumns().get(0).getFilterable(), is(true));
        assertThat(results.getMetaData().getColumns().get(0).getReportDataType(), is("IDSET"));
        assertThat(results.getMetaData().getColumns().get(0).getRequired(), is(false));
        assertThat(results.getMetaData().getColumns().get(10).getReferenceData(), is(Arrays.asList("productCategoryMembership")));

        SimpleProduct result1 = results.getResults().get(0);
        assertThat(result1.getProductId(), is(1009));
        assertThat(result1.getProductName(), is("PPL Book 2: Human Performance"));
        assertThat(result1.getSku(), is("PPL2"));

        assertThat((List<Double>) results.getReference().get("productCategoryMembership").get("1009"), is(Arrays.asList(302.0d)));

    }

    @Test
    public void testValidReferenceData() {
        ApiSession session = basicApiSession();
        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/product-search")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/product/product_search.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        SearchResults<ReferenceDataProduct> results = session.search(ServiceSearchRequestBuilder.newSearchRequest(ServiceName.PRODUCT, "/product-search", ReferenceDataProduct.class));

        ReferenceDataProduct result1 = results.getResults().get(0);
        assertThat(result1.getProductId(), is(1009));
        assertThat(result1.getProductName(), is("PPL Book 2: Human Performance"));
        assertThat(result1.getSku(), is("PPL2"));
        assertThat(result1.getBrightpearlCategoryCode(), is(1009));
        assertThat(result1.getProductCategoryMembership(), is(Arrays.asList(302)));

    }

    @Test
    public void testInvalidReferenceKey() {
        ApiSession session = basicApiSession();
        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/product-search")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/product/product_search.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        SearchResults<ReferenceDataInvalidKeyProduct> results = session.search(ServiceSearchRequestBuilder.newSearchRequest(ServiceName.PRODUCT, "/product-search", ReferenceDataInvalidKeyProduct.class));

        ReferenceDataInvalidKeyProduct result1 = results.getResults().get(0);
        assertThat(result1.getBrightpearlCategoryCode(), is(1009));
        assertThat(result1.getProductCategoryMembership(), is(nullValue()));
    }

    @Test
    public void testInvalidReferenceField() {
        ApiSession session = basicApiSession();
        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/product-search")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/product/product_search.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        SearchResults<ReferenceDataInvalidFieldProduct> results = session.search(ServiceSearchRequestBuilder.newSearchRequest(ServiceName.PRODUCT, "/product-search", ReferenceDataInvalidFieldProduct.class));

        ReferenceDataInvalidFieldProduct result1 = results.getResults().get(0);
        assertThat(result1.getBrightpearlCategoryCode(), is(1009));
        assertThat(result1.getProductCategoryMembership(), is(nullValue()));
    }

    @Test(expected=BrightpearlClientException.class)
    public void testInvalidReferenceType() {
        ApiSession session = basicApiSession();
        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/product-search")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/product/product_search.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        SearchResults<ReferenceDataInvalidTypeProduct> results = session.search(ServiceSearchRequestBuilder.newSearchRequest(ServiceName.PRODUCT, "/product-search", ReferenceDataInvalidTypeProduct.class));

        ReferenceDataInvalidTypeProduct result1 = results.getResults().get(0);
        assertThat(result1.getBrightpearlCategoryCode(), is(1009));
        assertThat(result1.getProductCategoryMembership(), is(nullValue()));
    }

    @Test
    public void testMissingReferenceMap() {
        ApiSession session = basicApiSession();
        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/product-search")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/product/product_search_missing_reference_map.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        SearchResults<ReferenceDataProduct> results = session.search(ServiceSearchRequestBuilder.newSearchRequest(ServiceName.PRODUCT, "/product-search", ReferenceDataProduct.class));

        ReferenceDataProduct result1 = results.getResults().get(0);
        assertThat(result1.getBrightpearlCategoryCode(), is(1009));
        assertThat(result1.getProductCategoryMembership(), is(nullValue()));
    }

    @Test
    public void testMissingReferenceValue() {
        ApiSession session = basicApiSession();
        driver.addExpectation(
                onRequestTo("/public-api/visalia/product-service/product-search")
                        .withMethod(Method.GET)
                        .withHeader(AppAuthorisation.APP_HEADER, APP_REFERENCE)
                        .withHeader(AppAuthorisation.ACCOUNT_TOKEN_HEADER, ACCOUNT_TOKEN),
                giveResponse(fromFile("json/product/product_search_missing_reference_value.json"), JSON_CONTENT_TYPE)
                        .withStatus(200)
        );
        SearchResults<ReferenceDataProduct> results = session.search(ServiceSearchRequestBuilder.newSearchRequest(ServiceName.PRODUCT, "/product-search", ReferenceDataProduct.class));

        ReferenceDataProduct result1 = results.getResults().get(0);
        assertThat(result1.getBrightpearlCategoryCode(), is(1009));
        assertThat(result1.getProductCategoryMembership(), is(nullValue()));
    }

    private static class SimpleProduct {
        private Integer productId;
        private String productName;
        @SerializedName("SKU")
        private String sku;
        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
    }

    private static class ReferenceDataProduct {
        private Integer productId;
        private String productName;
        @SerializedName("SKU")
        private String sku;
        @ReferenceKey("productCategoryMembership")
        private Integer brightpearlCategoryCode;
        @ReferenceField("productCategoryMembership")
        private List<Integer> productCategoryMembership;
        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public Integer getBrightpearlCategoryCode() { return brightpearlCategoryCode; }
        public List<Integer> getProductCategoryMembership() { return productCategoryMembership; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public void setBrightpearlCategoryCode(Integer brightpearlCategoryCode) { this.brightpearlCategoryCode = brightpearlCategoryCode; }
        public void setProductCategoryMembership(List<Integer> productCategoryMembership) { this.productCategoryMembership = productCategoryMembership; }
    }

    private static class ReferenceDataInvalidKeyProduct {
        @ReferenceKey("missing")
        private Integer brightpearlCategoryCode;
        @ReferenceField("productCategoryMembership")
        private List<Integer> productCategoryMembership;
        public Integer getBrightpearlCategoryCode() { return brightpearlCategoryCode; }
        public List<Integer> getProductCategoryMembership() { return productCategoryMembership; }
        public void setBrightpearlCategoryCode(Integer brightpearlCategoryCode) { this.brightpearlCategoryCode = brightpearlCategoryCode; }
        public void setProductCategoryMembership(List<Integer> productCategoryMembership) { this.productCategoryMembership = productCategoryMembership; }
    }

    private static class ReferenceDataInvalidFieldProduct {
        @ReferenceKey("productCategoryMembership")
        private Integer brightpearlCategoryCode;
        @ReferenceField("missing")
        private List<Integer> productCategoryMembership;
        public Integer getBrightpearlCategoryCode() { return brightpearlCategoryCode; }
        public List<Integer> getProductCategoryMembership() { return productCategoryMembership; }
        public void setBrightpearlCategoryCode(Integer brightpearlCategoryCode) { this.brightpearlCategoryCode = brightpearlCategoryCode; }
        public void setProductCategoryMembership(List<Integer> productCategoryMembership) { this.productCategoryMembership = productCategoryMembership; }
    }

    private static class ReferenceDataInvalidTypeProduct {
        @ReferenceKey("productCategoryMembership")
        private Integer brightpearlCategoryCode;
        @ReferenceField("productCategoryMembership")
        private Boolean productCategoryMembership;
        public Integer getBrightpearlCategoryCode() { return brightpearlCategoryCode; }
        public Boolean getProductCategoryMembership() { return productCategoryMembership; }
        public void setBrightpearlCategoryCode(Integer brightpearlCategoryCode) { this.brightpearlCategoryCode = brightpearlCategoryCode; }
        public void setProductCategoryMembership(Boolean productCategoryMembership) { this.productCategoryMembership = productCategoryMembership; }
    }

}
