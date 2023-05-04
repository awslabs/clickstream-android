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

package software.aws.solution.clickstream.client;

/**
 * ScreenRefererTool for save previous screen info.
 */
public final class ScreenRefererTool {

    private static String mPreviousScreenId;
    private static String mCurrentScreenId;
    private static String mPreviousScreenName;
    private static String mCurrentScreenName;

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
}
