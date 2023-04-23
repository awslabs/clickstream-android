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

package software.aws.solution.clickstream;

import android.content.Context;
import android.database.Cursor;
import androidx.test.core.app.ApplicationProvider;

import com.amazonaws.logging.Log;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.ClickstreamContext;
import software.aws.solution.clickstream.client.ClickstreamManager;
import software.aws.solution.clickstream.client.Event;
import software.aws.solution.clickstream.client.Session;
import software.aws.solution.clickstream.client.SessionClient;
import software.aws.solution.clickstream.client.db.ClickstreamDBUtil;
import software.aws.solution.clickstream.util.ReflectUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link software.aws.solution.clickstream.client.SessionClient}.
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
        JSONObject attributes = jsonObject.getJSONObject("attributes");
        assertNotNull(attributes.getString("_session_id"));
        assertNotNull(attributes.getString("_session_start_timestamp"));
        assertNotNull(attributes.getString("_session_duration"));
        assertFalse(attributes.has("_session_stop_timestamp"));
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
        JSONObject attributes = jsonObject.getJSONObject("attributes");
        assertNotNull(attributes.getString("_session_id"));
        assertNotNull(attributes.getString("_session_start_timestamp"));
        assertNotNull(attributes.getString("_session_duration"));
        assertFalse(attributes.has("_session_stop_timestamp"));

        cursor.moveToNext();
        eventString = cursor.getString(2);
        jsonObject = new JSONObject(eventString);
        attributes = jsonObject.getJSONObject("attributes");
        assertNotNull(attributes.getString("_session_id"));
        assertNotNull(attributes.getString("_session_start_timestamp"));
        assertNotNull(attributes.getString("_session_duration"));
        assertNotNull(attributes.getString("_session_stop_timestamp"));
        cursor.close();
    }

    /**
     * test handleFirstOpen method.
     *
     * @throws Exception exception.
     */
    @Test
    public void testHandleFirstOpen() throws Exception {
        client.handleFirstOpen();
        assertEquals(1, dbUtil.getTotalNumber());
        Cursor cursor = dbUtil.queryAllEvents();
        cursor.moveToFirst();
        String eventString = cursor.getString(2);
        JSONObject jsonObject = new JSONObject(eventString);
        String eventType = jsonObject.getString("event_type");
        assertEquals(Event.PresetEvent.FIRST_OPEN, eventType);
        cursor.close();
    }

    /**
     * test execute handleFirstOpen method multi times.
     *
     * @throws Exception exception.
     */
    @Test
    public void testHandleFirstOpenMultiTimes() throws Exception {
        client.handleFirstOpen();
        client.handleFirstOpen();
        client.handleFirstOpen();
        assertEquals(1, dbUtil.getTotalNumber());
        Cursor cursor = dbUtil.queryAllEvents();
        cursor.moveToFirst();
        String eventString = cursor.getString(2);
        JSONObject jsonObject = new JSONObject(eventString);
        String eventType = jsonObject.getString("event_type");
        assertEquals(Event.PresetEvent.FIRST_OPEN, eventType);
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
