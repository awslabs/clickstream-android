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

package software.aws.solution.clickstream.client.util;

import android.os.Looper;

/**
 * Thread utility methods.
 */
public final class ThreadUtil {

    /**
     * Default constructor.
     */
    private ThreadUtil() {
    }

    /**
     * method for return current thread whether not in main thread.
     *
     * @return boolean value notInMainThread.
     */
    public static boolean notInMainThread() {
        return Looper.getMainLooper() != Looper.myLooper();
    }

}

