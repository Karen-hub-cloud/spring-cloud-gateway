package org.springframework.cloud.gateway.route;

import reactor.core.publisher.Flux;

/**
 * 组合多种RouteDefinitionLocator的实现
 * @author Karen
 */
public class CompositeRouteDefinitionLocator implements RouteDefinitionLocator {

    /**
     * RouteDefinitionLocator 数组
     */
	private final Flux<RouteDefinitionLocator> delegates;

	public CompositeRouteDefinitionLocator(Flux<RouteDefinitionLocator> delegates) {
		this.delegates = delegates;
	}

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		return this.delegates.flatMap(RouteDefinitionLocator::getRouteDefinitions);
	}

}
