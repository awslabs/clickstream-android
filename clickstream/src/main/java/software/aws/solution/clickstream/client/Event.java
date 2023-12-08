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
 * handle the event errors.
 */
public final class Event {

    private Event() {
    }

    /**
     * event limit value.
     */
    public static final class Limit {
        /**
         * max limit of single event attribute number.
         */
        public static final int MAX_NUM_OF_ATTRIBUTES = 500;
        /**
         * max limit of single event user attribute number.
         */
        public static final int MAX_NUM_OF_USER_ATTRIBUTES = 100;
        /**
         * max limit of attribute name character length.
         */
        public static final int MAX_LENGTH_OF_NAME = 50;
        /**
         * max limit of attribute value character length.
         */
        public static final int MAX_LENGTH_OF_VALUE = 1024;
        /**
         * max limit of user attribute value character length.
         */
        public static final int MAX_LENGTH_OF_USER_VALUE = 256;
        /**
         * max limit of item attribute value character length.
         */
        public static final int MAX_LENGTH_OF_ITEM_VALUE = 256;
        /**
         * max limit of one batch event number.
         */
        public static final int MAX_EVENT_NUMBER_OF_BATCH = 100;
        /**
         * max limit of error attribute value length.
         */
        public static final int MAX_LENGTH_OF_ERROR_VALUE = 256;

        /**
         * max limit of item number in one event.
         */
        public static final int MAX_NUM_OF_ITEMS = 100;

        /**
         * max limit of item custom attribute number in one item.
         */
        public static final int MAX_NUM_OF_CUSTOM_ITEM_ATTRIBUTE = 10;

        private Limit() {
        }
    }

    /**
     * the event error code constants.
     */
    public static final class ErrorCode {
        /**
         * no error code.
         */
        public static final int NO_ERROR = 0;
        /**
         * error code for event name invalid.
         */
        public static final int EVENT_NAME_INVALID = 1001;
        /**
         * error code for event name length exceed the limit.
         */
        public static final int EVENT_NAME_LENGTH_EXCEED = 1002;
        /**
         * error code for attribute name length exceed.
         */
        public static final int ATTRIBUTE_NAME_LENGTH_EXCEED = 2001;
        /**
         * error code for attribute name invalid.
         */
        public static final int ATTRIBUTE_NAME_INVALID = 2002;
        /**
         * error code for attribute value length exceed.
         */
        public static final int ATTRIBUTE_VALUE_LENGTH_EXCEED = 2003;
        /**
         * error code for attribute size exceed.
         */
        public static final int ATTRIBUTE_SIZE_EXCEED = 2004;
        /**
         * error code for user attribute size exceed.
         */
        public static final int USER_ATTRIBUTE_SIZE_EXCEED = 3001;
        /**
         * error code for user attribute name length exceed.
         */
        public static final int USER_ATTRIBUTE_NAME_LENGTH_EXCEED = 3002;
        /**
         * error code for user user attribute name invalid.
         */
        public static final int USER_ATTRIBUTE_NAME_INVALID = 3003;
        /**
         * error code for user attribute value length exceed.
         */
        public static final int USER_ATTRIBUTE_VALUE_LENGTH_EXCEED = 3004;

        /**
         * error code for item size exceed.
         */
        public static final int ITEM_SIZE_EXCEED = 4001;
        /**
         * error code for item value length exceed.
         */
        public static final int ITEM_ATTRIBUTE_VALUE_LENGTH_EXCEED = 4002;
        /**
         * item custom attribute size exceed the max size.
         */
        public static final int ITEM_CUSTOM_ATTRIBUTE_SIZE_EXCEED = 4003;
        /**
         * item custom attribute key length exceed the max length.
         */
        public static final int ITEM_CUSTOM_ATTRIBUTE_KEY_LENGTH_EXCEED = 4004;
        /**
         * item custom attribute key name invalid.
         */
        public static final int ITEM_CUSTOM_ATTRIBUTE_KEY_INVALID = 4005;

        private ErrorCode() {
        }
    }

