package org.springframework.cloud.gateway.route;

import reactor.core.publisher.Flux;

/**
 * 路由定义定位器接口：从哪里读取?
 * 有5种实现，
 * 缓存 {@link CachingRouteDefinitionLocator}
 * 组合 {@link CompositeRouteDefinitionLocator}
 * 注册中心列表 {@link org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator}
 * 内存 {@link InMemoryRouteDefinitionRepository}
 * 配置文件 {@link org.springframework.cloud.gateway.config.PropertiesRouteDefinitionLocator}
 * @author Karen
 */
public interface RouteDefinitionLocator {

	/**
	 * 获取服务的RouteDefinition数组
	 * @return
	 */
	Flux<RouteDefinition> getRouteDefinitions();
}
