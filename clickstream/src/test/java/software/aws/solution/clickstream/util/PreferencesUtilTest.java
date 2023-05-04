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

package software.aws.solution.clickstream.util;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import software.aws.solution.clickstream.client.system.AndroidPreferences;
import software.aws.solution.clickstream.client.util.PreferencesUtil;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class PreferencesUtilTest {
    private AndroidPreferences preferences;

    /**
     * setup AndroidPreferences.
     */
    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        preferences = new AndroidPreferences(context,
            context.getApplicationContext().getPackageName() + "294262d4-8dbd-4bfd-816d-0fc81b3d32b7");
    }

    /**
     * test get user attribute is null when not set.
     */
    @Test
    public void testGetUserAttributeWhenNotSet() {
        JSONObject userAttribute = PreferencesUtil.getUserAttribute(preferences);
        assertEquals(0, userAttribute.length());
    }

    /**
     * test get user attribute with json exception.
     */
    @Test
    public void testGetUserAttributeWithJSONException() {
        preferences.putString("clickstream_user_attributes", "{/}");
        JSONObject userAttribute = PreferencesUtil.getUserAttribute(preferences);
        assertEquals(0, userAttribute.length());
    }

    /**
     * test get new user info with json exception.
     */
    @Test
    public void testGetNewUserInfoWithJSONException() {
        preferences.putString("clickstream_user_unique_id", "{/}");
        JSONObject userAttribute = PreferencesUtil.getNewUserInfo(preferences, "3232111");
        assertEquals(0, userAttribute.length());
    }

    /**
     * test store UserAttribute.
     *
     * @throws JSONException exception
     */
    @Test
    public void testStoreUserAttribute() throws JSONException {
        JSONObject userAttribute = new JSONObject();
        userAttribute.put("user_name", "carl");
        userAttribute.put("_user_id", "10837409");
        userAttribute.put("user_age", 21);
        userAttribute.put("null", true);
        userAttribute.put("score", 85.5);
        PreferencesUtil.updateUserAttribute(preferences, userAttribute);
        JSONObject prefUserAttribute = PreferencesUtil.getUserAttribute(preferences);
        Assert.assertEquals("carl", prefUserAttribute.getString("user_name"));
        Assert.assertEquals("10837409", prefUserAttribute.getString("_user_id"));
        assertTrue(prefUserAttribute.getBoolean("null"));
        Assert.assertEquals(85.5, prefUserAttribute.getDouble("score"), 0.01);
    }

    /**
     * test update UserAttribute.
     *
     * @throws JSONException exception
     */
    @Test
    public void testUpdateUserAttribute() throws JSONException {
        JSONObject userAttribute = new JSONObject();
        userAttribute.put("user_name", "carl");
        userAttribute.put("_user_id", "10837409");
        userAttribute.put("user_age", 21);
        userAttribute.put("null", true);
        userAttribute.put("score", 85.5);
        PreferencesUtil.updateUserAttribute(preferences, userAttribute);
        userAttribute.put("custom", "value");
        userAttribute.put("user_age", 22);
        PreferencesUtil.updateUserAttribute(preferences, userAttribute);
        JSONObject prefUserAttribute = PreferencesUtil.getUserAttribute(preferences);
        Assert.assertEquals("value", prefUserAttribute.getString("custom"));
        Assert.assertEquals(22, prefUserAttribute.getInt("user_age"));
    }

    /**
     * test set and get current userId.
     */
    @Test
    public void testCurrentUserId() {
        String currentUserId = "3728398";
        PreferencesUtil.setCurrentUserId(preferences, currentUserId);
        Assert.assertEquals(currentUserId, PreferencesUtil.getCurrentUserId(preferences));
    }

    /**
     * test get current user uniqueId multiple times.
     */
    @Test
    public void testGetCurrentUserUniqueId() {
        String currentUserUniqueId1 = PreferencesUtil.getCurrentUserUniqueId(preferences);
        String currentUserUniqueId2 = PreferencesUtil.getCurrentUserUniqueId(preferences);
        Assert.assertEquals(currentUserUniqueId1, currentUserUniqueId2);

        String userUniqueId = UUID.randomUUID().toString();
        PreferencesUtil.setCurrentUserUniqueId(preferences, userUniqueId);
        Assert.assertEquals(userUniqueId, PreferencesUtil.getCurrentUserUniqueId(preferences));
    }

    /**
     * test for user from unLogin to login user 1 to login user 2 then return to login user 1.
     *
     * @throws JSONException exception
     */
    @Test
    public void testGetNewUserUniqueId() throws JSONException {
        String userUniqueIdUnLogin = PreferencesUtil.getCurrentUserUniqueId(preferences);
        long firstTouchTimestamp = PreferencesUtil.getCurrentUserFirstTouchTimestamp(preferences);

        String userId1 = "111";
        JSONObject userInfo1 = PreferencesUtil.getNewUserInfo(preferences, userId1);
        String userUniqueId1 = userInfo1.getString("user_unique_id");
        long firstTouchTimestampUserId1 = userInfo1.getLong("user_first_touch_timestamp");
        Assert.assertEquals(userUniqueIdUnLogin, userUniqueId1);
        Assert.assertEquals(firstTouchTimestamp, firstTouchTimestampUserId1);

        String userId2 = "222";
        JSONObject userInfo2 = PreferencesUtil.getNewUserInfo(preferences, userId2);
        String userUniqueId2 = userInfo2.getString("user_unique_id");
        long firstTouchTimestampUserId2 = userInfo2.getLong("user_first_touch_timestamp");
        Assert.assertTrue(userUniqueId2.length() > 0);
        Assert.assertNotEquals(userUniqueIdUnLogin, userUniqueId2);
        Assert.assertNotEquals(userUniqueId1, userUniqueId2);
        Assert.assertNotEquals(firstTouchTimestampUserId1, firstTouchTimestampUserId2);

        JSONObject userInfo3 = PreferencesUtil.getNewUserInfo(preferences, userId1);
        String userUniqueId3 = userInfo3.getString("user_unique_id");
        long firstTouchTimestampUserId3 = userInfo3.getLong("user_first_touch_timestamp");
        Assert.assertTrue(userUniqueId3.length() > 0);
        Assert.assertEquals(userUniqueId3, userUniqueId1);
        Assert.assertEquals(firstTouchTimestampUserId3, firstTouchTimestampUserId1);
    }
}
