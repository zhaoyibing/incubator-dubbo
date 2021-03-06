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
package org.apache.dubbo.common.extension;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.support.ActivateComparator;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.ArrayUtils;
import org.apache.dubbo.common.utils.ClassHelper;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.dubbo.common.utils.Holder;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Load dubbo extensions
 * <ul>
 * <li>auto inject dependency extension </li>
 * <li>auto wrap extension in wrapper </li>
 * <li>default extension is an adaptive instance</li>
 * </ul>
 *
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">Service Provider in Java 5</a>
 * @see org.apache.dubbo.common.extension.SPI
 * @see org.apache.dubbo.common.extension.Adaptive
 * @see org.apache.dubbo.common.extension.Activate
 */
/**
 * @desc: load extensions
 * @author: zhaoyibing
 * @time: 2019年5月5日 下午4:31:19
 */
public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    // jdk的SPI扩展机制中配置文件路径， dubbo为了兼容jdk的SPI
    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    // 用于用户自定义的扩展实现配置文件存放路径
    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";
    // 用于dubbo内部提供的扩展实现配置文件存放路径
    private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";

    // spi接口实现类的名称key分隔符
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    // 扩展加载器集合，key为扩展接口，例如Protocol等
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    // 扩展实现集合，key为扩展实现类，value为扩展对象
    // 例如key为Class<DubboProtocol>，value为DubboProtocol对象
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // ==============================
    // 扩展接口，如Protocol等
    private final Class<?> type;

    // 对象工厂，获得扩展实现的实例，用于injectExtension方法中将扩展实现类的实例注入到相关的依赖属性。
    // 比如StubProxyFactoryWrapper类中有Protocol protocol属性，就是通过set方法把Protocol的实现类实例赋值
    private final ExtensionFactory objectFactory;
    
    // 以下提到的扩展名就是在配置文件中的key值，类似于“dubbo”等

    // 缓存的扩展名 和 扩展类映射， 和 cacheClasses 的key 、 value 对换
    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();
   
    // 缓存的扩展实现类 key为扩展名 value为类
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    // 扩展名与加有@Activate的自动激活类的映射
    private final Map<String, Object> cachedActivates = new ConcurrentHashMap<>();
    
    // 缓存的扩展对象集合，key为扩展名，value为扩展对象
    // 例如：Protocol 扩展， key为dubbo，value为DubboProtocol
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    
    // 缓存的自适应（@Adaptive）扩展对象，例如：AdaptiveExtensionFactory类的对象
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();
    
    // 缓存的自适应扩展对象的类，例如：AdaptiveExtensionFactory类，一种类型的Adaptive最多只能有一个
    private volatile Class<?> cachedAdaptiveClass = null;
    
    // 缓存的默认扩展名，就是 @SPI中设置的值
    private String cachedDefaultName;
    
    // 创建 cachedAdaptiveInstance 异常
    private volatile Throwable createAdaptiveInstanceError;

    // 扩展wrapper实现类集合
    private Set<Class<?>> cachedWrapperClasses;

    // 扩展名与加载对应扩展类发生的异常的映射
    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

    /**
     *	@desc:构造函数private，不允许外部new。
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:21:15
     */
    private ExtensionLoader(Class<?> type) {
        this.type = type;
        objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }

    /**
     *	@desc:判断接口类上是否有@SPI注解
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:20:14
     */
    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    /**
     *	@desc:根据class获取指定类型的ExtensionLoader。
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:24:05
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        
        // 判断type是否是一个接口类
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }
        // 判断是否为可扩展的接口
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type (" + type +
                    ") is not an extension, because it is NOT annotated with @" + SPI.class.getSimpleName() + "!");
        }

        // 从扩展加载器集合中取出扩展接口对应的扩展加载器
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        // 如果为空，创建该扩展接口的扩展加载器，并且添加到EXTENSION_LOADERS
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    // For testing purposes only
    /**
     *	@desc:
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:29:40
     */
    public static void resetExtensionLoader(Class type) {
        ExtensionLoader loader = EXTENSION_LOADERS.get(type);
        if (loader != null) {
            // Remove all instances associated with this loader as well
            Map<String, Class<?>> classes = loader.getExtensionClasses();
            for (Map.Entry<String, Class<?>> entry : classes.entrySet()) {
                EXTENSION_INSTANCES.remove(entry.getValue());
            }
            classes.clear();
            EXTENSION_LOADERS.remove(type);
        }
    }

    /**
     *	@desc:获取ExtensionLoader的类加载器
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:28:48
     */
    private static ClassLoader findClassLoader() {
        return ClassHelper.getClassLoader(ExtensionLoader.class);
    }

    
    /**
     *	@desc: 通过扩展类实例获取扩展名
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:31:14
     */
    public String getExtensionName(T extensionInstance) {
        return getExtensionName(extensionInstance.getClass());
    }

    /**
     *	@desc:通过扩展类获得扩展名
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:31:49
     */
    public String getExtensionName(Class<?> extensionClass) {
        getExtensionClasses();// load class
        return cachedNames.get(extensionClass);
    }

    /**
     * This is equivalent to {@code getActivateExtension(url, key, null)}
     *
     * @param url url
     * @param key url parameter key which used to get extension point names
     * @return extension list which are activated.
     * @see #getActivateExtension(org.apache.dubbo.common.URL, String, String)
     */
    /**
     *	@desc:获得符合自动激活条件的扩展实现类对象集合（使用没有group条件的自动激活类）
     * 	在所有的激活中，要使用key 指定的扩展
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:31:29
     */
    public List<T> getActivateExtension(URL url, String key) {
        return getActivateExtension(url, key, null);
    }

    /**
     * This is equivalent to {@code getActivateExtension(url, values, null)}
     *
     * @param url    url
     * @param values extension point names
     * @return extension list which are activated
     * @see #getActivateExtension(org.apache.dubbo.common.URL, String[], String)
     */
    public List<T> getActivateExtension(URL url, String[] values) {
        return getActivateExtension(url, values, null);
    }

    /**
     * This is equivalent to {@code getActivateExtension(url, url.getParameter(key).split(","), null)}
     *
     * @param url   url
     * @param key   url parameter key which used to get extension point names
     * @param group group
     * @return extension list which are activated.
     * @see #getActivateExtension(org.apache.dubbo.common.URL, String[], String)
     */
    /**
     *	@desc:获得符合自动激活条件的扩展实现类对象集合（适用含有value和group条件的自动激活类）
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:32:46
     */
    public List<T> getActivateExtension(URL url, String key, String group) {
        String value = url.getParameter(key);
        return getActivateExtension(url, StringUtils.isEmpty(value) ? null : Constants.COMMA_SPLIT_PATTERN.split(value), group);
    }

    /**
     * Get activate extensions.
     *
     * @param url    url
     * @param values extension point names
     * @param group  group
     * @return extension list which are activated
     * @see org.apache.dubbo.common.extension.Activate
     */
    /**
     *	@desc:获得自动激活的扩展对象
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:34:07
     * 	dubbo在构建filter链时非常灵活，有几个关键点在这里做一下总结：
	 * 	1、filter被分为两类，
	 * 		一类是标注了Activate注解的filter，包括dubbo原生的和用户自定义的；
	 * 		一类是用户在spring配置文件中手动注入的filter
	 *	2、对标注了Activate注解的filter，可以通过before、after和order属性来控制它们之间的相对顺序，还可以通过group来区分服务端和消费端
	 *	3、手动注入filter时，可以用default来代表所有标注了Activate注解的filter，以此来控制两类filter之间的顺序
	 *	4、手动注入filter时，可以在filter名称前加一个"-"表示排除某一个filter，比如说如果配置了一个-default的filter，将不再加载所有标注了Activate注解的filter
     */
    public List<T> getActivateExtension(URL url, String[] values, String group) {
        List<T> exts = new ArrayList<>();
        List<String> names = values == null ? new ArrayList<>(0) : Arrays.asList(values);
        
        // 判断不存在配置 `"-name"` 。例如，<dubbo:service filter="-default" /> ，代表移除所有默认过滤器。
        // 如果用户配置的filter列表名称中不包含-default，则加载标注了Activate注解的filter列表
        if (!names.contains(Constants.REMOVE_VALUE_PREFIX + Constants.DEFAULT_KEY)) {
        	
        	// 加载配置文件，获取所有标注有Activate注解的类，存入cachedActivates中
            getExtensionClasses();
            for (Map.Entry<String, Object> entry : cachedActivates.entrySet()) {
                String name = entry.getKey();
                Object activate = entry.getValue();

                String[] activateGroup, activateValue;

                if (activate instanceof Activate) {
                    activateGroup = ((Activate) activate).group();
                    activateValue = ((Activate) activate).value();
                } else if (activate instanceof com.alibaba.dubbo.common.extension.Activate) {
                    activateGroup = ((com.alibaba.dubbo.common.extension.Activate) activate).group();
                    activateValue = ((com.alibaba.dubbo.common.extension.Activate) activate).value();
                } else {
                    continue;
                }
                if (isMatchGroup(group, activateGroup)) {
                	// 通过扩展名获得扩展对象
                    T ext = getExtension(name);
                    // 对于每一个dubbo中原生的filter，需要满足以下3个条件才会被加载：
                    // 1.用户配置的filter列表中不包含该名称的filter
                    // 2.用户配置的filter列表中不包含该名称前加了"-"的filter
                    // 3.该Activate注解被激活，具体激活条件随后详解
                    if (!names.contains(name)
                            && !names.contains(Constants.REMOVE_VALUE_PREFIX + name)
                            && isActive(activateValue, url)) {
                        exts.add(ext);
                    }
                }
            }
            // 排序
            exts.sort(ActivateComparator.COMPARATOR);
        }
        List<T> usrs = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            // 还是判断是否是被移除的配置
            if (!name.startsWith(Constants.REMOVE_VALUE_PREFIX)
                    && !names.contains(Constants.REMOVE_VALUE_PREFIX + name)) {
            	//在配置中把自定义的配置放在自动激活的扩展对象前面，可以让自定义的配置先加载
                //例如，<dubbo:service filter="demo,default,demo2" /> ，则 DemoFilter 就会放在默认的过滤器前面
                if (Constants.DEFAULT_KEY.equals(name)) {
                    if (!usrs.isEmpty()) {
                        exts.addAll(0, usrs);
                        usrs.clear();
                    }
                } else {
                    T ext = getExtension(name);
                    usrs.add(ext);
                }
            }
        }
        if (!usrs.isEmpty()) {
            exts.addAll(usrs);
        }
        return exts;
    }

    /**
     *	@desc:判断group值是否合法，匹配分组
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:38:55
     */
    private boolean isMatchGroup(String group, String[] groups) {
        if (StringUtils.isEmpty(group)) {
            return true;
        }
        if (groups != null && groups.length > 0) {
            for (String g : groups) {
                if (group.equals(g)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *	@desc:判断是否被激活加载
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:26:30
     */
    private boolean isActive(String[] keys, URL url) {
    	// 如果注解没有配置value属性，则一定是激活的
        if (keys.length == 0) {
            return true;
        }
        for (String key : keys) {
        	// 对配置了value属性的注解，如果服务的url属性中存在与value属性值相匹配的属性且改属性值不为空，则该注解也是激活的
            for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if ((k.equals(key) || k.endsWith("." + key))
                        && ConfigUtils.isNotEmpty(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get extension's instance. Return <code>null</code> if extension is not found or is not initialized. Pls. note
     * that this method will not trigger extension load.
     * <p>
     * In order to trigger extension load, call {@link #getExtension(String)} instead.
     *
     * @see #getExtension(String)
     */
    /**
     *	@desc:通过扩展名获得扩展接口实现类的实例。此方法不会触发extension的加载操作
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:24:29
     */
    @SuppressWarnings("unchecked")
    public T getLoadedExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        Holder<Object> holder = getOrCreateHolder(name);
        return (T) holder.get();
    }

    /**
     *	@desc:创建扩展类的实例对象缓存
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:54:44
     */
    private Holder<Object> getOrCreateHolder(String name) {
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        return holder;
    }

    /**
     * Return the list of extensions which are already loaded.
     * <p>
     * Usually {@link #getSupportedExtensions()} should be called in order to get all extensions.
     *
     * @see #getSupportedExtensions()
     */
    public Set<String> getLoadedExtensions() {
        return Collections.unmodifiableSet(new TreeSet<>(cachedInstances.keySet()));
    }

    public Object getLoadedAdaptiveExtensionInstances() {
        return cachedAdaptiveInstance.get();
    }

    /**
     * Find the extension with the given name. If the specified name is not found, then {@link IllegalStateException}
     * will be thrown.
     */
    /**
     *	@desc:根据扩展名获取类实例对象
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:54:00
     */
    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        // 查找默认的扩展实现，也就是@SPI中的默认值作为key
        if ("true".equals(name)) {
            return getDefaultExtension();
        }
        // 缓存中获取对应的扩展对象
        Holder<Object> holder = getOrCreateHolder(name);
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                	// 通过扩展名创建接口实现类的实例
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * Return default extension, return <code>null</code> if it's not configured.
     */
    /**
     *	@desc:获得默认的类实例，如果没有配置，返回null
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:21:49
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName) || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    /**
     *	@desc:判断是否存在扩展名name
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:20:43
     */
    public boolean hasExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        Class<?> c = this.getExtensionClass(name);
        return c != null;
    }

    /**
     *	@desc: 获得type接口所有的扩展名集合
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:28:28
     */
    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<>(clazzes.keySet()));
    }

    /**
     * Return default extension name, return <code>null</code> if not configured.
     */
    /**
     *	@desc: 获取接口上配置的默认值
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:29:27
     */
    public String getDefaultExtensionName() {
        getExtensionClasses();
        return cachedDefaultName;
    }

    /**
     * Register new extension via API
     *
     * @param name  extension name
     * @param clazz extension class
     * @throws IllegalStateException when extension with the same name has already been registered.
     */
    /**
     *	@desc:通过api注册新的实现
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:30:16
     */
    public void addExtension(String name, Class<?> clazz) {
        getExtensionClasses(); // load classes
        // clazz必须 implements type接口
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Input type " +
                    clazz + " doesn't implement the Extension " + type);
        }
        // clazz不允许是接口
        if (clazz.isInterface()) {
            throw new IllegalStateException("Input type " +
                    clazz + " can't be interface!");
        }

        // clazz 类上无@Adaptive注解
        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("Extension name is blank (Extension " + type + ")!");
            }
            if (cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("Extension name " +
                        name + " already exists (Extension " + type + ")!");
            }
            
            // 更新两个缓存
            cachedNames.put(clazz, name);
            cachedClasses.get().put(name, clazz);
        } else {
        	// clazz 类上有 @Adaptive注解
            if (cachedAdaptiveClass != null) {
                throw new IllegalStateException("Adaptive Extension already exists (Extension " + type + ")!");
            }

            cachedAdaptiveClass = clazz;
        }
    }

    /**
     * Replace the existing extension via API
     *
     * @param name  extension name
     * @param clazz extension class
     * @throws IllegalStateException when extension to be placed doesn't exist
     * @deprecated not recommended any longer, and use only when test
     */
    /**
     *	@desc:过时，只在测试的时候用
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:35:31
     */
    @Deprecated
    public void replaceExtension(String name, Class<?> clazz) {
        getExtensionClasses(); // load classes

        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Input type " +
                    clazz + " doesn't implement Extension " + type);
        }
        if (clazz.isInterface()) {
            throw new IllegalStateException("Input type " +
                    clazz + " can't be interface!");
        }

        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("Extension name is blank (Extension " + type + ")!");
            }
            if (!cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("Extension name " +
                        name + " doesn't exist (Extension " + type + ")!");
            }

            cachedNames.put(clazz, name);
            cachedClasses.get().put(name, clazz);
            cachedInstances.remove(name);
        } else {
            if (cachedAdaptiveClass == null) {
                throw new IllegalStateException("Adaptive Extension doesn't exist (Extension " + type + ")!");
            }

            cachedAdaptiveClass = clazz;
            cachedAdaptiveInstance.set(null);
        }
    }

    /**
     *	@desc:获得接口适配器类
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:36:50
     */
    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if (createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("Failed to create adaptive instance: " + t.toString(), t);
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Failed to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
            }
        }

        return (T) instance;
    }

    /**
     *	@desc:根据扩展名查找异常信息
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午9:20:02
     */
    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);


        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(StringUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }

    /**
     *	@desc:通过扩展名创建对应扩展类的实例对象
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:53:42
     */
    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
        	// 看缓存中是否有该类的对象
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            // 向对象中注入依赖的属性（自动装配）
            injectExtension(instance);
            // 创建 Wrapper 扩展对象（自动包装）
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (CollectionUtils.isNotEmpty(wrapperClasses)) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance (name: " + name + ", class: " +
                    type + ") couldn't be instantiated: " + t.getMessage(), t);
        }
    }

    /**
     *	@desc:向创建的扩展类 注入其依赖的属性
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:38:20
     */
    private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {
            	// 反射获取所有的方法
                for (Method method : instance.getClass().getMethods()) {
                	// 是set方法
                    if (isSetter(method)) {
                        /**
                         * Check {@link DisableInject} to see if we need auto injection for this property
                         */
                        if (method.getAnnotation(DisableInject.class) != null) {
                            continue;
                        }
                        // 获取参数的类型，如果参数是基本类型(boolean,char,byte,short,int,float,double,long,)或者是日期的子类，num的子类等无法注入的类型
                        Class<?> pt = method.getParameterTypes()[0];
                        if (ReflectUtils.isPrimitives(pt)) {
                            continue;
                        }
                        try {
                        	// 获得属性，比如：StubProxyFactoryWrapper 类中有Protocol protocol属性
                            String property = getSetterProperty(method);
                            // 获得属性值，比如Protocol对象，也可能是Bean对象
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                            	// 注入依赖属性
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            logger.error("Failed to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }

    /**
     * get properties name for setter, for instance: setVersion, return "version"
     * <p>
     * return "", if setter name with length less than 3
     */
    /**
     *	@desc:获得setter方法对应属性名字
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:46:57
     */
    private String getSetterProperty(Method method) {
        return method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
    }

    /**
     * return true if and only if:
     * <p>
     * 1, public
     * <p>
     * 2, name starts with "set"
     * <p>
     * 3, only has one parameter
     */
    /**
     *	@desc:判断一个方法是否为set方法
     *    	方法名以set开头
     *    	参数有1个
     *    	方法是public的
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:39:36
     */
    private boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getParameterTypes().length == 1
                && Modifier.isPublic(method.getModifiers());
    }

    /**
     *	@desc:根据扩展名获取对应的扩展实现类
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:24:48
     */
    private Class<?> getExtensionClass(String name) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Extension name == null");
        }
        return getExtensionClasses().get(name);
    }

    /**
     *	@desc:获得扩展类信息，设置 cachedClasses 属性
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:32:35
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    // synchronized in getExtensionClasses
    /**
     *	@desc:spi扩展实现，加载META-INF/dubbo/internal/,META-INF/dubbo/,META-INF/services/ 目录下的配置文件
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:36:37
     */
    private Map<String, Class<?>> loadExtensionClasses() {
        cacheDefaultExtensionName();

        Map<String, Class<?>> extensionClasses = new HashMap<>();
        loadDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY, type.getName());
        loadDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY, type.getName().replace("org.apache", "com.alibaba"));
        loadDirectory(extensionClasses, DUBBO_DIRECTORY, type.getName());
        loadDirectory(extensionClasses, DUBBO_DIRECTORY, type.getName().replace("org.apache", "com.alibaba"));
        loadDirectory(extensionClasses, SERVICES_DIRECTORY, type.getName());
        loadDirectory(extensionClasses, SERVICES_DIRECTORY, type.getName().replace("org.apache", "com.alibaba"));
        return extensionClasses;
    }

    /**
     * extract and cache default extension name if exists
     */
    /**
     *	@desc:提取并缓存 cacheDefaultName
     * 	@author：zhaoyibing
     * 	@time：2019年5月6日 下午8:18:32
     * 
     */
    private void cacheDefaultExtensionName() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
            	
            	// @SPI注解上配置的默认实现类的key，不允许有英文逗号
                String[] names = NAME_SEPARATOR.split(value);
                // 只允许有一个默认值
                if (names.length > 1) {
                    throw new IllegalStateException("More than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if (names.length == 1) {
                    cachedDefaultName = names[0];
                }
            }
        }
    }

    /**
     *	@desc:加载dir目录下的type类型的文件
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:39:04
     *  @param extensionClasses 存储k-->v格式数据的载体
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir, String type) {
    	// 拼接接口全限定名，得到完整的文件名
        String fileName = dir + type;
        try {
            Enumeration<java.net.URL> urls;
            // 获取类加载器
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
            	// 遍历文件
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    /**
     *	@desc:加载文件中的内容
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:43:56
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                	// 跳过被 # 注释的内容
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = null;
                            int i = line.indexOf('=');
                            if (i > 0) {
                            	// 根据 "=" 拆分key和value
                                name = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() > 0) {
                            	// 加载扩展类
                                loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
                            }
                        } catch (Throwable t) {
                            IllegalStateException e = new IllegalStateException("Failed to load extension class (interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                            exceptions.put(line, e);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    /**
     *	@desc: 根据配置文件中的value加载扩展类
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:46:14
     */
    private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {
    	// clazz 是否实现扩展接口 type
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error occurred when loading extension class (interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + " is not subtype of interface.");
        }
        // 判断该类是否为扩展接口的适配器
        if (clazz.isAnnotationPresent(Adaptive.class)) {
        	// 缓存适配器类
            cacheAdaptiveClass(clazz);
        } else if (isWrapperClass(clazz)) {
        	// 缓存包装类型
            cacheWrapperClass(clazz);
        } else {
        	// 默认的无参构造函数，反射获取默认的无参构造器
            clazz.getConstructor();
            if (StringUtils.isEmpty(name)) {
                name = findAnnotationName(clazz);
                if (name.length() == 0) {
                    throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + resourceURL);
                }
            }

            String[] names = NAME_SEPARATOR.split(name);
            if (ArrayUtils.isNotEmpty(names)) {
                cacheActivateClass(clazz, names[0]);
                for (String n : names) {
                    cacheName(clazz, n);
                    saveInExtensionClass(extensionClasses, clazz, name);
                }
            }
        }
    }

    /**
     * cache name
     */
    /**
     * @desc:扩展名 和 扩展类映射
     * @author: zhaoyibing
     * @time: 2019年5月6日 下午5:44:42
     */
    private void cacheName(Class<?> clazz, String name) {
        if (!cachedNames.containsKey(clazz)) {
            cachedNames.put(clazz, name);
        }
    }

    /**
     * put clazz in extensionClasses
     */
    /**
     * @desc:将class保存到extensionClasses，extensionClasses最后保存到 cachedClasses
     * @author: zhaoyibing
     * @time: 2019年5月6日 下午5:48:14
     * 
     * @see #loadExtensionClasses()
     */
    private void saveInExtensionClass(Map<String, Class<?>> extensionClasses, Class<?> clazz, String name) {
        Class<?> c = extensionClasses.get(name);
        if (c == null) {
            extensionClasses.put(name, clazz);
        } else if (c != clazz) {
            throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + name + " on " + c.getName() + " and " + clazz.getName());
        }
    }

    /**
     * cache Activate class which is annotated with <code>Activate</code>
     * <p>
     * for compatibility, also cache class with old alibaba Activate annotation
     */
    /**
     *	@desc:缓存@Activate注解的类，设置cachedActivates属性，同时支持com.alibaba.**.@Activate注解
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午9:59:54
     */
    private void cacheActivateClass(Class<?> clazz, String name) {
    	// 首先查找默认的 Activate 注解
        Activate activate = clazz.getAnnotation(Activate.class);
        if (activate != null) {
            cachedActivates.put(name, activate);
        } else {
            // support com.alibaba.dubbo.common.extension.Activate
        	// 查找旧版本的com.alibaba.**.Activate注解
            com.alibaba.dubbo.common.extension.Activate oldActivate = clazz.getAnnotation(com.alibaba.dubbo.common.extension.Activate.class);
            if (oldActivate != null) {
                cachedActivates.put(name, oldActivate);
            }
        }
    }

    /**
     * cache Adaptive class which is annotated with <code>Adaptive</code>
     */
    /**
     *	@desc: 缓存适配器类，只允许最多有一个
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午10:03:18
     */
    private void cacheAdaptiveClass(Class<?> clazz) {
        if (cachedAdaptiveClass == null) {
            cachedAdaptiveClass = clazz;
        } else if (!cachedAdaptiveClass.equals(clazz)) {
            throw new IllegalStateException("More than 1 adaptive class found: "
                    + cachedAdaptiveClass.getClass().getName()
                    + ", " + clazz.getClass().getName());
        }
    }

    /**
     * cache wrapper class
     * <p>
     * like: ProtocolFilterWrapper, ProtocolListenerWrapper
     */
    /**
     * @desc:缓存cachedWrapperClasses
     * @author: zhaoyibing
     * @time: 2019年5月6日 下午5:45:27
     */
    private void cacheWrapperClass(Class<?> clazz) {
        if (cachedWrapperClasses == null) {
            cachedWrapperClasses = new ConcurrentHashSet<>();
        }
        cachedWrapperClasses.add(clazz);
    }

    /**
     * test if clazz is a wrapper class
     * <p>
     * which has Constructor with given class type as its only argument
     */
    /**
     *	@desc:判断类是否为包装类，根据构造方法的参数判断
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午10:04:44
     */
    private boolean isWrapperClass(Class<?> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     *	@desc:DemoFilter类生成扩展名为demo
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午10:07:29
     */
    @SuppressWarnings("deprecation")
    private String findAnnotationName(Class<?> clazz) {
        org.apache.dubbo.common.Extension extension = clazz.getAnnotation(org.apache.dubbo.common.Extension.class);
        if (extension == null) {
            String name = clazz.getSimpleName();
            if (name.endsWith(type.getSimpleName())) {
                name = name.substring(0, name.length() - type.getSimpleName().length());
            }
            return name.toLowerCase();
        }
        return extension.value();
    }

    /**
     *	@desc: 创建适配器对象
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午10:08:27
     */
    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can't create adaptive extension " + type + ", cause: " + e.getMessage(), e);
        }
    }

    /**
     *	@desc:获得接口适配类
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午10:09:38
     * 
     *  @see #loadClass(Map, java.net.URL, Class, String)
     */
    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }

    /**
     *	@desc:创建适配器类，类似于dubbo动态生成的Transporter$Adpative这样的类
     * 	@author：zhaoyibing
     * 	@time：2019年5月5日 下午10:13:43
     */
    private Class<?> createAdaptiveExtensionClass() {
    	// 创建动态生成的适配器类代码
        String code = new AdaptiveClassCodeGenerator(type, cachedDefaultName).generate();
        ClassLoader classLoader = findClassLoader();
        org.apache.dubbo.common.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(org.apache.dubbo.common.compiler.Compiler.class).getAdaptiveExtension();
        // 编译代码，返回该类
        return compiler.compile(code, classLoader);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + type.getName() + "]";
    }

}
