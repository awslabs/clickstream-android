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

import android.app.Activity;
import androidx.annotation.NonNull;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import software.aws.solution.clickstream.client.util.PreferencesUtil;
import software.aws.solution.clickstream.client.util.StringUtil;

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

    /**
     * whether app is first open from install.
     */
    private boolean isFirstOpen;

    /**
     * current screen is entrances.
     */
    private boolean isEntrances;

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
        this.isFirstOpen = clickstreamContext.getSystem().getPreferences().getBoolean("isFirstOpen", true);
        checkAppVersionUpdate();
        checkOSVersionUpdate();
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
        event.addAttribute("entrances", isEntrances ? 1 : 0);
        this.clickstreamContext.getAnalyticsClient().recordEvent(event);
        isEntrances = false;
        LOG.debug("record an _screen_view event, screenId:" + screenId + "lastScreenId:" +
            ScreenRefererTool.getPreviousScreenId());
    }

    /**
     * record user engagement event.
     */
    public void recordUserEngagement() {
        long engagementTime = System.currentTimeMillis() -
            PreferencesUtil.getEngageStartTimestamp(this.clickstreamContext.getSystem().getPreferences());
        if (engagementTime > MIN_ENGAGEMENT_TIME) {
            final AnalyticsEvent event =
                this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.USER_ENGAGEMENT);
            event.addAttribute("engagement_time_msec", engagementTime);
            this.clickstreamContext.getAnalyticsClient().recordEvent(event);
        }
    }

    /**
     * update engage timestamp.
     */
    public void updateEngageTimestamp() {
        PreferencesUtil.saveEngageStartTimestamp(this.clickstreamContext.getSystem().getPreferences());
    }

    /**
     * check and record _app_update event.
     */
    private void checkAppVersionUpdate() {
        String previousAppVersion = clickstreamContext.getSystem().getPreferences().getString("appVersion", "");
        if (!StringUtil.isNullOrEmpty(previousAppVersion)) {
            String currentVersion = clickstreamContext.getSystem().getAppDetails().versionName();
            if (!currentVersion.equals(previousAppVersion)) {
                final AnalyticsEvent event =
                    this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.APP_UPDATE);
                event.addAttribute("previous_app_version", previousAppVersion);
                this.clickstreamContext.getAnalyticsClient().recordEvent(event);
            }
        } else {
            clickstreamContext.getSystem().getPreferences()
                .putString("appVersion", clickstreamContext.getSystem().getAppDetails().versionName());
        }
    }

    /**
     * check and record _os_update event.
     */
    private void checkOSVersionUpdate() {
        String previousOSVersion = clickstreamContext.getSystem().getPreferences().getString("osVersion", "");
        if (!StringUtil.isNullOrEmpty(previousOSVersion)) {
            String currentOSVersion = clickstreamContext.getSystem().getDeviceDetails().platformVersion();
            if (!currentOSVersion.equals(previousOSVersion)) {
                final AnalyticsEvent event =
                    this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.OS_UPDATE);
                event.addAttribute("previous_os_version", previousOSVersion);
                this.clickstreamContext.getAnalyticsClient().recordEvent(event);
            }
        } else {
            clickstreamContext.getSystem().getPreferences()
                .putString("osVersion", clickstreamContext.getSystem().getDeviceDetails().platformVersion());
        }
    }

    /**
     * handle the first open event.
     */
    public void handleFirstOpen() {
        if (isFirstOpen) {
            final AnalyticsEvent event =
                this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.FIRST_OPEN);
            this.clickstreamContext.getAnalyticsClient().recordEvent(event);
            clickstreamContext.getSystem().getPreferences().putBoolean("isFirstOpen", false);
            isFirstOpen = false;
        }
    }

    /**
     * setter for isEntrances.
     */
    public void setIsEntrances() {
        isEntrances = true;
    }
}

