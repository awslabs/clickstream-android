/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import software.aws.solution.clickstream.client.AnalyticsClient;
import software.aws.solution.clickstream.client.ClickstreamManager;
import software.aws.solution.clickstream.client.Event;
import software.aws.solution.clickstream.util.ReflectUtil;

import java.util.Map;
import java.util.Objects;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AnalyticsClientTest {

    private AnalyticsClient analyticsClient;
    private Map<String, Object> globalAttributes;
    private JSONObject userAttributes;
    private String exceedLengthName = "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghij";
    private final String invalidName = "1_goods_expose";
    private String exceedLengthValue = "";

    /**
     * prepare eventRecorder and context.
     *
     * @throws Exception exception.
     */
    @Before
    public void setup() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        AWSClickstreamPluginConfiguration.Builder configurationBuilder = AWSClickstreamPluginConfiguration.builder();
        configurationBuilder.withAppId("demo-app")
            .withEndpoint("http://cs-se-serve-1qtj719j88vwn-1291141553.ap-southeast-1.elb.amazonaws.com/collect")
            .withSendEventsInterval(10000).withTrackAppLifecycleEvents(false);
        AWSClickstreamPluginConfiguration clickstreamPluginConfiguration = configurationBuilder.build();
        ClickstreamManager clickstreamManager =
            ClickstreamManagerFactory.create(context, clickstreamPluginConfiguration);
        analyticsClient = clickstreamManager.getAnalyticsClient();

        globalAttributes = (Map<String, Object>) ReflectUtil.getFiled(analyticsClient, "globalAttributes");
        userAttributes = (JSONObject) ReflectUtil.getFiled(analyticsClient, "userAttributes");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            sb.append(exceedLengthName);
        }
        exceedLengthValue = sb.toString();
        exceedLengthName = exceedLengthName + "1";
    }

    /**
     * test create event when event name exceed the max length or event name was not valid.
     */
    @Test
    public void testCreateEventException() {
        try {
            analyticsClient.createEvent(exceedLengthName);
        } catch (Exception exception) {
            Assert.assertEquals("The event name passed into create event was too long", exception.getMessage());
        }
        try {
            analyticsClient.createEvent(invalidName);
        } catch (Exception exception) {
            Assert.assertEquals("The event name was not valid", exception.getMessage());
        }
    }

    /**
     * test add global attribute when success.
     */
    @Test
    public void testAddGlobalAttributeWhenSuccess() {
        analyticsClient.addGlobalAttribute("channel", "HUAWEI");
        Assert.assertTrue(globalAttributes.containsKey("channel"));
        Assert.assertEquals("HUAWEI", Objects.requireNonNull(globalAttributes.get("channel")));
    }

    /**
     * test add global attribute when error.
     */
    @Test
    public void testAddGlobalAttributeWhenError() {
        analyticsClient.addGlobalAttribute(exceedLengthName, "value");
        Assert.assertTrue(globalAttributes.containsKey("_error_name_length_exceed"));
        Assert.assertTrue(Objects.requireNonNull(globalAttributes.get("_error_name_length_exceed")).toString()
            .contains(exceedLengthName));

        analyticsClient.addGlobalAttribute(invalidName, "value");
        Assert.assertTrue(globalAttributes.containsKey("_error_name_invalid"));
        Assert.assertTrue(
            Objects.requireNonNull(globalAttributes.get("_error_name_invalid")).toString().contains(invalidName));

        analyticsClient.addGlobalAttribute("name01", exceedLengthValue);
        Assert.assertTrue(globalAttributes.containsKey("_error_value_length_exceed"));
        Assert.assertTrue(
            Objects.requireNonNull(globalAttributes.get("_error_value_length_exceed")).toString().contains("name01"));
        Assert.assertTrue(Objects.requireNonNull(globalAttributes.get("_error_value_length_exceed")).toString()
            .contains("attribute value:"));
    }

    /**
     * test delete global attribute.
     */
    @Test
    public void deleteGlobalAttribute() {
        analyticsClient.addGlobalAttribute("name01", "value");
        analyticsClient.addGlobalAttribute("name02", "value1");
        analyticsClient.deleteGlobalAttribute("name01");
        Assert.assertTrue(globalAttributes.containsKey("name02"));
        Assert.assertFalse(globalAttributes.containsKey("name01"));
    }

    /**
     * test add global attribute for same name multi times, the value of the global attribute ame will
     * covered by the last value.
     */
    @Test
    public void testAddGlobalAttributeSameNameMultiTimes() {
        for (int i = 0; i < 501; i++) {
            analyticsClient.addGlobalAttribute("name", "value" + i);
        }
        Assert.assertFalse(globalAttributes.containsKey("_error_attribute_size_exceed"));
        Assert.assertEquals(1, globalAttributes.size());
        Assert.assertEquals("value500", Objects.requireNonNull(globalAttributes.get("name")).toString());
    }

    /**
     * test add user attribute when success.
     *
     * @throws JSONException exception
     */
    @Test
    public void testAddUserAttributeWhenSuccess() throws JSONException {
        analyticsClient.addUserAttribute("_user_age", 18);
        Assert.assertTrue(userAttributes.has("_user_age"));
        Assert.assertEquals(18, ((JSONObject) userAttributes.get("_user_age")).get("value"));
        Assert.assertTrue(System.currentTimeMillis() -
            (Long) (((JSONObject) userAttributes.get("_user_age")).get("set_timestamp")) < 1000);
    }

    /**
     * test add user attribute when error.
     */
    @Test
    public void testAddUserAttributeWhenError() {
        analyticsClient.addUserAttribute(exceedLengthName, "value");
        Assert.assertTrue(globalAttributes.containsKey("_error_name_length_exceed"));
        Assert.assertTrue(Objects.requireNonNull(globalAttributes.get("_error_name_length_exceed")).toString()
            .contains(exceedLengthName));

        analyticsClient.addUserAttribute(invalidName, "value");
        Assert.assertTrue(globalAttributes.containsKey("_error_name_invalid"));
        Assert.assertTrue(
            Objects.requireNonNull(globalAttributes.get("_error_name_invalid")).toString().contains(invalidName));

        analyticsClient.addUserAttribute("name01", exceedLengthValue);
        Assert.assertTrue(globalAttributes.containsKey("_error_value_length_exceed"));
        Assert.assertTrue(
            Objects.requireNonNull(globalAttributes.get("_error_value_length_exceed")).toString().contains("name01"));
        Assert.assertTrue(Objects.requireNonNull(globalAttributes.get("_error_value_length_exceed")).toString()
            .contains("attribute value:"));
    }

    /**
     * test add user attribute when exceed max number limit.
     */
    @Test
    public void testAddUserAttributeWhenExceedNumberLimit() {
        for (int i = 0; i < 101; i++) {
            analyticsClient.addUserAttribute("name" + i, "value" + i);
        }
        Assert.assertTrue(globalAttributes.containsKey("_error_attribute_size_exceed"));
        Assert.assertEquals("attribute name: name99",
            Objects.requireNonNull(globalAttributes.get("_error_attribute_size_exceed")).toString());
    }

    /**
     * test add user attribute for same name multi times, the value of the user attribute ame will
     * covered by the last value.
     *
     * @throws JSONException exception
     */
    @Test
    public void testAddUserAttributeSameNameMultiTimes() throws JSONException {
        for (int i = 0; i < 101; i++) {
            analyticsClient.addUserAttribute("name", "value" + i);
        }
        Assert.assertFalse(globalAttributes.containsKey("_error_attribute_size_exceed"));
        Assert.assertEquals(2, userAttributes.length());
        Assert.assertEquals("value100", ((JSONObject) userAttributes.get("name")).get("value"));
    }

    /**
     * test add global attribute for null value and verify the global attribute is deleted.
     */
    @Test
    public void testAddGlobalAttributeForNullValue() {
        analyticsClient.addGlobalAttribute("Channel", "HUAWEI");
        Assert.assertTrue(globalAttributes.containsKey("Channel"));
        analyticsClient.addGlobalAttribute("Channel", null);
        Assert.assertFalse(globalAttributes.containsKey("Channel"));
    }

    /**
     * test add user attribute for null value and verify the user attribute is deleted.
     */
    @Test
    public void testAddUserAttributeForNullValue() {
        analyticsClient.addUserAttribute("UserAge", 20);
        Assert.assertTrue(userAttributes.has("UserAge"));
        analyticsClient.addUserAttribute("UserAge", null);
        Assert.assertFalse(userAttributes.has("UserAge"));
    }

    /**
     * test add global attribute for null value when reached max length,
     * and verify the global attribute is deleted.
     */
    @Test
    public void testAddGlobalAttributeForNullValueWhenReachedMaxLength() {
        for (int i = 0; i < 500; i++) {
            analyticsClient.addGlobalAttribute("name" + i, "value" + i);
        }
        Assert.assertTrue(globalAttributes.containsKey("name0"));
        analyticsClient.addGlobalAttribute("name0", null);
        Assert.assertFalse(globalAttributes.containsKey("name0"));
        Assert.assertEquals(499, globalAttributes.size());
    }

    /**
     * test add user attribute for null value when reached max length,
     * and verify the user attribute is deleted.
     */
    @Test
    public void testAddUserAttributeForNullValueWhenReachedMaxLength() {
        for (int i = 0; i < 100; i++) {
            analyticsClient.addUserAttribute("name" + i, "value" + i);
        }
        Assert.assertTrue(userAttributes.has("name0"));
        analyticsClient.addUserAttribute("name0", null);
        Assert.assertFalse(userAttributes.has("name0"));
        Assert.assertEquals(99, userAttributes.length());
    }

    /**
     * test delete and non-existing global attribute.
     */
    @Test
    public void deleteNonExistingGlobalAttribute() {
        for (int i = 0; i < 500; i++) {
            analyticsClient.addGlobalAttribute("name" + i, "value" + i);
        }
        analyticsClient.addGlobalAttribute("name1000", null);
        Assert.assertEquals(500, globalAttributes.size());
    }

    /**
     * test delete and non-existing user attribute.
     */
    @Test
    public void deleteNonExistingUserAttribute() {
        for (int i = 0; i < 100; i++) {
            analyticsClient.addUserAttribute("name" + i, "value" + i);
        }
        analyticsClient.addUserAttribute("name1000", null);
        Assert.assertEquals(100, userAttributes.length());
    }

    /**
     * test get user attribute from storage when next sdk init.
     *
     * @throws Exception exception
     */
    @Test
    public void testUserAttributeForStorage() throws Exception {
        analyticsClient.addUserAttribute("user_name", "carl");
        analyticsClient.addUserAttribute("_user_id", "10837409");
        analyticsClient.addUserAttribute("user_age", 21);
        analyticsClient.addUserAttribute("isNew", true);
        analyticsClient.addUserAttribute("score", 85.5);
        analyticsClient.updateUserAttribute();
        Context context = ApplicationProvider.getApplicationContext();

        ClickstreamManager clickstreamManager =
            ClickstreamManagerFactory.create(context, AWSClickstreamPluginConfiguration
                .builder().withAppId("demo-app").withEndpoint("http://example.com/collect").build());
        JSONObject userAttributesFromStorage =
            (JSONObject) ReflectUtil.getFiled(clickstreamManager.getAnalyticsClient(), "userAttributes");
        Assert.assertEquals(6, userAttributesFromStorage.length());
        Assert.assertEquals("carl", ((JSONObject) userAttributesFromStorage.get("user_name")).getString("value"));
        Assert.assertEquals("10837409", ((JSONObject) userAttributesFromStorage.get("_user_id")).getString("value"));
        Assert.assertEquals(21, ((JSONObject) userAttributesFromStorage.get("user_age")).getInt("value"));
        Assert.assertTrue(((JSONObject) userAttributesFromStorage.get("isNew")).getBoolean("value"));
        Assert.assertEquals(85.5, ((JSONObject) userAttributesFromStorage.get("score")).getDouble("value"), 0.01);
        long userFirstTouchTimestamp =
            ((JSONObject) userAttributesFromStorage.get(Event.ReservedAttribute.USER_FIRST_TOUCH_TIMESTAMP)).getLong(
                "value");
        Assert.assertTrue(userFirstTouchTimestamp - System.currentTimeMillis() < 500);
    }

    /**
     * test initial value in AnalyticsClient.
     *
     * @throws Exception exception
     */
    @Test
    public void testInitialValueInAnalyticsClient() throws Exception {
        String userId = (String) ReflectUtil.getFiled(analyticsClient, "userId");
        String userUniqueId = (String) ReflectUtil.getFiled(analyticsClient, "userUniqueId");
        Assert.assertEquals("", userId);
        Assert.assertNotNull(userUniqueId);

        userAttributes = (JSONObject) ReflectUtil.getFiled(analyticsClient, "userAttributes");
        Assert.assertTrue(userAttributes.has(Event.ReservedAttribute.USER_FIRST_TOUCH_TIMESTAMP));
    }

    /**
     * test update same userId twice.
     *
     * @throws Exception exception
     */
    @Test
    public void testUpdateSameUserIdTwice() throws Exception {
        String userIdForA = "aaa";
        String userUniqueId = (String) ReflectUtil.getFiled(analyticsClient, "userUniqueId");
        analyticsClient.updateUserId(userIdForA);
        analyticsClient.addUserAttribute("user_age", 12);
        analyticsClient.updateUserId(userIdForA);
        userAttributes = (JSONObject) ReflectUtil.getFiled(analyticsClient, "userAttributes");
        Assert.assertTrue(userAttributes.has("user_age"));
        Assert.assertEquals(userUniqueId, ReflectUtil.getFiled(analyticsClient, "userUniqueId"));
    }

    /**
     * test update different userId.
     *
     * @throws Exception exception
     */
    @Test
    public void testUpdateDifferentUserId() throws Exception {
        String userIdForA = "aaa";
        String userIdForB = "bbb";
        String userUniqueId = (String) ReflectUtil.getFiled(analyticsClient, "userUniqueId");
        analyticsClient.updateUserId(userIdForA);
        analyticsClient.addUserAttribute("user_age", 12);
        analyticsClient.updateUserId(userIdForB);
        userAttributes = (JSONObject) ReflectUtil.getFiled(analyticsClient, "userAttributes");
        Assert.assertFalse(userAttributes.has("user_age"));
        Assert.assertNotEquals(userUniqueId, ReflectUtil.getFiled(analyticsClient, "userUniqueId"));
    }

    /**
     * test change to origin user.
     *
     * @throws Exception exception
     */
    @Test
    public void testChangeToOriginUserId() throws Exception {
        String userIdForA = "aaa";
        String userIdForB = "bbb";
        String userUniqueId = (String) ReflectUtil.getFiled(analyticsClient, "userUniqueId");
        analyticsClient.updateUserId(userIdForA);
        analyticsClient.updateUserId(userIdForB);
        String userUniqueIdForB = (String) ReflectUtil.getFiled(analyticsClient, "userUniqueId");
        analyticsClient.updateUserId(userIdForA);
        Assert.assertEquals(userUniqueId, ReflectUtil.getFiled(analyticsClient, "userUniqueId"));
        analyticsClient.updateUserId(userIdForB);
        Assert.assertEquals(userUniqueIdForB, ReflectUtil.getFiled(analyticsClient, "userUniqueId"));
    }

    /**
     * tearDown.
     */
    @After
    public void tearDown() {
    }

}
