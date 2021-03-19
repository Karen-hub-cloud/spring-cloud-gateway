package org.springframework.cloud.gateway.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.tuple.Tuple;

/**
 * @author karen
 */
public class AddResponseHeaderGatewayFilterFactory implements GatewayFilterFactory {

	@Override
	public List<String> argNames() {
		return Arrays.asList(NAME_KEY, VALUE_KEY);
	}

	@Override
	public GatewayFilter apply(Tuple args) {
		final String header = args.getString(NAME_KEY);
		final String value = args.getString(VALUE_KEY);

		return (exchange, chain) -> {
			exchange.getResponse().getHeaders().add(header, value);

			return chain.filter(exchange);
		};
	}
}
