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

package com.amazonaws.solution.clickstream.uniqueid;

import com.amazonaws.solution.clickstream.client.ClickstreamContext;
import com.amazonaws.solution.clickstream.client.system.AndroidPreferences;
import com.amazonaws.solution.clickstream.client.system.AndroidSystem;
import com.amazonaws.solution.clickstream.client.uniqueid.SharedPrefsUniqueIdService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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
public class SharedPrefsUniqueIdServiceTest {

    private ClickstreamContext mockClickstreamContext;
    private AndroidSystem mockSystem;
    private AndroidPreferences mockPreferences;

    private SharedPrefsUniqueIdService serviceToTest = null;

    /**
     * setup the mockSystem,mockPreferences and SharedPrefsUniqueIdService.
     */
    @Before
    public void setup() {
        mockClickstreamContext = mock(ClickstreamContext.class);
        mockSystem = mock(AndroidSystem.class);
        mockPreferences = mock(AndroidPreferences.class);
        when(mockClickstreamContext.getSystem()).thenReturn(mockSystem);
        when(mockSystem.getPreferences()).thenReturn(mockPreferences);
        serviceToTest = new SharedPrefsUniqueIdService();
    }

    /**
     * test getUniqueId when sp is null then return empty id.
     */
    @Test
    public void getUniqueIdWhenSpIsNull() {
        when(mockSystem.getPreferences()).thenReturn(null);
        String uniqueId = serviceToTest.getUniqueId(mockClickstreamContext);
        assertEquals(uniqueId, "");
    }

    /**
     * test getUniqueId when id does not exist then create and store id.
     */
    @Test
    public void getUniqueIdWhenIdDoesNotExist() {
        String expectedUniqueIdKey = "UniqueId";
        when(mockPreferences.getString(eq(expectedUniqueIdKey), anyString())).thenReturn(null);
        String uniqueId = serviceToTest.getUniqueId(mockClickstreamContext);
        assertNotNull(uniqueId);
        verify(mockPreferences, times(1)).putString(eq(expectedUniqueIdKey), any(String.class));
    }

}
