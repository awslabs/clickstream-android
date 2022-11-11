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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link com.amazonaws.solution.clickstream}.
 */
@RunWith(RobolectricTestRunner.class)
public final class AutoSessionTrackerUnitTest {

    /**
     * Setup dependencies and object under test.
     */
    @Before
    public void setup() {
        // Dependencies
    }

    /**
     * test add.
     */
    @Test
    public void isCorrect() {
        assertEquals(4, 2 + 2);
    }

}
