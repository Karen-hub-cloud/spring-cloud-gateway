package org.springframework.cloud.gateway.filter.factory;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.support.ArgumentHints;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.tuple.Tuple;

/**
 * GatewayFilterFactory 工厂类。
 * 可能是版本不同，看到有的帖子说其继承自GatewayFilter接口
 * 共16个实现类，分类为Header、Parameter、Path、Status、Redirect、Hystrix、RateLimiter。
 * 注意如果采用配置的构建，命名是有规范的。（Spring boot遵循规约大于配置的原则）
 * 例如：
 * spring:
 *     cloud:
 *       gateway:
 *         routes:
 *         # =====================================
 *         - id: add_request_parameter_route
 *           uri: http://example.org
 *           filters:
 *           - AddRequestParameter=foo, bar
 *           predicate:
 *           -
 * 1.filter的配置会绑定到一个FilterDefinition对象
 * AddRequestParameter对应FilterDefinition中的name，
 * AddRequestParameter为AddRequestHeaderGatewayFilterFactory的类名前缀
 * foo, bar会被解析成FilterDefinition中的MAp类型属性：args。
 * 详细请看{@link org.springframework.cloud.gateway.filter.FilterDefinition}
 * 2.predicate同理
 * 详细请看{@link org.springframework.cloud.gateway.handler.predicate.PredicateDefinition}
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