    /**
     * Event for return.
     */
    public static class EventError {
        private int errorCode;
        private String errorMessage;

        EventError() {
        }

        EventError(int errorType, String errorMessage) {
            this.errorCode = errorType;
            this.errorMessage = errorMessage;
        }

        /**
         * get error type.
         *
         * @return error type
         */
        public int getErrorCode() {
            return errorCode;
        }

        /**
         * get error message.
         *
         * @return error message.
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * set error code.
         *
         * @param errorCode the error code
         */
        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        /**
         * set error message.
         *
         * @param errorMessage the error message
         */
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * reserved attribute for Clickstream.
     */
    public static final class ReservedAttribute {
        /**
         * user id.
         */
        public static final String USER_ID = "_user_id";

        /**
         * user id for identity the unused case when invoke identifyUser() method.
         */
        public static final String USER_ID_UNSET = "_clickstream_user_id_unset";
        /**
         * user first touch timestamp.
         */
        public static final String USER_FIRST_TOUCH_TIMESTAMP = "_user_first_touch_timestamp";

        /**
         * screen name.
         */
        public static final String SCREEN_NAME = "_screen_name";
        /**
         * screen id.
         */
        public static final String SCREEN_ID = "_screen_id";

        /**
         * screen unique id.
         */
        public static final String SCREEN_UNIQUE_ID = "_screen_unique_id";
        /**
         * previous screen name.
         */
        public static final String PREVIOUS_SCREEN_NAME = "_previous_screen_name";
        /**
         * previous screen id.
         */
        public static final String PREVIOUS_SCREEN_ID = "_previous_screen_id";

        /**
         * previous screen unique id.
         */
        public static final String PREVIOUS_SCREEN_UNIQUE_ID = "_previous_screen_unique_id";
        /**
         * previous event timestamp.
         */
        public static final String PREVIOUS_TIMESTAMP = "_previous_timestamp";
        /**
         * entrances.
         */
        public static final String ENTRANCES = "_entrances";
        /**
         * previous app version.
         */
        public static final String PREVIOUS_APP_VERSION = "_previous_app_version";
        /**
         * previous os version.
         */
        public static final String PREVIOUS_OS_VERSION = "_previous_os_version";

        /**
         * engagement time msec.
         */
        public static final String ENGAGEMENT_TIMESTAMP = "_engagement_time_msec";

        /**
         * is the first time attribute.
         */
        public static final String IS_FIRST_TIME = "_is_first_time";
        /**
         * is the error code attribute.
         */
        public static final String ERROR_CODE = "_error_code";
        /**
         * is the error message attribute.
         */
        public static final String ERROR_MESSAGE = "_error_message";

        private ReservedAttribute() {
        }
    }

    /**
     * preset event for Clickstream.
     */
    public static final class PresetEvent {
        /**
         * The eventType recorded for session start events.
         */
        public static final String SESSION_START = "_session_start";

        /**
         * The eventType recorded for app first open from install.
         */
        public static final String FIRST_OPEN = "_first_open";

        /**
         * The eventType recorded for app start when app move to foreground.
         */
        public static final String APP_START = "_app_start";

        /**
         * The eventType recorded for app end when app move to background.
         */
        public static final String APP_END = "_app_end";

        /**
         * The user engagement event when the app is in the foreground at least one second.
         */
        public static final String USER_ENGAGEMENT = "_user_engagement";

        /**
         * The screen view event send when activity resume lifecycle method called.
         */
        public static final String SCREEN_VIEW = "_screen_view";

        /**
         * App version update event.
         */
        public static final String APP_UPDATE = "_app_update";

        /**
         * OS version update event.
         */
        public static final String OS_UPDATE = "_os_update";

        /**
         * app exception event when crash.
         */
        public static final String APP_EXCEPTION = "_app_exception";

        /**
         * profile set event for user attribute changes.
         */
        public static final String PROFILE_SET = "_profile_set";

        /**
         * clickstream error event.
         */
        public static final String CLICKSTREAM_ERROR = "_clickstream_error";

        private PresetEvent() {
        }
    }
}
