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

/**
 * Remoting Client. (API/SPI, Prototype, ThreadSafe)
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Client%E2%80%93server_model">Client/Server</a>
 *
 * @see org.apache.dubbo.remoting.Transporter#connect(org.apache.dubbo.common.URL, ChannelHandler)
 */
/**
 * @desc:客户端接口，继承Channel是因为客户端跟通道是一一对应的
 * @author: zhaoyibing
 * @time: 2019年5月18日 下午4:51:31
 */
public interface Client extends Endpoint, Channel, Resetable, IdleSensible {

    /**
     * reconnect.
     */
    /**
     * @desc:重连
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:52:26
     */
    void reconnect() throws RemotingException;

    /**
     * @desc:重置
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午4:52:32
     */
    @Deprecated
    void reset(org.apache.dubbo.common.Parameters parameters);

}
