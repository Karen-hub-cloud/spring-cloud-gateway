package org.springframework.cloud.gateway.filter;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import reactor.core.publisher.Mono;

/**
 * 全局过滤器接口,GlobalFilter 会作用到所有的 Route 上。
 * 顺序：
 * 1.{@link NettyWriteResponseFilter} -1
 * 2.{@link WebClientWriteResponseFilter} -1
 * 3.{@link RouteToRequestUrlFilter} 10000
 * 4.{@link LoadBalancerClientFilter} 10100
 * 5.{@link ForwardRoutingFilter} Integer.MAX_VALUE
 * 6.{@link NettyRoutingFilter} Integer.MAX_VALUE
 * 7.{@link WebClientHttpRoutingFilter} Integer.MAX_VALUE
 * 8.{@link WebsocketRoutingFilter} Integer.MAX_VALUE
 *
 * 思考与{@link GatewayFilter}的区别
 *
 * @author karen
 */
public interface GlobalFilter {

	/**
	 * Process the Web request and (optionally) delegate to the next
	 * {@code WebFilter} through the given {@link GatewayFilterChain}.
	 * @param exchange the current server exchange
	 * @param chain provides a way to delegate to the next filter
	 * @return {@code Mono<Void>} to indicate when request processing is complete
	 */
	Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain);

}
