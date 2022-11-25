/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.solution.clickstream;

import android.content.Context;

import com.amplifyframework.util.UserAgent;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.solution.clickstream.client.ClickstreamConfiguration;
import com.amazonaws.solution.clickstream.client.ClickstreamManager;

/**
 * Factory class to vend out clickstream client.
 */
final class ClickstreamManagerFactory {
    private ClickstreamManagerFactory() {
    }

    static ClickstreamManager create(Context context,
                                     AWSClickstreamPluginConfiguration clickstreamPluginConfiguration) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setUserAgent(UserAgent.string());

        // Construct configuration using information from the configure method
        ClickstreamConfiguration clickstreamConfiguration =
            new ClickstreamConfiguration(context, clickstreamPluginConfiguration.getEndpoint())
                .withClientConfiguration(clientConfiguration)
                .withSendEventsSize(clickstreamPluginConfiguration.getSendEventsSize())
                .withSendEventsInterval(clickstreamPluginConfiguration.getSendEventsInterval())
                .withCompressEvents(clickstreamPluginConfiguration.isCompressEvents())
                .withTrackAppLifecycleEvents(clickstreamPluginConfiguration.isTrackAppLifecycleEvents());

        return new ClickstreamManager(clickstreamConfiguration);
    }
}
