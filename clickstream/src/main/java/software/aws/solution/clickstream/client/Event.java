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

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import software.aws.solution.clickstream.client.util.StringUtil;

import java.util.regex.Pattern;

/**
 * handle the event errors.
 */
public final class Event {
    private static final Log LOG = LogFactory.getLog(EventError.class);

    private Event() {
    }

    /**
     * check the event type.
     *
     * @param eventName the event name
     * @return the EventError object
     */
    public static EventError checkEventName(String eventName) {
        if (!isValidName(eventName)) {
            return new EventError(ErrorCode.EVENT_NAME_INVALID,
                "Event name can only contains uppercase and lowercase letters, " +
                    "underscores, number, and is not start with a number. event name: " + eventName);
        } else if (eventName.length() > Limit.MAX_LENGTH_OF_NAME) {
            return new EventError(ErrorCode.EVENT_NAME_LENGTH_EXCEED,
                "Event name is too long, the max event type length is " +
                    Limit.MAX_LENGTH_OF_NAME + "characters. event name: " + eventName);
        }
        return null;
    }

    /**
     * check the attribute error.
     *
     * @param currentNumber current attribute number
     * @param name          attribute name.
     * @param value         attribute value.
     * @return the ErrorType
     */
    public static EventError checkAttribute(int currentNumber, String name, Object value) {
        if (currentNumber >= Limit.MAX_NUM_OF_ATTRIBUTES) {
            LOG.error("reached the max number of attributes limit ("
                + Limit.MAX_NUM_OF_ATTRIBUTES + "). and the attribute: " + name + " will not be recorded");
            return new EventError(ErrorCode.ATTRIBUTE_SIZE_EXCEED,
                StringUtil.clipString("attribute name: " + name, Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (name.length() > Limit.MAX_LENGTH_OF_NAME) {
            LOG.error("attribute : " + name + ", reached the max length of attributes name limit("
                + Limit.MAX_LENGTH_OF_NAME + "). current length is:(" + name.length() +
                ") and the attribute will not be recorded");
            return new EventError(ErrorCode.ATTRIBUTE_NAME_LENGTH_EXCEED,
                StringUtil.clipString("attribute name length is:(" + name.length() + ") name is:" + name,
                    Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (!isValidName(name)) {
            LOG.error("attribute : " + name + ", was not valid, attribute name can only contains" +
                " uppercase and lowercase letters, underscores, number, and is not start with a number." +
                " so the attribute will not be recorded");
            return new EventError(ErrorCode.ATTRIBUTE_NAME_INVALID,
                StringUtil.clipString(name, Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }

        if (value instanceof String) {
            int valueLength = ((String) value).length();
            if (valueLength > Limit.MAX_LENGTH_OF_VALUE) {
                LOG.error("attribute : " + name + ", reached the max length of attributes value limit ("
                    + Limit.MAX_LENGTH_OF_VALUE + "). current length is:(" + valueLength +
                    "). and the attribute will not be recorded, attribute value:" + value);

                return new EventError(ErrorCode.ATTRIBUTE_VALUE_LENGTH_EXCEED,
                    StringUtil.clipString("attribute name:" + name + ", attribute value:" + value,
                        Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
            }
        }
        return null;
    }

    /**
     * check the user attribute error.
     *
     * @param currentNumber current user attribute number.
     * @param name          attribute name.
     * @param value         attribute value.
     * @return the ErrorType
     */
    public static EventError checkUserAttribute(int currentNumber, String name, Object value) {
        if (currentNumber >= Limit.MAX_NUM_OF_USER_ATTRIBUTES) {
            LOG.error("reached the max number of user attributes limit ("
                + Limit.MAX_NUM_OF_USER_ATTRIBUTES + "). and the user attribute: " + name + " will not be recorded");
            return new EventError(ErrorCode.USER_ATTRIBUTE_SIZE_EXCEED,
                StringUtil.clipString("attribute name: " + name, Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (name.length() > Limit.MAX_LENGTH_OF_NAME) {
            LOG.error("user attribute : " + name + ", reached the max length of attributes name limit("
                + Limit.MAX_LENGTH_OF_NAME + "). current length is:(" + name.length() +
                ") and the attribute will not be recorded");
            return new EventError(ErrorCode.USER_ATTRIBUTE_NAME_LENGTH_EXCEED,
                StringUtil.clipString("user attribute name length is:(" + name.length() + ") name is:" + name,
                    Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (!isValidName(name)) {
            LOG.error("user attribute : " + name + ", reached the max length of attributes name limit("
                + Limit.MAX_LENGTH_OF_NAME + "). current length is:(" + name.length() +
                ") and the attribute will not be recorded");
            LOG.error("user attribute : " + name + ", was not valid, user attribute name can only contains" +
                " uppercase and lowercase letters, underscores, number, and is not start with a number." +
                " so the attribute will not be recorded");
            return new EventError(ErrorCode.USER_ATTRIBUTE_NAME_INVALID,
                StringUtil.clipString(name, Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (value instanceof String) {
            int valueLength = ((String) value).length();
            if (valueLength > Limit.MAX_LENGTH_OF_USER_VALUE) {
                LOG.error("user attribute : " + name + ", reached the max length of attributes value limit ("
                    + Limit.MAX_LENGTH_OF_USER_VALUE + "). current length is:(" + valueLength +
                    "). and the attribute will not be recorded, attribute value:" + value);
                return new EventError(ErrorCode.USER_ATTRIBUTE_VALUE_LENGTH_EXCEED,
                    StringUtil.clipString("user attribute name:" + name + ", attribute value:" + value,
                        Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
            }
        }
        return null;
    }

    /**
     * verify the string whether only contains number, uppercase and lowercase letters, underscores,
     * and is not start with a number.
     *
     * @param name the name to verify
     * @return the name is valid.
     */
    public static Boolean isValidName(String name) {
        String pattern = "^(?![0-9])[0-9a-zA-Z_]+$";
        return Pattern.matches(pattern, name);
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
         * max limit of one batch event number.
         */
        public static final int MAX_EVENT_NUMBER_OF_BATCH = 100;
        /**
         * max limit of error attribute value length.
         */
        private static final int MAX_LENGTH_OF_ERROR_VALUE = 256;

        private Limit() {
        }
    }

    /**
     * the event error code constants.
     */
    public static final class ErrorCode {
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

        private ErrorCode() {
        }
    }

    /**
     * Event for return.
     */
    public static class EventError {
        private final int errorCode;
        private final String errorMessage;

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
