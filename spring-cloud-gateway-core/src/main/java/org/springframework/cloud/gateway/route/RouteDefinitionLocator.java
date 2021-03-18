package org.springframework.cloud.gateway.route;

import reactor.core.publisher.Flux;

/**
 * 路由定义定位器接口
 *
 * @author Spencer Gibb
 */
public interface RouteDefinitionLocator {

	/**
	 * 获取服务的RouteDefinition数组
	 * @return
	 */
	Flux<RouteDefinition> getRouteDefinitions();
}
