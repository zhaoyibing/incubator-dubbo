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

package org.apache.dubbo.remoting.exchange.support.header;

import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.Request;

/**
 * HeartbeatTimerTask
 */
/**
 * @desc:心跳监测任务
 * @author: zhaoyibing
 * @time: 2019年5月20日 下午5:28:54
 */
public class HeartbeatTimerTask extends AbstractTimerTask {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatTimerTask.class);

    // 心跳间隔 单位：ms
    private final int heartbeat;

    HeartbeatTimerTask(ChannelProvider channelProvider, Long heartbeatTick, int heartbeat) {
        super(channelProvider, heartbeatTick);
        this.heartbeat = heartbeat;
    }

    /**
     * @desc:心跳监测
     * @author: zhaoyibing
     * @time: 2019年5月22日 下午2:35:11
     */
    @Override
    protected void doTask(Channel channel) {
        try {
        	// 最后读
            Long lastRead = lastRead(channel);
            // 最后写
            Long lastWrite = lastWrite(channel);
            // 最后读的时间不空 当前时间减去最后读的时间超过监测周期
            if ((lastRead != null && now() - lastRead > heartbeat)
            		//或者 最后写的时间不空  当前时间减去最后写的时间超过监测周期
                    || (lastWrite != null && now() - lastWrite > heartbeat)) {
            	// 通道发送HEARTBEAT_EVENT
                Request req = new Request();
                req.setVersion(Version.getProtocolVersion());
                req.setTwoWay(true);
                req.setEvent(Request.HEARTBEAT_EVENT);
                channel.send(req);
                if (logger.isDebugEnabled()) {
                    logger.debug("Send heartbeat to remote channel " + channel.getRemoteAddress()
                            + ", cause: The channel has no data-transmission exceeds a heartbeat period: "
                            + heartbeat + "ms");
                }
            }
        } catch (Throwable t) {
            logger.warn("Exception when heartbeat to remote channel " + channel.getRemoteAddress(), t);
        }
    }
}
