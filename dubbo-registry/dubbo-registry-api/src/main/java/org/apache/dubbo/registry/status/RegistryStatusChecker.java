/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.registry.status;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.status.Status;
import org.apache.dubbo.common.status.StatusChecker;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

import java.util.Collection;

/**
 * RegistryStatusChecker
 *
 */
/**
 * @desc:注册中心 状态检查
 * @author: zhaoyibing
 * @time: 2019年5月5日 下午4:07:09
 */
@Activate
public class RegistryStatusChecker implements StatusChecker {

    @Override
    public Status check() {
    	// 获得所有的注册中心对象
        Collection<Registry> registries = AbstractRegistryFactory.getRegistries();
        if (registries.isEmpty()) {
            return new Status(Status.Level.UNKNOWN);
        }
        Status.Level level = Status.Level.OK;
        StringBuilder buf = new StringBuilder();
        // 拼接注册中心url中的地址
        for (Registry registry : registries) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(registry.getUrl().getAddress());
            // 如果注册中心的节点不可用，则拼接  disconnected，并且状态设置为error
            if (!registry.isAvailable()) {
                level = Status.Level.ERROR;
                buf.append("(disconnected)");
            } else {
                buf.append("(connected)");
            }
        }
        return new Status(level, buf.toString());
    }

}
