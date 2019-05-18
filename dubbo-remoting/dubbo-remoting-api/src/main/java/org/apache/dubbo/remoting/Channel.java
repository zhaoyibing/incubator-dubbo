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
package org.apache.dubbo.remoting;

import java.net.InetSocketAddress;

/**
 * Channel. (API/SPI, Prototype, ThreadSafe)
 *
 * @see org.apache.dubbo.remoting.Client
 * @see org.apache.dubbo.remoting.Server#getChannels()
 * @see org.apache.dubbo.remoting.Server#getChannel(InetSocketAddress)
 */
/**
 * @desc:该接口是通道接口，通道是通讯的载体。channel是client和server的传输桥梁。
 * channel 和 client是一一对应的，也就是一个client对应一个channel，
 * channel 和 server是多对一的关系，也就是一个server可以对应多个channel
 * @author: zhaoyibing
 * @time: 2019年5月18日 下午4:44:32
 */
public interface Channel extends Endpoint {

    /**
     * get remote address.
     *
     * @return remote address.
     */
    /**
     * @desc:获得远端地址
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:46:22
     */
    InetSocketAddress getRemoteAddress();

    /**
     * is connected.
     *
     * @return connected
     */
    /**
     * @desc:判断通道是否连接
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:46:39
     */
    boolean isConnected();

    /**
     * has attribute.
     *
     * @param key key.
     * @return has or has not.
     */
    /**
     * @desc:判断是否有该key的值
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:46:50
     */
    boolean hasAttribute(String key);

    /**
     * get attribute.
     *
     * @param key key.
     * @return value.
     */
    /**
     * @desc:获取该key对应的值
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:47:12
     */
    Object getAttribute(String key);

    /**
     * set attribute.
     *
     * @param key   key.
     * @param value value.
     */
    /**
     * @desc:添加属性
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:47:31
     */
    void setAttribute(String key, Object value);

    /**
     * remove attribute.
     *
     * @param key key.
     */
    /**
     * @desc:移除属性
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:47:40
     */
    void removeAttribute(String key);
}