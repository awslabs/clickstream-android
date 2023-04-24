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

package software.aws.solution.clickstream.client;

import android.content.Context;

import okhttp3.Dns;

/**
 * Clickstream Configuration.
 */
public class ClickstreamConfiguration {

    private Context context;
    private String appId;
    private String endpoint;
    private Dns dns;
    private long sendEventsInterval;
    private long callTimeOut;
    private boolean isCompressEvents;
    private boolean isTrackAppLifecycleEvents;
    private boolean isTrackAppExceptionEvents;
    private boolean isLogEvents;
    private String authCookie;

    /**
     * Create an {@link ClickstreamConfiguration} object with the specified parameters.
     *
     * @param context  the android context object.
     * @param appId    the Clickstream appId.
     * @param endpoint the Clickstream endpoint.
     */
    public ClickstreamConfiguration(final Context context, final String appId, final String endpoint) {
        this.context = context;
        this.appId = appId;
        this.endpoint = endpoint;
    }

    /**
     * The Android Context. Interface to global information about an application environment.
     * This is an abstract class whose implementation is provided by the Android system.
     * It allows access to application-specific resources and classes, as well as up-calls for application-level
     * operations such as launching activities, broadcasting and receiving intents, etc.
     *
     * @return the Android Context object.
     */
    public Context getAppContext() {
        return this.context;
    }

    /**
     * The Clickstream AppId.
     *
     * @return the Clickstream AppId.
     */
    public String getAppId() {
        return this.appId;
    }

    /**
     * The Clickstream AppId.
     *
     * @param appId The Clickstream Application Id.
     * @return the current ClickstreamConfiguration instance.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public ClickstreamConfiguration withAppId(final String appId) {
        this.appId = appId;
        return this;
    }

    /**
     * The Clickstream endpoint configured.
     *
     * @return the endpoint.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * The endpoint for Clickstream.
     *
     * @param endpoint The endpoint.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withEndpoint(final String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * For get the Clickstream Okhttp3 dns.
     *
     * @return the dns.
     */
    public Dns getDns() {
        return this.dns;
    }

    /**
     * The Custom Okhttp3 dns for Clickstream.
     *
     * @param dns The custom dns.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withCustomDns(final Dns dns) {
        this.dns = dns;
        return this;
    }

    /**
     * The interval of events sent at once.
     *
     * @return submit events interval.
     */
    public long getSendEventsInterval() {
        return this.sendEventsInterval;
    }

    /**
     * The interval of events sent at once.
     *
     * @param sendEventsInterval Submit events interval.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withSendEventsInterval(final long sendEventsInterval) {
        this.sendEventsInterval = sendEventsInterval;
        return this;
    }

    /**
     * The time out of entire http call.
     *
     * @return callTimeOut.
     */
    public Long getCallTimeOut() {
        return this.callTimeOut;
    }

    /**
     * The http call time out.
     *
     * @param callTimeOut call time out.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withCallTimeOut(final long callTimeOut) {
        this.callTimeOut = callTimeOut;
        return this;
    }

    /**
     * Is compress events.
     *
     * @return Is compress events.
     */
    public boolean isCompressEvents() {
        return this.isCompressEvents;
    }

    /**
     * Is compress events.
     *
     * @param compressEvents Is compress events.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withCompressEvents(final boolean compressEvents) {
        this.isCompressEvents = compressEvents;
        return this;
    }

    /**
     * Is track app lifecycle events.
     *
     * @return Is track appLifecycle events.
     */
    public boolean isTrackAppLifecycleEvents() {
        return this.isTrackAppLifecycleEvents;
    }

    /**
     * Is track app lifecycle events.
     *
     * @param isTrackAppLifecycleEvents Is track app lifecycle events.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withTrackAppLifecycleEvents(final boolean isTrackAppLifecycleEvents) {
        this.isTrackAppLifecycleEvents = isTrackAppLifecycleEvents;
        return this;
    }

    /**
     * Is track app exception events.
     *
     * @return Is track app exception events.
     */
    public boolean isTrackAppExceptionEvents() {
        return this.isTrackAppExceptionEvents;
    }

    /**
     * Is track app exception events.
     *
     * @param isTrackAppExceptionEvents Is track app exception events.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withTrackAppExceptionEvents(final boolean isTrackAppExceptionEvents) {
        this.isTrackAppExceptionEvents = isTrackAppExceptionEvents;
        return this;
    }

    /**
     * Is log events.
     *
     * @return Is log events json when record event.
     */
    public boolean isLogEvents() {
        return this.isLogEvents;
    }

    /**
     * Is log events json when record event, set true for debug mode.
     *
     * @param isLogEvents Is log events json.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withLogEvents(final boolean isLogEvents) {
        this.isLogEvents = isLogEvents;
        return this;
    }

    /**
     * Get The Clickstream authCookie.
     *
     * @return the authCookie.
     */
    public String getAuthCookie() {
        return this.authCookie;
    }

    /**
     * Set the auth cookie for Clickstream.
     *
     * @param authCookie The authCookie.
     * @return the current ClickstreamConfiguration instance.
     */
    public ClickstreamConfiguration withAuthCookie(final String authCookie) {
        this.authCookie = authCookie;
        return this;
    }
}

