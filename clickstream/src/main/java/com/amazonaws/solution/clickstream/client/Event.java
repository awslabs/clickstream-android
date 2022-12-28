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

package com.amazonaws.solution.clickstream.client;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import com.amazonaws.solution.clickstream.client.util.StringUtil;

import java.util.regex.Pattern;

/**
 * handle the event errors.
 */
public final class Event {
    private static final Log LOG = LogFactory.getLog(EventError.class);

    private Event() {
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
            return new EventError(ErrorType.ATTRIBUTE_SIZE_EXCEED,
                StringUtil.clipString("attribute name: " + name, Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (name.length() > Limit.MAX_LENGTH_OF_NAME) {
            LOG.error("attribute : " + name + ", reached the max length of attributes name limit("
                + Limit.MAX_LENGTH_OF_NAME + "). current length is:(" + name.length() +
                ") and the attribute will not be recorded");
            return new EventError(ErrorType.ATTRIBUTE_NAME_LENGTH_EXCEED,
                StringUtil.clipString("attribute name length is:(" + name.length() + ") name is:" + name,
                    Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (!isValidName(name)) {
            LOG.error("attribute : " + name + ", was not valid, attribute name can only contains" +
                " uppercase and lowercase letters, underscores, number, and is not start with a number." +
                " so the attribute will not be recorded");
            return new EventError(ErrorType.ATTRIBUTE_NAME_INVALID,
                StringUtil.clipString(name, Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }

        if (value instanceof String) {
            int valueLength = ((String) value).length();
            if (valueLength > Limit.MAX_LENGTH_OF_VALUE) {
                LOG.error("attribute : " + name + ", reached the max length of attributes value limit ("
                    + Limit.MAX_LENGTH_OF_NAME + "). current length is:(" + valueLength +
                    "). and the attribute will not be recorded, attribute value:" + value);

                return new EventError(ErrorType.ATTRIBUTE_VALUE_LENGTH_EXCEED,
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
            return new EventError(ErrorType.ATTRIBUTE_SIZE_EXCEED,
                StringUtil.clipString("attribute name: " + name, Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (name.length() > Limit.MAX_LENGTH_OF_NAME) {
            LOG.error("user attribute : " + name + ", reached the max length of attributes name limit("
                + Limit.MAX_LENGTH_OF_NAME + "). current length is:(" + name.length() +
                ") and the attribute will not be recorded");
            return new EventError(ErrorType.ATTRIBUTE_NAME_LENGTH_EXCEED,
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
            return new EventError(ErrorType.ATTRIBUTE_NAME_INVALID,
                StringUtil.clipString(name, Limit.MAX_LENGTH_OF_ERROR_VALUE, true));
        }
        if (value instanceof String) {
            int valueLength = ((String) value).length();
            if (valueLength > Limit.MAX_LENGTH_OF_USER_VALUE) {
                LOG.error("user attribute : " + name + ", reached the max length of attributes value limit ("
                    + Limit.MAX_LENGTH_OF_USER_VALUE + "). current length is:(" + valueLength +
                    "). and the attribute will not be recorded, attribute value:" + value);
                return new EventError(ErrorType.ATTRIBUTE_VALUE_LENGTH_EXCEED,
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
     * event error type.
     */
    public static final class ErrorType {
        private static final String ATTRIBUTE_NAME_INVALID = "_error_name_invalid";
        private static final String ATTRIBUTE_NAME_LENGTH_EXCEED = "_error_name_length_exceed";
        private static final String ATTRIBUTE_VALUE_LENGTH_EXCEED = "_error_value_length_exceed";
        private static final String ATTRIBUTE_SIZE_EXCEED = "_error_attribute_size_exceed";

        private ErrorType() {
        }
    }

    /**
     * Event for return.
     */
    public static class EventError {
        private final String errorType;
        private final String errorMessage;

        EventError(String errorType, String errorMessage) {
            this.errorType = errorType;
            this.errorMessage = errorMessage;
        }

        /**
         * get error type.
         *
         * @return error type
         */
        public String getErrorType() {
            return errorType;
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

        private ReservedAttribute() {
        }
    }
}
