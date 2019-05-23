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
package org.apache.dubbo.remoting.exchange;

import org.apache.dubbo.remoting.ChannelHandler;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.telnet.TelnetHandler;

import java.util.concurrent.CompletableFuture;

/**
 * ExchangeHandler. (API, Prototype, ThreadSafe)
 */
/**
 * @desc:该接口是信息交换处理器接口
 * @author: zhaoyibing
 * @time: 2019年5月20日 下午6:00:34
 */
public interface ExchangeHandler extends ChannelHandler, TelnetHandler {

    /**
     * reply.
     *
     * @param channel
     * @param request
     * @return response
     * @throws RemotingException
     */
    /**
     * @desc: 回复请求结果
     * @author: zhaoyibing
     * @time: 2019年5月20日 下午6:01:23
     */
    CompletableFuture<Object> reply(ExchangeChannel channel, Object request) throws RemotingException;

}