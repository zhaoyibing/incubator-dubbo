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

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.RemotingException;

/**
 * ExchangeChannel. (API/SPI, Prototype, ThreadSafe)
 */
/**
 * @desc:该接口是信息交换通道接口，有四个方法，前两个是发送请求消息，区别就是第二个发送请求有超时的参数，
 * @author: zhaoyibing
 * @time: 2019年5月20日 下午4:46:55
 */
public interface ExchangeChannel extends Channel {

    /**
     * send request.
     *
     * @param request
     * @return response future
     * @throws RemotingException
     */
    ResponseFuture request(Object request) throws RemotingException;

    /**
     * send request.
     *
     * @param request
     * @param timeout
     * @return response future
     * @throws RemotingException
     */
    ResponseFuture request(Object request, int timeout) throws RemotingException;

    /**
     * get message handler.
     *
     * @return message handler
     */
    /**
     * @desc:返回一个信息交换处理器
     * @author: zhaoyibing
     * @time: 2019年5月20日 下午4:47:38
     */
    ExchangeHandler getExchangeHandler();

    /**
     * graceful close.
     *
     * @param timeout
     */
    @Override
    void close(int timeout);
}
