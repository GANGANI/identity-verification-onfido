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

import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifier;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifierFactory;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO;

/**
 * This class contains the implementation for the OnfidoIdentityVerifierProvider.
 */
public class OnfidoIdentityVerifierFactory implements IdentityVerifierFactory {

    @Override
    public IdentityVerifier getIdentityVerifier(String identityVerifierName) {

        if (ONFIDO.equals(identityVerifierName)) {
            return new OnfidoIdentityVerifier();
        } else {
            throw new IllegalArgumentException("Identity verifier not found for the name: " + identityVerifierName);
        }
    }

    @Override
    public String getIdentityVerifierName() {

        return ONFIDO;
    }
}
