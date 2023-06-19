# Onfido Identity Verification Connector

The Onfido Identity Verification Connector is a software component that enables integration with Onfido's identity verification services. 
It provides a convenient way to verify the identity of individuals using Onfido's powerful identity verification platform.

This repository contains the necessary code and resources to set up and use the Onfido Identity Verification Connector 
in your application or system.

## Design

![Design Diagram](docs/design.png "Design Diagram")

## Prerequisites

Before using the Onfido Identity Verification Connector, make sure you have the following prerequisites in place:

- An active Onfido account: You need to sign up for an account with Onfido to obtain the required API credentials. Please [contact](https://onfido.com/signup/) the Onfido team and they will be happy to help.
- This version of the connector is tested with WSO2 Identity Server version 6.2.0. Make sure to download and set up
  the correct version of the [Identity Server](https://wso2.com/identity-and-access-management) on your environment.
- API credentials: Retrieve the API credentials (API tokens, Base URLs etc.) from your Onfido account dashboard.

## Installation

To use the Onfido Identity Verification Connector, follow these steps:

1. Download the connector from [WSO2 Connector Store](https://store.wso2.com/store/assets/isconnector/list).
2. Copy the ```org.wso2.carbon.identity.verification.onfido.connector-x.x.x.jar``` file to
   ```<IS-HOME>/repository/components/dropins``` folder.
3. Add the following configurations to the ```<IS-HOME>/repository/conf/deployment.toml``` file.
    ```$xslt
    [[event_handler]]
    name = "evidentEventHandler"
    subscriptions =["POST_ADD_USER", "PRE_AUTHENTICATION"]
    ```
4. Restart the server.

## Configuration

Before using the connector, you need to configure it with your Onfido API credentials. Follow these steps to set up the configuration:

1. Open the configuration file (e.g., `config.json`) in the project directory.
2. Update the file with your Onfido API credentials, such as the API key and other necessary tokens.
3. Save the changes to the configuration file.

## Usage

The Onfido Identity Verification Connector provides a set of APIs and functions to interact with Onfido's identity verification services. Here's an example of how to use the connector:

```[insert code snippet or example usage here]```

Refer to the documentation and code samples provided in this repository for detailed instructions on using the connector's features.

