package org.springframework.cloud.gateway.filter.factory;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.support.ArgumentHints;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.tuple.Tuple;

/**
 * GatewayFilterFactory 工厂类。
 *
 * @author karen
 */
@FunctionalInterface
public interface GatewayFilterFactory extends ArgumentHints {

	String NAME_KEY = "name";
	String VALUE_KEY = "value";

	GatewayFilter apply(Tuple args);

	default String name() {
		return NameUtils.normalizeFilterName(getClass());
	}
}
