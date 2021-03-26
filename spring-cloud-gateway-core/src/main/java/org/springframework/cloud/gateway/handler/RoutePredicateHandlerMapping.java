package org.springframework.cloud.gateway.handler;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_HANDLER_MAPPER_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * 【🌟核心】请求到来的第一步：接收到请求，匹配 Route
 * @author karen
 */
public class RoutePredicateHandlerMapping extends AbstractHandlerMapping {

	private final FilteringWebHandler webHandler;
	private final RouteLocator routeLocator;

	public RoutePredicateHandlerMapping(FilteringWebHandler webHandler, RouteLocator routeLocator) {
		this.webHandler = webHandler;
		this.routeLocator = routeLocator;

		// RequestMappingHandlerMapping 之后
		setOrder(1);
	}

	@Override
	protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {
		// 设置 GATEWAY_HANDLER_MAPPER_ATTR 为 RoutePredicateHandlerMapping
		exchange.getAttributes().put(GATEWAY_HANDLER_MAPPER_ATTR, getClass().getSimpleName());
		// 匹配 Route
		return lookupRoute(exchange)
				// 返回 FilteringWebHandler
				.flatMap((Function<Route, Mono<?>>) r -> {
					if (logger.isDebugEnabled()) {
						logger.debug("Mapping [" + getExchangeDesc(exchange) + "] to " + r);
					}

					// 设置 GATEWAY_ROUTE_ATTR 为 匹配的 Route
					exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, r);
					// 返回
					return Mono.just(webHandler);
				})
				// 匹配不到 Route
				.switchIfEmpty(Mono.empty().then(Mono.fromRunnable(() -> {
					if (logger.isTraceEnabled()) {
						logger.trace("No RouteDefinition found for [" + getExchangeDesc(exchange) + "]");
					}
				})));
	}

	//TODO: get desc from factory?
	private String getExchangeDesc(ServerWebExchange exchange) {
		StringBuilder out = new StringBuilder();
		out.append("Exchange: ");
		out.append(exchange.getRequest().getMethod());
		out.append(" ");
		out.append(exchange.getRequest().getURI());
		return out.toString();
	}

	/**
	 * 顺序匹配 Route
	 */
	protected Mono<Route> lookupRoute(ServerWebExchange exchange) {
		//调用 RouteLocator#getRoutes() 方法，获得全部 Route
		return this.routeLocator.getRoutes()
				//调用 Predicate#test(ServerWebExchange) 方法，顺序匹配一个 Route。
				.filter(route -> route.getPredicate().test(exchange))
				.next()
				.map(route -> {
					if (logger.isDebugEnabled()) {
						logger.debug("RouteDefinition matched: " + route.getId());
					}
					//调用 #validateRoute(Route, ServerWebExchange) 方法，校验 Route 的有效性
					validateRoute(route, exchange);
					return route;
				});
	}

	/**
	 * Validate the given handler against the current request.
	 * <p>The default implementation is empty. Can be overridden in subclasses,
	 * for example to enforce specific preconditions expressed in URL mappings.
	 *
	 * @param route the Route object to validate
	 * @param exchange current exchange
	 * @throws Exception if validation failed
	 */
	@SuppressWarnings("UnusedParameters")
	protected void validateRoute(Route route, ServerWebExchange exchange) {
	}

}
