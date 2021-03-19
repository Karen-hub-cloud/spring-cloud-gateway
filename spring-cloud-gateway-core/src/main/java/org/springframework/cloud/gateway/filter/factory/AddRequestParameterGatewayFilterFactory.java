package org.springframework.cloud.gateway.filter.factory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.tuple.Tuple;
import org.springframework.util.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;

/**
 * {@link AddRequestHeaderGatewayFilterFactory}
 * Filter：指定RequestParameter为新的request
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *       # =====================================
 *       - id: add_request_parameter_route
 *         uri: http://example.org
 *         filters:
 *         - AddRequestParameter=foo, bar
 *
 * @author karen
 */
public class AddRequestParameterGatewayFilterFactory implements GatewayFilterFactory {

	@Override
	public List<String> argNames() {
		return Arrays.asList(NAME_KEY, VALUE_KEY);
	}

	/**
	 *
	 * @param args
	 * @return
	 */
	@Override
	public GatewayFilter apply(Tuple args) {
		String parameter = args.getString(NAME_KEY);
		String value = args.getString(VALUE_KEY);

		return (exchange, chain) -> {

			URI uri = exchange.getRequest().getURI();
			StringBuilder query = new StringBuilder();
			String originalQuery = uri.getQuery();

			if (StringUtils.hasText(originalQuery)) {
				query.append(originalQuery);
				if (originalQuery.charAt(originalQuery.length() - 1) != '&') {
					query.append('&');
				}
			}

			//TODO urlencode?
			query.append(parameter);
			query.append('=');
			query.append(value);

			try {
				URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
						uri.getPath(), query.toString(), uri.getFragment());

				ServerHttpRequest request = exchange.getRequest().mutate().uri(newUri).build();

				return chain.filter(exchange.mutate().request(request).build());
			} catch (URISyntaxException ex) {
				throw new IllegalStateException("Invalid URI query: \"" + query.toString() + "\"");
			}
		};
	}
}
