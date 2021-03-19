package org.springframework.cloud.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.tuple.Tuple;

/**
 * 给path添加前置prefix
 *
 * @author karen
 */
public class PrefixPathGatewayFilterFactory implements GatewayFilterFactory {

	private static final Log log = LogFactory.getLog(PrefixPathGatewayFilterFactory.class);

	public static final String PREFIX_KEY = "prefix";

	@Override
	public List<String> argNames() {
		return Arrays.asList(PREFIX_KEY);
	}

	@Override
	@SuppressWarnings("unchecked")
	public GatewayFilter apply(Tuple args) {
		final String prefix = args.getString(PREFIX_KEY);

		return (exchange, chain) -> {
			ServerHttpRequest req = exchange.getRequest();
			addOriginalRequestUrl(exchange, req.getURI());
			String newPath = prefix + req.getURI().getPath();

			ServerHttpRequest request = req.mutate()
					.path(newPath) // 设置 Path
					.build();

			exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());

			if (log.isTraceEnabled()) {
				log.trace("Prefixed URI with: " + prefix + " -> " + request.getURI());
			}

			return chain.filter(exchange.mutate().request(request).build());
		};
	}
}
