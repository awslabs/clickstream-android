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

package software.aws.solution.clickstream.event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import software.aws.solution.clickstream.client.Event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 23)
public class EventTest {

    /**
     * test the name is valid.
     */
    @Test
    public void tesIsValidName() {
        assertFalse(Event.isValidName(""));
        assertTrue(Event.isValidName("abc"));
        assertFalse(Event.isValidName("123"));
        assertTrue(Event.isValidName("AAA"));
        assertTrue(Event.isValidName("a_ab"));
        assertTrue(Event.isValidName("a_ab_1A"));
        assertTrue(Event.isValidName("add_to_cart"));
        assertTrue(Event.isValidName("Screen_view"));
        assertFalse(Event.isValidName("0abc"));
        assertFalse(Event.isValidName("1abc"));
        assertFalse(Event.isValidName("9Abc"));
        assertTrue(Event.isValidName("A9bc"));
        assertFalse(Event.isValidName("A9bc-"));
    }
}
