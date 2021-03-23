package org.springframework.cloud.gateway.filter;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import reactor.core.publisher.Mono;

/**
 * Contract to allow a {@link WebFilter} to delegate to the next in the chain.
 * 网关过滤器链接口
 *
 * @author karen
 */
public interface GatewayFilterChain {

	/**
	 * Delegate to the next {@code WebFilter} in the chain.
	 *
	 * @param exchange the current server exchange
	 * @return {@code Mono<Void>} to indicate when request handling is complete
	 */
	Mono<Void> filter(ServerWebExchange exchange);

}
