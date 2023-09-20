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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.amplifyframework.analytics.AnalyticsEventBehavior;
import com.amplifyframework.analytics.AnalyticsException;
import com.amplifyframework.analytics.AnalyticsPlugin;
import com.amplifyframework.analytics.AnalyticsProperties;
import com.amplifyframework.analytics.AnalyticsPropertyBehavior;
import com.amplifyframework.analytics.UserProfile;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.AnalyticsEvent;
import software.aws.solution.clickstream.client.ClickstreamManager;
import software.aws.solution.clickstream.client.Event;

import java.util.Map;

/**
 * The plugin implementation for Clickstream in Analytics category.
 */
public final class AWSClickstreamPlugin extends AnalyticsPlugin<Object> {

    private static final Log LOG = LogFactory.getLog(AWSClickstreamPlugin.class);
    private final Context context;
    private AnalyticsClient analyticsClient;
    private AutoEventSubmitter autoEventSubmitter;
    private ActivityLifecycleManager activityLifecycleManager;

    /**
     * Constructs a new {@link AWSClickstreamPlugin}.
     *
     * @param context ApplicationContext
     */
    public AWSClickstreamPlugin(final Context context) {
        this.context = context;
    }

    @Override
    public void identifyUser(@NonNull String userId, @Nullable UserProfile profile) {
        if (userId.equals(Event.ReservedAttribute.USER_ID_UNSET)) {
            if (profile instanceof ClickstreamUserAttribute) {
                for (Map.Entry<String, AnalyticsPropertyBehavior<?>> entry :
                    ((ClickstreamUserAttribute) profile).getUserAttributes()) {
                    AnalyticsPropertyBehavior<?> property = entry.getValue();
                    analyticsClient.addUserAttribute(entry.getKey(), property.getValue());
                }
            }
        } else {
            analyticsClient.updateUserId(userId);
        }
        analyticsClient.updateUserAttribute();
        recordEvent(Event.PresetEvent.PROFILE_SET);
    }

    @Override
    public void disable() {
        autoEventSubmitter.stop();
        activityLifecycleManager.stopLifecycleTracking(context);
    }

    @Override
    public void enable() {
        autoEventSubmitter.start();
        activityLifecycleManager.startLifecycleTracking(context, ProcessLifecycleOwner.get().getLifecycle());
    }

    @Override
    public void recordEvent(@NonNull String eventName) {
        final AnalyticsEvent event = analyticsClient.createEvent(eventName);
        if (event != null) {
            analyticsClient.recordEvent(event);
        }
    }

    @Override
    public void recordEvent(@NonNull AnalyticsEventBehavior analyticsEvent) {
        final AnalyticsEvent clickstreamEvent =
            analyticsClient.createEvent(analyticsEvent.getName());

        if (clickstreamEvent != null && analyticsEvent.getProperties() != null) {
            for (Map.Entry<String, AnalyticsPropertyBehavior<?>> entry : analyticsEvent.getProperties()) {
                AnalyticsPropertyBehavior<?> property = entry.getValue();
                clickstreamEvent.addAttribute(entry.getKey(), property.getValue());
            }
            analyticsClient.recordEvent(clickstreamEvent);
        }
    }

    @Override
    public void registerGlobalProperties(@NonNull AnalyticsProperties properties) {
        for (Map.Entry<String, AnalyticsPropertyBehavior<?>> entry : properties) {
            AnalyticsPropertyBehavior<?> property = entry.getValue();
            analyticsClient.addGlobalAttribute(entry.getKey(), property.getValue());
        }
    }

    @Override
    public void unregisterGlobalProperties(@NonNull String... propertyNames) {
        for (String name : propertyNames) {
            analyticsClient.deleteGlobalAttribute(name);
        }
    }

    @Override
    public void flushEvents() {
        analyticsClient.submitEvents();
    }

    @NonNull
    @Override
    public String getPluginKey() {
        return "awsClickstreamPlugin";
    }

