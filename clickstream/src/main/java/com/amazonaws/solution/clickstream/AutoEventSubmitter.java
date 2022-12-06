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

import android.os.Handler;
import android.os.HandlerThread;

import com.amplifyframework.core.Amplify;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;

import java.util.Locale;

/**
 * Submits all the recorded event periodically.
 */
final class AutoEventSubmitter {
    private static final Log LOG = LogFactory.getLog(AutoEventSubmitter.class);
    private final Handler handler;
    private Runnable submitRunnable;
    private final long autoFlushInterval;

    AutoEventSubmitter(final long autoFlushInterval) {
        HandlerThread handlerThread = new HandlerThread("AutoEventSubmitter");
        handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());
        this.autoFlushInterval = autoFlushInterval;
        this.submitRunnable = () -> {
            LOG.debug(String.format(Locale.US, "Auto submitting events after %d seconds", autoFlushInterval));
            Amplify.Analytics.flushEvents();
            handler.postDelayed(this.submitRunnable, autoFlushInterval);
        };
        LOG.debug("Auto submitting init");
    }

    synchronized void start() {
        handler.postDelayed(submitRunnable, autoFlushInterval);
        LOG.debug("Auto submitting start");
    }

    synchronized void stop() {
        handler.removeCallbacksAndMessages(null);
        LOG.debug("Auto submitting stop");
    }
}

