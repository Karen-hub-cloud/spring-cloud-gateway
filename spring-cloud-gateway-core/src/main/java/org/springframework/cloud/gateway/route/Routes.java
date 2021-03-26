package org.springframework.cloud.gateway.route;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilters;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;

/**
 * 注意与{@link Route}的区别
 * 我们在自定义RouteLocator类时，会通过Routes来创建多个Route，详见自定义的RouteLocator类
 * {@link org.springframework.cloud.gateway.sample.GatewaySampleApplication#customRouteLocator}
 * @author karen
 */
public class Routes {

	public static LocatorBuilder locator() {
		return new LocatorBuilder();
	}

	/**
	 * 用于创建RouteLocator组件
	 */
	public static class LocatorBuilder {

		//LocatorBuilder已经创建好的Route数组
		private List<Route> routes = new ArrayList<>();

		//首先创建RouteSpec对象，后调用其id方法，创建PredicateSpec对象。
		//原因：Routes里创建Route是有序的链式过程
		public PredicateSpec route(String id) {
			return new RouteSpec(this).id(id);
		}

		//添加已创建好的Route
		private void add(Route route) {
			this.routes.add(route);
		}

		LocatorBuilder uri(Route.Builder builder, String uri) {
			Route route = builder.uri(uri).build();
			routes.add(route);
			return this;
		}

		LocatorBuilder uri(Route.Builder builder, URI uri) {
			Route route = builder.uri(uri).build();
			routes.add(route);
			return this;
		}

		public RouteLocator build() {
			return () -> Flux.fromIterable(this.routes);
		}

	}

	/**
	 * 用于创建Route组件
	 */
	public static class RouteSpec {
		private final Route.Builder builder = Route.builder();
		private final LocatorBuilder locatorBuilder;

		private RouteSpec(LocatorBuilder locatorBuilder) {
			this.locatorBuilder = locatorBuilder;
		}

		public PredicateSpec id(String id) {
			this.builder.id(id);
			return predicateBuilder();
		}

		private PredicateSpec predicateBuilder() {
			return new PredicateSpec(this.builder, this.locatorBuilder);
		}

	}

	/**
	 * 用于创建Predicate组件
	 */
	public static class PredicateSpec {

		private final Route.Builder builder;
		private LocatorBuilder locatorBuilder;

		private PredicateSpec(Route.Builder builder, LocatorBuilder locatorBuilder) {
			this.builder = builder;
			this.locatorBuilder = locatorBuilder;
		}

		/* TODO: has and, or & negate of Predicate with terminal andFilters()?
		public RoutePredicateBuilder predicate() {
		}
		// this goes in new class
		public RoutePredicateBuilder host(String pattern) {
			Predicate<ServerWebExchange> predicate = RoutePredicates.host(pattern);
		}*/

		public PredicateSpec order(int order) {
			this.builder.order(order);
			return this;
		}

		public GatewayFilterSpec predicate(Predicate<ServerWebExchange> predicate) {
			this.builder.predicate(predicate);
			return gatewayFilterBuilder();
		}

		private GatewayFilterSpec gatewayFilterBuilder() {
			return new GatewayFilterSpec(this.builder, this.locatorBuilder);
		}

		public LocatorBuilder uri(String uri) {
			return this.locatorBuilder.uri(this.builder, uri);
		}

		public LocatorBuilder uri(URI uri) {
			return this.locatorBuilder.uri(this.builder, uri);
		}
	}

	/**
	 * 用于创建GatewayFilter组件
	 */
	public static class GatewayFilterSpec {
		private Route.Builder builder;
		private LocatorBuilder locatorBuilder;

		public GatewayFilterSpec(Route.Builder routeBuilder, LocatorBuilder locatorBuilder) {
			this.builder = routeBuilder;
			this.locatorBuilder = locatorBuilder;
		}

		public GatewayFilterSpec gatewayFilters(List<GatewayFilter> gatewayFilters) {
			this.addAll(gatewayFilters);
			return this;
		}

		public GatewayFilterSpec add(GatewayFilter gatewayFilter) {
			return this.filter(gatewayFilter);
		}

		public GatewayFilterSpec filter(GatewayFilter gatewayFilter) {
			return this.filter(gatewayFilter, 0);
		}

		public GatewayFilterSpec filter(GatewayFilter gatewayFilter, int order) {
			this.builder.add(new OrderedGatewayFilter(gatewayFilter, order));
			return this;
		}

		public GatewayFilterSpec addAll(Collection<GatewayFilter> gatewayFilters) {
			this.builder.addAll(gatewayFilters);
			return this;
		}

		public GatewayFilterSpec addResponseHeader(String headerName, String headerValue) {
			return add(GatewayFilters.addResponseHeader(headerName, headerValue));
		}

		public LocatorBuilder uri(String uri) {
			return this.locatorBuilder.uri(this.builder, uri);
		}

		public LocatorBuilder uri(URI uri) {
			return this.locatorBuilder.uri(this.builder, uri);
		}
	}

}
