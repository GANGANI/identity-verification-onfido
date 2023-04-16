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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.extension.identity.verification.mgt.AbstractIdentityVerifier;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifier;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVProperty;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoException;
import org.wso2.carbon.identity.verification.onfido.connector.web.OnfidoAPIClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICANT_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.CHECK_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.COMPLETED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.DOCUMENT_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.DOCUMENT_UPLOADED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.INITIATED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.IN_PROGRESS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.PHOTO_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.PHOTO_UPLOADED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.SOURCE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.STATUS;

/**
 * This class contains the implementation of OnfidoIdentityVerifier.
 */
public class OnfidoIdentityVerifier extends AbstractIdentityVerifier implements IdentityVerifier {

    private static final Log log = LogFactory.getLog(OnfidoIdentityVerifier.class);

    @Override
    public IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException {

        Map<String, String> idVPropertyMap = getIdVPropertyMap(identityVerifierData);
        IdVProvider idVProvider = getIdVProvider(identityVerifierData, tenantId);
        JSONObject responseObject = new JSONObject();
        try {
            if (idVPropertyMap.containsKey(STATUS)) {
                Map<String, String> idVConfigPropertyMap = getIdVConfigPropertyMap(idVProvider);
                switch (idVPropertyMap.get(STATUS)) {
                    case INITIATED:
                        Map<String, String> idVClaimsWithValues =
                                getIdVClaimsWithValues(userId, idVProvider, tenantId);
                        JSONObject idVClaimRequestBody = getIdVClaimRequestBody(idVClaimsWithValues);
                        JsonObject onFidoJsonObject =
                                OnfidoAPIClient.createApplicantResponse(idVConfigPropertyMap, idVClaimRequestBody);
                        responseObject.put(STATUS, INITIATED);
                        responseObject.put(SOURCE, ONFIDO);
                        if (onFidoJsonObject != null) {
                            responseObject.put(APPLICANT_ID, onFidoJsonObject.get(ID).toString());
                        }
                        break;
                    case DOCUMENT_UPLOADED:
                        JsonObject uploadDocumentJsonObject =
                                OnfidoAPIClient.uploadDocument(idVConfigPropertyMap, idVPropertyMap);
                        responseObject.put(STATUS, DOCUMENT_UPLOADED);
                        responseObject.put(SOURCE, ONFIDO);
                        if (uploadDocumentJsonObject != null) {
                            responseObject.put(APPLICANT_ID, uploadDocumentJsonObject.get(APPLICANT_ID).toString());
                            responseObject.put(DOCUMENT_ID, uploadDocumentJsonObject.get(ID).toString());
                        }
                        break;
                    case PHOTO_UPLOADED:
                        JsonObject uploadPhotoJsonObject =
                                OnfidoAPIClient.uploadPhoto(idVConfigPropertyMap, idVPropertyMap);
                        responseObject.put(STATUS, DOCUMENT_UPLOADED);
                        responseObject.put(SOURCE, ONFIDO);
                        if (uploadPhotoJsonObject != null) {
                            responseObject.put(APPLICANT_ID, idVPropertyMap.get(APPLICANT_ID));
                            responseObject.put(PHOTO_ID, uploadPhotoJsonObject.get(ID).toString());
                        }
                        break;
                    case COMPLETED:
                        JsonObject checkJsonObject =
                                OnfidoAPIClient.verificationCheck(idVConfigPropertyMap, idVPropertyMap);
                        responseObject.put(STATUS, IN_PROGRESS);
                        responseObject.put(SOURCE, ONFIDO);
                        if (checkJsonObject != null) {
                            responseObject.put(APPLICANT_ID, checkJsonObject.get(APPLICANT_ID).toString());
                            responseObject.put(CHECK_ID, checkJsonObject.get(ID).toString());
                        }
                        break;
                }

                List<IdVClaim> idVClaims = getIdVClaims(idVProvider, responseObject);
                storeIdVClaims(userId, idVClaims, tenantId);
                identityVerifierData.setIdVClaims(idVClaims);
            }
        } catch (IOException e) {
            log.error("Error while creating the applicant response.", e);
        } catch (JSONException e) {
            log.error("Error while creating the json object.", e);
        } catch (OnfidoException e) {
            throw new RuntimeException(e);
        }
        return identityVerifierData;
    }

    private List<IdVClaim> getIdVClaims(IdVProvider idVProvider, JSONObject responseObject) {

        Map<String, String> verificationClaims = getClaimMappings(idVProvider);
        List<IdVClaim> idVClaims = new ArrayList<>();
        for (Map.Entry<String, String> claimMapping : verificationClaims.entrySet()) {
            IdVClaim idVClaim = new IdVClaim();
            idVClaim.setClaimUri(claimMapping.getKey());
            idVClaim.setClaimValue(claimMapping.getValue());
            idVClaim.setMetadata(responseObject);
            idVClaim.setIdVPId(ONFIDO);
            idVClaim.setStatus(false);
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

    private JSONObject getIdVClaimRequestBody(Map<String, String> idVClaimsWithValues) {

        JSONObject idVClaimRequestBody = new JSONObject();
        for (Map.Entry<String, String> idVClaim : idVClaimsWithValues.entrySet()) {
            idVClaimRequestBody.put(idVClaim.getKey(), idVClaim.getValue());
        }
        return idVClaimRequestBody;
    }
}
