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

package software.aws.solution.clickstream.client.uniqueid;

import com.amazonaws.logging.Log;
import com.amazonaws.logging.LogFactory;
import software.aws.solution.clickstream.client.ClickstreamContext;
import software.aws.solution.clickstream.client.system.AndroidPreferences;

import java.util.UUID;

/**
 * Shared Prefs Unique ID Services.
 */
public class SharedPrefsUniqueIdService {

    /**
     * The unique ID key.
     */
    protected static final String UNIQUE_ID_KEY = "UniqueId";
    private static final Log LOG = LogFactory.getLog(SharedPrefsUniqueIdService.class);

    /**
     * Uses Shared prefs to recall and store the unique ID.
     */
    public SharedPrefsUniqueIdService() {
    }

    /**
     * Get the Id based on the passed in clickstreamContext.
     *
     * @param context The Analytics clickstreamContext to use when looking up the id.
     * @return the Id of Analytics clickstreamContext.
     */
    public String getUniqueId(ClickstreamContext context) {
        if (context == null || context.getSystem() == null
            || context.getSystem().getPreferences() == null) {
            LOG.debug("Unable to generate unique id, clickstreamContext has not been fully initialized.");
            return "";
        }

        String uniqueId = getIdFromPreferences(context.getSystem().getPreferences());
        if (uniqueId == null || uniqueId.length() == 0) {
            // an id doesn't exist for this clickstreamContext, create one and persist it
            uniqueId = UUID.randomUUID().toString();
            storeUniqueId(context.getSystem().getPreferences(), uniqueId);
        }

        return uniqueId;
    }

    private String getIdFromPreferences(AndroidPreferences preferences) {
        return preferences.getString(UNIQUE_ID_KEY, null);
    }

    private void storeUniqueId(AndroidPreferences preferences,
                               String uniqueId) {
        try {
            preferences.putString(UNIQUE_ID_KEY, uniqueId);
        } catch (Exception exception) {
            // Do not log ex due to potentially sensitive information
            LOG.error(
                "Exception when trying to store the unique id into the Preferences.");
        }
    }
}

