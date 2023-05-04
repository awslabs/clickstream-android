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

package software.aws.solution.clickstream.util;

import androidx.annotation.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

/**
 * Sample code for custom OkhttpDns.
 */
public final class CustomOkhttpDns implements Dns {

    private static CustomOkhttpDns instance = null;
    private String defaultIp = null;
    private Boolean isResolutionTimeout = false;
    private Boolean isUnKnowHost = false;

    private CustomOkhttpDns() {
    }

    /**
     * get instance for CustomOkhttpDns.
     *
     * @return instance.
     */
    public static CustomOkhttpDns getInstance() {
        if (instance == null) {
            instance = new CustomOkhttpDns();
        }
        return instance;
    }

    /**
     * Lookup method for dns.
     *
     * @param hostname host name.
     * @return list of ip.
     * @throws UnknownHostException exception.
     */
    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        String ip = getIpByHost(hostname);
        if (ip != null) {
            // if ip is not null，use this ip to request network.
            return Arrays.asList(InetAddress.getAllByName(ip));
        }
        // if return null，use the system dns.
        return Dns.SYSTEM.lookup(hostname);
    }

    /**
     * Get ip by host.
     *
     * @param hostname host name
     * @return ip address.
     * @throws UnknownHostException exception.
     */
    public String getIpByHost(String hostname) throws UnknownHostException {
        if (isResolutionTimeout) {
            try {
                Thread.sleep(1001);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        if (isUnKnowHost) {
            throw new UnknownHostException("unknown host exception for test");
        }

        // your dns logic
        return defaultIp;
    }

    /**
     * set default ip for test.
     *
     * @param defaultIp default ip.
     */
    public void setDefaultIp(String defaultIp) {
        this.defaultIp = defaultIp;
    }

    /**
     * set whether ip resolution time out.
     *
     * @param isResolutionTimeout boolean for is ip resolution timeout.
     */
    public void setIsResolutionTimeout(Boolean isResolutionTimeout) {
        this.isResolutionTimeout = isResolutionTimeout;
    }

    /**
     * set whether is unKnow host.
     *
     * @param isUnKnowHost boolean for is unKnow host.
     */
    public void setIsUnKnowHost(Boolean isUnKnowHost) {
        this.isUnKnowHost = isUnKnowHost;
    }
}
