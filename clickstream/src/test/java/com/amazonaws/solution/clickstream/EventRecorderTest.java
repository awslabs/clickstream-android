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
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;

import com.amazonaws.logging.Log;
import com.amazonaws.solution.clickstream.client.AnalyticsClient;
import com.amazonaws.solution.clickstream.client.AnalyticsEvent;
import com.amazonaws.solution.clickstream.client.ClickstreamConfiguration;
import com.amazonaws.solution.clickstream.client.ClickstreamContext;
import com.amazonaws.solution.clickstream.client.ClickstreamManager;
import com.amazonaws.solution.clickstream.client.EventRecorder;
import com.amazonaws.solution.clickstream.client.config.AndroidPreferencesConfiguration;
import com.amazonaws.solution.clickstream.client.db.ClickstreamDBUtil;
import com.amazonaws.solution.clickstream.util.ReflectUtil;
import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import org.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.latency;
import static com.github.dreamhead.moco.Moco.status;
import static com.github.dreamhead.moco.Moco.text;
import static com.github.dreamhead.moco.Moco.uri;
import static com.github.dreamhead.moco.Runner.runner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class EventRecorderTest {
    private static final String COLLECT_SUCCESS = "/collect/success";
    private static final String COLLECT_SUCCESS_LATENCY200 = "/collect/success/latency1";
    private static final String COLLECT_SUCCESS_LATENCY2000 = "/collect/success/latency3";
    private static final String COLLECT_FAIL = "/collect/fail";
    private static Runner runner;

    private ClickstreamDBUtil dbUtil;
    private EventRecorder eventRecorder;
    private ClickstreamContext clickstreamContext;
    private AnalyticsEvent event;

    private ExecutorService executorService;
    private Log log;

    /**
     * beforeClass to init environment before all test case.
     */
    @BeforeClass
    public static void beforeClass() {
        //config and start server
        final HttpServer server = httpServer(8082);
        server.request(by(uri(COLLECT_SUCCESS))).response(status(200), text("success"));
        server.request(by(uri(COLLECT_SUCCESS_LATENCY2000)))
            .response(status(200), text("success"), latency(2000, TimeUnit.MILLISECONDS));
        server.request(by(uri(COLLECT_SUCCESS_LATENCY200)))
            .response(status(200), text("success"), latency(200, TimeUnit.MILLISECONDS));
        server.request(by(uri(COLLECT_FAIL))).response(status(403), text("fail"));
        runner = runner(server);
        runner.start();
    }

    /**
     * prepare eventRecorder and context.
     *
     * @throws Exception exception.
     */
    @Before
    public void setup() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        dbUtil = new ClickstreamDBUtil(context);

        AWSClickstreamPluginConfiguration.Builder configurationBuilder = AWSClickstreamPluginConfiguration.builder();
        configurationBuilder.withAppId("demo-app")
            .withEndpoint("http://cs-se-serve-1qtj719j88vwn-1291141553.ap-southeast-1.elb.amazonaws.com/collect")
            .withSendEventsInterval(10000).withTrackAppLifecycleEvents(false);
        AWSClickstreamPluginConfiguration clickstreamPluginConfiguration = configurationBuilder.build();
        ClickstreamManager clickstreamManager =
            ClickstreamManagerFactory.create(context, clickstreamPluginConfiguration);
        AnalyticsClient analyticsClient = clickstreamManager.getAnalyticsClient();
        event = analyticsClient.createEvent("testEvent");
        clickstreamContext = clickstreamManager.getClickstreamContext();

        executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.DiscardPolicy());
        // Using reflection to get the method
        eventRecorder =
            (EventRecorder) ReflectUtil.newInstance(EventRecorder.class, clickstreamContext, dbUtil, executorService);
        log = mock(Log.class);
        ReflectUtil.modifyFiled(eventRecorder, "LOG", log);

    }

    /**
     * test insert single event.
     */
    @Test
    public void testRecordEvent() {
        final Uri uri = eventRecorder.recordEvent(event);
        final int idInserted = Integer.parseInt(uri.getLastPathSegment());
        assertNotEquals(0, idInserted);
        final Cursor c = dbUtil.queryAllEvents();
        assertNotNull(c);
        assertEquals(1, c.getCount());
        c.close();
    }

    /**
     * test getBatchOfEvents for one event.
     *
     * @throws Exception exception.
     */
    @Test
    public void testGetBatchOfEventsForOneEvent() throws Exception {
        eventRecorder.recordEvent(event);
        Cursor cursor = dbUtil.queryAllEvents();
        String[] result = getBatchOfEvents(cursor);
        assertEquals(result.length, 2);
        assertEquals("[" + event.toJSONObject().toString() + "]", result[0]);
        assertEquals("1", result[1]);
        cursor.close();
    }

    /**
     * test getBatchOfEvents for one event reached limit size.
     *
     * @throws Exception exception.
     */
    @Test
    public void testGetBatchOfEventsForOneEventReachedLimitSize() throws Exception {
        String json = event.toString();
        for (int i = 0; i < 300; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        eventRecorder.recordEvent(event);
        Cursor cursor = dbUtil.queryAllEvents();
        String[] result = getBatchOfEvents(cursor);
        assertEquals(result.length, 2);
        assertEquals("", result[0]);
        assertEquals("-1", result[1]);
        cursor.close();
    }

    /**
     * test getBatchOfEvents when not reached default limited size.
     *
     * @throws Exception exception.
     */
    @Test
    public void testGetBatchOfEventsNotReachedDefaultLimitSize() throws Exception {
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        Cursor cursor = dbUtil.queryAllEvents();
        String[] result = getBatchOfEvents(cursor);
        assertEquals(2, result.length);
        assertEquals(new JSONArray(result[0]).length(), 20);
        assertEquals("20", result[1]);
        cursor.close();
    }

    /**
     * test getBatchOfEvents when reached default limited size.
     *
     * @throws Exception exception.
     */
    @Test
    public void testGetBatchOfEventsReachedDefaultLimitSize() throws Exception {
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        Cursor cursor = dbUtil.queryAllEvents();
        String[] result = getBatchOfEvents(cursor);
        assertEquals(2, result.length);
        int length = new JSONArray(result[0]).length();
        assertTrue(length < 20);
        assertEquals(String.valueOf(length), result[1]);
        cursor.close();
    }

    /**
     * test getBatchOfEvents when not reach custom limited size.
     *
     * @throws Exception exception.
     */
    @Test
    public void testGetBatchOfEventsNotReachCustomLimitedSize() throws Exception {
        ClickstreamContext mockContext = mock(ClickstreamContext.class);
        ReflectUtil.modifyFiled(eventRecorder, "clickstreamContext", mockContext);
        AndroidPreferencesConfiguration config = mock(AndroidPreferencesConfiguration.class);
        when(mockContext.getConfiguration()).thenReturn(config);
        when(config.optLong(any(String.class), any(Long.class))).thenReturn(256 * 1024L);
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        Cursor cursor = dbUtil.queryAllEvents();
        String[] result = getBatchOfEvents(cursor);
        assertEquals(2, result.length);
        int length = new JSONArray(result[0]).length();
        assertEquals(length, 20);
        assertEquals(String.valueOf(length), result[1]);
        cursor.close();
    }

    /**
     * test process None event.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessNoneEvent() throws Exception {
        setRequestPath(COLLECT_SUCCESS);
        assertEquals(0, dbUtil.getTotalNumber());
        int totalEventNumber = (int) ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        verify(log).info("No events available to submit.");
        assertEquals(0, totalEventNumber);
    }


    /**
     * test process event when success.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessOneEventSuccess() throws Exception {
        setRequestPath(COLLECT_SUCCESS);
        eventRecorder.recordEvent(event);
        assertEquals(1, dbUtil.getTotalNumber());
        ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(0, dbUtil.getTotalNumber());
    }

    /**
     * test process event when request fail.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessOneEventFail() throws Exception {
        setRequestPath(COLLECT_FAIL);
        eventRecorder.recordEvent(event);
        assertEquals(1, dbUtil.getTotalNumber());
        ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(1, dbUtil.getTotalNumber());
    }

    /**
     * test processEvent() for send multi event success with one request.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessMultiEventSuccess() throws Exception {
        setRequestPath(COLLECT_SUCCESS);
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(20, dbUtil.getTotalNumber());
        ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(0, dbUtil.getTotalNumber());
    }

    /**
     * test processEvent() for send multi event fail with one request.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessMultiEventFail() throws Exception {
        setRequestPath(COLLECT_FAIL);
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(20, dbUtil.getTotalNumber());
        ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(20, dbUtil.getTotalNumber());
    }


    /**
     * test processEvent() for more than one request and not reached default maxSubmissions.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessEventNearReachMaxSubmissions() throws Exception {
        setRequestPath(COLLECT_SUCCESS);
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(20, dbUtil.getTotalNumber());
        int eventNumber = (int) ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(20, eventNumber);
        verify(log).info("deleted event number: 12");
        verify(log).info("deleted event number: 8");
        assertEquals(0, dbUtil.getTotalNumber());

    }

    /**
     * test processEvent() for reached default maxSubmissions and not submit all event.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessEventReachedMaxSubmissions() throws Exception {
        setRequestPath(COLLECT_SUCCESS);
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 40; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(40, dbUtil.getTotalNumber());
        int eventNumber = (int) ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(36, eventNumber);
        verify(log, times(3)).info("deleted event number: 12");
        verify(log).info("reached maxSubmissions: 3");
        assertEquals(4, dbUtil.getTotalNumber());
    }

    /**
     * test processEvent() for custom maxSubmissions for reached default and not reached custom.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessEventForCustomMaxSubmissions() throws Exception {
        setRequestPath(COLLECT_SUCCESS);

        AndroidPreferencesConfiguration config = mock(AndroidPreferencesConfiguration.class);
        ReflectUtil.modifyFiled(clickstreamContext, "configuration", config);
        when(config.optLong(any(String.class), any(Long.class))).thenReturn(128 * 1024L);
        when(config.optInt(any(String.class), any(Integer.class))).thenReturn(4);

        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 40; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(40, dbUtil.getTotalNumber());
        int eventNumber = (int) ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(40, eventNumber);
        verify(log, times(3)).info("deleted event number: 12");
        verify(log).info("deleted event number: 4");
        verify(log, never()).info("reached maxSubmissions: 3");
        assertEquals(0, dbUtil.getTotalNumber());
    }

    /**
     * test processEvent() for multi events with one latency request.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessOneSubmissionsInLatency() throws Exception {
        setRequestPath(COLLECT_SUCCESS_LATENCY2000);
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(20, dbUtil.getTotalNumber());
        int eventNumber = (int) ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(20, eventNumber);
        verify(log).info("deleted event number: 20");
        assertEquals(0, dbUtil.getTotalNumber());
    }

    /**
     * test processEvent() for multi latency request.
     *
     * @throws Exception exception.
     */
    @Test
    public void testProcessEventForMultiSubmissionsInLatency() throws Exception {
        setRequestPath(COLLECT_SUCCESS_LATENCY2000);
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(20, dbUtil.getTotalNumber());
        int eventNumber = (int) ReflectUtil.invokeMethod(eventRecorder, "processEvents");
        assertEquals(20, eventNumber);
        verify(log).info("deleted event number: 12");
        verify(log).info("deleted event number: 8");
        assertEquals(0, dbUtil.getTotalNumber());
    }

    /**
     * test submitEvents() for submit all event once.
     *
     * @throws Exception exception.
     */
    @Test
    public void testSubmitAllEventForOneRequest() throws Exception {
        setRequestPath(COLLECT_SUCCESS_LATENCY200);
        for (int i = 0; i < 20; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(20, dbUtil.getTotalNumber());
        eventRecorder.submitEvents();
        assertEquals(1, ((ThreadPoolExecutor) executorService).getTaskCount());
        Thread.sleep(1500);
        assertEquals(0, dbUtil.getTotalNumber());
    }

    /**
     * test submitEvents() for submit part of event.
     *
     * @throws Exception exception.
     */
    @Test
    public void testSubmitPartOfEventForMultiRequest() throws Exception {
        setRequestPath(COLLECT_SUCCESS_LATENCY200);
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 40; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(40, dbUtil.getTotalNumber());
        eventRecorder.submitEvents();
        assertEquals(1, ((ThreadPoolExecutor) executorService).getTaskCount());
        Thread.sleep(1500);
        verify(log, times(3)).info("deleted event number: 12");
        verify(log).info("reached maxSubmissions: 3");
        assertEquals(4, dbUtil.getTotalNumber());
    }

    /**
     * test submitEvents() for submit all event twice.
     *
     * @throws Exception exception.
     */
    @Test
    public void testSubmitAllEventForMultiRequest() throws Exception {
        setRequestPath(COLLECT_SUCCESS_LATENCY200);
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 40; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(40, dbUtil.getTotalNumber());
        eventRecorder.submitEvents();
        eventRecorder.submitEvents();
        assertEquals(2, ((ThreadPoolExecutor) executorService).getTaskCount());
        assertTrue(((ThreadPoolExecutor) executorService).getActiveCount() < 2);
        Thread.sleep(1500);
        verify(log, times(3)).info("deleted event number: 12");
        verify(log).info("reached maxSubmissions: 3");
        verify(log).info("deleted event number: 4");
        assertEquals(0, dbUtil.getTotalNumber());
    }


    /**
     * test to submitEvents() for three times，and each times with multi request. the last times db is null.
     *
     * @throws Exception exception.
     */
    @Test
    public void testTimerThreeTimesSubmitAllEventForMultiRequest() throws Exception {
        setRequestPath(COLLECT_SUCCESS_LATENCY200);
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 40; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(40, dbUtil.getTotalNumber());
        for (int i = 0; i < 3; i++) {
            Thread.sleep(100);
            eventRecorder.submitEvents();
        }
        Thread.sleep(1500);
        verify(log, times(3)).info("deleted event number: 12");
        verify(log).info("reached maxSubmissions: 3");
        verify(log).info("deleted event number: 4");
        verify(log).info("No events available to submit.");
        assertEquals(0, dbUtil.getTotalNumber());
    }


    /**
     * test to submitEvents() reached the thread pool LinkedBlockingQueue capacity.
     *
     * @throws Exception exception.
     */
    @Test
    public void testSubmitAllEventForReachTheQueueLimit() throws Exception {
        setRequestPath(COLLECT_SUCCESS_LATENCY200);
        String json = event.toString();
        for (int i = 0; i < 10; i++) {
            event.addAttribute("test_json_" + i, json);
        }
        for (int i = 0; i < 120; i++) {
            eventRecorder.recordEvent(event);
        }
        assertEquals(120, dbUtil.getTotalNumber());
        for (int i = 0; i < 1100; i++) {
            eventRecorder.submitEvents();
        }
        assertTrue(((ThreadPoolExecutor) executorService).getActiveCount() < 2);
        assertEquals(1001, ((ThreadPoolExecutor) executorService).getTaskCount());
    }

    /**
     * common method to set request path.
     *
     * @param path request path for mock.
     * @throws Exception exception
     */
    private void setRequestPath(String path) throws Exception {
        ClickstreamContext context = (ClickstreamContext) ReflectUtil.getFiled(eventRecorder, "clickstreamContext");
        ClickstreamConfiguration config =
            (ClickstreamConfiguration) ReflectUtil.getFiled(context, "clickstreamConfiguration");
        ReflectUtil.modifyFiled(config, "endpoint", "http://localhost:8082" + path);
    }

    /**
     * getBatchOfEvents util.
     *
     * @param cursor db Cursor.
     * @return getBatchOfEvents() result.
     * @throws Exception exception.
     */
    private String[] getBatchOfEvents(Cursor cursor) throws Exception {
        cursor.moveToFirst();
        Method method = EventRecorder.class.getDeclaredMethod("getBatchOfEvents", Cursor.class);
        return (String[]) ReflectUtil.invokeMethod(eventRecorder, method, cursor);
    }

    /**
     * close db and stop mock server runner.
     */
    @After
    public void tearDown() {
        dbUtil.closeDB();
        executorService.shutdownNow();
    }

    /**
     * after class stop runner.
     */
    @AfterClass
    public static void afterClass() {
        runner.stop();
    }
}