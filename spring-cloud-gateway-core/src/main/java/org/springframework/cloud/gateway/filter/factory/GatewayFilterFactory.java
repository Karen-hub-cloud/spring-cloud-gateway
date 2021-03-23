package org.springframework.cloud.gateway.filter.factory;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.support.ArgumentHints;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.tuple.Tuple;

/**
 * GatewayFilterFactory 工厂类。
 * 可能是版本不同，看到有的帖子说其继承自GatewayFilter接口
 * @author karen
 */
//这个注解表明它是函数式接口
@FunctionalInterface
public interface GatewayFilterFactory extends ArgumentHints {

	String NAME_KEY = "name";
	String VALUE_KEY = "value";

	GatewayFilter apply(Tuple args);

	default String name() {
		return NameUtils.normalizeFilterName(getClass());
	}
}
