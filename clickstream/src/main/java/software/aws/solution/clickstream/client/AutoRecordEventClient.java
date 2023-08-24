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

import android.app.Activity;
import androidx.annotation.NonNull;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import software.aws.solution.clickstream.client.system.AndroidPreferences;
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
    private static final int DEVICE_ID_CLIP_LENGTH = 8;
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
     * whether app is first time start from process launch.
     */
    private boolean isFirstTime = true;

    /**
     * current screen is entrances.
     */
    private boolean isEntrances;

    private long startEngageTimestamp;
    private long endEngageTimestamp;
    private long lastEngageTime;
    private final AndroidPreferences preferences;

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
        this.preferences = clickstreamContext.getSystem().getPreferences();
        this.isFirstOpen = preferences.getBoolean("isFirstOpen", true);
    }

    /**
     * record view screen event.
     *
     * @param activity the activity to record.
     */
    public void recordViewScreen(Activity activity) {
        if (!clickstreamContext.getClickstreamConfiguration().isTrackScreenViewEvents()) {
            return;
        }
        String screenId = activity.getClass().getCanonicalName();
        String screenName = activity.getClass().getSimpleName();
        String screenUniqueId = getScreenUniqueId(activity);
        if (ScreenRefererTool.isSameScreen(screenId, screenName, screenUniqueId)) {
            return;
        }
        ScreenRefererTool.setCurrentScreenId(screenId);
        ScreenRefererTool.setCurrentScreenName(screenName);
        ScreenRefererTool.setCurrentScreenUniqueId(screenUniqueId);
        final AnalyticsEvent event =
            this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.SCREEN_VIEW);
        long currentTimestamp = event.getEventTimestamp();
        startEngageTimestamp = currentTimestamp;
        event.addAttribute(Event.ReservedAttribute.SCREEN_NAME, ScreenRefererTool.getCurrentScreenName());
        event.addAttribute(Event.ReservedAttribute.SCREEN_ID, ScreenRefererTool.getCurrentScreenId());
        event.addAttribute(Event.ReservedAttribute.SCREEN_UNIQUE_ID, ScreenRefererTool.getCurrentScreenUniqueId());
        event.addAttribute(Event.ReservedAttribute.PREVIOUS_SCREEN_NAME, ScreenRefererTool.getPreviousScreenName());
        event.addAttribute(Event.ReservedAttribute.PREVIOUS_SCREEN_ID, ScreenRefererTool.getPreviousScreenId());
        event.addAttribute(Event.ReservedAttribute.PREVIOUS_SCREEN_UNIQUE_ID,
            ScreenRefererTool.getPreviousScreenUniqueId());
        long lastScreenViewEventTimestamp = PreferencesUtil.getPreviousScreenViewTimestamp(preferences);
        if (lastScreenViewEventTimestamp > 0) {
            event.addAttribute(Event.ReservedAttribute.PREVIOUS_TIMESTAMP, lastScreenViewEventTimestamp);
        }
        event.addAttribute(Event.ReservedAttribute.ENTRANCES, isEntrances ? 1 : 0);
        event.addAttribute(Event.ReservedAttribute.ENGAGEMENT_TIMESTAMP, lastEngageTime);
        this.clickstreamContext.getAnalyticsClient().recordEvent(event);
        PreferencesUtil.savePreviousScreenViewTimestamp(preferences, currentTimestamp);
        isEntrances = false;
        LOG.debug("record an _screen_view event, screenId:" + screenId + "lastScreenId:" +
            ScreenRefererTool.getPreviousScreenId());
    }

    /**
     * get the screen unique id for activity.
     * the unique id calculated by appending the last 8 characters of the device id with "_" and the activity hash code.
     *
     * @param activity the activity for holding the screen
     * @return the unique of the activity
     */
    public String getScreenUniqueId(Activity activity) {
        String clipDeviceId = StringUtil.trimOrPadString(clickstreamContext.getDeviceId(), DEVICE_ID_CLIP_LENGTH, '_');
        return clipDeviceId + "_" + activity.hashCode();
    }

    /**
     * record user engagement event.
     */
    public void recordUserEngagement() {
        lastEngageTime = endEngageTimestamp - startEngageTimestamp;
        if (clickstreamContext.getClickstreamConfiguration().isTrackUserEngagementEvents() &&
            lastEngageTime > MIN_ENGAGEMENT_TIME) {
            final AnalyticsEvent event =
                this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.USER_ENGAGEMENT);
            event.addAttribute(Event.ReservedAttribute.ENGAGEMENT_TIMESTAMP, lastEngageTime);
            event.addAttribute(Event.ReservedAttribute.SCREEN_NAME, ScreenRefererTool.getCurrentScreenName());
            event.addAttribute(Event.ReservedAttribute.SCREEN_ID, ScreenRefererTool.getCurrentScreenId());
            event.addAttribute(Event.ReservedAttribute.SCREEN_UNIQUE_ID, ScreenRefererTool.getCurrentScreenUniqueId());
            this.clickstreamContext.getAnalyticsClient().recordEvent(event);
        }
    }

    /**
     * the method for flush events when app move to background.
     */
    public void flushEvents() {
        this.clickstreamContext.getAnalyticsClient().submitEvents();
    }

    /**
     * update start engage timestamp.
     */
    public void updateStartEngageTimestamp() {
        startEngageTimestamp = System.currentTimeMillis();
    }

    /**
     * update end engage timestamp.
     */
    public void updateEndEngageTimestamp() {
        endEngageTimestamp = System.currentTimeMillis();
    }

    /**
     * check and record _app_update event.
     */
    private void checkAppVersionUpdate() {
        String previousAppVersion = preferences.getString("appVersion", "");
        if (!StringUtil.isNullOrEmpty(previousAppVersion)) {
            String currentVersion = clickstreamContext.getSystem().getAppDetails().versionName();
            if (!currentVersion.equals(previousAppVersion)) {
                final AnalyticsEvent event =
                    this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.APP_UPDATE);
                event.addAttribute(Event.ReservedAttribute.PREVIOUS_APP_VERSION, previousAppVersion);
                this.clickstreamContext.getAnalyticsClient().recordEvent(event);
                preferences.putString("appVersion", currentVersion);
            }
        } else {
            preferences.putString("appVersion", clickstreamContext.getSystem().getAppDetails().versionName());
        }
    }

    /**
     * check and record _os_update event.
     */
    private void checkOSVersionUpdate() {
        String previousOSVersion = preferences.getString("osVersion", "");
        if (!StringUtil.isNullOrEmpty(previousOSVersion)) {
            String currentOSVersion = clickstreamContext.getSystem().getDeviceDetails().platformVersion();
            if (!currentOSVersion.equals(previousOSVersion)) {
                final AnalyticsEvent event =
                    this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.OS_UPDATE);
                event.addAttribute(Event.ReservedAttribute.PREVIOUS_OS_VERSION, previousOSVersion);
                this.clickstreamContext.getAnalyticsClient().recordEvent(event);
                preferences.putString("osVersion", currentOSVersion);
            }
        } else {
            preferences.putString("osVersion", clickstreamContext.getSystem().getDeviceDetails().platformVersion());
        }
    }

    /**
     * handle the first open event.
     */
    public void handleAppStart() {
        checkAppVersionUpdate();
        checkOSVersionUpdate();
        if (isFirstOpen) {
            final AnalyticsEvent event =
                this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.FIRST_OPEN);
            this.clickstreamContext.getAnalyticsClient().recordEvent(event);
            preferences.putBoolean("isFirstOpen", false);
            isFirstOpen = false;
        }
        final AnalyticsEvent event =
            this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.APP_START);
        event.addAttribute(Event.ReservedAttribute.IS_FIRST_TIME, isFirstTime);
        event.addAttribute(Event.ReservedAttribute.SCREEN_NAME, ScreenRefererTool.getCurrentScreenName());
        event.addAttribute(Event.ReservedAttribute.SCREEN_ID, ScreenRefererTool.getCurrentScreenId());
        this.clickstreamContext.getAnalyticsClient().recordEvent(event);
        isFirstTime = false;
    }

    /**
     * setter for isEntrances.
     */
    public void setIsEntrances() {
        isEntrances = true;
    }
}

