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

import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import com.amazonaws.solution.clickstream.client.db.ClickstreamDBUtil;
import com.amazonaws.solution.clickstream.client.db.EventTable;
import com.amazonaws.solution.clickstream.client.network.NetRequest;
import com.amazonaws.solution.clickstream.client.network.NetUtil;
import com.amazonaws.solution.clickstream.client.util.StringUtil;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Event Recorder.
 */
public class EventRecorder {
    static final String KEY_MAX_SUBMISSIONS_ALLOWED = "maxSubmissionAllowed";
    static final String KEY_MAX_SUBMISSION_SIZE = "maxSubmissionSize";

    private static final int DEFAULT_MAX_SUBMISSIONS_ALLOWED = 3;
    private static final int MAX_EVENT_OPERATIONS = 1000;
    private static final long DEFAULT_MAX_SUBMISSION_SIZE = 128 * 1024;
    private static final Log LOG = LogFactory.getLog(EventRecorder.class);

    private static final int JSON_COLUMN_INDEX = EventTable.ColumnIndex.JSON.getValue();
    private static final int ID_COLUMN_INDEX = EventTable.ColumnIndex.ID.getValue();
    private static final int SIZE_COLUMN_INDEX = EventTable.ColumnIndex.SIZE.getValue();

    private final ClickstreamContext clickstreamContext;
    private final ClickstreamDBUtil dbUtil;
    private final ExecutorService submissionRunnableQueue;

    EventRecorder(final ClickstreamContext clickstreamContext, final ClickstreamDBUtil dbUtil,
                  final ExecutorService submissionRunnableQueue) {
        this.clickstreamContext = clickstreamContext;
        this.dbUtil = dbUtil;
        this.submissionRunnableQueue = submissionRunnableQueue;
    }

    /**
     * Constructs a new EventRecorder specifying the client to use.
     *
     * @param clickstreamContext The ClickstreamContext.
     * @return The instance of the ClickstreamContext.
     */
    public static EventRecorder newInstance(final ClickstreamContext clickstreamContext) {
        final ExecutorService submissionRunnableQueue =
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(MAX_EVENT_OPERATIONS),
                new ThreadPoolExecutor.DiscardPolicy());
        return new EventRecorder(clickstreamContext,
            new ClickstreamDBUtil(clickstreamContext.getApplicationContext().getApplicationContext()),
            submissionRunnableQueue);
    }

    /**
     * Records an {@link AnalyticsEvent}.
     *
     * @param event the analytics event
     * @return Uri the event uri.
     */
    public Uri recordEvent(@NonNull final AnalyticsEvent event) {
        final Uri uri = this.dbUtil.saveEvent(event);
        if (uri == null) {
            LOG.error(String.format("Error to save event with EventType: %s", event.getEventType()));
        }
        return uri;
    }

    /**
     * Submit the events.
     */
    public void submitEvents() {
        if (NetUtil.isNetworkAvailable(clickstreamContext.getApplicationContext())) {
            submissionRunnableQueue.execute(this::processEvents);
        } else {
            LOG.warn("Device is offline, skipping submitting events to Clickstream server");
        }
    }

    /**
     * Process the events.
     */
    int processEvents() {
        final long start = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        int totalEventNumber = 0;
        try (Cursor cursor = dbUtil.queryAllEvents()) {
            if (!cursor.moveToFirst()) {
                // if the cursor is empty there is nothing to do.
                LOG.info("No events available to submit.");
                return totalEventNumber;
            }

            int submissions = 0;
            final long maxSubmissionsAllowed = clickstreamContext.getConfiguration()
                .optInt(KEY_MAX_SUBMISSIONS_ALLOWED, DEFAULT_MAX_SUBMISSIONS_ALLOWED);

            do {
                final String[] event = this.getBatchOfEvents(cursor);
                int lastId = Integer.parseInt(event[1]);
                // upload events to server
                boolean result = NetRequest.uploadEvents(event[0], clickstreamContext.getClickstreamConfiguration());
                if (!result) {
                    // if fail to upload event then end the process.
                    break;
                }
                // delete all uploaded event by last event id.
                try {
                    int deleteSize = dbUtil.deleteBatchEvents(lastId);
                    submissions++;
                    totalEventNumber += deleteSize;
                    LOG.info("deleted event number: " + deleteSize);
                } catch (final IllegalArgumentException exc) {
                    LOG.error(
                        String.format(Locale.US, "Failed to delete last event: %d with %s", lastId, exc.getMessage()));
                }
                // if the submissions time
                if (submissions >= maxSubmissionsAllowed) {
                    LOG.info("reached maxSubmissions: " + maxSubmissionsAllowed);
                    break;
                }
            } while (cursor.moveToNext());
            LOG.debug(String.format(Locale.US, "Time of attemptDelivery: %d",
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - start));
        } catch (Exception exception) {
            LOG.error("Failed to send event", exception);
        }
        LOG.info(String.format(Locale.US, "Submitted %s events", totalEventNumber));
        return totalEventNumber;
    }

    /**
     * Reads events of maximum of KEY_MAX_SUBMISSION_SIZE size.
     * The default max request size is DEFAULT_MAX_SUBMISSION_SIZE.
     *
     * @param cursor the cursor to the database to read events from.
     * @return an String array of the events json and lastEventId.
     */
    String[] getBatchOfEvents(final Cursor cursor) {
        long currentRequestSize = 0;
        String lastEventId = null;
        final long maxRequestSize =
            clickstreamContext.getConfiguration().optLong(KEY_MAX_SUBMISSION_SIZE, DEFAULT_MAX_SUBMISSION_SIZE);
        final StringBuilder eventBuilder = new StringBuilder();
        eventBuilder.append("[");
        String suffix = ",";
        do {
            int size = cursor.getInt(SIZE_COLUMN_INDEX);
            String eventJson = cursor.getString(JSON_COLUMN_INDEX);
            if (!StringUtil.isNullOrEmpty(eventJson)) {
                currentRequestSize += size;
                if (currentRequestSize > maxRequestSize) {
                    if (eventBuilder.length() > 2) {
                        int length = eventBuilder.length();
                        eventBuilder.replace(length - 1, length, "]");
                        lastEventId = String.valueOf(cursor.getInt(ID_COLUMN_INDEX) - 1);
                        cursor.moveToPrevious();
                    } else {
                        eventBuilder.deleteCharAt(0);
                        lastEventId = "-1";
                    }
                    break;
                }
                if (cursor.isLast()) {
                    lastEventId = String.valueOf(cursor.getInt(ID_COLUMN_INDEX));
                    suffix = "]";
                }
                eventBuilder.append(eventJson);
                eventBuilder.append(suffix);
            }
        } while (cursor.moveToNext());

        return new String[] {eventBuilder.toString(), lastEventId};
    }
}

