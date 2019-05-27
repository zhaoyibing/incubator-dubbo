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

import java.util.Map;

/**
 * Invocation. (API, Prototype, NonThreadSafe)
 *
 * @serial Don't change the class name and package name.
 * @see org.apache.dubbo.rpc.Invoker#invoke(Invocation)
 * @see org.apache.dubbo.rpc.RpcInvocation
 */
/**
 * @desc:Invocation 是会话域，它持有调用过程中的变量，比如方法名，参数等。
 * @author: zhaoyibing
 * @time: 2019年5月27日 下午3:26:44
 */
public interface Invocation {

    /**
     * get method name.
     *
     * @return method name.
     * @serial
     */
    /**
     * @desc:获得方法名称
     * @author: zhaoyibing
     * @time: 2019年5月27日 下午3:26:47
     */
    String getMethodName();

    /**
     * get parameter types.
     *
     * @return parameter types.
     * @serial
     */
    Class<?>[] getParameterTypes();

    /**
     * get arguments.
     *
     * @return arguments.
     * @serial
     */
    Object[] getArguments();

    /**
     * get attachments.
     *
     * @return attachments.
     * @serial
     */
    /**
     * @desc:获得附加值集合
     * @author: zhaoyibing
     * @time: 2019年5月27日 下午3:27:35
     */
    Map<String, String> getAttachments();

    /**
     * get attachment by key.
     *
     * @return attachment value.
     * @serial
     */
    /**
     * @desc:获得附加值
     * @author: zhaoyibing
     * @time: 2019年5月27日 下午3:27:52
     */
    String getAttachment(String key);

    /**
     * get attachment by key with default value.
     *
     * @return attachment value.
     * @serial
     */
    /**
     * @desc:获得附加值
     * @author: zhaoyibing
     * @time: 2019年5月27日 下午3:28:11
     */
    String getAttachment(String key, String defaultValue);

    /**
     * get the invoker in current context.
     *
     * @return invoker.
     * @transient
     */
    /**
     * @desc:获得当前上下文的invoker
     * @author: zhaoyibing
     * @time: 2019年5月27日 下午3:28:16
     */
    Invoker<?> getInvoker();

}