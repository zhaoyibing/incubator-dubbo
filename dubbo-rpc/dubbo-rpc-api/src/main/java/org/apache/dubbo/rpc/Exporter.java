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
package org.apache.dubbo.rpc;

/**
 * Exporter. (API/SPI, Prototype, ThreadSafe)
 *
 * @see org.apache.dubbo.rpc.Protocol#export(Invoker)
 * @see org.apache.dubbo.rpc.ExporterListener
 * @see org.apache.dubbo.rpc.protocol.AbstractExporter
 */
/**
 * @desc:该接口是暴露服务的接口，定义了两个方法分别是获得invoker和取消暴露服务。
 * @author: zhaoyibing
 * @time: 2019年5月27日 下午3:32:42
 */
public interface Exporter<T> {

    /**
     * get invoker.
     *
     * @return invoker
     */
    /**
     * @desc:获得对应的实体域invoker
     * @author: zhaoyibing
     * @time: 2019年5月27日 下午3:33:37
     */
    Invoker<T> getInvoker();

    /**
     * unexport.
     * <p>
     * <code>
     * getInvoker().destroy();
     * </code>
     */
    /**
     * @desc:取消暴露
     * @author: zhaoyibing
     * @time: 2019年5月27日 下午3:33:26
     */
    void unexport();

}