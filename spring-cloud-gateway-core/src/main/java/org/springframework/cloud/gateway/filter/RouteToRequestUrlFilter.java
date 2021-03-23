package org.springframework.cloud.gateway.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * 根据匹配的 Route，计算请求的地址。注意，这里的地址指的是 URL ，而不是 URI 。
 * @author karen
 */
public class RouteToRequestUrlFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(RouteToRequestUrlFilter.class);
	public static final int ROUTE_TO_URL_FILTER_ORDER = 10000;

	@Override
	public int getOrder() {
		return ROUTE_TO_URL_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
	    // 获得 请求所匹配的Route
		Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
		if (route == null) {
			return chain.filter(exchange);
		}
		log.trace("RouteToRequestUrlFilter start");
		// 拼接 requestUrl
		URI requestUrl = UriComponentsBuilder.fromHttpRequest(exchange.getRequest())
				.uri(route.getUri())
				// encoded=true
				.build(true)
				.toUri();
		// 设置 requestUrl 到 GATEWAY_REQUEST_URL_ATTR {@link RewritePathGatewayFilterFactory}
		exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
		// 提交过滤器链继续过滤
		return chain.filter(exchange);
	}

}
