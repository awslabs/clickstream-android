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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.AnalyticsEvent;
import software.aws.solution.clickstream.client.ClickstreamManager;
import software.aws.solution.clickstream.client.Event;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AnalyticsEventTest {
    private AnalyticsClient analyticsClient = null;

    /**
     * init the analyticsClient.
     */
    @Before
    public void init() {
        analyticsClient = getAnalyticsClient();
    }

    /**
     * get AnalyticsClient instance util method.
     *
     * @return AnalyticsClient
     */
    public static AnalyticsClient getAnalyticsClient() {
        AWSClickstreamPluginConfiguration.Builder configurationBuilder = AWSClickstreamPluginConfiguration.builder();
        configurationBuilder.withEndpoint(
            "http://click-serve-HCJIDWGD3S9F-1166279006.ap-southeast-1.elb.amazonaws.com/collect");
        AWSClickstreamPluginConfiguration clickstreamPluginConfiguration = configurationBuilder.build();
        Context context = ApplicationProvider.getApplicationContext();
        ClickstreamManager clickstreamManager =
            ClickstreamManagerFactory.create(context, clickstreamPluginConfiguration);
        return clickstreamManager.getAnalyticsClient();
    }

    /**
     * test the analyticsClient with createEvent.
     */
    @Test
    public void createEvent() {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        Assert.assertNotNull(event.getEventId());
        Assert.assertEquals(event.getEventType(), "testEvent");
        Assert.assertEquals(event.getSdkName(), "aws-solution-clickstream-sdk");
        Assert.assertNotNull(event.getSdkVersion());
        Assert.assertNotNull(event.getEventTimestamp());
        Assert.assertNotNull(event.getUniqueId());
        Assert.assertNotNull(event.getAppDetails().getAppTitle());
        Assert.assertNotNull(event.getAppDetails().packageName());
        Assert.assertEquals(0, event.getAttributes().length());
    }


    /**
     * test the event Attribute name and value.
     */
    @Test
    public void eventAttribute() {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        StringBuilder eventName = new StringBuilder();
        eventName.append("abcdefghijabcdefghijabcde");
        event.addAttribute("name", "value");
        event.addAttribute(eventName.toString(), "123");
        Assert.assertEquals(event.getStringAttribute("name"), "value");
        Assert.assertNotNull(event.getStringAttribute(eventName.toString()));
    }

    /**
     * test the event attribute reach the limit.
     *
     * @throws JSONException the json exception
     */
    @Test
    public void eventAttributeReachLimit() throws JSONException {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        for (int i = 1; i < 502; i++) {
            event.addAttribute("name" + i, "value" + i);
        }

        Assert.assertEquals(event.getStringAttribute("name11"), "value11");
        Assert.assertEquals(event.getStringAttribute("name500"), "value500");
        Assert.assertNull(event.getStringAttribute("name501"));
        Assert.assertEquals(502, event.getCurrentNumOfAttributes());
        Assert.assertEquals(Event.ErrorCode.ATTRIBUTE_SIZE_EXCEED,
            event.getAttributes().get(Event.ReservedAttribute.ERROR_CODE));
    }

    /**
     * test the event to json.
     */
    @Test
    public void eventToJsonObject() {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        event.addAttribute("name", "amazon aws solution");
        event.addAttribute("height", 30);
        event.addAttribute("double_value", 25.32);
        JSONObject object = event.toJSONObject();
        try {
            Assert.assertEquals(object.getJSONObject("attributes").get("name"), "amazon aws solution");
            Assert.assertEquals(object.getJSONObject("attributes").get("height"), 30);
            Assert.assertEquals(object.getJSONObject("attributes").get("double_value"), 25.32);
            Assert.assertEquals(object.getString("event_type"), "testEvent");
            Assert.assertNotNull(object.getString("event_id"));
            Assert.assertNotNull(object.getString("platform"));
            Assert.assertNotNull(object.getString("model"));
            Assert.assertNotNull(object.getString("carrier"));
        } catch (JSONException error) {
            Assert.fail("Json parse err" + error.getMessage());
        }
    }

    /**
     * test add items reached the max number of items limit.
     *
     * @throws JSONException exception
     */
    @Test
    public void testReachedTheMaxNumOfItems() throws JSONException {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        ClickstreamItem[] items = new ClickstreamItem[Event.Limit.MAX_NUM_OF_ITEMS + 1];
        for (int i = 0; i < 101; i++) {
            items[i] = new ClickstreamItem.Builder()
                .add(ClickstreamAnalytics.Item.ITEM_ID, i)
                .build();
        }
        event.addItems(items);
        JSONObject attributes = event.getAttributes();
        JSONArray eventItems = event.getItems();
        Assert.assertEquals(Event.ErrorCode.ITEM_SIZE_EXCEED, attributes.getInt(Event.ReservedAttribute.ERROR_CODE));
        Assert.assertEquals(Event.Limit.MAX_NUM_OF_ITEMS, eventItems.length());
    }

    /**
     * test add items reached the max number of custom item attribute.
     *
     * @throws JSONException exception
     */
    @Test
    public void testReachedTheMaxNumOfCustomItemAttribute() throws JSONException {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        ClickstreamItem.Builder builder = new ClickstreamItem.Builder()
            .add(ClickstreamAnalytics.Item.ITEM_ID, 123)
            .add(ClickstreamAnalytics.Item.ITEM_NAME, "custom_item");
        for (int i = 0; i < Event.Limit.MAX_NUM_OF_CUSTOM_ITEM_ATTRIBUTE + 1; i++) {
            builder.add("custom_key_" + (i + 1), "custom_value");
        }
        event.addItems(new ClickstreamItem[] {builder.build()});
        JSONObject attributes = event.getAttributes();
        Assert.assertEquals(Event.ErrorCode.ITEM_CUSTOM_ATTRIBUTE_SIZE_EXCEED,
            attributes.getInt(Event.ReservedAttribute.ERROR_CODE));
        JSONArray eventItems = event.getItems();
        Assert.assertEquals(0, eventItems.length());
    }

    /**
     * test add items with item attribute key exceed the max length.
     *
     * @throws JSONException exception
     */
    @Test
    public void testReachedTheMaxLengthOfCustomItemAttributeKey() throws JSONException {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        String exceedLengthName = "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij1";
        ClickstreamItem item = new ClickstreamItem.Builder()
            .add(ClickstreamAnalytics.Item.ITEM_ID, 123)
            .add(exceedLengthName, "test_value")
            .build();
        event.addItems(new ClickstreamItem[] {item});
        JSONObject attributes = event.getAttributes();
        Assert.assertEquals(Event.ErrorCode.ITEM_CUSTOM_ATTRIBUTE_KEY_LENGTH_EXCEED,
            attributes.getInt(Event.ReservedAttribute.ERROR_CODE));
        JSONArray eventItems = event.getItems();
        Assert.assertEquals(0, eventItems.length());
    }

    /**
     * test add items with invalid item attribute key.
     *
     * @throws JSONException exception
     */
    @Test
    public void testAddInvalidCustomItemAttributeKey() throws JSONException {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        ClickstreamItem item = new ClickstreamItem.Builder()
            .add(ClickstreamAnalytics.Item.ITEM_ID, 123)
            .add("01test", "test_value")
            .build();
        event.addItems(new ClickstreamItem[] {item});
        JSONObject attributes = event.getAttributes();
        Assert.assertEquals(Event.ErrorCode.ITEM_CUSTOM_ATTRIBUTE_KEY_INVALID,
            attributes.getInt(Event.ReservedAttribute.ERROR_CODE));
        JSONArray eventItems = event.getItems();
        Assert.assertEquals(0, eventItems.length());
    }


    /**
     * test add items reached the max length of attribute value.
     *
     * @throws JSONException exception
     */
    @Test
    public void testReachedTheMaxLengthOfCustomItemAttributeValue() throws JSONException {
        AnalyticsEvent event = analyticsClient.createEvent("testEvent");
        String str = "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            sb.append(str);
        }
        String exceedLengthValue = sb.toString();
        ClickstreamItem item = new ClickstreamItem.Builder()
            .add(ClickstreamAnalytics.Item.ITEM_ID, 123)
            .add(ClickstreamAnalytics.Item.ITEM_NAME, exceedLengthValue)
            .add("testKey", exceedLengthValue)
            .build();
        event.addItems(new ClickstreamItem[] {item});
        JSONObject attributes = event.getAttributes();
        Assert.assertEquals(Event.ErrorCode.ITEM_ATTRIBUTE_VALUE_LENGTH_EXCEED,
            attributes.getInt(Event.ReservedAttribute.ERROR_CODE));
        JSONArray eventItems = event.getItems();
        Assert.assertEquals(0, eventItems.length());
    }

}
