package org.springframework.cloud.gateway.route;

import reactor.core.publisher.Flux;

/**
 * 组合多种 RouteLocator 的实现类，为 RoutePredicateHandlerMapping 提供统一入口访问路由
 * @author karen
 */
public class CompositeRouteLocator implements RouteLocator {

	private final Flux<RouteLocator> delegates;

	public CompositeRouteLocator(Flux<RouteLocator> delegates) {
		this.delegates = delegates;
	}

	/**
	 * 提供统一方法，将组合的 delegates 的路由全部返回。
	 * @return
	 */
	@Override
	public Flux<Route> getRoutes() {
		return this.delegates.flatMap(RouteLocator::getRoutes);
	}
}
