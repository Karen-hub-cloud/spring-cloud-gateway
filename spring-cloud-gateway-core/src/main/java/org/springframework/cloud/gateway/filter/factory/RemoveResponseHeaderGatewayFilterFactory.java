package org.springframework.cloud.gateway.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.tuple.Tuple;

import reactor.core.publisher.Mono;

/**
 * 移除header中的东西
 *
 * @author karen
 */
public class RemoveResponseHeaderGatewayFilterFactory implements GatewayFilterFactory {

	@Override
	public List<String> argNames() {
		return Arrays.asList(NAME_KEY);
	}

	@Override
	public GatewayFilter apply(Tuple args) {
		final String header = args.getString(NAME_KEY);

		return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
			exchange.getResponse().getHeaders().remove(header);
		}));
	}
}
