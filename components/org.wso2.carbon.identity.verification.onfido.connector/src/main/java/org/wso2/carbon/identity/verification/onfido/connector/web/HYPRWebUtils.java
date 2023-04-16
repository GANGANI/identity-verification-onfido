/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.verification.onfido.connector.web;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoException;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.fileupload.FileUploadBase.FORM_DATA;
import static org.apache.commons.fileupload.FileUploadBase.MULTIPART_FORM_DATA;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICANT_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICATION_JSON;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.DOCUMENT_UPLOADED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN_HEADER;

/**
 * The HYPRWebUtils class contains all the general helper functions required by the HYPR Authenticator.
 */
public class HYPRWebUtils {

    private HYPRWebUtils() {

    }

    /**
     * Send an HTTP POST request.
     *
     * @param apiToken    API token provided by HYPR.
     * @param requestURL  The URL to which the POST request should be sent.
     * @param requestBody A hashmap that includes the parameters to be sent through the request.
     * @return httpResponse         The response received from the HTTP call.
     * @throws IOException         Exception thrown when an error occurred during extracting the HTTP response content.
     * @throws OnfidoException Exception thrown when an error occurred with the HTTP client connection.
     */
    public static HttpResponse httpPost(String apiToken, String requestURL, String requestBody)
            throws IOException, OnfidoException {

        HttpPost request = new HttpPost(requestURL);
        request.addHeader(HttpHeaders.AUTHORIZATION, TOKEN_HEADER + apiToken);
        request.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        CloseableHttpClient client = HTTPClientManager.getInstance().getHttpClient();
        try (CloseableHttpResponse response = client.execute(request)) {
            return toHttpResponse(response);
        }
    }

    public static HttpResponse httpPost(String apiToken, String requestURL, String filePath,
                                        String applicantId, String scenario) throws IOException {

        HttpPost request = new HttpPost(requestURL);
        request.addHeader(HttpHeaders.AUTHORIZATION, TOKEN_HEADER + apiToken);
        request.setHeader(HttpHeaders.CONTENT_TYPE, MULTIPART_FORM_DATA);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addPart("file", new FileBody(new File(filePath)))
                .addPart(APPLICANT_ID, new StringBody(applicantId, ContentType.TEXT_PLAIN));

        if (DOCUMENT_UPLOADED.equals(scenario)) {
            builder.addPart("type", new StringBody("driving_licence", ContentType.TEXT_PLAIN));
        }
        request.setEntity(builder.build());

        CloseableHttpClient client = HttpClientBuilder.create().build();
        try (CloseableHttpResponse response = client.execute(request)) {
            return toHttpResponse(response);
        }
    }

    private static HttpResponse toHttpResponse(final CloseableHttpResponse response) throws IOException {

        final HttpResponse result = new BasicHttpResponse(response.getStatusLine());
        if (response.getEntity() != null) {
            result.setEntity(new BufferedHttpEntity(response.getEntity()));
        }
        return result;
    }
}
