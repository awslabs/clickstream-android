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

import android.content.Context;
import android.database.Cursor;
import androidx.test.core.app.ApplicationProvider;

import com.amazonaws.logging.Log;
import com.amazonaws.solution.clickstream.client.AnalyticsClient;
import com.amazonaws.solution.clickstream.client.ClickstreamContext;
import com.amazonaws.solution.clickstream.client.ClickstreamManager;
import com.amazonaws.solution.clickstream.client.Session;
import com.amazonaws.solution.clickstream.client.SessionClient;
import com.amazonaws.solution.clickstream.client.db.ClickstreamDBUtil;
import com.amazonaws.solution.clickstream.util.ReflectUtil;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link com.amazonaws.solution.clickstream.client.SessionClient}.
 */
@RunWith(RobolectricTestRunner.class)
public class SessionClientTest {
    private SessionClient client;
    private AnalyticsClient analyticsClient;
    private ClickstreamDBUtil dbUtil;

    /**
     * setup the params.
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
        ClickstreamContext clickstreamContext = clickstreamManager.getClickstreamContext();
        analyticsClient = clickstreamManager.getAnalyticsClient();
        client = new SessionClient(clickstreamContext);

    }

    /**
     * test SessionClient execute startSession() method.
     *
     * @throws Exception exception.
     */
    @Test
    public void testExecuteStart() throws Exception {
        client.startSession();
        Session session = (Session) ReflectUtil.getFiled(client, "session");
        assertNotNull(session);
        Session clientSession = (Session) ReflectUtil.getFiled(analyticsClient, "session");
        assertNotNull(clientSession);
        assertEquals(1, dbUtil.getTotalNumber());
        Cursor cursor = dbUtil.queryAllEvents();
        cursor.moveToFirst();
        String eventString = cursor.getString(2);
        JSONObject jsonObject = new JSONObject(eventString);
        JSONObject sessionObject = jsonObject.getJSONObject("session");
        assertNotNull(sessionObject.getString("id"));
        assertNotNull(sessionObject.getString("startTimestamp"));
        assertNotNull(sessionObject.getString("duration"));
        assertFalse(sessionObject.has("stopTimestamp"));
        cursor.close();
    }

    /**
     * test SessionClient execute stopSession() method.
     *
     * @throws Exception exception.
     */
    @Test
    public void testExecuteStop() throws Exception {
        Log log = mock(Log.class);
        ReflectUtil.modifyFiled(client, "LOG", log);
        client.stopSession();
        Session session = (Session) ReflectUtil.getFiled(client, "session");
        assertNull(session);
        verify(log).info("Session Stop Failed: No session exists.");
    }

    /**
     * test SessionClient execute startSession then execute stopSession() method.
     *
     * @throws Exception exception.
     */
    @Test
    public void testExecuteStartAndStop() throws Exception {
        client.startSession();
        client.stopSession();
        Session session = (Session) ReflectUtil.getFiled(client, "session");
        assertNull(session);
        assertEquals(2, dbUtil.getTotalNumber());
        Cursor cursor = dbUtil.queryAllEvents();
        cursor.moveToFirst();
        String eventString = cursor.getString(2);
        JSONObject jsonObject = new JSONObject(eventString);
        JSONObject sessionObject = jsonObject.getJSONObject("session");
        assertNotNull(sessionObject.getString("id"));
        assertNotNull(sessionObject.getString("startTimestamp"));
        assertNotNull(sessionObject.getString("duration"));
        assertFalse(sessionObject.has("stopTimestamp"));

        cursor.moveToNext();
        eventString = cursor.getString(2);
        jsonObject = new JSONObject(eventString);
        sessionObject = jsonObject.getJSONObject("session");
        assertNotNull(sessionObject.getString("id"));
        assertNotNull(sessionObject.getString("startTimestamp"));
        assertNotNull(sessionObject.getString("duration"));
        assertNotNull(sessionObject.getString("stopTimestamp"));
        cursor.close();
    }

    /**
     * close db.
     */
    @After
    public void tearDown() {
        dbUtil.closeDB();
        client.stopSession();
    }
}
