package org.springframework.cloud.gateway.config;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import reactor.core.publisher.Flux;

/**
 * 从配置文件( 例如，YML / Properties 等 ) 读取路由配置数组
 * @author karen
 */
public class PropertiesRouteDefinitionLocator implements RouteDefinitionLocator {

	private final GatewayProperties properties;

	public PropertiesRouteDefinitionLocator(GatewayProperties properties) {
		this.properties = properties;
	}

	/**
	 * 从配置文件( 例如，YML / Properties 等 ) 读取路由配置
	 * @return
	 */
	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		return Flux.fromIterable(this.properties.getRoutes());
	}
}
