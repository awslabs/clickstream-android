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

import androidx.annotation.NonNull;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import software.aws.solution.clickstream.client.util.StringUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A Session Object Mostly immutable, with the exception of its stop time. This
 * session object tracks whether or not it has been paused by checking the
 * status of it's stop time. A session's stop time is only set when the session
 * has been paused.
 */
public final class Session {

    // - Session ID configuration constants -------------------------=
    /**
     * The session ID date format.
     */
    private static final String SESSION_ID_DATE_FORMAT = "yyyyMMdd";
    /**
     * The session ID time format.
     */
    private static final String SESSION_ID_TIME_FORMAT = "HHmmssSSS";
    /**
     * The session ID delimiter.
     */
    private static final char SESSION_ID_DELIMITER = '-';
    /**
     * The session ID pad char.
     */
    private static final char SESSION_ID_PAD_CHAR = '_';
    /**
     * The session ID unique ID length.
     */
    private static final int SESSION_ID_UNIQUE_ID_LENGTH = 8;
    private static final Log LOG = LogFactory.getLog(Session.class);
    // - Field Declarations -----------------------------------------=
    private final DateFormat sessionIdTimeFormat;
    private final String sessionID;
    private final Long startTime;
    private Long stopTime;

    /**
     * CONSTRUCTOR - ACTUAL Used by SessionClient.
     *
     * @param context The context of ClickstreamContext.
     * @throws NullPointerException When the context is null.
     */
    Session(@NonNull final ClickstreamContext context) {
        this.sessionIdTimeFormat = new SimpleDateFormat(SESSION_ID_DATE_FORMAT +
            SESSION_ID_DELIMITER + SESSION_ID_TIME_FORMAT, Locale.US);
        this.sessionIdTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.startTime = System.currentTimeMillis();
        this.stopTime = null;
        this.sessionID = this.generateSessionID(context);
    }

    /**
     * Used by deserializer.
     *
     * @param sessionID The session ID.
     * @param startTime The start session timestamp.
     * @param stopTime  The stop session timestamp.
     */
    Session(final String sessionID, final Long startTime, final Long stopTime) {
        this.sessionIdTimeFormat = new SimpleDateFormat(SESSION_ID_DATE_FORMAT +
            SESSION_ID_DELIMITER + SESSION_ID_TIME_FORMAT, Locale.US);
        this.sessionIdTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.sessionID = sessionID;
    }

    /**
     * STATIC FACTORY.
     *
     * @param context The {@link ClickstreamContext}.
     * @return new Session object.
     */
    public static Session newInstance(final ClickstreamContext context) {
        return new Session(context);
    }

    /**
     * Session is considered paused if stopTime is not null.
     *
     * @return true iff session is currently paused.
     */
    public boolean isPaused() {
        return (this.stopTime != null);
    }

    /**
     * Pauses the session object. Generates a stop time.
     */
    public void pause() {
        if (!isPaused()) {
            this.stopTime = System.currentTimeMillis();
        }
    }

    /**
     * Calculates and returns the session's duration Returns a duration of 0 if
     * session is not paused or the system clock has been tampered with.
     *
     * @return session duration in milliseconds.
     */
    public Long getSessionDuration() {
        Long time = this.stopTime;
        if (time == null) {
            time = System.currentTimeMillis();
        }

        if (time < this.startTime) {
            return 0L;
        }
        return time - this.startTime;
    }

    /**
     * Generates Session ID by concatenating present AppKey, UniqueID, and
     * AppKey-UniqueID-yyyyMMdd-HHmmssSSS.
     *
     * @param context The {@link ClickstreamContext}.
     * @return [String] SessionID.
     */
    public String generateSessionID(final ClickstreamContext context) {
        final String uniqueId = context.getUniqueId();
        final String time = this.sessionIdTimeFormat.format(this.startTime);
        return StringUtil.trimOrPadString(uniqueId, SESSION_ID_UNIQUE_ID_LENGTH, SESSION_ID_PAD_CHAR) +
            SESSION_ID_DELIMITER + time;
    }

    /**
     * Get session ID.
     *
     * @return The string of session ID.
     */
    public String getSessionID() {
        return this.sessionID;
    }

    /**
     * Get the start session timestamp.
     *
     * @return The long value of start session timestamp.
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Get the stop session timestamp.
     *
     * @return The long value of stop session timestamp.
     */
    public Long getStopTime() {
        return this.stopTime;
    }
}

