/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.aws.solution.clickstream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;

import com.amplifyframework.analytics.AnalyticsProperties;
import com.amplifyframework.analytics.AnalyticsPropertyBehavior;

import java.util.Objects;

/**
 * AnalyticsLongProperty wraps an Long value to store in {@link AnalyticsProperties}.
 */
public final class AnalyticsLongProperty implements AnalyticsPropertyBehavior<Long> {
    private final Long value;

    private AnalyticsLongProperty(Long value) {
        this.value = value;
    }

    /**
     * getValue returns the wrapped Long value stored in the property.
     *
     * @return The wrapped Boolean value
     */
    @NonNull
    @Override
    public Long getValue() {
        return value;
    }

    /**
     * Factory method to instantiate an {@link AnalyticsLongProperty} from a {@link Long} value.
     *
     * @param value an Long value
     * @return an instance of {@link AnalyticsLongProperty}
     */
    @NonNull
    public static AnalyticsLongProperty from(@NonNull Long value) {
        return new AnalyticsLongProperty(Objects.requireNonNull(value));
    }

    @Override
    public boolean equals(@Nullable Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        AnalyticsLongProperty that = (AnalyticsLongProperty) thatObject;
        return ObjectsCompat.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "AnalyticsLongProperty{" +
            "value=" + value +
            '}';
    }
}