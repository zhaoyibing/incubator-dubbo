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

import org.apache.dubbo.common.Resetable;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Remoting Server. (API/SPI, Prototype, ThreadSafe)
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Client%E2%80%93server_model">Client/Server</a>
 *
 * @see org.apache.dubbo.remoting.Transporter#bind(org.apache.dubbo.common.URL, ChannelHandler)
 */
/**
 * @desc:服务端接口，继承Endpoint是因为服务端也是一个点
 * @author: zhaoyibing
 * @time: 2019年5月18日 下午4:55:23
 */
public interface Server extends Endpoint, Resetable, IdleSensible {

    /**
     * is bound.
     *
     * @return bound
     */
    /**
     * @desc:判断是否绑定到本地端口，也就是该服务是否启动成功，能够连接、接收消息，提供服务
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:56:38
     */
    boolean isBound();

    /**
     * get channels.
     *
     * @return channels
     */
    /**
     * @desc:获得连接该服务器的通道，也就是客户端，channel和client一一对应
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:57:34
     */
    Collection<Channel> getChannels();

    /**
     * get channel.
     *
     * @param remoteAddress
     * @return channel
     */
    /**
     * @desc:通过远程地址获取通道
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:58:11
     */
    Channel getChannel(InetSocketAddress remoteAddress);

    @Deprecated
    void reset(org.apache.dubbo.common.Parameters parameters);

}
