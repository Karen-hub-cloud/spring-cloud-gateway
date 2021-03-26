package org.springframework.cloud.gateway.route;

import reactor.core.publisher.Flux;

/**
 * Route 定位器接口，获得Route 数组
 * 三个实现类：{@link CachingRouteLocator},{@link CompositeRouteLocator},{@link RouteDefinitionRouteLocator}
 * 注意与{@link RouteDefinitionLocator}，{@link RouteDefinitionRouteLocator}的关系
 * @author Spencer Gibb
 */
public interface RouteLocator {

	/**
	 * 获得 getRouteDefinitions数组，进入获得Route 数组
	 * @return
	 */
	Flux<Route> getRoutes();

}
