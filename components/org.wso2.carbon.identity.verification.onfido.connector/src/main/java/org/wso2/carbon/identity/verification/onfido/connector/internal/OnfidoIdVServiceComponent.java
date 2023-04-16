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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifier;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifierFactory;
import org.wso2.carbon.identity.verification.onfido.connector.OnfidoIdentityVerifier;
import org.wso2.carbon.identity.verification.onfido.connector.OnfidoIdentityVerifierFactory;

/**
 * Service holder class for Identity Verifier.
 */
@Component(
        name = "onfido.identity.verifier",
        immediate = true)
public class OnfidoIdVServiceComponent {

    private static final Log log = LogFactory.getLog(OnfidoIdVServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            IdentityVerifierFactory onfidoIdentityVerifierFactory
                    = new OnfidoIdentityVerifierFactory();
            ctxt.getBundleContext().registerService(IdentityVerifierFactory.class.getName(),
                    onfidoIdentityVerifierFactory, null);

            IdentityVerifier onfidoIdentityVerifier = new OnfidoIdentityVerifier();
            ctxt.getBundleContext().registerService(IdentityVerifier.class.getName(),
                    onfidoIdentityVerifier, null);
            if (log.isDebugEnabled()) {
                log.debug("");
            }
        } catch (Throwable e) {
            log.fatal("", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("");
        }
    }

    @Reference(
            name = "IdVClaimManager",
            service = org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdVClaimManager")
    protected void setIdentityVerificationManager(IdentityVerificationManager identityVerificationManager) {

        OnfidoIDVDataHolder.getInstance().setIdentityVerificationManager(identityVerificationManager);
    }

    protected void unsetIdentityVerificationManager(IdentityVerificationManager identityVerificationManager) {

        OnfidoIDVDataHolder.getInstance().setIdentityVerificationManager(null);
    }

    @Reference(
            name = "IdVProviderManager",
            service = org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdVProviderManager")
    protected void setIdVProviderManager(IdVProviderManager idVProviderManager) {

        OnfidoIDVDataHolder.getInstance().setIdVProviderManager(idVProviderManager);
    }

    protected void unsetIdVProviderManager(IdVProviderManager idVProviderManager) {

        OnfidoIDVDataHolder.getInstance().setIdVProviderManager(null);
    }

}
