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

import org.apache.dubbo.common.URL;

import java.net.InetSocketAddress;

/**
 * Endpoint. (API/SPI, Prototype, ThreadSafe)
 *
 *
 * @see org.apache.dubbo.remoting.Channel
 * @see org.apache.dubbo.remoting.Client
 * @see org.apache.dubbo.remoting.Server
 */
public interface Endpoint {

    /**
     * get url.
     *
     * @return url
     */
    /**
     * @desc:获得该端的url
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:36:32
     */
    URL getUrl();

    /**
     * get channel handler.
     *
     * @return channel handler
     */
    /**
     * @desc:获得该端的通道处理器
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:37:04
     */
    ChannelHandler getChannelHandler();

    /**
     * get local address.
     *
     * @return local address.
     */
    /**
     * @desc:获得该端的本地地址
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:37:24
     */
    InetSocketAddress getLocalAddress();

    /**
     * send message.
     *
     * @param message
     * @throws RemotingException
     */
    /**
     * @desc:发送消息
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:37:48
     */
    void send(Object message) throws RemotingException;

    /**
     * send message.
     *
     * @param message
     * @param sent    already sent to socket?
     */
    /**
     * @desc:
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:37:57
     * @see org.apache.dubbo.remoting.transport.netty4.NettyChannel
     */
    void send(Object message, boolean sent) throws RemotingException;

    /**
     * close the channel.
     */
    /**
     * @desc:关闭
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:40:40
     */
    void close();

    /**
     * Graceful close the channel.
     */
    /**
     * @desc:优雅的关闭，加入了等待时间
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:40:56
     */
    void close(int timeout);

    /**
     * @desc:开始关闭
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:41:27
     */
    void startClose();

    /**
     * is closed.
     *
     * @return closed
     */
    /**
     * @desc:判断是否已关闭
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:41:34
     */
    boolean isClosed();

}