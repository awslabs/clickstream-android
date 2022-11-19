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

package com.amazonaws.solution.clickstream.client;

import android.content.Context;

import java.io.Serializable;

/**
 * Clickstream Context.
 */
@SuppressWarnings("serial")
public class ClickstreamContext implements Serializable {
    /**
     * The configuration of Clickstream.
     */
    private final ClickstreamConfiguration clickstreamConfiguration;
    /**
     * The info of SDK.
     */
    private final SDKInfo sdkInfo;
    /**
     * The context of application.
     */
    private final Context applicationContext;
    /**
     * The instance of AnalyticsClient.
     */
    private AnalyticsClient analyticsClient;

    /**
     * The default constructor of ClickstreamContext.
     */
    public ClickstreamContext() {
        this.clickstreamConfiguration = null;
        this.sdkInfo = null;
        this.applicationContext = null;
        this.analyticsClient = null;
    }

    /**
     * The constructor with parameters.
     *
     * @param applicationContext       The context of Android.
     * @param sdkInfo                  The SDK info.
     * @param clickstreamConfiguration The configuration of Clickstream.
     */
    public ClickstreamContext(final Context applicationContext,
                              final SDKInfo sdkInfo,
                              final ClickstreamConfiguration clickstreamConfiguration) {
        this.sdkInfo = sdkInfo;
        this.clickstreamConfiguration = clickstreamConfiguration;
        this.applicationContext = applicationContext;
    }

    /**
     * Get the AnalyticsClient.
     *
     * @return The instance of AnalyticsClient.
     */
    public AnalyticsClient getAnalyticsClient() {
        return analyticsClient;
    }

    /**
     * Set the AnalyticsClient.
     *
     * @param analyticsClient The instance of AnalyticsClient.
     */
    public void setAnalyticsClient(AnalyticsClient analyticsClient) {
        this.analyticsClient = analyticsClient;
    }

    /**
     * Get the configuration of Clickstream.
     *
     * @return The configuration of Clickstream.
     */
    public ClickstreamConfiguration getClickstreamConfiguration() {
        return clickstreamConfiguration;
    }

    /**
     * Get the info of SDK.
     *
     * @return The info of SDK.
     */
    public SDKInfo getSDKInfo() {
        return sdkInfo;
    }

    /**
     * Get the context of application.
     *
     * @return The context of application.
     */
    public Context getApplicationContext() {
        return applicationContext;
    }

}

