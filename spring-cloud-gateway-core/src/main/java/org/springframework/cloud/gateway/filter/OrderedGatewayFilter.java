package org.springframework.cloud.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 有序的网关过滤器实现类。在 FilterChain 里，过滤器数组首先会按照 order 升序排序，按照顺序过滤请求。
 *
 * @author karen
 */
public class OrderedGatewayFilter implements GatewayFilter, Ordered {

	/** 委托的 GlobalFilter */
	private final GatewayFilter delegate;
	private final int order;

	public OrderedGatewayFilter(GatewayFilter delegate, int order) {
		this.delegate = delegate;
		this.order = order;
	}

	/**
	 * 使用 delegate 过滤请求
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return this.delegate.filter(exchange, chain);
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("OrderedGatewayFilter{");
		sb.append("delegate=").append(delegate);
		sb.append(", order=").append(order);
		sb.append('}');
		return sb.toString();
	}
}
