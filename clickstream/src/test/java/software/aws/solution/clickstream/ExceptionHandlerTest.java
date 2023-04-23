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

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import software.aws.solution.clickstream.client.ClickstreamManager;
import software.aws.solution.clickstream.client.Event;
import software.aws.solution.clickstream.client.db.ClickstreamDBUtil;
import software.aws.solution.clickstream.util.ReflectUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the ClickstreamExceptionHandler.
 */
@RunWith(RobolectricTestRunner.class)
public class ExceptionHandlerTest {

    private ClickstreamDBUtil dbUtil;

    /**
     * prepare start up environment and context.
     *
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
        });
        dbUtil = new ClickstreamDBUtil(context);

        AWSClickstreamPluginConfiguration.Builder configurationBuilder = AWSClickstreamPluginConfiguration.builder();
        configurationBuilder.withAppId("demo-app")
            .withEndpoint("http://cs-se-serve-1qtj719j88vwn-1291141553.ap-southeast-1.elb.amazonaws.com/collect")
            .withSendEventsInterval(10000).withTrackAppLifecycleEvents(false);
        AWSClickstreamPluginConfiguration clickstreamPluginConfiguration = configurationBuilder.build();
        ClickstreamManager clickstreamManager =
            ClickstreamManagerFactory.create(context, clickstreamPluginConfiguration);
        ReflectUtil.invokeMethod(clickstreamManager, "enableTrackAppException");
    }

    /**
     * test exception for record _app_exception event.
     *
     * @throws Exception                exception
     * @throws IllegalArgumentException exception
     */
    @Test
    public void testExceptionRecord() throws Exception {
        Thread testThread = new Thread() {
            public void run() {
                throw new IllegalArgumentException("test exception");
            }
        };
        testThread.start();
        testThread.join();

        assertEquals(1, dbUtil.getTotalNumber());

        try (Cursor cursor = dbUtil.queryAllEvents()) {
            cursor.moveToNext();
            String eventString = cursor.getString(2);
            JSONObject jsonObject = new JSONObject(eventString);
            String eventName = jsonObject.getString("event_type");
            assertEquals(eventName, Event.PresetEvent.APP_EXCEPTION);

            JSONObject attributes = jsonObject.getJSONObject("attributes");
            assertNotNull(attributes.getString("exception_message"));
            assertNotNull(attributes.getString("exception_stack"));
            assertEquals("test exception", attributes.getString("exception_message"));
            assertTrue(
                attributes.getString("exception_stack").contains("java.lang.IllegalArgumentException: test exception"));
            assertTrue(attributes.getString("exception_stack").contains("ExceptionHandlerTest.java:"));
        }
    }

    /**
     * close db.
     */
    @After
    public void tearDown() {
        dbUtil.closeDB();
    }

}
