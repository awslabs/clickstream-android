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

package software.aws.solution.clickstream.client;

/**
 * ScreenRefererTool for save previous screen info.
 */
public final class ScreenRefererTool {

    private static String mPreviousScreenId;
    private static String mCurrentScreenId;
    private static String mPreviousScreenName;
    private static String mCurrentScreenName;
    private static String mPreviousScreenUniqueId;
    private static String mCurrentScreenUniqueId;

    private ScreenRefererTool() {
    }

    /**
     * set current screen name.
     *
     * @param screenName current screen name to set.
     */
    public static void setCurrentScreenName(String screenName) {
        mPreviousScreenName = mCurrentScreenName;
        mCurrentScreenName = screenName;
    }

    /**
     * set current screen id.
     *
     * @param screenId current screen id to set.
     */
    public static void setCurrentScreenId(String screenId) {
        mPreviousScreenId = mCurrentScreenId;
        mCurrentScreenId = screenId;
    }

    /**
     * set current screen unique id.
     *
     * @param screenUniqueId current screen unique id to set.
     */
    public static void setCurrentScreenUniqueId(String screenUniqueId) {
        mPreviousScreenUniqueId = mCurrentScreenUniqueId;
        mCurrentScreenUniqueId = screenUniqueId;
    }

    /**
     * get current ScreenName.
     *
     * @return mCurrentScreenName
     */
    public static String getCurrentScreenName() {
        return mCurrentScreenName;
    }

    /**
     * get current screenId.
     *
     * @return mCurrentScreenId
     */
    public static String getCurrentScreenId() {
        return mCurrentScreenId;
    }

    /**
     * get current screen unique id.
     *
     * @return mCurrentScreenUniqueId
     */
    public static String getCurrentScreenUniqueId() {
        return mCurrentScreenUniqueId;
    }

    /**
     * get previous ScreenName.
     *
     * @return mPreviousScreenName
     */
    public static String getPreviousScreenName() {
        return mPreviousScreenName;
    }

    /**
     * get previous screenId.
     *
     * @return mPreviousScreenId
     */
    public static String getPreviousScreenId() {
        return mPreviousScreenId;
    }

    /**
     * get previous screen unique id.
     *
     * @return mPreviousScreenUniqueId
     */
    public static String getPreviousScreenUniqueId() {
        return mPreviousScreenUniqueId;
    }

    /**
     * Judging that the current screen is the same as the previous screen.
     *
     * @param screenName     current screen name
     * @param screenUniqueId current screen unique id
     * @return the boolean value for is the same screen
     */
    public static boolean isSameScreen(String screenName, String screenUniqueId) {
        return mCurrentScreenName != null &&
            mCurrentScreenName.equals(screenName) &&
            (mCurrentScreenUniqueId == null || mCurrentScreenUniqueId.equals(screenUniqueId));
    }

    /**
     * method for clear cached screen information.
     */
    public static void clear() {
        mCurrentScreenId = null;
        mCurrentScreenName = null;
        mCurrentScreenUniqueId = null;
        mPreviousScreenId = null;
        mPreviousScreenName = null;
        mPreviousScreenUniqueId = null;
    }
}
