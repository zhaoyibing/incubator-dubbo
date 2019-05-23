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

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.exchange.support.header.HeaderExchanger;

/**
 * Exchanger. (SPI, Singleton, ThreadSafe)
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Message_Exchange_Pattern">Message Exchange Pattern</a>
 * <a href="http://en.wikipedia.org/wiki/Request-response">Request-Response</a>
 */
/**
 * @desc:数据交换者接口，该接口是一个可扩展接口默认实现的是HeaderExchanger类
 * @author: zhaoyibing
 * @time: 2019年5月23日 上午11:06:47
 */
@SPI(HeaderExchanger.NAME)
public interface Exchanger {

    /**
     * bind.
     *
     * @param url
     * @param handler
     * @return message server
     */
    /**
     * @desc:绑定一个服务器  返回 server
     * @author: zhaoyibing
     * @time: 2019年5月23日 上午11:04:23
     */
    @Adaptive({Constants.EXCHANGER_KEY})
    ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException;

    /**
     * connect.
     *
     * @param url
     * @param handler
     * @return message channel
     */
    /**
     * @desc:连接一个服务器，创建客户端
     * @author: zhaoyibing
     * @time: 2019年5月23日 上午11:04:42
     */
    @Adaptive({Constants.EXCHANGER_KEY})
    ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException;

}