    @Override
    public void configure(
        JSONObject pluginConfiguration,
        @NonNull Context context
    ) throws AnalyticsException {
        if (pluginConfiguration == null) {
            throw new AnalyticsException(
                "Missing configuration for " + getPluginKey(),
                "Check amplifyconfiguration.json to make sure that there is a section for " +
                    getPluginKey() + " under the analytics category."
            );
        }

        AWSClickstreamPluginConfiguration.Builder configurationBuilder =
            AWSClickstreamPluginConfiguration.builder();

        // Read all the data from the configuration object to be used for record event
        try {
            configurationBuilder.withAppId(pluginConfiguration
                .getString(ConfigurationKey.APP_ID.getConfigurationKey()));

            configurationBuilder.withEndpoint(pluginConfiguration
                .getString(ConfigurationKey.ENDPOINT.getConfigurationKey()));

            if (pluginConfiguration.has(ConfigurationKey.SEND_EVENTS_INTERVAL.getConfigurationKey())) {
                configurationBuilder.withSendEventsInterval(pluginConfiguration
                    .getLong(ConfigurationKey.SEND_EVENTS_INTERVAL.getConfigurationKey()));
            }

            if (pluginConfiguration.has(ConfigurationKey.COMPRESS_EVENTS.getConfigurationKey())) {
                configurationBuilder.withCompressEvents(
                    pluginConfiguration.getBoolean(ConfigurationKey.COMPRESS_EVENTS.getConfigurationKey()));
            }

            if (pluginConfiguration.has(ConfigurationKey.TRACK_APP_EXCEPTION_EVENTS.getConfigurationKey())) {
                configurationBuilder.withTrackAppExceptionEvents(pluginConfiguration
                    .getBoolean(ConfigurationKey.TRACK_APP_EXCEPTION_EVENTS.getConfigurationKey()));
            }
        } catch (JSONException exception) {
            throw new AnalyticsException(
                "Unable to read appId or endpoint from the amplify configuration json.", exception,
                "Make sure amplifyconfiguration.json is a valid json object in expected format. " +
                    "Please take a look at the documentation for expected format of amplifyconfiguration.json."
            );
        }

        AWSClickstreamPluginConfiguration clickstreamPluginConfiguration = configurationBuilder.build();
        ClickstreamManager clickstreamManager = ClickstreamManagerFactory.create(
            context,
            clickstreamPluginConfiguration
        );
        this.analyticsClient = clickstreamManager.getAnalyticsClient();

        LOG.debug("AWSClickstreamPlugin create AutoEventSubmitter.");
        autoEventSubmitter = new AutoEventSubmitter(clickstreamPluginConfiguration.getSendEventsInterval());
        autoEventSubmitter.start();

        activityLifecycleManager = new ActivityLifecycleManager(clickstreamManager);
        activityLifecycleManager.startLifecycleTracking(this.context, ProcessLifecycleOwner.get().getLifecycle());
    }

    @Override
    public AnalyticsClient getEscapeHatch() {
        return analyticsClient;
    }

    @NonNull
    @Override
    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Clickstream configuration in amplifyconfiguration.json contains following values.
     */
    public enum ConfigurationKey {

        /**
         * The Clickstream appId.
         */
        APP_ID("appId"),

        /**
         * the Clickstream Endpoint.
         */
        ENDPOINT("endpoint"),

        /**
         * Time interval after which the events are automatically submitted to server.
         */
        SEND_EVENTS_INTERVAL("sendEventsInterval"),

        /**
         * Whether to compress events.
         */
        COMPRESS_EVENTS("isCompressEvents"),

        /**
         * Whether to track app exception events automatically.
         */
        TRACK_APP_EXCEPTION_EVENTS("isTrackAppExceptionEvents");

        /**
         * The key this property is listed under in the config JSON.
         */
        private final String configurationKey;

        /**
         * Construct the enum with the config key.
         *
         * @param configurationKey The key this property is listed under in the config JSON.
         */
        ConfigurationKey(final String configurationKey) {
            this.configurationKey = configurationKey;
        }

        /**
         * Returns the key this property is listed under in the config JSON.
         *
         * @return The key as a string
         */
        public String getConfigurationKey() {
            return configurationKey;
        }
    }
}

