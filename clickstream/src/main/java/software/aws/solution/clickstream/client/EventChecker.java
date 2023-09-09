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
import software.aws.solution.clickstream.client.Event.ErrorType;
import software.aws.solution.clickstream.client.Event.EventError;
import software.aws.solution.clickstream.client.Event.Limit;
import software.aws.solution.clickstream.client.util.StringUtil;

/**
 * handle the event errors.
 */
public final class EventChecker {
    private static final Log LOG = LogFactory.getLog(EventChecker.class);

    private EventChecker() {
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
        if (!Event.isValidName(name)) {
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
                    + Limit.MAX_LENGTH_OF_VALUE + "). current length is:(" + valueLength +
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
        if (!Event.isValidName(name)) {
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

}
