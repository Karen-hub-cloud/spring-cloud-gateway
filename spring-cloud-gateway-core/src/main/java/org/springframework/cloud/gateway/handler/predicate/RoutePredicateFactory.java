package org.springframework.cloud.gateway.handler.predicate;

import org.springframework.cloud.gateway.support.ArgumentHints;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

/**
 * 路由谓语工厂接口：
 * Spring Cloud Gateway 创建 Route 对象时，使用 RoutePredicateFactory 创建 Predicate 对象。
 * Predicate 对象可以赋值给 Route.predicate 属性，用于匹配请求对应的 Route 。
 * @author karen
 */
@FunctionalInterface
public interface RoutePredicateFactory extends ArgumentHints {

    String PATTERN_KEY = "pattern";

	/**
	 * 接口方法，创建 Predicate，有十种实现，
	 * 例如AfterRoutePredicateFactory，BeforeRoutePredicateFactory。。。
	 * @param args
	 * @return
	 */
	Predicate<ServerWebExchange> apply(Tuple args);

	/**
	 * 获得 RoutePredicateFactory 的名字。该方法截取类名前半段，
	 * 例如 QueryRoutePredicateFactory 的结果为 Query
	 * @return
	 */
	default String name() {
		return NameUtils.normalizePredicateName(getClass());
	}

}
