/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.aws.solution.clickstream;

import android.content.Context;

import software.aws.solution.clickstream.client.ClickstreamConfiguration;
import software.aws.solution.clickstream.client.ClickstreamManager;

/**
 * Factory class to vend out clickstream client.
 */
final class ClickstreamManagerFactory {
    private ClickstreamManagerFactory() {
    }

    static ClickstreamManager create(Context context,
                                     AWSClickstreamPluginConfiguration clickstreamPluginConfiguration) {
        // Construct configuration using information from the configure method
        ClickstreamConfiguration clickstreamConfiguration =
            new ClickstreamConfiguration(context, clickstreamPluginConfiguration.getAppId(),
                clickstreamPluginConfiguration.getEndpoint())
                .withSendEventsInterval(clickstreamPluginConfiguration.getSendEventsInterval())
                .withCallTimeOut(clickstreamPluginConfiguration.getCallTimeOut())
                .withCompressEvents(clickstreamPluginConfiguration.isCompressEvents())
                .withTrackScreenViewEvents(clickstreamPluginConfiguration.isTrackScreenViewEvents())
                .withTrackAppExceptionEvents(clickstreamPluginConfiguration.isTrackAppExceptionEvents())
                .withSessionTimeoutDuration(clickstreamPluginConfiguration.getSessionTimeOut());

        return new ClickstreamManager(clickstreamConfiguration);
    }
}

