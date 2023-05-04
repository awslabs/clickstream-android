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

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Exception handler for record app exception event.
 */
public final class ClickstreamExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Log LOG = LogFactory.getLog(ClickstreamExceptionHandler.class);
    private static ClickstreamExceptionHandler handlerInstance;
    private static final int SLEEP_TIMEOUT_MS = 500;
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private ClickstreamContext clickstreamContext;

    private ClickstreamExceptionHandler() {
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * init static method for ClickstreamExceptionHandler.
     *
     * @return ClickstreamExceptionHandler the instance.
     */
    public static synchronized ClickstreamExceptionHandler init() {
        if (handlerInstance == null) {
            handlerInstance = new ClickstreamExceptionHandler();
        }
        return handlerInstance;
    }

    /**
     * setter for clickstreamContext.
     *
     * @param context ClickstreamContext
     */
    public void setClickstreamContext(ClickstreamContext context) {
        this.clickstreamContext = context;
    }

    /**
     * fetch uncaught exception and record crash event.
     *
     * @param thread    the thread
     * @param throwable the exception
     */
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        try {
            String exceptionMessage = "";
            String exceptionStack = "";
            try {
                if (throwable.getMessage() != null) {
                    exceptionMessage = throwable.getMessage();
                }
                final Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                throwable.printStackTrace(printWriter);
                Throwable cause = throwable.getCause();
                while (cause != null) {
                    cause.printStackTrace(printWriter);
                    cause = cause.getCause();
                }
                printWriter.close();
                exceptionStack = writer.toString();
            } catch (Exception exception) {
                LOG.error("exception for get exception stack:", exception);
            }

            final AnalyticsEvent event =
                this.clickstreamContext.getAnalyticsClient().createEvent(Event.PresetEvent.APP_EXCEPTION);
            event.addInternalAttribute("exception_message", exceptionMessage);
            event.addInternalAttribute("exception_stack", exceptionStack);
            this.clickstreamContext.getAnalyticsClient().recordEvent(event);

            try {
                Thread.sleep(SLEEP_TIMEOUT_MS);
            } catch (InterruptedException exception) {
                LOG.error("interrupted exception for sleep:", exception);
            }
            if (defaultExceptionHandler != null) {
                defaultExceptionHandler.uncaughtException(thread, throwable);
            } else {
                killProcessAndExit();
            }
        } catch (Exception exception) {
            LOG.error("uncaughtException:", exception);
        }
    }

    /**
     * exit app.
     */
    private void killProcessAndExit() {
        try {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        } catch (Exception exception) {
            LOG.error("exit app exception:", exception);
        }
    }
}
