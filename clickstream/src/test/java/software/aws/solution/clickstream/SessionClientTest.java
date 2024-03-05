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

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.ClickstreamContext;
import software.aws.solution.clickstream.client.ClickstreamManager;
import software.aws.solution.clickstream.client.Session;
import software.aws.solution.clickstream.client.SessionClient;
import software.aws.solution.clickstream.util.ReflectUtil;

import static org.mockito.Mockito.mock;

/**
 * Tests the {@link software.aws.solution.clickstream.client.SessionClient}.
 */
@RunWith(RobolectricTestRunner.class)
public class SessionClientTest {
    private SessionClient client;
    private AnalyticsClient analyticsClient;
    private ClickstreamContext clickstreamContext;

    /**
     * setup the params.
     */
    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();

        AWSClickstreamPluginConfiguration.Builder configurationBuilder = AWSClickstreamPluginConfiguration.builder();
        configurationBuilder.withAppId("demo-app")
            .withEndpoint("http://cs-se-serve-1qtj719j88vwn-1291141553.ap-southeast-1.elb.amazonaws.com/collect")
            .withSendEventsInterval(10000)
            .withSessionTimeOut(1800000L);
        AWSClickstreamPluginConfiguration clickstreamPluginConfiguration = configurationBuilder.build();
        ClickstreamManager clickstreamManager =
            ClickstreamManagerFactory.create(context, clickstreamPluginConfiguration);
        clickstreamContext = clickstreamManager.getClickstreamContext();
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
        boolean isNewSession = client.initialSession();
        Session session = (Session) ReflectUtil.getFiled(client, "session");
        Assert.assertNotNull(session);
        Session clientSession = (Session) ReflectUtil.getFiled(analyticsClient, "session");
        Assert.assertNotNull(clientSession);
        Assert.assertTrue(isNewSession);
    }

    /**
     * test SessionClient execute startSession then execute storeSession() method.
     *
     * @throws Exception exception.
     */
    @Test
    public void testExecuteStartAndStore() throws Exception {
        client.initialSession();
        Session session = (Session) ReflectUtil.getFiled(client, "session");
        Assert.assertTrue(session.isNewSession());

        client.storeSession();
        Session storedSession = (Session) ReflectUtil.getFiled(client, "session");
        Assert.assertFalse(storedSession.isNewSession());
    }


    /**
     * test SessionClient execute twice startSession method without session timeout.
     *
     * @throws Exception exception.
     */
    @Test
    public void testExecuteStartTwiceWithoutSessionTimeout() throws Exception {
        client.initialSession();
        Session session = (Session) ReflectUtil.getFiled(client, "session");
        Assert.assertTrue(session.isNewSession());
        Assert.assertEquals(1, session.getSessionIndex());

        client.storeSession();
        Session storedSession = (Session) ReflectUtil.getFiled(client, "session");
        Assert.assertFalse(storedSession.isNewSession());

        client.initialSession();
        Session newSession = (Session) ReflectUtil.getFiled(client, "session");

        Assert.assertFalse(newSession.isNewSession());
        Assert.assertEquals(session.getSessionID(), newSession.getSessionID());
        Assert.assertEquals(session.getStartTime(), newSession.getStartTime());
        Assert.assertEquals(1, newSession.getSessionIndex());
    }


    /**
     * test SessionClient execute twice startSession method with session timeout.
     *
     * @throws Exception exception.
     */
    @Test
    public void testExecuteStartTwiceWithSessionTimeout() throws Exception {
        client.initialSession();
        Session session = (Session) ReflectUtil.getFiled(client, "session");
        Assert.assertTrue(session.isNewSession());
        Assert.assertEquals(1, session.getSessionIndex());

        client.storeSession();
        Session storedSession = (Session) ReflectUtil.getFiled(client, "session");
        Assert.assertFalse(storedSession.isNewSession());

        clickstreamContext.getClickstreamConfiguration().withSessionTimeoutDuration(0);
        client.initialSession();
        Session newSession = (Session) ReflectUtil.getFiled(client, "session");

        Assert.assertTrue(newSession.isNewSession());
        Assert.assertNotEquals(session.getSessionID(), newSession.getSessionID());
        Assert.assertNotEquals(session.getStartTime(), newSession.getStartTime());
        Assert.assertEquals(2, newSession.getSessionIndex());
    }


    /**
     * test init SessionClient with IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitSessionClientWithNullAnalyticsClient() {
        ClickstreamContext context = mock(ClickstreamContext.class);
        new SessionClient(context);
    }
}
