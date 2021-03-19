package org.springframework.cloud.gateway.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.tuple.Tuple;

import reactor.core.publisher.Mono;

/**
 * takes name and value parameters.
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *       # =====================================
 *       - id: setresponseheader_route
 *         uri: http://example.org
 *         filters:
 *         - SetResponseHeader=X-Response-Foo, Bar
 * This GatewayFilter replaces all headers with the given name,rather than adding.
 * So if the downstream server responded with a X-Response-Foo:1234,this would be
 * replaced with X-Response-Foo:Bar, which is what the gateway client would receive.
 *
 * @author karen
 */
public class SetResponseHeaderGatewayFilterFactory implements GatewayFilterFactory {

	@Override
	public List<String> argNames() {
		return Arrays.asList(NAME_KEY, VALUE_KEY);
	}

	@Override
	public GatewayFilter apply(Tuple args) {
		final String header = args.getString(NAME_KEY);
		final String value = args.getString(VALUE_KEY);

		return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
			exchange.getResponse().getHeaders().set(header, value);
		}));
	}
}
