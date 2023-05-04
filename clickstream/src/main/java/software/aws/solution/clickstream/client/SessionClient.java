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

import androidx.annotation.NonNull;

import software.aws.solution.clickstream.client.util.PreferencesUtil;

/**
 * Client for managing start and pause session.
 */
public class SessionClient {

    /**
     * The context object wraps all the essential information from the app
     * that are required.
     */
    private final ClickstreamContext clickstreamContext;

    /**
     * session object.
     */
    private Session session;

    /**
     * constructor for session client.
     *
     * @param clickstreamContext The {@link ClickstreamContext}.
     * @throws IllegalArgumentException When the clickstreamContext.getAnalyticsClient is null.
     */
    public SessionClient(@NonNull final ClickstreamContext clickstreamContext) {
        if (clickstreamContext.getAnalyticsClient() == null) {
            throw new IllegalArgumentException("A valid AnalyticsClient must be provided!");
        }
        this.clickstreamContext = clickstreamContext;
    }

    /**
     * When the app starts for the first time. Or the app was launched to the foreground
     * and the time between the last exit exceeded `session_time_out` period,
     * then the session start event will be recorded.
     *
     * @return is new session.
     */
    public synchronized boolean initialSession() {
        session = Session.getInstance(clickstreamContext);
        this.clickstreamContext.getAnalyticsClient().setSession(session);
        if (session.isNewSession()) {
            final AnalyticsEvent event =
                this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.SESSION_START);
            this.clickstreamContext.getAnalyticsClient().recordEvent(event);
        }
        return session.isNewSession();
    }

    /**
     * store a session when the application goes to the background.
     */
    public void storeSession() {
        session.pause();
        PreferencesUtil.saveSession(clickstreamContext.getSystem().getPreferences(), session);
    }

}

