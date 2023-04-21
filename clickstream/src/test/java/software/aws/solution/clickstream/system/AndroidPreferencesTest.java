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

package software.aws.solution.clickstream.system;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import software.aws.solution.clickstream.client.system.AndroidPreferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AndroidPreferencesTest {

    // This is the suffix we use. In real code we prepend this with the appId
    private static final String PREFERENCE_KEY = "294262d4-8dbd-4bfd-816d-0fc81b3d32b7";

    private SharedPreferences pref;
    private Context context;

    /**
     * set up the application context and sharedPreferences.
     */
    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        pref = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    /**
     * test getBoolean.
     */
    @Test
    public void getBoolean() {
        pref.edit().putBoolean("boolean", true).commit();
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        boolean value = preferences.getBoolean("boolean", false);
        assertTrue(value);
    }

    /**
     * test getInt.
     */
    @Test
    public void getInt() {
        pref.edit().putInt("int", 1).commit();
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        int value = preferences.getInt("int", 0);
        assertEquals(value, 1);
    }

    /**
     * test getFloat.
     */
    @Test
    public void getFloat() {
        pref.edit().putFloat("float", 1.0f).commit();
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        float value = preferences.getFloat("float", 0.0f);
        assertEquals(value, 1.0f, .01f);
    }

    /**
     * test getLong.
     */
    @Test
    public void getLong() {
        pref.edit().putLong("long", 1L).commit();
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        long value = preferences.getLong("long", 0L);
        assertEquals(value, 1L);
    }

    /**
     * test getString.
     */
    @Test
    public void getString() {
        pref.edit().putString("string", "value").commit();
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        String value = preferences.getString("string", "other");
        assertEquals(value, "value");
    }

    /**
     * test putBoolean.
     */
    @Test
    public void putBoolean() {
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        preferences.putBoolean("boolean", true);
        assertTrue(pref.getBoolean("boolean", false));
    }

    /**
     * test putInt.
     */
    @Test
    public void putInt() {
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        preferences.putInt("int", 1);
        assertEquals(pref.getInt("int", 5), 1);
    }

    /**
     * test putFloat.
     */
    @Test
    public void putFloat() {
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        preferences.putFloat("float", 1.0f);
        assertEquals(pref.getFloat("float", 5.0f), 1.0f, .05f);
    }

    /**
     * test putLong.
     */
    @Test
    public void putLong() {
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        preferences.putLong("long", 1L);
        assertEquals(pref.getLong("long", 5L), 1L);
    }

    /**
     * test putString.
     */
    @Test
    public void putString() {
        AndroidPreferences preferences = new AndroidPreferences(context, PREFERENCE_KEY);
        preferences.putString("string", "value");
        assertSame(pref.getString("string", "nonValue"), "value");
    }
}
