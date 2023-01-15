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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;

import com.amazonaws.solution.clickstream.client.ClickstreamManager;
import com.amazonaws.solution.clickstream.client.Event;
import com.amazonaws.solution.clickstream.client.db.ClickstreamDBUtil;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test the AutoRecordEventClient.
 */
@RunWith(RobolectricTestRunner.class)
public class AutoRecordEventClientTest {
    private ClickstreamDBUtil dbUtil;
    private Application.ActivityLifecycleCallbacks callbacks;

    /**
     * prepare AutoRecordEventClient and context.
     */
    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        dbUtil = new ClickstreamDBUtil(context);
        AWSClickstreamPluginConfiguration.Builder configurationBuilder = AWSClickstreamPluginConfiguration.builder();
        configurationBuilder.withAppId("demo-app")
            .withEndpoint("http://cs-se-serve-1qtj719j88vwn-1291141553.ap-southeast-1.elb.amazonaws.com/collect")
            .withSendEventsInterval(10000).withTrackAppLifecycleEvents(false);
        AWSClickstreamPluginConfiguration clickstreamPluginConfiguration = configurationBuilder.build();
        ClickstreamManager clickstreamManager =
            ClickstreamManagerFactory.create(context, clickstreamPluginConfiguration);
        callbacks = new ActivityLifecycleManager(clickstreamManager);
    }


    /**
     * test record user engagement event after view screen more than 1 second.
     *
     * @throws Exception exception.
     */
    @Test
    public void testUserEngagementSuccess() throws Exception {
        Activity activity = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        callbacks.onActivityCreated(activity, bundle);
        callbacks.onActivityStarted(activity);
        callbacks.onActivityResumed(activity);
        Thread.sleep(1500);
        callbacks.onActivityPaused(activity);
        callbacks.onActivityStopped(activity);
        Cursor cursor = dbUtil.queryAllEvents();
        List<String> eventList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String eventString = cursor.getString(2);
            JSONObject jsonObject = new JSONObject(eventString);
            String eventName = jsonObject.getString("event_type");
            eventList.add(eventName);
            if (eventName.equals(Event.PresetEvent.USER_ENGAGEMENT)) {
                JSONObject attributes = jsonObject.getJSONObject("attributes");
                assertTrue(attributes.has("engagement_time_msec"));
                assertTrue(attributes.getInt("engagement_time_msec") > 1000);
            }
        }
        assertTrue(eventList.contains(Event.PresetEvent.USER_ENGAGEMENT));
    }


    /**
     * test record user engagement event after view screen less than 1 second.
     *
     * @throws Exception exception.
     */
    @Test
    public void testUserEngagementFail() throws Exception {
        Activity activity = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        callbacks.onActivityCreated(activity, bundle);
        callbacks.onActivityStarted(activity);
        callbacks.onActivityResumed(activity);
        callbacks.onActivityPaused(activity);
        callbacks.onActivityStopped(activity);
        Cursor cursor = dbUtil.queryAllEvents();
        List<String> eventList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String eventString = cursor.getString(2);
            JSONObject jsonObject = new JSONObject(eventString);
            eventList.add(jsonObject.getString("event_type"));
        }
        assertFalse(eventList.contains(Event.PresetEvent.USER_ENGAGEMENT));
    }

    /**
     * test view only one screen and record an screen view event.
     *
     * @throws Exception exception.
     */
    @Test
    public void testViewOneScreenEvent() throws Exception {
        Activity activity = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        callbacks.onActivityCreated(activity, bundle);
        callbacks.onActivityStarted(activity);
        callbacks.onActivityResumed(activity);
        Cursor cursor = dbUtil.queryAllEvents();
        List<String> eventList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String eventString = cursor.getString(2);
            JSONObject jsonObject = new JSONObject(eventString);
            String eventName = jsonObject.getString("event_type");
            eventList.add(eventName);
            if (eventName.equals(Event.PresetEvent.SCREEN_VIEW)) {
                JSONObject attributes = jsonObject.getJSONObject("attributes");
                assertNotNull(attributes.getString("screen_name"));
                assertNotNull(attributes.getString("screen_id"));
                Assert.assertFalse(attributes.has("previous_screen_name"));
                Assert.assertFalse(attributes.has("previous_screen_id"));
            }
        }
        assertTrue(eventList.contains(Event.PresetEvent.SCREEN_VIEW));
    }

    /**
     * test view two different screen and record last screen view event with
     * previous screen information.
     *
     * @throws Exception exception.
     */
    @Test
    public void testViewTwoScreenEvent() throws Exception {
        Activity activity1 = mock(Activity.class);
        Activity activity2 = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        callbacks.onActivityCreated(activity1, bundle);
        callbacks.onActivityStarted(activity1);
        callbacks.onActivityResumed(activity1);
        callbacks.onActivityPaused(activity1);

        callbacks.onActivityCreated(activity2, bundle);
        callbacks.onActivityStarted(activity2);
        callbacks.onActivityResumed(activity2);

        Cursor cursor = dbUtil.queryAllEvents();
        cursor.moveToLast();
        String eventString = cursor.getString(2);
        JSONObject jsonObject = new JSONObject(eventString);
        String eventName = jsonObject.getString("event_type");
        assertEquals(Event.PresetEvent.SCREEN_VIEW, eventName);
        JSONObject attributes = jsonObject.getJSONObject("attributes");
        assertNotNull(attributes.getString("screen_name"));
        assertNotNull(attributes.getString("screen_id"));
        assertNotNull(attributes.getString("previous_screen_name"));
        assertNotNull(attributes.getString("previous_screen_id"));
        assertEquals(activity2.getClass().getSimpleName(), attributes.getString("screen_name"));
        assertEquals(activity2.getClass().getCanonicalName(), attributes.getString("screen_id"));
        assertEquals(activity1.getClass().getSimpleName(), attributes.getString("previous_screen_name"));
        assertEquals(activity1.getClass().getCanonicalName(), attributes.getString("previous_screen_id"));
    }

    /**
     * close db.
     */
    @After
    public void tearDown() {
        dbUtil.closeDB();
    }
}
