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
package org.wso2.carbon.identity.verification.onfido.connector;

import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.extension.identity.verification.mgt.AbstractIdentityVerifier;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifier;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationClientException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVProperty;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoException;
import org.wso2.carbon.identity.verification.onfido.connector.internal.OnfidoIDVDataHolder;
import org.wso2.carbon.identity.verification.onfido.connector.web.OnfidoAPIClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICANT_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.CHECK_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.COMPLETED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.INITIATED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.IN_PROGRESS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.SDK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.SOURCE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;

/**
 * This class contains the implementation of OnfidoIdentityVerifier.
 */
public class OnfidoIdentityVerifier extends AbstractIdentityVerifier implements IdentityVerifier {

    @Override
    public IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException {

        IdVProvider idVProvider = getIdVProvider(identityVerifierData, tenantId);
        Map<String, String> idVProperties = getIdVPropertyMap(identityVerifierData);
        Map<String, String> idVClaimsWithValues =
                getIdVPClaimWithValueMap(userId, idVProvider, identityVerifierData, tenantId);
        Map<String, String> idVProviderConfigProperties = getIdVConfigPropertyMap(idVProvider);
        JSONObject responseObject = new JSONObject();
        List<IdVClaim> idVClaims;

        try {
            if (idVProperties.containsKey(STATUS)) {
                switch (idVProperties.get(STATUS)) {
                    case INITIATED:
                        JsonObject onFidoJsonObject = null;
                        String applicantId = null;
                        for (IdVClaim idVClaim : identityVerifierData.getIdVClaims()) {
                            IdVClaim verificationClaim = OnfidoIDVDataHolder.getInstance().
                                    getIdentityVerificationManager().getIdVClaim(userId, idVClaim.getClaimUri(),
                                            idVProvider.getIdVProviderUuid(), tenantId);
                            if (verificationClaim.getMetadata().get(APPLICANT_ID) != null) {
                                idVClaimsWithValues.remove(idVClaim.getClaimUri());
                            }
                        }
                        if (idVClaimsWithValues.isEmpty()) {
                            applicantId = idVProperties.get(APPLICANT_ID);
                        } else {
                            JSONObject applicantRequestBody = getApplicantRequestBody(idVClaimsWithValues);
                            onFidoJsonObject = OnfidoAPIClient.
                                    createApplicantResponse(idVProviderConfigProperties, applicantRequestBody);
                            if (onFidoJsonObject != null) {
                                applicantId = onFidoJsonObject.get(ID).getAsString();
                            }
                        }

                        responseObject.put(STATUS, INITIATED);
                        responseObject.put(SOURCE, ONFIDO);
                        responseObject.put(APPLICANT_ID, applicantId);

                        JSONObject sdkTokenRequestBody = new JSONObject();
                        sdkTokenRequestBody.put(APPLICANT_ID, applicantId);
                        JsonObject sdkTokenJsonObject =
                                OnfidoAPIClient.createSDKToken(idVProviderConfigProperties, sdkTokenRequestBody);

                        if (sdkTokenJsonObject != null) {
                            responseObject.put(SDK_TOKEN, sdkTokenJsonObject.get(TOKEN).getAsString());
                        } else {
                            throw new IdentityVerificationException("Error while retrieving the sdk token.",
                                    "", null);
                        }
                        break;
                    case COMPLETED:
                        JsonObject checkJsonObject =
                                OnfidoAPIClient.verificationCheck(idVProviderConfigProperties);
                        if (checkJsonObject != null) {
                            responseObject.put(STATUS, IN_PROGRESS);
                            responseObject.put(SOURCE, ONFIDO);
                            responseObject.put(APPLICANT_ID, checkJsonObject.get(APPLICANT_ID).toString());
                            responseObject.put(CHECK_ID, checkJsonObject.get(ID).toString());
                        } else {
                            throw new IdentityVerificationException("Error while retrieving the sdk token.",
                                    "", null);
                        }
                        break;
                }
                idVClaims = getIdVClaims(userId, idVProvider, responseObject, idVClaimsWithValues);
                updateIdVClaims(userId, idVClaims, tenantId);
                identityVerifierData.setIdVClaims(idVClaims);
            } else {
                throw new IdentityVerificationClientException("Status is not defined.", "", null);
            }
        } catch (IOException e) {
            throw new IdentityVerificationException("Error while creating the json object.", "", e);
        } catch (JSONException e) {
            throw new IdentityVerificationException("Error while creating the json object.", "", e);
        } catch (OnfidoException e) {
            throw new IdentityVerificationException("Error while creating the json object.", "", e);
        }
        return identityVerifierData;
    }

    private List<IdVClaim> getIdVClaims(String userId, IdVProvider idVProvider, JSONObject responseObject,
                                        Map<String, String> idVClaimsWithValues) {

        Map<String, String> verificationClaims = getClaimMappings(idVProvider);
        List<IdVClaim> idVClaims = new ArrayList<>();
        for (Map.Entry<String, String> claimMapping : verificationClaims.entrySet()) {
            IdVClaim idVClaim = new IdVClaim();
            idVClaim.setUserId(userId);
            idVClaim.setClaimUri(claimMapping.getKey());
            idVClaim.setClaimValue(idVClaimsWithValues.get(claimMapping.getValue()));
//            idVClaim.setMetadata(responseObject);
            idVClaim.setIdVPId(idVProvider.getIdVProviderUuid());
            idVClaim.setIsVerified(false);
            idVClaims.add(idVClaim);
        }
        return idVClaims;
    }

    private Map<String, String> getIdVPropertyMap(IdentityVerifierData identityVerifierData) {

        List<IdVProperty> identityVerificationProperties = identityVerifierData.getIdVProperties();
        Map<String, String> idVPropertyMap = new HashMap<>();
        for (IdVProperty idVProperty : identityVerificationProperties) {
            idVPropertyMap.put(idVProperty.getName(), idVProperty.getValue());
        }
        return idVPropertyMap;
    }

    private JSONObject getApplicantRequestBody(Map<String, String> idVClaimsWithValues) {

        JSONObject idVClaimRequestBody = new JSONObject();
        for (Map.Entry<String, String> idVClaim : idVClaimsWithValues.entrySet()) {
            idVClaimRequestBody.put(idVClaim.getKey(), idVClaim.getValue());
        }
        return idVClaimRequestBody;
    }
}
