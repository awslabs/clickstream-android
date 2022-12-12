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

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import androidx.test.core.app.ApplicationProvider;

import com.amplifyframework.analytics.AnalyticsEvent;
import com.amplifyframework.core.Amplify;

import com.amazonaws.logging.Log;
import com.amazonaws.solution.clickstream.client.AnalyticsClient;
import com.amazonaws.solution.clickstream.client.ClickstreamConfiguration;
import com.amazonaws.solution.clickstream.client.ClickstreamContext;
import com.amazonaws.solution.clickstream.client.EventRecorder;
import com.amazonaws.solution.clickstream.client.db.ClickstreamDBUtil;
import com.amazonaws.solution.clickstream.util.ReflectUtil;
import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.status;
import static com.github.dreamhead.moco.Moco.text;
import static com.github.dreamhead.moco.Moco.uri;
import static com.github.dreamhead.moco.Runner.runner;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class IntegrationTest {
    private static final String COLLECT_SUCCESS = "/collect/success";
    private static final String COLLECT_FAIL = "/collect/fail";
    private static Runner runner;
    private static Handler handler;
    private AWSClickstreamPlugin plugin;
    private ClickstreamDBUtil dbUtil;
    private AnalyticsClient analyticsClient;
    private EventRecorder eventRecorder;

    /**
     * beforeClass to init environment before all test case.
     */
    @BeforeClass
    public static void beforeClass() {
        //config and start server
        final HttpServer server = httpServer(8082);
        server.request(by(uri(COLLECT_SUCCESS))).response(status(200), text("success"));
        server.request(by(uri(COLLECT_FAIL))).response(status(403), text("fail"));
        runner = runner(server);
        runner.start();
    }


    /**
     * setup Amplify get dbUtil and init handler background environment.
     *
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        Application application = mock(Application.class);
        plugin = new AWSClickstreamPlugin(application);
        Boolean isConfigured = (Boolean) ReflectUtil.invokeSuperMethod(Amplify.Analytics, "isConfigured");
        if (!isConfigured) {
            Amplify.addPlugin(plugin);
            Amplify.configure(context);
        } else {
            plugin = (AWSClickstreamPlugin) Amplify.Analytics.getPlugin("awsClickstreamPlugin");
        }
        analyticsClient = plugin.getEscapeHatch();
        assert analyticsClient != null;
        eventRecorder = (EventRecorder) ReflectUtil.getFiled(analyticsClient, "eventRecorder");
        dbUtil = (ClickstreamDBUtil) ReflectUtil.getFiled(eventRecorder, "dbUtil");
    }

    /**
     * test record event with name use ClickstreamAnalytics api and make sure
     * the event has be auto submitted success after handler post massage.
     *
     * @throws Exception exception
     */
    @Test
    public void testRecordEventWithName() throws Exception {
        executeBackground();
        ClickstreamAnalytics.recordEvent("testRecordEventWithName");
        assertEquals(1, dbUtil.getTotalNumber());
        Thread.sleep(2000);
        assertEquals(0, dbUtil.getTotalNumber());
    }

    /**
     * test record one AnalyticsEvent use ClickstreamAnalytics api and
     * make sure the event data is valid from db.
     *
     * @throws Exception exception
     */
    @Test
    public void testRecordOneEvent() throws Exception {
        executeBackground();
        AnalyticsEvent event =
            AnalyticsEvent.builder()
                .name("PasswordReset")
                .addProperty("Channel", "SMS")
                .addProperty("Successful", true)
                .addProperty("ProcessDuration", 792)
                .addProperty("UserAge", 120.3)
                .build();
        ClickstreamAnalytics.recordEvent(event);
        assertEquals(1, dbUtil.getTotalNumber());

        Cursor cursor = dbUtil.queryAllEvents();
        cursor.moveToFirst();
        String eventString = cursor.getString(2);
        JSONObject jsonObject = new JSONObject(eventString);
        JSONObject attribute = jsonObject.getJSONObject("attributes");
        Assert.assertEquals("PasswordReset", jsonObject.getString("event_type"));
        Assert.assertEquals("SMS", attribute.getString("Channel"));
        Assert.assertTrue(attribute.getBoolean("Successful"));
        Assert.assertEquals(792, attribute.getInt("ProcessDuration"));
        Assert.assertEquals(120.3, attribute.getDouble("UserAge"), 0.01);

        Thread.sleep(2000);
        assertEquals(0, dbUtil.getTotalNumber());
        cursor.close();
    }

    /**
     * test record one event use ClickstreamAnalytics api and
     * make sure when event submit fail the event in db has not be deleted.
     *
     * @throws Exception exception
     */
    @Test
    public void testSubmitEventFail() throws Exception {
        executeBackground();
        setRequestPathToFail();
        ClickstreamAnalytics.recordEvent("testRecordEventWithName");
        assertEquals(1, dbUtil.getTotalNumber());
        Thread.sleep(2000);
        assertEquals(1, dbUtil.getTotalNumber());
    }

    /**
     * test record multi event which need to be flush twice, and
     * make sure each flush event has been auto executed successfully.
     *
     * @throws Exception exception
     */
    @Test
    public void testRecordEventWithSubmitterTwice() throws Exception {
        executeBackground();
        Log log = mock(Log.class);
        ReflectUtil.modifyFiled(eventRecorder, "LOG", log);

        AnalyticsEvent.Builder builder = AnalyticsEvent.builder()
            .name("PasswordReset")
            .addProperty("Channel", "SMS")
            .addProperty("Successful", true)
            .addProperty("ProcessDuration", 792)
            .addProperty("UserAge", 120.3);
        String longString = analyticsClient.createEvent("testEvent").toString();
        for (int i = 0; i < 20; i++) {
            builder.addProperty("str" + i, longString);
        }
        AnalyticsEvent event = builder.build();
        for (int i = 0; i < 20; i++) {
            ClickstreamAnalytics.recordEvent(event);
        }
        assertEquals(20, dbUtil.getTotalNumber());
        Thread.sleep(3000);
        assertEquals(0, dbUtil.getTotalNumber());
        verify(log, times(3)).info("deleted event number: 6");
        verify(log).info("reached maxSubmissions: 3");
        verify(log).info("deleted event number: 2");
    }

    /**
     * test enable.
     *
     * @throws Exception exception
     */
    @Test
    public void testEnable() throws Exception {
        AutoEventSubmitter submitter = (AutoEventSubmitter) ReflectUtil.getFiled(plugin, "autoEventSubmitter");
        Log log = mock(Log.class);
        ReflectUtil.modifyFiled(submitter, "LOG", log);

        Amplify.Analytics.enable();
        verify(log).debug("Auto submitting start");
    }

    /**
     * test disable.
     *
     * @throws Exception exception
     */
    @Test
    public void testDisable() throws Exception {
        AutoEventSubmitter submitter = (AutoEventSubmitter) ReflectUtil.getFiled(plugin, "autoEventSubmitter");
        Log log = mock(Log.class);
        ReflectUtil.modifyFiled(submitter, "LOG", log);

        Amplify.Analytics.disable();
        verify(log).debug("Auto submitting stop");
        assertEquals(0, dbUtil.getTotalNumber());
        ClickstreamAnalytics.recordEvent("testRecordEventWithName");
        assertEquals(0, dbUtil.getTotalNumber());
    }

    /**
     * common method to set request path.
     *
     * @throws Exception exception
     */
    private void setRequestPathToFail() throws Exception {
        ClickstreamContext context = (ClickstreamContext) ReflectUtil.getFiled(eventRecorder, "clickstreamContext");
        ClickstreamConfiguration config =
            (ClickstreamConfiguration) ReflectUtil.getFiled(context, "clickstreamConfiguration");
        ReflectUtil.modifyFiled(config, "endpoint", "http://localhost:8082" + IntegrationTest.COLLECT_FAIL);
    }

    /**
     * mock handler.postDelayed() in 1s.
     *
     * @param handler handler
     */
    private void mockHandler(Handler handler) {
        when(handler.postDelayed(any(Runnable.class), anyLong())).thenAnswer(invocation -> {
            Thread.sleep(1000);
            invocation.getArgument(0, Runnable.class).run();
            return null;
        });
    }

    /**
     * make sure the handler executed not in main thread.
     */
    private void executeBackground() {
        new Thread(() -> {
            handler = mock(Handler.class);
            mockHandler(handler);
            AutoEventSubmitter submitter = null;
            try {
                submitter = (AutoEventSubmitter) ReflectUtil.getFiled(plugin, "autoEventSubmitter");
                ReflectUtil.modifyFiled(submitter, "handler", handler);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            assert submitter != null;
            submitter.start();
        }).start();
    }

    /**
     * when each case execute finish we should stop the handler timer.
     *
     * @throws Exception exception
     */
    private void stopThreadSafely() throws Exception {
        AutoEventSubmitter submitter = (AutoEventSubmitter) ReflectUtil.getFiled(plugin, "autoEventSubmitter");
        ReflectUtil.modifyFiled(submitter, "handler", mock(Handler.class));
    }

    /**
     * close db and stop handler executed thread.
     *
     * @throws Exception exception
     */
    @After
    public void tearDown() throws Exception {
        dbUtil.closeDB();
        stopThreadSafely();
    }

    /**
     * after class stop runner.
     */
    @AfterClass
    public static void afterClass() {
        runner.stop();
    }

}