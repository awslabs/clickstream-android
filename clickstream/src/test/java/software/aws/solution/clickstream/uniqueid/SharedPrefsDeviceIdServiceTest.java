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

package software.aws.solution.clickstream.uniqueid;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import software.aws.solution.clickstream.client.ClickstreamContext;
import software.aws.solution.clickstream.client.system.AndroidPreferences;
import software.aws.solution.clickstream.client.system.AndroidSystem;
import software.aws.solution.clickstream.client.uniqueid.SharedPrefsDeviceIdService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SharedPrefsDeviceIdServiceTest {

    private ClickstreamContext mockClickstreamContext;
    private AndroidSystem mockSystem;
    private AndroidPreferences mockPreferences;

    private SharedPrefsDeviceIdService serviceToTest = null;

    /**
     * setup the mockSystem,mockPreferences and SharedPrefsDeviceIdService.
     */
    @Before
    public void setup() {
        mockClickstreamContext = mock(ClickstreamContext.class);
        mockSystem = mock(AndroidSystem.class);
        mockPreferences = mock(AndroidPreferences.class);
        when(mockClickstreamContext.getSystem()).thenReturn(mockSystem);
        when(mockClickstreamContext.getApplicationContext()).thenReturn(ApplicationProvider.getApplicationContext());
        when(mockSystem.getPreferences()).thenReturn(mockPreferences);
        serviceToTest = new SharedPrefsDeviceIdService();
    }

    /**
     * test getUniqueId when sp is null then return empty id.
     */
    @Test
    public void getDeviceIdWhenSpIsNull() {
        when(mockSystem.getPreferences()).thenReturn(null);
        String uniqueId = serviceToTest.getDeviceId(mockClickstreamContext);
        assertEquals(uniqueId, "");
    }

    /**
     * test getUniqueId when id does not exist then create and store id.
     */
    @Test
    public void getDeviceIdWhenIdDoesNotExist() {
        String expectedUniqueIdKey = "DeviceId";
        when(mockPreferences.getString(eq(expectedUniqueIdKey), anyString())).thenReturn(null);
        String uniqueId = serviceToTest.getDeviceId(mockClickstreamContext);
        assertNotNull(uniqueId);
        verify(mockPreferences, times(1)).putString(eq(expectedUniqueIdKey), any(String.class));
    }

}
