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

import android.util.DisplayMetrics;
import androidx.annotation.NonNull;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A client to manage creating and sending analytics events.
 */
public class AnalyticsClient {
    private static final Log LOG = LogFactory.getLog(AnalyticsClient.class);
    private static final int MAX_EVENT_TYPE_LENGTH = 50;
    private final ClickstreamContext context;
    private final Map<String, Object> globalAttributes = new ConcurrentHashMap<>();
    private final Map<String, Object> userAttributes = new ConcurrentHashMap<>();
    private final EventRecorder eventRecorder;
    private Session session;

    /**
     * A client to manage creating and sending analytics events.
     *
     * @param context The {@link ClickstreamContext} of the ClickStream Manager.
     */
    public AnalyticsClient(@NonNull final ClickstreamContext context) {
        this.context = context;
        eventRecorder = EventRecorder.newInstance(context);
    }

    /**
     * add global attribute for all event, the max limit of attribute in single event is 500,
     * if exceed, the attribute will not be record.
     *
     * @param name  attribute name.
     * @param value attribute value.
     * @throws IllegalArgumentException throws when fail to check the attribute name.
     */
    public void addGlobalAttribute(String name, Object value) {
        Event.EventError error = Event.checkAttribute(globalAttributes.size(), name, value);
        if (error != null) {
            if (!globalAttributes.containsKey(error.getErrorType())) {
                globalAttributes.put(error.getErrorType(), error.getErrorMessage());
            }
            return;
        }
        globalAttributes.put(name, value);
    }

    /**
     * delete global attribute for all event.
     *
     * @param name attribute name.
     */
    public void deleteGlobalAttribute(String name) {
        globalAttributes.remove(name);
    }

    /**
     * add user attribute for all event, the max limit of user attribute in single event is 100,
     * if exceed, the user attribute will not be record.
     * if the user attribute name if not valid or exceed the length limit the user attribute will discard and log error.
     * if the user attribute value exceed the length limit the user attribute will discard and log error.
     * see the event limit definitions {@link Event.Limit} for detail.
     *
     * @param name  user attribute name.
     * @param value user attribute value.
     */
    public void addUserAttribute(String name, Object value) {
        Event.EventError error = Event.checkUserAttribute(userAttributes.size(), name, value);
        if (error != null) {
            if (!userAttributes.containsKey(error.getErrorType())) {
                userAttributes.put(error.getErrorType(), error.getErrorMessage());
            }
            return;
        }
        userAttributes.put(name, value);
    }

    /**
     * Create an event with the specified eventType. The eventType is a
     * developer defined String that can be used to distinguish between
     * different scenarios within an application. Note: You can have at most
     * 500 different eventTypes per app.
     *
     * @param eventType the type of event to create.
     * @return AnalyticsEvent.
     * @throws IllegalArgumentException throws when fail to check the argument.
     */
    public AnalyticsEvent createEvent(String eventType) {
        if (eventType.length() > MAX_EVENT_TYPE_LENGTH) {
            LOG.error("The event name is too long, the max event type length is "
                + MAX_EVENT_TYPE_LENGTH + " characters. event name:" + eventType);
            throw new IllegalArgumentException("The event name passed into create event was too long");
        }
        if (!Event.isValidName(eventType)) {
            LOG.error("Event name can only contains uppercase and lowercase letters, underscores, number, " +
                "and is not start with a number. event name:" + eventType);
            throw new IllegalArgumentException("The event name was not valid");
        }
        long timestamp = System.currentTimeMillis();
        String uniqueId = this.context.getUniqueId();
        AnalyticsEvent event = new AnalyticsEvent(eventType, globalAttributes, userAttributes, timestamp, uniqueId);
        event.setAndroidId(context.getSystem().getAndroidId());
        event.setSdkInfo(context.getSDKInfo());
        event.setAppDetails(context.getSystem().getAppDetails());
        event.setDeviceDetails(context.getSystem().getDeviceDetails());
        event.setConnectivity(context.getSystem().getConnectivity());
        if (session != null) {
            event.setSession(session);
        }
        DisplayMetrics dm = this.context.getApplicationContext().getResources().getDisplayMetrics();
        if (dm != null) {
            event.setHeightPixels(dm.heightPixels);
            event.setWidthPixels(dm.widthPixels);
        }
        return event;
    }

    /**
     * Record event for AnalyticsEvent object.
     *
     * @param event AnalyticsEvent object.
     */
    public void recordEvent(@NonNull AnalyticsEvent event) {
        eventRecorder.recordEvent(event);
    }

    /**
     * Submit all recorded events
     * If the device is off line, this is a no-op. See
     * {@link ClickstreamConfiguration}
     * for customizing which Internet connection the SDK can submit on.
     */
    public void submitEvents() {
        eventRecorder.submitEvents();
    }

    /**
     * Sets the session.
     *
     * @param session The current Session object.
     */
    public void setSession(Session session) {
        this.session = session;
    }
}
