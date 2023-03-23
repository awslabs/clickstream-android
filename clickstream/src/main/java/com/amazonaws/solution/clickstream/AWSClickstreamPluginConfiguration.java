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

/**
 * Configuration options for Amplify Analytics Clickstream plugin.
 */
public final class AWSClickstreamPluginConfiguration {
    private static final long DEFAULT_SEND_EVENTS_INTERVAL = 10000L;
    private static final long DEFAULT_CALL_TIME_OUT = 15000L;

    // Clickstream configuration options
    private final String appId;
    private final String endpoint;
    private final long sendEventsInterval;
    private final long callTimeOut;
    private final boolean isTrackAppLifecycleEvents;
    private final boolean isTrackAppExceptionEvents;
    private final boolean isCompressEvents;

    private AWSClickstreamPluginConfiguration(Builder builder) {
        this.appId = builder.appId;
        this.isTrackAppLifecycleEvents = builder.isTrackAppLifecycleEvents;
        this.isTrackAppExceptionEvents = builder.isTrackAppExceptionEvents;
        this.callTimeOut = builder.callTimeOut;
        this.sendEventsInterval = builder.sendEventsInterval;
        this.endpoint = builder.endpoint;
        this.isCompressEvents = builder.isCompressEvents;
    }

    /**
     * AppId getter.
     *
     * @return appId
     */
    String getAppId() {
        return appId;
    }

    /**
     * Accessor for auto event flush interval.
     *
     * @return auto event flush interval.
     */
    long getSendEventsInterval() {
        return sendEventsInterval;
    }

    /**
     * Accessor for http call time out.
     *
     * @return callTimeOut.
     */
    long getCallTimeOut() {
        return callTimeOut;
    }

    /**
     * Is auto session tracking enabled.
     *
     * @return Is auto session tracking enabled.
     */
    boolean isTrackAppLifecycleEvents() {
        return isTrackAppLifecycleEvents;
    }

    /**
     * Is auto exception tracking enabled.
     *
     * @return Is auto exception tracking enabled.
     */
    boolean isTrackAppExceptionEvents() {
        return isTrackAppExceptionEvents;
    }

    /**
     * Accessor for endpoint.
     *
     * @return The endpoint.
     */
    String getEndpoint() {
        return endpoint;
    }

    /**
     * Is compress events enabled.
     *
     * @return Is compress events enabled.
     */
    boolean isCompressEvents() {
        return isCompressEvents;
    }

    /**
     * Return a builder that can be used to construct a new instance of
     * {@link AWSClickstreamPluginConfiguration}.
     *
     * @return An {@link Builder} instance
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Used for fluent construction of an immutable {@link AWSClickstreamPluginConfiguration} object.
     */
    static final class Builder {
        private String appId;
        private String endpoint;
        private long sendEventsInterval = DEFAULT_SEND_EVENTS_INTERVAL;
        private final long callTimeOut = DEFAULT_CALL_TIME_OUT;
        private boolean isCompressEvents = true;
        private boolean isTrackAppLifecycleEvents = true;
        private boolean isTrackAppExceptionEvents = false;

        Builder withAppId(final String appId) {
            this.appId = appId;
            return this;
        }

        Builder withEndpoint(final String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        Builder withSendEventsInterval(final long sendEventsInterval) {
            this.sendEventsInterval = sendEventsInterval;
            return this;
        }

        Builder withCompressEvents(final boolean compressEvents) {
            this.isCompressEvents = compressEvents;
            return this;
        }

        Builder withTrackAppLifecycleEvents(final boolean trackAppLifecycleEvents) {
            this.isTrackAppLifecycleEvents = trackAppLifecycleEvents;
            return this;
        }

        Builder withTrackAppExceptionEvents(final boolean trackAppExceptionEvents) {
            this.isTrackAppExceptionEvents = trackAppExceptionEvents;
            return this;
        }

        AWSClickstreamPluginConfiguration build() {
            return new AWSClickstreamPluginConfiguration(this);
        }
    }
}

