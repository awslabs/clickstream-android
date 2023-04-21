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

package software.aws.solution.clickstream.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import software.aws.solution.clickstream.client.util.StringUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 23)
public class StringUtilTest {

    /**
     * test isNullOrEmptyForEmptyString.
     */
    @Test
    public void isNullOrEmptyForEmptyString() {
        assertTrue(StringUtil.isNullOrEmpty(""));
    }

    /**
     * test isNullOrEmptyForBlankString.
     */
    @Test
    public void isNullOrEmptyForBlankString() {
        assertFalse(StringUtil.isNullOrEmpty(" "));
    }

    /**
     * test isNullOrEmptyForNonBlankString.
     */
    @Test
    public void isNullOrEmptyForNonBlankString() {
        assertFalse(StringUtil.isNullOrEmpty("abcde"));
    }

    /**
     * test isBlankForEmptyString.
     */
    @Test
    public void isBlankForEmptyString() {
        assertTrue(StringUtil.isBlank(""));
    }

    /**
     * test isBlankForBlankString.
     */
    @Test
    public void isBlankForBlankString() {
        assertTrue(StringUtil.isBlank(" "));
    }

    /**
     * test isBlankForNonBlankString.
     */
    @Test
    public void isBlankForNonBlankString() {
        assertFalse(StringUtil.isBlank("abcde"));
    }

    /**
     * test trimOrPadStringForTrimsString.
     */
    @Test
    public void trimOrPadStringForTrimsString() {
        assertEquals(StringUtil.trimOrPadString("abcdefg", 7, ' '), "abcdefg");
        assertEquals(StringUtil.trimOrPadString("abcdefg", 6, ' '), "bcdefg");
        assertEquals(StringUtil.trimOrPadString("abcdefg", 5, ' '), "cdefg");
        assertEquals(StringUtil.trimOrPadString("abcdefg", 4, ' '), "defg");
        assertEquals(StringUtil.trimOrPadString("abcdefg", 3, ' '), "efg");
        assertEquals(StringUtil.trimOrPadString("abcdefg", 2, ' '), "fg");
        assertEquals(StringUtil.trimOrPadString("abcdefg", 1, ' '), "g");
        assertEquals(StringUtil.trimOrPadString("abcdefg", 0, ' '), "");
    }

    /**
     * test trimOrPadStringForPadsString.
     */
    @Test
    public void trimOrPadStringForPadsString() {
        assertEquals(StringUtil.trimOrPadString("abc", 7, '_'), "____abc");
        assertEquals(StringUtil.trimOrPadString("abc", 6, '-'), "---abc");
        assertEquals(StringUtil.trimOrPadString("abc", 5, '\\'), "\\\\abc");
        assertEquals(StringUtil.trimOrPadString("abc", 4, '$'), "$abc");

        assertEquals(StringUtil.trimOrPadString("", 10, '&'), "&&&&&&&&&&");
        assertEquals(StringUtil.trimOrPadString("", 5, '"'), "\"\"\"\"\"");
        assertEquals(StringUtil.trimOrPadString("\b\b", 5, '\n'), "\n\n\n\b\b");

        int l = 100;
        String s = StringUtil.trimOrPadString("", l, '\\');
        assertEquals(s.length(), l);
        for (int i = 0; i < s.length(); i++) {
            assertEquals(s.charAt(i), '\\');
        }
    }

    /**
     * test trimOrPadStringWorksWithInvalidArgs.
     */
    @Test
    public void trimOrPadStringWorksWithInvalidArgs() {
        // len < 0
        assertEquals(StringUtil.trimOrPadString("abcdefg", -1, ' '), "");
        assertEquals(StringUtil.trimOrPadString("abcdefg", -2, ' '), "");
        // null string
        assertEquals(StringUtil.trimOrPadString(null, 5, '+'), "+++++");
    }

    /**
     * test clipStringForAppendEllipses.
     */
    @Test
    public void clipStringForAppendEllipses() {
        assertTrue(StringUtil.clipString("abcdefgh", 5, true).endsWith("..."));
        assertFalse(StringUtil.clipString("abcdefgh", 10, true).endsWith("..."));
    }
}
