package com.alibaba.dubbo.common.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value= {
		ElementType.CONSTRUCTOR,
		ElementType.FIELD,
		ElementType.LOCAL_VARIABLE,
		ElementType.METHOD,
		ElementType.PARAMETER
})
public @interface ZhaoYiBing {

	String value() default "";
	
	String date() default "";
}
