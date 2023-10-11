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

package software.aws.solution.clickstream;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import software.aws.solution.clickstream.client.AutoRecordEventClient;
import software.aws.solution.clickstream.client.ClickstreamManager;
import software.aws.solution.clickstream.client.ScreenRefererTool;
import software.aws.solution.clickstream.client.SessionClient;

/**
 * Tracks when the host application enters or leaves foreground.
 * The constructor registers to receive activity lifecycle events.
 **/
final class ActivityLifecycleManager implements Application.ActivityLifecycleCallbacks, LifecycleEventObserver {
    private static final Log LOG = LogFactory.getLog(ActivityLifecycleManager.class);
    private static boolean isFromForeground;
    private final SessionClient sessionClient;
    private final AutoRecordEventClient autoRecordEventClient;

    /**
     * Constructor. Registers to receive activity lifecycle events.
     *
     * @param clickstreamManager Clickstream manager
     */
    ActivityLifecycleManager(final ClickstreamManager clickstreamManager) {
        this.sessionClient = clickstreamManager.getSessionClient();
        this.autoRecordEventClient = clickstreamManager.getAutoRecordEventClient();
    }

    void startLifecycleTracking(final Context context, Lifecycle lifecycle) {
        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(this);
            lifecycle.addObserver(this);
        } else {
            LOG.warn("The context is not ApplicationContext, so lifecycle events are not automatically recorded");
        }
    }

    void stopLifecycleTracking(final Context context, Lifecycle lifecycle) {
        if (context instanceof Application) {
            ((Application) context).unregisterActivityLifecycleCallbacks(this);
            lifecycle.removeObserver(this);
        }
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle bundle) {
        LOG.debug("Activity created: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        LOG.debug("Activity started: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        // An activity came to foreground. Application potentially entered foreground as well
        // if there were no other activities in the foreground.
        LOG.debug("Activity resumed: " + activity.getLocalClassName());
        boolean isSameScreen =
            ScreenRefererTool.isSameScreen(activity.getClass().getCanonicalName(), activity.getClass().getSimpleName(),
                autoRecordEventClient.getScreenUniqueId(activity));
        if (ScreenRefererTool.getCurrentScreenName() != null && !isSameScreen) {
            if (!isFromForeground) {
                autoRecordEventClient.recordUserEngagement();
            } else {
                autoRecordEventClient.resetLastEngageTime();
            }
        }
        autoRecordEventClient.recordViewScreen(activity);
        isFromForeground = false;
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        // onPause is always followed by onStop except when the app is interrupted by an event such
        // as a phone call, pop-ups or app losing focus in a multi-window mode, in which case activity is
        // resumed if app regains focus.In either case, app foreground status does not change for the
        // purpose of session tracking.
        LOG.debug("Activity paused: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(final Activity activity) {
        // An activity entered stopped state. Application potentially entered background if there are
        // no other activities in non-stopped states, in which case app is not visible to user and has
        // entered background state.
        LOG.debug("Activity stopped: " + activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
        LOG.debug("Activity state saved: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        // onStop is always called before onDestroy so no action is required in onActivityDestroyed.
        LOG.debug("Activity destroyed " + activity.getLocalClassName());
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_STOP) {
            LOG.debug("Application entered the background.");
            autoRecordEventClient.recordUserEngagement();
            autoRecordEventClient.handleAppEnd();
            sessionClient.storeSession();
            autoRecordEventClient.flushEvents();
        } else if (event == Lifecycle.Event.ON_START) {
            LOG.debug("Application entered the foreground.");
            isFromForeground = true;
            autoRecordEventClient.handleAppStart();
            boolean isNewSession = sessionClient.initialSession();
            if (isNewSession) {
                autoRecordEventClient.setIsEntrances();
            }
            autoRecordEventClient.updateStartEngageTimestamp();
        }
    }
}
