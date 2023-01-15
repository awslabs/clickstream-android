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

import android.app.Activity;
import androidx.annotation.NonNull;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;

import java.util.HashMap;
import java.util.Objects;

/**
 * Client for manage and send auto record event.
 */
public class AutoRecordEventClient {
    /**
     * Logger instance for AutoRecordEventClient.
     */
    private static final Log LOG = LogFactory.getLog(AutoRecordEventClient.class);
    private static final int MIN_ENGAGEMENT_TIME = 1000;
    /**
     * The context object wraps all the essential information from the app
     * that are required.
     */
    private final ClickstreamContext clickstreamContext;

    private final HashMap<Integer, Long> activityStartTimeMap = new HashMap<>();

    /**
     * CONSTRUCTOR.
     *
     * @param clickstreamContext The {@link ClickstreamContext}.
     * @throws IllegalArgumentException When the clickstreamContext.getAnalyticsClient is null.
     */
    public AutoRecordEventClient(@NonNull final ClickstreamContext clickstreamContext) {
        if (clickstreamContext.getAnalyticsClient() == null) {
            throw new IllegalArgumentException("A valid AnalyticsClient must be provided!");
        }
        this.clickstreamContext = clickstreamContext;
    }

    /**
     * record view screen event.
     *
     * @param activity the activity to record.
     */
    public void recordViewScreen(Activity activity) {
        String screenId = activity.getClass().getCanonicalName();
        String screenName = activity.getClass().getSimpleName();
        ScreenRefererTool.setCurrentScreenId(screenId);
        ScreenRefererTool.setCurrentScreenName(screenName);
        final AnalyticsEvent event =
            this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.SCREEN_VIEW);
        event.addAttribute("screen_name", ScreenRefererTool.getCurrentScreenName());
        event.addAttribute("screen_id", ScreenRefererTool.getCurrentScreenId());
        event.addAttribute("previous_screen_name", ScreenRefererTool.getPreviousScreenName());
        event.addAttribute("previous_screen_id", ScreenRefererTool.getPreviousScreenId());
        this.clickstreamContext.getAnalyticsClient().recordEvent(event);
        LOG.debug("record an _screen_view event, screenId:" + screenId + "lastScreenId:" +
            ScreenRefererTool.getPreviousScreenId());
    }

    /**
     * record user engagement event.
     *
     * @param activity the activity for engagement event.
     */
    public void recordUserEngagement(Activity activity) {
        if (activity != null && activityStartTimeMap.containsKey(activity.hashCode())) {
            int engagementTime = (int) (System.currentTimeMillis() -
                Objects.requireNonNull(activityStartTimeMap.get(activity.hashCode())));
            if (engagementTime > MIN_ENGAGEMENT_TIME) {
                final AnalyticsEvent event =
                    this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.USER_ENGAGEMENT);
                event.addAttribute("engagement_time_msec", engagementTime);
                event.addAttribute("screen_name", activity.getClass().getSimpleName());
                event.addAttribute("screen_id", activity.getClass().getCanonicalName());
                this.clickstreamContext.getAnalyticsClient().recordEvent(event);
            } else {
                LOG.warn("activity: " + activity.getClass().getSimpleName() + ", foreground time:" + engagementTime +
                    "ms, and will not record an _user_engagement event");
            }
        }
    }

    /**
     * record activity start time stamp.
     *
     * @param activity the resumed activity.
     */
    public void recordActivityStart(Activity activity) {
        if (activity != null) {
            activityStartTimeMap.put(activity.hashCode(), System.currentTimeMillis());
        }
    }

    /**
     * remove activity start time stamp.
     *
     * @param activity the stopped activity.
     */
    public void removeActivityStart(Activity activity) {
        if (activity != null) {
            activityStartTimeMap.remove(activity.hashCode());
        }
    }
}

