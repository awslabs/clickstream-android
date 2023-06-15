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

package software.aws.solution.clickstream;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.amplifyframework.analytics.AnalyticsCategory;
import com.amplifyframework.analytics.AnalyticsEventBehavior;
import com.amplifyframework.analytics.AnalyticsProperties;

/**
 * ClickstreamEvent is a custom analytics event that holds a name and a number of
 * {@link AnalyticsProperties}. This data object is used to indicate an event occurred such as a user taking
 * an action in your application.
 *
 * <pre>
 *     ClickstreamEvent event = ClickstreamEvent.builder()
 *          .name("LikedPost")
 *          .add("PostType", "UserImage")
 *          .add("LikedUserID", 78219)
 *          .add("FirstLike", true)
 *          .build();
 * </pre>
 * <p>
 * Once built, a ClickstreamEvent can be submitted to an analytics plugin through
 * {@link AnalyticsCategory#recordEvent(AnalyticsEventBehavior)}.
 */
public final class ClickstreamEvent implements AnalyticsEventBehavior {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_VALUE_LENGTH = 1024;
    private final String name;
    private final AnalyticsProperties properties;

    private ClickstreamEvent(String name, AnalyticsProperties properties) {
        this.name = name;
        this.properties = properties;
    }

    /**
     * Returns the name of the event.
     *
     * @return The name of the event
     */
    @Override
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link AnalyticsProperties} of the event.
     *
     * @return The {@link AnalyticsProperties} of the event
     */
    @Override
    @NonNull
    public AnalyticsProperties getProperties() {
        return properties;
    }

    /**
     * Returns a new {@link Builder} to configure an instance of ClickstreamEvent.
     *
     * @return a {@link Builder}
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder is used to create and configure an instance of {@link ClickstreamEvent}. Its
     * methods return the Builder instance to allow for fluent method chaining. This Builder reuses
     * {@link AnalyticsProperties.Builder} to construct the properties to store in the event.
     *
     * @see AnalyticsProperties
     */
    public static final class Builder {
        private String name;
        private AnalyticsProperties.Builder propertiesBuilder;

        /**
         * the builder for add event attribute.
         */
        public Builder() {
            this.propertiesBuilder = AnalyticsProperties.builder();
        }

        /**
         * Adds a name to the {@link ClickstreamEvent} under construction.
         *
         * @param name The name of the event
         * @return Current Builder instance, for fluent method chaining
         */
        @NonNull
        public Builder name(@NonNull @Size(min = 1L, max = MAX_NAME_LENGTH) String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a String property to the {@link ClickstreamEvent} under construction.
         *
         * @param name  The name of the property
         * @param value The String value of the property
         * @return Current Builder instance, for fluent method chaining
         */
        @NonNull
        public Builder add(@NonNull @Size(min = 1L, max = MAX_NAME_LENGTH) String name,
                           @NonNull @Size(min = 0L, max = MAX_VALUE_LENGTH) String value) {
            this.propertiesBuilder.add(name, value);
            return this;
        }

        /**
         * Adds a Double property to the {@link ClickstreamEvent} under construction.
         *
         * @param name  The name of the property
         * @param value The Double value of the property
         * @return Current Builder instance, for fluent method chaining
         */
        @NonNull
        public Builder add(@NonNull @Size(min = 1L, max = MAX_NAME_LENGTH) String name, @NonNull Double value) {
            this.propertiesBuilder.add(name, value);
            return this;
        }

        /**
         * Adds a Boolean property to the {@link ClickstreamEvent} under construction.
         *
         * @param name  The name of the property
         * @param value The Boolean value of the property
         * @return Current Builder instance, for fluent method chaining
         */
        @NonNull
        public Builder add(@NonNull @Size(min = 1L, max = MAX_NAME_LENGTH) String name,
                           @NonNull Boolean value) {
            this.propertiesBuilder.add(name, value);
            return this;
        }

        /**
         * Adds an Integer property to the {@link ClickstreamEvent} under construction.
         *
         * @param name  The name of the property
         * @param value The Integer value of the property
         * @return Current Builder instance, for fluent method chaining
         */
        @NonNull
        public Builder add(@NonNull @Size(min = 1L, max = MAX_NAME_LENGTH) String name,
                           @NonNull Integer value) {
            this.propertiesBuilder.add(name, value);
            return this;
        }

        /**
         * Adds an Long property to the {@link ClickstreamEvent} under construction.
         *
         * @param name  The name of the property
         * @param value The Long value of the property
         * @return Current Builder instance, for fluent method chaining
         */
        @NonNull
        public Builder add(@NonNull @Size(min = 1L, max = MAX_NAME_LENGTH) String name,
                           @NonNull Long value) {
            this.propertiesBuilder.add(name, AnalyticsLongProperty.from(value));
            return this;
        }

        /**
         * Returns the built {@link ClickstreamEvent}.
         *
         * @return The constructed {@link ClickstreamEvent} configured with the parameters set in
         * the Builder
         */
        @NonNull
        public ClickstreamEvent build() {
            return new ClickstreamEvent(name, propertiesBuilder.build());
        }
    }
}
