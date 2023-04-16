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

package org.wso2.carbon.identity.verification.onfido.connector.internal;

import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;

/**
 * Service holder class for Onfido IDV.
 */
public class OnfidoIDVDataHolder {

    public static OnfidoIDVDataHolder instance = new OnfidoIDVDataHolder();
    private static IdVProviderManager idVProviderManager;
    private static IdentityVerificationManager identityVerificationManager;

    private OnfidoIDVDataHolder() {

    }

    public static OnfidoIDVDataHolder getInstance() {

        return instance;
    }

    public IdVProviderManager getIdVProviderManager() {

        if (idVProviderManager == null) {
            throw new RuntimeException("IdVProviderManager was not set during the " +
                    "OnfidoIdVServiceComponent startup");
        }
        return idVProviderManager;
    }

    public void setIdVProviderManager(IdVProviderManager idVProviderManager) {

        OnfidoIDVDataHolder.idVProviderManager = idVProviderManager;
    }

    public IdentityVerificationManager getIdentityVerificationManager() {

        if (identityVerificationManager == null) {
            throw new RuntimeException("IdentityVerificationManager was not set during the " +
                    "OnfidoIdVServiceComponent startup");
        }
        return identityVerificationManager;
    }
    
    public void setIdentityVerificationManager(IdentityVerificationManager identityVerificationManager) {

        OnfidoIDVDataHolder.identityVerificationManager = identityVerificationManager;
    }
}
