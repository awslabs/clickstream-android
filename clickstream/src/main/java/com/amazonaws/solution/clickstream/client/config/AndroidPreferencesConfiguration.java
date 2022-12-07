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

package com.amazonaws.solution.clickstream.client.config;

import androidx.annotation.NonNull;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import com.amazonaws.solution.clickstream.client.ClickstreamContext;
import com.amazonaws.solution.clickstream.client.system.AndroidPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Android Preferences Configuration.
 */
public class AndroidPreferencesConfiguration {

    private static final Log LOG = LogFactory.getLog(AndroidPreferencesConfiguration.class);
    private static final String CONFIG_KEY = "configuration";
    private final ClickstreamContext context;
    private final Map<String, String> properties = new ConcurrentHashMap<>();

    /**
     * The construct function with parameters.
     *
     * @param context The context of Clickstream plugin.
     */
    AndroidPreferencesConfiguration(@NonNull final ClickstreamContext context) {
        this.context = context;

        // load the configuration
        JSONObject configJson = null;

        final AndroidPreferences preferences = getContext().getSystem().getPreferences();
        if (preferences != null) {
            // load our serialized prefs
            String configurationJsonString = preferences.getString(CONFIG_KEY, null);
            if (configurationJsonString != null) {
                try {
                    configJson = new JSONObject(configurationJsonString);
                } catch (final JSONException jsonException) {
                    // Do not log e due to potential sensitive information.
                    LOG.error("Could not create Json object of Config.");
                }
            }
        }

        // initialize the internal mappings
        updateMappings(configJson);
    }

    /**
     * Get the instance of AndroidPreferencesConfiguration.
     *
     * @param context The context of Clickstream context.
     * @return AndroidPreferencesConfiguration.
     */
    public static AndroidPreferencesConfiguration newInstance(@NonNull final ClickstreamContext context) {
        return new AndroidPreferencesConfiguration(context);
    }

    /**
     * Get the long value of the property name.
     *
     * @param propertyName The name of property.
     * @return The long value of the property name.
     */
    public Long getLong(final String propertyName) {
        Long value = null;
        String valueString = properties.get(propertyName);

        if (valueString != null) {
            try {
                value = Long.decode(valueString);
            } catch (NumberFormatException nfe) {
                // Do not log property due to potential sensitive information.
                LOG.error("Could not get Long for property: " + propertyName);
            }
        }

        return value;
    }

    /**
     * Get the string value of the property name.
     *
     * @param propertyName The name of property.
     * @return The string value of the property name.
     */
    public String getString(final String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Get the integer value of the property name.
     *
     * @param propertyName The name of property.
     * @return The integer value of the property name.
     */
    public Integer getInt(final String propertyName) {
        Integer value = null;
        String valueString = properties.get(propertyName);

        if (valueString != null) {
            try {
                value = Integer.decode(valueString);
            } catch (NumberFormatException nfe) {
                // Do not log property due to potential sensitive information.
                LOG.error("Could not get Integer for property: " + propertyName);
            }
        }

        return value;
    }

    /**
     * Get the double value of the property name.
     *
     * @param propertyName The name of property.
     * @return The double value of the property name.
     */
    public Double getDouble(final String propertyName) {
        Double value = null;
        String valueString = properties.get(propertyName);

        if (valueString != null) {
            try {
                value = Double.parseDouble(valueString);
            } catch (NumberFormatException nfe) {
                // Do not log property due to potential sensitive information.
                LOG.error("Could not get Double for property: " + propertyName);
            }
        }

        return value;
    }

    /**
     * Get the boolean value of the property name.
     *
     * @param propertyName The name of property.
     * @return The boolean value of the property name.
     */
    public Boolean getBoolean(final String propertyName) {
        Boolean value = null;
        String valueString = properties.get(propertyName);

        if (valueString != null) {
            try {
                value = Boolean.parseBoolean(valueString);
            } catch (Exception exception) {
                // Do not log property due to potential sensitive information.
                LOG.error("Could not get Boolean for property: " + propertyName);
            }
        }

        return value;
    }

    /**
     * Get the short value of the property name.
     *
     * @param propertyName The name of property.
     * @return The short value of the property name.
     */
    public Short getShort(final String propertyName) {
        Short value = null;
        String valueString = properties.get(propertyName);

        if (valueString != null) {
            try {
                if (properties.containsKey(propertyName)) {
                    value = Short.decode(valueString);
                }
            } catch (NumberFormatException nfe) {
                // Do not log property due to potential sensitive information.
                LOG.error("Could not get Short for property: " + propertyName);
            }
        }

        return value;
    }

    /**
     * Get the long value of property name.
     *
     * @param propertyName The name of property.
     * @param optValue     The default value when no property name not found.
     * @return The long value of property name.
     */
    public Long optLong(final String propertyName, final Long optValue) {
        Long value = this.getLong(propertyName);
        return (value != null) ? value : optValue;
    }

    /**
     * Get the string value of property name.
     *
     * @param propertyName The name of property.
     * @param optValue     The default value when no property name not found.
     * @return The string value of property name.
     */
    public String optString(final String propertyName, final String optValue) {
        String value = this.getString(propertyName);
        return (value != null) ? value : optValue;
    }

    /**
     * Get the integer value of property name.
     *
     * @param propertyName The name of property.
     * @param optValue     The default value when no property name not found.
     * @return The integer value of property name.
     */
    public Integer optInt(final String propertyName, final Integer optValue) {
        Integer value = this.getInt(propertyName);
        return (value != null) ? value : optValue;
    }

    /**
     * Get the short value of property name.
     *
     * @param propertyName The name of property.
     * @param optValue     The default value when no property name not found.
     * @return The short value of property name.
     */
    public Short optShort(final String propertyName, final Short optValue) {
        Short value = this.getShort(propertyName);
        return (value != null) ? value : optValue;
    }

    /**
     * Get the double value of property name.
     *
     * @param propertyName The name of property.
     * @param optValue     The default value when no property name not found.
     * @return The double value of property name.
     */
    public Double optDouble(final String propertyName, final Double optValue) {
        Double value = this.getDouble(propertyName);
        return (value != null) ? value : optValue;
    }

    /**
     * Get the boolean value of property name.
     *
     * @param propertyName The name of property.
     * @param optValue     The default value when no property name not found.
     * @return The boolean value of property name.
     */
    public Boolean optBoolean(final String propertyName, final Boolean optValue) {
        Boolean value = this.getBoolean(propertyName);
        return (value != null) ? value : optValue;
    }

    /**
     * Update the property map with the JSON key value pairs.
     *
     * @param configJson The Json to add to the map. If null, the internal map
     *                   is empty.
     */
    private void updateMappings(final JSONObject configJson) {
        HashMap<String, String> newProperties = new HashMap<>();

        if (configJson != null) {
            Iterator<?> keys = configJson.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    String value = configJson.getString(key);
                    newProperties.put(key, value);
                } catch (JSONException jsonException) {
                    // Do not log property mappings due to potential sensitive information.
                    LOG.error("Could not update property mappings.");
                }
            }
        }
        // put all new properties in our map
        properties.putAll(newProperties);
    }

    private ClickstreamContext getContext() {
        return this.context;
    }

}


