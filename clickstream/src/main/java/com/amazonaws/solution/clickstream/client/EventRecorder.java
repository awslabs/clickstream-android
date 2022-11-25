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
import com.amazonaws.solution.clickstream.client.util.StringUtil;

/**
 * Event Recorder.
 */
public class EventRecorder {
    private static final Log LOG = LogFactory.getLog(EventRecorder.class);

    private final int clippedEventLength = 10;
    private final ClickstreamContext clickstreamContext;

    EventRecorder(final ClickstreamContext clickstreamContext) {
        this.clickstreamContext = clickstreamContext;
    }

    /**
     * Constructs a new EventRecorder specifying the client to use.
     * @param clickstreamContext The ClickstreamContext.
     * @return The instance of the ClickstreamContext.
     */
    public static EventRecorder newInstance(final ClickstreamContext clickstreamContext) {
        return new EventRecorder(clickstreamContext);
    }

    /**
     * Records an {@link AnalyticsEvent}.
     *
     * @param event the analytics event
     */
    public void recordEvent(@NonNull final AnalyticsEvent event) {

        LOG.info(String.format("Event Recorded to database with EventType: %s",
            StringUtil.clipString(event.getEventType(), clippedEventLength, true)));
        LOG.info("event json:\n" + event);

    }

}

