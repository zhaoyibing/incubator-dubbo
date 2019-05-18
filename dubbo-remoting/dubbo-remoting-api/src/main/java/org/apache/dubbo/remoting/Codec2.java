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

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.remoting.buffer.ChannelBuffer;

import java.io.IOException;

/**
 * @desc:编解码器
 * 1、Codec2是一个可扩展的接口，因为有@SPI注解。
 * 2、用到了Adaptive机制，首先去url中寻找key为codec的value，来加载url携带的配置中指定的codec的实现
 * 3、该接口中有个枚举类型DecodeResult，因为解码过程中，需要解决 TCP 拆包、粘包的场景，所以增加了这两种解码结果
 * @author: zhaoyibing
 * @time: 2019年5月18日 下午5:01:04
 */
@SPI
public interface Codec2 {

    /**
     * @desc:编码
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午5:01:45
     */
    @Adaptive({Constants.CODEC_KEY})
    void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException;

    /**
     * @desc:解码
     * @author: zhaoyibing
     * @time: 2019年5月18日 下午5:01:53
     */
    @Adaptive({Constants.CODEC_KEY})
    Object decode(Channel channel, ChannelBuffer buffer) throws IOException;


    enum DecodeResult {
        NEED_MORE_INPUT, SKIP_SOME_INPUT
    }

}

