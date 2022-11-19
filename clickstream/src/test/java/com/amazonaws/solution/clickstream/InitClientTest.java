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

import com.amplifyframework.AmplifyException;

import com.amazonaws.solution.clickstream.client.AnalyticsClient;
import com.amazonaws.solution.clickstream.client.ClickstreamContext;
import com.amazonaws.solution.clickstream.client.ClickstreamManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class InitClientTest {
    private AWSClickstreamPluginConfiguration clickstreamPluginConfiguration = null;

    /**
     * init the clickstreamPluginConfiguration.
     */
    @Before
    public void init() {
        AWSClickstreamPluginConfiguration.Builder configurationBuilder =
            AWSClickstreamPluginConfiguration.builder();
        configurationBuilder
            .withEndpoint("http://click-serve-HCJIDWGD3S9F-1166279006.ap-southeast-1.elb.amazonaws.com/collect")
            .withSendEventsInterval(15000)
            .withTrackAppLifecycleEvents(false);
        clickstreamPluginConfiguration = configurationBuilder.build();
    }

    /**
     * test the init process.
     * with the clickstreamPluginConfiguration to init analyticsClient and clickstreamContext.
     */
    @Test
    public void initClientParam() {
        Activity activity = mock(Activity.class);
        ClickstreamManager clickstreamManager = ClickstreamManagerFactory.create(
            activity.getApplicationContext(),
            clickstreamPluginConfiguration
        );
        AnalyticsClient analyticsClient = clickstreamManager.getAnalyticsClient();
        ClickstreamContext clickstreamContext = clickstreamManager.getClickstreamContext();

        Assert.assertNotNull(analyticsClient);
        Assert.assertNotNull(clickstreamContext);

        Assert.assertEquals(clickstreamContext.getClickstreamConfiguration().getEndpoint(),
            "http://click-serve-HCJIDWGD3S9F-1166279006.ap-southeast-1.elb.amazonaws.com/collect");
        Assert.assertEquals(clickstreamContext.getClickstreamConfiguration().getSendEventsSize(), 100);
        Assert.assertEquals(clickstreamContext.getClickstreamConfiguration().getSendEventsInterval(), 15000);
        Assert.assertTrue(clickstreamContext.getClickstreamConfiguration().isCompressEvents());
        Assert.assertFalse(clickstreamContext.getClickstreamConfiguration().isTrackAppLifecycleEvents());

        Assert.assertEquals(clickstreamContext.getSDKInfo().getName(), "aws-solution-clickstream-sdk");
        Assert.assertEquals(clickstreamContext.getSDKInfo().getVersion(), BuildConfig.VERSION_NAME);
    }

    /**
     * Attempting to call {@link ClickstreamAnalytics#init(android.app.Application)}
     * without a generated configuration file throws an AmplifyException.
     *
     * @throws Exception if the premise of the test is incorrect
     */
    @Test(expected = AmplifyException.class)
    public void testMissingConfigurationFileThrowsAmplifyException() throws Exception {
        Activity activity = mock(Activity.class);
        ClickstreamAnalytics.init(activity.getApplication());
    }

}
