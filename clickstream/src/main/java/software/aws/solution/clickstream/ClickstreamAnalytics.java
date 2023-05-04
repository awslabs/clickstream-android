/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import android.app.Application;
import androidx.annotation.NonNull;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.core.Amplify;

import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.ClickstreamConfiguration;
import software.aws.solution.clickstream.client.Event;

/**
 * This is the top-level customer-facing interface to The ClickstreamAnalytics.
 */
public final class ClickstreamAnalytics {

    private ClickstreamAnalytics() {
        throw new UnsupportedOperationException("No instances allowed.");
    }

    /**
     * Init ClickstreamAnalytics Plugin.
     *
     * @param context ApplicationContext
     * @throws AmplifyException Exception of init.
     */
    public static void init(@NonNull Application context) throws AmplifyException {
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
     * add user clickstreamAttribute.
     *
     * @param clickstreamAttribute the global clickstreamAttribute.
     */
    public static void addGlobalAttributes(ClickstreamAttribute clickstreamAttribute) {
        Amplify.Analytics.registerGlobalProperties(clickstreamAttribute.getAttributes());
    }

    /**
     * delete global attributes.
     *
     * @param attributeName the attribute name to delete.
     */
    public static void deleteGlobalAttributes(@NonNull String... attributeName) {
        Amplify.Analytics.unregisterGlobalProperties(attributeName);
    }

    /**
     * add user attributes.
     *
     * @param userProfile user
     */
    public static void addUserAttributes(ClickstreamUserAttribute userProfile) {
        Amplify.Analytics.identifyUser(Event.ReservedAttribute.USER_ID_UNSET, userProfile);
    }

    /**
     * set user id.
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
     * get clickstream configuration
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
}
