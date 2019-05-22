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

/**
 * Callback
 */
/**
 * @desc:返回回调
 * 
 * @see org.apache.dubbo.remoting.exchange.support.DefaultFuture#received(org.apache.dubbo.remoting.Channel, Response) (回调的调用处)
 * 
 * @author: zhaoyibing
 * @time: 2019年5月22日 下午5:15:30
 */
public interface ResponseCallback {

    /**
     * done.
     *
     * @param response
     */
    /**
     * @desc:完成时回调
     * @author: zhaoyibing
     * @time: 2019年5月22日 下午6:07:17
     */
    void done(Object response);

    /**
     * caught exception.
     *
     * @param exception
     */
    /**
     * @desc:抛出异常时回调此方法
     * @author: zhaoyibing
     * @time: 2019年5月22日 下午6:07:25
     */
    void caught(Throwable exception);

}