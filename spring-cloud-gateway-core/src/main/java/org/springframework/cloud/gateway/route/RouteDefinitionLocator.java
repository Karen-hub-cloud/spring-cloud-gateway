package org.springframework.cloud.gateway.route;

import reactor.core.publisher.Flux;

/**
 * 路由定义定位器接口：从哪里读取
 * 有5种实现，缓存、组合、注册中心列表、内存、配置文件。具体看实现类
 * @author Spencer Gibb
 */
public interface RouteDefinitionLocator {

	/**
	 * 获取服务的RouteDefinition数组
	 * @return
	 */
	Flux<RouteDefinition> getRouteDefinitions();
}
