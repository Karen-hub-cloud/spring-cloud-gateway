package org.springframework.cloud.gateway.filter;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 网关过滤器接口，GatewayFilter 有三种类型的子类实现
 * TODO Q:与GlobalFilter的区别？{@link GlobalFilter}
 * 什么时候发挥作用？应该是需要{@link Route#setFilter}
 * @author karen
 */
public interface GatewayFilter {

	/**
	 * Process the Web request and (optionally) delegate to the next
	 * {@code WebFilter} through the given {@link GatewayFilterChain}.
	 * @param exchange the current server exchange
	 * @param chain provides a way to delegate to the next filter
	 * @return {@code Mono<Void>} to indicate when request processing is complete
	 */
	Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain);

}

