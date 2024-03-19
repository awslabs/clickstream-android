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
import com.amplifyframework.analytics.AnalyticsException;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Resources;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.ClickstreamContext;
import software.aws.solution.clickstream.client.ClickstreamManager;
import software.aws.solution.clickstream.util.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class InitClientTest {
    private ClickstreamConfiguration clickstreamPluginConfiguration = null;

    /**
     * init the clickstreamPluginConfiguration.
     *
     * @throws Exception Exception
     */
    @Before
    public void init() throws Exception {
        ReflectUtil.makeAmplifyNotConfigured();
        clickstreamPluginConfiguration = ClickstreamConfiguration.getDefaultConfiguration();
        clickstreamPluginConfiguration.withEndpoint(
                "http://click-serve-HCJIDWGD3S9F-1166279006.ap-southeast-1.elb.amazonaws.com/collect")
            .withSendEventsInterval(15000);
    }

    /**
     * test invoke ClickstreamAnalytics constructor will throw UnsupportedOperationException.
     *
     * @throws Exception Exception
     */
    @Test
    public void testNewConstructorException() throws Exception {
        try {
            Constructor<ClickstreamAnalytics> constructor = ClickstreamAnalytics.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            Assert.assertTrue(cause instanceof UnsupportedOperationException);
        }
    }


    /**
     * test the init process.
     * with the clickstreamPluginConfiguration to init analyticsClient and clickstreamContext.
     */
    @Test
    public void testInitClientParam() {
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
     * Attempting to call {@link ClickstreamAnalytics#init(Context)}
     * and test AWSClickstreamPlugin.
     *
     * @throws Exception if the premise of the test is incorrect
     */
    @Test
    public void testPluginAfterSDKInit() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        ClickstreamAnalytics.init(context);
        AWSClickstreamPlugin plugin = (AWSClickstreamPlugin) Amplify.Analytics.getPlugin("awsClickstreamPlugin");
        Assert.assertEquals(plugin.getVersion(), BuildConfig.VERSION_NAME);
        try {
            plugin.configure(null, context);
        } catch (AnalyticsException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Test Plugin with JSONException.
     *
     * @throws Exception if the premise of the test is incorrect
     */
    @Test
    public void testPluginWithJSONException() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        ClickstreamAnalytics.init(context);
        AWSClickstreamPlugin plugin = (AWSClickstreamPlugin) Amplify.Analytics.getPlugin("awsClickstreamPlugin");
        try {
            JSONObject configure = new JSONObject();
            plugin.configure(configure, context);
        } catch (AnalyticsException exception) {
            Assert.assertEquals("No value for appId", Objects.requireNonNull(exception.getCause()).getMessage());
        }
    }

    /**
     * Attempting to call {@link ClickstreamAnalytics#init(Context, ClickstreamConfiguration)}
     * without ClickstreamConfigurations.
     */
    @Test
    public void testInitSDKWithDefaultConfigurations() {
        try {
            ClickstreamAnalytics.init(ApplicationProvider.getApplicationContext(), new ClickstreamConfiguration());
        } catch (AmplifyException exception) {
            Assert.fail();
        }
    }

    /**
     * Attempting to call {@link ClickstreamAnalytics#init(Context, ClickstreamConfiguration)}
     * without amplifyconfiguration.json file.
     */
    @Test
    public void testInitSDKWithoutConfigurationFile() {
        try {
            MockedStatic<Resources> mockedResources = Mockito.mockStatic(Resources.class);
            mockedResources.when(() -> Resources.getRawResourceId(any(Context.class), eq("amplifyconfiguration")))
                .thenThrow(new Resources.ResourceLoadingException("no amplifyconfiguration file", null));
            ClickstreamConfiguration configuration = new ClickstreamConfiguration()
                .withAppId("test123")
                .withEndpoint("http://example.com/collect123");
            ClickstreamAnalytics.init(ApplicationProvider.getApplicationContext(), configuration);
            ClickstreamConfiguration config = ClickstreamAnalytics.getClickStreamConfiguration();
            Assert.assertEquals("test123", config.getAppId());
            Assert.assertEquals("http://example.com/collect123", config.getEndpoint());
            mockedResources.reset();
            mockedResources.clearInvocations();
            mockedResources.close();
        } catch (Exception exception) {
            Assert.fail("test failed with exception:" + exception.getMessage());
        }
    }

    /**
     * Attempting to call {@link ClickstreamAnalytics#init(Context, ClickstreamConfiguration)}
     * with all ClickstreamConfigurations.
     */
    @Test
    public void testInitSDKWithAllConfigurations() {
        ClickstreamAttribute globalAttributes = ClickstreamAttribute.builder()
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
            .withTrackAppExceptionEvents(false)
            .withTrackUserEngagementEvents(false)
            .withTrackScreenViewEvents(false)
            .withSendEventsInterval(12000)
            .withSessionTimeoutDuration(100000)
            .withAuthCookie("testAutoCookie")
            .withInitialGlobalAttributes(globalAttributes);
        try {
            Context context = ApplicationProvider.getApplicationContext();
            ClickstreamAnalytics.init(context, configuration);
            ClickstreamConfiguration config = ClickstreamAnalytics.getClickStreamConfiguration();
            Assert.assertEquals("test123", config.getAppId());
            Assert.assertEquals("http://example.com/collect123", config.getEndpoint());
            Assert.assertEquals(true, config.isLogEvents());
            Assert.assertEquals(false, config.isCompressEvents());
            Assert.assertEquals(false, config.isTrackAppExceptionEvents());
            Assert.assertEquals(false, config.isTrackUserEngagementEvents());
            Assert.assertEquals(false, config.isTrackScreenViewEvents());
            Assert.assertEquals(12000, config.getSendEventsInterval());
            Assert.assertEquals(100000, config.getSessionTimeoutDuration());
            Assert.assertEquals("testAutoCookie", config.getAuthCookie());
        } catch (Exception exception) {
            Assert.fail();
        }
    }

    /**
     * Attempting to call {@link ClickstreamAnalytics#init(Context, ClickstreamConfiguration)}
     * with JSONException.
     *
     * @throws Exception exception
     */
    @Test
    public void testInitSDKWithException() throws Exception {
        ClickstreamConfiguration configuration = spy(new ClickstreamConfiguration());
        configuration.withAppId("test123")
            .withEndpoint("http://example.com/collect123");
        when(configuration.getAppId()).thenThrow(new RuntimeException("Test RuntimeException"));
        ClickstreamAnalytics.init(ApplicationProvider.getApplicationContext(), configuration);
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

    /**
     * the tear down method for test.
     *
     * @throws Exception exception
     */
    @After
    public void tearDown() throws Exception {
        ReflectUtil.makeAmplifyNotConfigured();
    }
}
