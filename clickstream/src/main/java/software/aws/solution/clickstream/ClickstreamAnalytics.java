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

import com.amplifyframework.AmplifyException;
import com.amplifyframework.core.Amplify;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.ClickstreamConfiguration;
import software.aws.solution.clickstream.client.Event.PresetEvent;
import software.aws.solution.clickstream.client.Event.ReservedAttribute;
import software.aws.solution.clickstream.client.util.ThreadUtil;

/**
 * This is the top-level customer-facing interface to The ClickstreamAnalytics.
 */
public final class ClickstreamAnalytics {
    private static final Log LOG = LogFactory.getLog(ClickstreamAnalytics.class);

    private ClickstreamAnalytics() {
        throw new UnsupportedOperationException("No instances allowed.");
    }

    /**
     * Init ClickstreamAnalytics Plugin.
     *
     * @param context ApplicationContext
     * @throws AmplifyException Exception of init.
     */
    public static void init(@NonNull Context context) throws AmplifyException {
        if (ThreadUtil.notInMainThread()) {
            throw new AmplifyException("Clickstream SDK initialization failed", "Please initialize in the main thread");
        }
        Amplify.addPlugin(new AWSClickstreamPlugin(context));
        Amplify.configure(context);
    }

    /**
     * Use this method to record Event.
     *
     * @param event ClickstreamEvent to record
     */
    public static void recordEvent(@NonNull final ClickstreamEvent event) {
        Amplify.Analytics.recordEvent(event);
    }

    /**
     * Use this method to record Event.
     *
     * @param eventName the event name
     */
    public static void recordEvent(@NonNull final String eventName) {
        Amplify.Analytics.recordEvent(eventName);
    }

    /**
     * Use this method to send events immediately.
     */
    public static void flushEvents() {
        Amplify.Analytics.flushEvents();
    }

    /**
     * Add user clickstreamAttribute.
     *
     * @param clickstreamAttribute the global clickstreamAttribute.
     */
    public static void addGlobalAttributes(ClickstreamAttribute clickstreamAttribute) {
        Amplify.Analytics.registerGlobalProperties(clickstreamAttribute.getAttributes());
    }

    /**
     * Delete global attributes.
     *
     * @param attributeName the attribute name to delete.
     */
    public static void deleteGlobalAttributes(@NonNull String... attributeName) {
        Amplify.Analytics.unregisterGlobalProperties(attributeName);
    }

    /**
     * Add user attributes.
     *
     * @param userProfile user
     */
    public static void addUserAttributes(ClickstreamUserAttribute userProfile) {
        Amplify.Analytics.identifyUser(ReservedAttribute.USER_ID_UNSET, userProfile);
    }

    /**
     * Set user id.
     *
     * @param userId user
     */
    public static void setUserId(String userId) {
        String newUserId = userId;
        if (newUserId == null) {
            newUserId = "";
        }
        Amplify.Analytics.identifyUser(newUserId, new ClickstreamUserAttribute.Builder().build());
    }

    /**
     * Enable clickstream SDK.
     */
    public static void enable() {
        if (ThreadUtil.notInMainThread()) {
            LOG.error("Clickstream SDK enabled failed, please execute in the main thread");
            return;
        }
        Amplify.Analytics.enable();
    }

    /**
     * Disable clickstream SDK.
     */
    public static void disable() {
        if (ThreadUtil.notInMainThread()) {
            LOG.error("Clickstream SDK disabled failed, please execute in the main thread");
            return;
        }
        Amplify.Analytics.disable();
    }

    /**
     * Get clickstream configuration
     * please config it after initialize.
     *
     * @return ClickstreamConfiguration configurationF
     */
    public static ClickstreamConfiguration getClickStreamConfiguration() {
        AnalyticsClient client =
            ((AWSClickstreamPlugin) Amplify.Analytics.getPlugin("awsClickstreamPlugin")).getEscapeHatch();
        assert client != null;
        return client.getClickstreamConfiguration();
    }

    /**
     * Item attributes.
     */
    public static class Item {
        /**
         * key to item id.
         */
        public static final String ITEM_ID = "id";
        /**
         * key to item name.
         */
        public static final String ITEM_NAME = "name";
        /**
         * key to item location id.
         */
        public static final String LOCATION_ID = "location_id";
        /**
         * key to item brand.
         */
        public static final String ITEM_BRAND = "brand";
        /**
         * key to item currency.
         */
        public static final String CURRENCY = "currency";
        /**
         * key to item price.
         */
        public static final String PRICE = "price";
        /**
         * key to item quantity.
         */
        public static final String QUANTITY = "quantity";
        /**
         * key to item creative name.
         */
        public static final String CREATIVE_NAME = "creative_name";
        /**
         * key to item creative slot.
         */
        public static final String CREATIVE_SLOT = "creative_slot";
        /**
         * key to item category.
         */
        public static final String ITEM_CATEGORY = "item_category";
        /**
         * key to item category2.
         */
        public static final String ITEM_CATEGORY2 = "item_category2";
        /**
         * key to item category3.
         */
        public static final String ITEM_CATEGORY3 = "item_category3";
        /**
         * key to item category4.
         */
        public static final String ITEM_CATEGORY4 = "item_category4";
        /**
         * key to item category5.
         */
        public static final String ITEM_CATEGORY5 = "item_category5";
    }

    /**
     * Preset Event.
     */
    public static class Event {

        /**
         * screen view.
         */
        public static final String SCREEN_VIEW = PresetEvent.SCREEN_VIEW;
    }

    /**
     * Preset Attributes.
     */
    public static class Attr {

        /**
         * screen name.
         */
        public static final String SCREEN_NAME = ReservedAttribute.SCREEN_NAME;

        /**
         * screen unique id.
         */
        public static final String SCREEN_UNIQUE_ID = ReservedAttribute.SCREEN_UNIQUE_ID;
    }
}
