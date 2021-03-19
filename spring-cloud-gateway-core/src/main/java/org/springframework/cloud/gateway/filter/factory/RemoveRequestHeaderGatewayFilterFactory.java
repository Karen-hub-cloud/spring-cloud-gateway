package org.springframework.cloud.gateway.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.tuple.Tuple;

/**
 * 移除header
 *
 * @author karen
 */
public class RemoveRequestHeaderGatewayFilterFactory implements GatewayFilterFactory {

	@Override
	public List<String> argNames() {
		return Arrays.asList(NAME_KEY);
	}

	@Override
	public GatewayFilter apply(Tuple args) {
		final String header = args.getString(NAME_KEY);

		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest().mutate()
					.headers(httpHeaders -> httpHeaders.remove(header))
					.build();

			return chain.filter(exchange.mutate().request(request).build());
		};
	}
}
