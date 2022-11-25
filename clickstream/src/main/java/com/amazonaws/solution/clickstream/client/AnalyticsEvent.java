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
import com.amazonaws.solution.clickstream.client.system.AndroidAppDetails;
import com.amazonaws.solution.clickstream.client.system.AndroidConnectivity;
import com.amazonaws.solution.clickstream.client.system.AndroidDeviceDetails;
import com.amazonaws.solution.clickstream.client.util.JSONBuilder;
import com.amazonaws.solution.clickstream.client.util.JSONSerializable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An event for clickstream.
 */
public class AnalyticsEvent implements JSONSerializable {

    static final int MAX_NUM_OF_ATTRIBUTES = 500;
    private static final Log LOG = LogFactory.getLog(AnalyticsEvent.class);
    private static final int INDENTATION = 4;
    private String androidId;
    private String eventId;
    private String eventType;
    private String sdkName;
    private String sdkVersion;
    private final JSONObject attributes = new JSONObject();
    private Long timestamp;
    private String uniqueId;
    private AndroidAppDetails appDetails;
    private AndroidDeviceDetails deviceDetails;
    private AndroidConnectivity connectivity;
    private int heightPixels;
    private int widthPixels;
    private AtomicInteger currentNumOfAttributes = new AtomicInteger(0);

    /**
     * The default constructor.
     *
     * @param eventType  The eventType of the new event.
     * @param attributes A list of attributes of the new event.
     * @param metrics    A list of metrics of the new event.
     * @param timestamp  The timestamp of the new event.
     * @param uniqueId   The uniqueId of the new event.
     */
    AnalyticsEvent(final String eventType, final Map<String, String> attributes, final Map<String, Double> metrics,
                   final long timestamp, final String uniqueId) {
        this(UUID.randomUUID().toString(), eventType, attributes, metrics, timestamp, uniqueId);
    }

