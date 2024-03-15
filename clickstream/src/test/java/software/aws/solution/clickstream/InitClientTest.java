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

import android.app.Activity;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import com.amplifyframework.AmplifyException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.ClickstreamContext;
import software.aws.solution.clickstream.client.ClickstreamManager;

import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class InitClientTest {
    private ClickstreamConfiguration clickstreamPluginConfiguration = null;

    /**
     * init the clickstreamPluginConfiguration.
     */
    @Before
    public void init() {
        clickstreamPluginConfiguration = ClickstreamConfiguration.getDefaultConfiguration();
        clickstreamPluginConfiguration.withEndpoint(
                "http://click-serve-HCJIDWGD3S9F-1166279006.ap-southeast-1.elb.amazonaws.com/collect")
            .withSendEventsInterval(15000);
    }

    /**
     * test the init process.
     * with the clickstreamPluginConfiguration to init analyticsClient and clickstreamContext.
     */
    @Test
    public void initClientParam() {
        Context context = ApplicationProvider.getApplicationContext();
        ClickstreamManager clickstreamManager = new ClickstreamManager(context, clickstreamPluginConfiguration);
        AnalyticsClient analyticsClient = clickstreamManager.getAnalyticsClient();
        ClickstreamContext clickstreamContext = clickstreamManager.getClickstreamContext();

        Assert.assertNotNull(analyticsClient);
        Assert.assertNotNull(clickstreamContext);

        Assert.assertEquals(clickstreamContext.getClickstreamConfiguration().getEndpoint(),
            "http://click-serve-HCJIDWGD3S9F-1166279006.ap-southeast-1.elb.amazonaws.com/collect");
        Assert.assertEquals(clickstreamContext.getClickstreamConfiguration().getSendEventsInterval(), 15000);
        Assert.assertTrue(clickstreamContext.getClickstreamConfiguration().isCompressEvents());
        Assert.assertTrue(clickstreamContext.getClickstreamConfiguration().isTrackScreenViewEvents());
        Assert.assertFalse(clickstreamContext.getClickstreamConfiguration().isTrackAppExceptionEvents());

        Assert.assertEquals(clickstreamContext.getSDKInfo().getName(), "aws-solution-clickstream-sdk");
        Assert.assertEquals(clickstreamContext.getSDKInfo().getVersion(), BuildConfig.VERSION_NAME);
    }

    /**
     * Attempting to call {@link ClickstreamAnalytics#init(Context)}
     * without a null Context throws an NullPointerException.
     *
     * @throws Exception if the premise of the test is incorrect
     */
    @Test(expected = NullPointerException.class)
    public void testContextIsNullWhenInitialize() throws Exception {
        Activity activity = mock(Activity.class);
        ClickstreamAnalytics.init(activity.getApplication());
    }

    /**
     * Attempting to call {@link ClickstreamAnalytics#init(Context, ClickstreamConfiguration)}
     * without ClickstreamConfigurations.
     */
    @Test
    public void testInitSDKWithConfigurations() {
        ClickstreamAttribute attribute = ClickstreamAttribute.builder()
            .add("testKey", "testValue")
            .add("intKey", 12)
            .add("boolKey", true)
            .add("doubleKey", 23.22)
            .build();
        ClickstreamConfiguration configuration = new ClickstreamConfiguration()
            .withAppId("test123")
            .withEndpoint("http://example.com/collect123")
            .withLogEvents(true)
            .withCompressEvents(false)
            .withTrackAppExceptionEvents(true)
            .withTrackUserEngagementEvents(false)
            .withTrackScreenViewEvents(false)
            .withSendEventsInterval(12000)
            .withInitialGlobalAttributes(attribute);
        try {
            ClickstreamAnalytics.init(ApplicationProvider.getApplicationContext(), configuration);
        } catch (AmplifyException exception) {
            Assert.fail();
        }
    }

    /**
     * test init SDK not in main thread.
     *
     * @throws Exception Exception for thread sleep.
     */
    @Test
    public void testInitSDKNotInMainThreadThrowsAmplifyException() throws Exception {
        new Thread(() -> {
            Activity activity = mock(Activity.class);
            try {
                ClickstreamAnalytics.init(activity.getApplication());
                Assert.fail();
            } catch (AmplifyException exception) {
                Assert.assertEquals("Clickstream SDK initialization failed", exception.getMessage());
                Assert.assertEquals("Please initialize in the main thread", exception.getRecoverySuggestion());
            }
        }).start();
        Thread.sleep(500);
    }
}
