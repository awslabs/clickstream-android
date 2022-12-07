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

import androidx.annotation.NonNull;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;

/**
 * Client for managing starting and stopping sessions which records session
 * events automatically as the session is stopped or started.
 * <p>
 * It is recommended to start the session when the application comes to the foreground
 * and stop the session when it goes to the background.
 */
public class SessionClient {
    /**
     * The eventType recorded for session start events.
     */
    public static final String SESSION_START_EVENT_TYPE = "cs_session_start";

    /**
     * The eventType recorded for session stop events.
     */
    public static final String SESSION_STOP_EVENT_TYPE = "cs_session_stop";

    /**
     * Logger instance for SessionClient.
     */
    private static final Log LOG = LogFactory.getLog(SessionClient.class);

    /**
     * The context object wraps all the essential information from the app
     * that are required.
     */
    private final ClickstreamContext clickstreamContext;

    /**
     * This session object tracks whether or not it has been paused by checking the
     * status of it's stop time. A session's stop time is only set when the session
     * has been paused, and is set to -1 if it is currently running. Can be
     * serialized and restored for persistence. This refers to the current session
     * object.
     */
    private Session session;

    /**
     * CONSTRUCTOR.
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
     * Start a session which records a SESSION_START_EVENT_TYPE event and
     * saves that sessionId to the AnalyticsClient to be used for recording future
     * events. This triggers an update of the endpointProfile. It is recommended to
     * start the session when the application comes to the foreground.
     */
    public synchronized void startSession() {
        executeStop();
        executeStart();
    }

    /**
     * Stops a session which records a SESSION_STOP_EVENT_TYPE
     * event and flushes the events in local storage for submission.
     * It is recommended to stop the session when the application
     * goes to the background.
     */
    public synchronized void stopSession() {
        executeStop();
    }

    /**
     * Overridden toString method for testing.
     *
     * @return diagnostic string.
     */
    @NonNull
    @Override
    public String toString() {
        return "[SessionClient]\n" + "- session: " +
            ((this.session == null) ? "<null>" : this.session.getSessionID()) +
            ((this.session != null && this.session.isPaused()) ? ": paused" : "");
    }

    /**
     * Start a new Session. Set the Session ID and start time and record
     * an event of type SESSION_START_EVENT_TYPE.
     */
    protected void executeStart() {
        session = Session.newInstance(clickstreamContext);
        this.clickstreamContext.getAnalyticsClient().setSession(session);
        final AnalyticsEvent event = this.clickstreamContext.getAnalyticsClient().createEvent(SESSION_START_EVENT_TYPE);
        this.clickstreamContext.getAnalyticsClient().recordEvent(event);
    }

    /**
     * Stop the current Session. First, pause the session if it's not paused
     * already. Record the stop time and record an event of type
     * SESSION_STOP_EVENT_TYPE. Additionally, stopping a session
     * clears the campaign attributes.
     */
    protected void executeStop() {
        // No session to stop
        if (session == null) {
            LOG.info("Session Stop Failed: No session exists.");
            return;
        }

        // pause the session if it's not already
        session.pause();

        final AnalyticsEvent event = this.clickstreamContext.getAnalyticsClient().createEvent(SESSION_STOP_EVENT_TYPE);
        this.clickstreamContext.getAnalyticsClient().recordEvent(event);

        // Kill Session Object
        session = null;
    }
}