    private AnalyticsEvent(final String eventId, final String eventType, final Map<String, String> attributes,
                           final Map<String, Double> metrics, final long timestamp, final String uniqueId) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.uniqueId = uniqueId;
        this.eventType = eventType;
        if (null != attributes) {
            for (final Map.Entry<String, String> kvp : attributes.entrySet()) {
                this.addAttribute(kvp.getKey(), kvp.getValue());
            }
        }
    }

    /**
     * Creates a new instance of an AnalyticsEvent.
     *
     * @param eventId    The eventId of the new event.
     * @param eventType  The eventType of the new event.
     * @param attributes A list of attributes of the new event.
     * @param metrics    A list of metrics of the new event.
     * @param timestamp  The timestamp of the new event.
     * @param uniqueId   The uniqueId of the new event.
     * @return An instance of an AnalyticsEvent object.
     */
    public static AnalyticsEvent newInstance(final String eventId, final String eventType,
                                             final Map<String, String> attributes, final Map<String, Double> metrics,
                                             final long timestamp, final String uniqueId) {
        return new AnalyticsEvent(eventId, eventType, attributes, metrics, timestamp, uniqueId);
    }

    /**
     * Setter for eventId.
     *
     * @param eventId The eventId.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Setter for eventType.
     *
     * @param eventType The eventType.
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Setter for sdkInfo.
     *
     * @param sdkInfo The sdkInfo.
     */
    public void setSdkInfo(SDKInfo sdkInfo) {
        this.sdkName = sdkInfo.getName();
        this.sdkVersion = sdkInfo.getVersion();
    }

    /**
     * Setter for attributes.
     *
     * @param attributes The map of the attributes.
     */
    public void setAttributes(Map<String, String> attributes) {
        if (null != attributes) {
            for (final Map.Entry<String, String> kvp : attributes.entrySet()) {
                this.addAttribute(kvp.getKey(), kvp.getValue());
            }
        }
    }

    /**
     * Setter for timestamp.
     *
     * @param timestamp The timestamp.
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Setter for uniqueId.
     *
     * @param uniqueId The uniqueId.
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * Setter for appDetails.
     *
     * @param appDetails The appDetails.
     */
    public void setAppDetails(AndroidAppDetails appDetails) {
        this.appDetails = appDetails;
    }

    /**
     * Setter for deviceDetails.
     *
     * @param deviceDetails The deviceDetails.
     */
    public void setDeviceDetails(AndroidDeviceDetails deviceDetails) {
        this.deviceDetails = deviceDetails;
    }

    /**
     * Setter for currentNumOfAttributes.
     *
     * @param currentNumOfAttributes The currentNumOfAttributes.
     */
    public void setCurrentNumOfAttributes(AtomicInteger currentNumOfAttributes) {
        this.currentNumOfAttributes = currentNumOfAttributes;
    }

    /**
     * Get the connectivity.
     *
     * @return The connectivity.
     */
    public AndroidConnectivity getConnectivity() {
        return connectivity;
    }

    /**
     * Setter for connectivity.
     *
     * @param connectivity The connectivity.
     */
    public void setConnectivity(AndroidConnectivity connectivity) {
        this.connectivity = connectivity;
    }

    /**
     * Get the Android ID.
     *
     * @return The Android ID.
     */
    public String getAndroidId() {
        return androidId;
    }

    /**
     * Setter for Android ID.
     *
     * @param androidId The Android ID.
     */
    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    /**
     * Get the screen height pixels.
     *
     * @return The screen height pixels.
     */
    public int getHeightPixels() {
        return heightPixels;
    }

    /**
     * Setter the screen height pixels.
     *
     * @param heightPixels The screen height pixels.
     */
    public void setHeightPixels(int heightPixels) {
        this.heightPixels = heightPixels;
    }

    /**
     * Get the screen width pixels.
     *
     * @return The screen width pixels.
     */
    public int getWidthPixels() {
        return widthPixels;
    }

    /**
     * Setter the screen width pixels.
     *
     * @param widthPixels The screen width pixels.
     */
    public void setWidthPixels(int widthPixels) {
        this.widthPixels = widthPixels;
    }

    /**
     * Create a event object.
     *
     * @param context      The client context.
     * @param sessionId    The ID of the session.
     * @param sessionStart The start timestamp of the session.
     * @param sessionEnd   The end timestamp of the session.
     * @param duration     The duration of the session.
     * @param timestamp    The timestamp of the event.
     * @param eventType    The type of the event.
     * @return The event object.
     */
    public static AnalyticsEvent newInstance(ClickstreamContext context, String sessionId, long sessionStart,
                                             Long sessionEnd, Long duration, long timestamp, String eventType) {
        AnalyticsEvent event = new AnalyticsEvent(eventType, null, null, timestamp, context.getUniqueId());
        event.setSdkInfo(context.getSDKInfo());
        event.setAppDetails(context.getSystem().getAppDetails());
        event.setDeviceDetails(context.getSystem().getDeviceDetails());
        event.setConnectivity(context.getSystem().getConnectivity());
        return event;
    }

    /**
     * Returns the eventId.
     *
     * @return the eventId.
     */
    public String getEventId() {
        return this.eventId;
    }

    /**
     * Adds an attribute to this {@link AnalyticsEvent} with the specified key.
     * Only 500 attributes are allowed to be added to an Event. If 500
     * attribute already exist on this Event, the call may be ignored.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     */
    public void addAttribute(final String name, final Object value) {
        if (null == name) {
            return;
        }

        if (null != value) {
            if (currentNumOfAttributes.get() < MAX_NUM_OF_ATTRIBUTES) {
                try {
                    attributes.putOpt(name, value);
                    currentNumOfAttributes.incrementAndGet();
                } catch (JSONException exception) {
                    LOG.warn("error parsing json");
                }
            } else {
                LOG.warn("Event: " + name + ", reached the max number of attributes limit ("
                    + MAX_NUM_OF_ATTRIBUTES + ").");
            }
        } else {
            attributes.remove(name);
        }
    }

    /**
     * Determines if this {@link AnalyticsEvent} contains a specific attribute.
     *
     * @param attributeName The name of the attribute.
     * @return true if this {@link AnalyticsEvent} has an attribute with the
     * specified name, false otherwise.
     */
    public boolean hasAttribute(final String attributeName) {
        if (attributeName == null) {
            return false;
        }
        return attributes.has(attributeName);
    }

    /**
     * Returns the name/type of this {@link AnalyticsEvent}.
     *
     * @return the name/type of this {@link AnalyticsEvent}.
     */
    public String getEventType() {
        return this.eventType;
    }

    /**
     * Returns the String value of the attribute with the specified name.
     *
     * @param name The name of the attribute to return.
     * @return The attribute with the specified name, or null if attribute does
     * not exist.
     */
    public String getStringAttribute(final String name) {
        if (name == null) {
            return null;
        }
        try {
            return attributes.getString(name);
        } catch (JSONException exception) {
            LOG.warn("error to get attribute: " + name);
            return null;
        }
    }

    /**
     * Get the event timestamp.
     *
     * @return The timestamp of the event.
     */
    public Long getEventTimestamp() {
        return timestamp;
    }

    /**
     * Get the unique ID to check the sequence of the event.
     *
     * @return The unique ID.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Get the name of the SDK.
     *
     * @return The SDK name.
     */
    public String getSdkName() {
        return sdkName;
    }

    /**
     * Get the version of the SDK.
     *
     * @return The SDK version.
     */
    public String getSdkVersion() {
        return sdkVersion;
    }

    /**
     * Adds an attribute to this {@link AnalyticsEvent} with the specified key.
     * Only 500 attributes are allowed to be added to an.
     * {@link AnalyticsEvent}. If 500 attribute already exist on this
     * {@link AnalyticsEvent}, the call may be ignored.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     * @return The same {@link AnalyticsEvent} instance is returned to allow for
     * method chaining.
     */
    public AnalyticsEvent withAttribute(String name, String value) {
        addAttribute(name, value);
        return this;
    }

    /**
     * getAttributes.
     *
     * @return the attributes JSONObject.
     */
    public JSONObject getAttributes() {
        return attributes;
    }

    /**
     * Returns the App specific information.
     *
     * @return the App specific information.
     */
    public AndroidAppDetails getAppDetails() {
        return appDetails;
    }

    /**
     * Get the zone offset.
     *
     * @return The zone offset.
     */
    public int getZoneOffset() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.ZONE_OFFSET);
    }

    /**
     * Convert from event JSON format to string.
     *
     * @return The string of event JSON format.
     */
    @NonNull
    @Override
    public String toString() {
        final JSONObject json = toJSONObject();
        try {
            return json.toString(INDENTATION);
        } catch (final JSONException jsonException) {
            return json.toString();
        }
    }

    /**
     * Convert event to JSON format.
     *
     * @return The JSON object.
     */
    @Override
    public JSONObject toJSONObject() {
        final Locale locale = this.deviceDetails.locale();
        final String localeString = locale != null ? locale.toString() : "UNKNOWN";
        final String displayCountryString = locale != null ? locale.getDisplayCountry() : "UNKNOWN";
        final String countryString = locale != null ? locale.getCountry() : "UNKNOWN";
        final String languageString = locale != null ? locale.getLanguage() : "UNKNOWN";
        final String carrier = this.deviceDetails.carrier();
        final String carrierString = carrier != null ? carrier : "UNKNOWN";

        final JSONBuilder builder = new JSONBuilder(this);

        // ****************************************************
        // ==================System Attributes=================
        // ****************************************************
        //builder.withAttribute("account_id", getAccountId()); // login ID
        // The non-login ID, it be changed after uninstalling then reinstalling the app.
        // https://developer.android.com/training/articles/user-data-ids
        builder.withAttribute("unique_id", getUniqueId());
        builder.withAttribute("type", "track"); // track for event, user_* for user
        builder.withAttribute("event_type", getEventType()); // event_name AKA event_type, NOT NULL when type = track
        builder.withAttribute("event_id", getEventId());
        // Unix time is the number of seconds since January 1st, 1970.
        // https://nickb.dev/blog/designing-a-rest-api-unix-time-vs-iso-8601/
        builder.withAttribute("timestamp", getEventTimestamp());
        // ****************************************************
        // ==============Device Details Attributes=============
        // ****************************************************
        // The user's device ID, iOS take the user's IDFV or UUID,
        // Android takes androidID
        builder.withAttribute("device_id", getAndroidId());
        builder.withAttribute("platform", this.deviceDetails.platform());
        builder.withAttribute("platform_version", this.deviceDetails.platformVersion());
        builder.withAttribute("make", this.deviceDetails.manufacturer());
        builder.withAttribute("model", this.deviceDetails.model());
        builder.withAttribute("locale", localeString);
        builder.withAttribute("carrier", carrierString);
        if (this.connectivity != null) {
            builder.withAttribute("network_type",
                this.connectivity.hasWAN() ? "Mobile" : this.connectivity.hasWifi() ? "WIFI" : "UNKNOWN");
        } else {
            builder.withAttribute("network_type", "UNKNOWN");
        }

        builder.withAttribute("screen_height", getHeightPixels());
        builder.withAttribute("screen_width", getWidthPixels());
        builder.withAttribute("zone_offset", getZoneOffset());
        builder.withAttribute("system_language", languageString);

        // ****************************************************
        // ==============Geo Details Attributes=============
        // ****************************************************
        builder.withAttribute("country", displayCountryString);
        builder.withAttribute("country_code", countryString);
        //builder.withAttribute("province", "");
        //builder.withAttribute("city", "");

        // ****************************************************
        // ====SDK Details Attributes -- Prefix with 'sdk_'====
        // ****************************************************
        builder.withAttribute("sdk_version", this.sdkVersion);
        builder.withAttribute("sdk_name", this.sdkName);

        // ****************************************************
        // Application Details Attributes -- Prefix with 'app_'
        // ****************************************************
        builder.withAttribute("app_version_name", this.appDetails.versionName());
        //builder.withAttribute("app_version_code", this.appDetails.versionCode());
        builder.withAttribute("app_package_name", this.appDetails.packageName());
        builder.withAttribute("app_title", this.appDetails.getAppTitle());
        builder.withAttribute("attributes", this.attributes);
        return builder.toJSONObject();
    }
}
