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

import org.apache.dubbo.common.extension.SPI;


/**
 * ChannelHandler. (API, Prototype, ThreadSafe)
 *
 * @see org.apache.dubbo.remoting.Transporter#bind(org.apache.dubbo.common.URL, ChannelHandler)
 * @see org.apache.dubbo.remoting.Transporter#connect(org.apache.dubbo.common.URL, ChannelHandler)
 */
/**
 * @desc:该接口是辅助channel中的逻辑处理，@SPI注解的类，可扩展接口
 * @author: zhaoyibing
 * @time: 2019年5月18日 下午4:48:44
 */
@SPI
public interface ChannelHandler {

    /**
     * on channel connected.
     *
     * @param channel channel.
     */
    /**
     * @desc:连接该通道
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:49:42
     */
    void connected(Channel channel) throws RemotingException;

    /**
     * on channel disconnected.
     *
     * @param channel channel.
     */
    /**
     * @desc:断开该通道
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:50:01
     */
    void disconnected(Channel channel) throws RemotingException;

    /**
     * on message sent.
     *
     * @param channel channel.
     * @param message message.
     */
    /**
     * @desc:往该通道发送消息
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:50:17
     */
    void sent(Channel channel, Object message) throws RemotingException;

    /**
     * on message received.
     *
     * @param channel channel.
     * @param message message.
     */
    void received(Channel channel, Object message) throws RemotingException;

    /**
     * on exception caught.
     *
     * @param channel   channel.
     * @param exception exception.
     */
    /**
     * @desc:从这个通道内捕获异常
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:50:33
     */
    void caught(Channel channel, Throwable exception) throws RemotingException;

}