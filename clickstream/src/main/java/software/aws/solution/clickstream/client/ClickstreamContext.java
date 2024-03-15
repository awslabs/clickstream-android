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

package software.aws.solution.clickstream.client;

import android.content.Context;

import software.aws.solution.clickstream.ClickstreamConfiguration;
import software.aws.solution.clickstream.client.system.AndroidSystem;
import software.aws.solution.clickstream.client.uniqueid.SharedPrefsDeviceIdService;

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
     * The instance of SessionClient.
     */
    private SessionClient sessionClient;
    /**
     * The system of Android.
     */
    private final AndroidSystem system;
    /**
     * The device unique ID.
     */
    private final String deviceId;

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
        this.system = new AndroidSystem(applicationContext);
        this.deviceId = new SharedPrefsDeviceIdService().getDeviceId(this);
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
     * Get the SessionClient.
     *
     * @return The instance of SessionClient.
     */
    public SessionClient getSessionClient() {
        return sessionClient;
    }

    /**
     * Set the SessionClient.
     *
     * @param sessionClient The instance of SessionClient.
     */
    public void setSessionClient(SessionClient sessionClient) {
        this.sessionClient = sessionClient;
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

    /**
     * Get the system of Android.
     *
     * @return The system of Android.
     */
    public AndroidSystem getSystem() {
        return system;
    }

    /**
     * Get the unique ID.
     *
     * @return The unique ID.
     */
    public String getDeviceId() {
        return deviceId;
    }

}

