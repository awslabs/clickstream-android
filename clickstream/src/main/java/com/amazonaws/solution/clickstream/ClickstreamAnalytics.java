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

import android.app.Application;
import androidx.annotation.NonNull;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.analytics.AnalyticsEvent;
import com.amplifyframework.core.Amplify;

/**
 * This is the top-level customer-facing interface to The ClickstreamAnalytics.
 */
public final class ClickstreamAnalytics {

    private ClickstreamAnalytics() {
        throw new UnsupportedOperationException("No instances allowed.");
    }

    /**
     * Init ClickstreamAnalytics Plugin.
     * @param context ApplicationContext
     * @throws AmplifyException Exception of init.
     */
    public static void init(@NonNull Application context) throws AmplifyException {
        Amplify.addPlugin(new AWSClickstreamPlugin(context));
        Amplify.configure(context);
    }

    /**
     * Use this method to record Event.
     *
     * @param event AnalyticsEvent to record
     */
    public static void recordEvent(@NonNull final AnalyticsEvent event) {
        Amplify.Analytics.recordEvent(event);
    }

    /**
     * Use this method to record Event.
     *
     * @param eventName the event name
     */
    public static void recordEvent(@NonNull final String eventName) {
        Amplify.Analytics.recordEvent(eventName);
    }
}
