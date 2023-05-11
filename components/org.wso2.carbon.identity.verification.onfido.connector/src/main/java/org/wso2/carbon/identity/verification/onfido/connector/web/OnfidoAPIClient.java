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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoException;

import java.io.IOException;
import java.util.Map;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;

/**
 * This class contains the implementation of OnfidoAPIClient.
 */
public class OnfidoAPIClient {

    public static JsonObject createApplicantResponse(Map<String, String> idVConfigPropertyMap,
                                                     JSONObject idvClaimsWithValues)
            throws OnfidoException, IOException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String uri = idVConfigPropertyMap.get(BASE_URL) + "/applicants";
        HttpResponse response = OnfidoWebUtils.httpPost(apiToken, uri, idvClaimsWithValues.toString());
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            return getJsonObject(response);
        }
        return null;
    }

    public static JsonObject createSDKToken(Map<String, String> idVConfigPropertyMap, JSONObject sdkTokenRequestBody)
            throws OnfidoException, IOException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String uri = idVConfigPropertyMap.get(BASE_URL) + "/sdk_token";

        HttpResponse response = OnfidoWebUtils.httpPost(apiToken, uri, sdkTokenRequestBody.toString());
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return getJsonObject(response);
        }
        return null;
    }

    private static JsonObject getJsonObject(HttpResponse response) throws IOException {

        Gson gson = new GsonBuilder().create();
        HttpEntity entity = response.getEntity();
        String jsonResponse = EntityUtils.toString(entity);
        return gson.fromJson(jsonResponse, JsonObject.class);
    }

    public static JsonObject verificationCheck(Map<String, String> idVConfigPropertyMap)
            throws OnfidoException, IOException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String uri = idVConfigPropertyMap.get(BASE_URL) + "/checks";

        HttpResponse response = OnfidoWebUtils.
                httpPost(apiToken, uri,
                        "{\n" +
                                "  \"applicant_id\": \"fddd5f0f-4950-4371-9270-ed12296eacbb\",\n" +
                                "  \"report_names\": [\"document\", \"facial_similarity_photo\"]\n" +
                                "}");
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {

            return getJsonObject(response);

        }
        return null;
    }
}
