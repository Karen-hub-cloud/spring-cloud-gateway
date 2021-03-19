package org.springframework.cloud.gateway.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.tuple.Tuple;

/**
 * 移除请求 Proxy 相关的 Header
 *
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.gateway.filter.remove-non-proxy-headers")
public class RemoveNonProxyHeadersGatewayFilterFactory implements GatewayFilterFactory {

	/**
	 * 默认
	 */
	public static final String[] DEFAULT_HEADERS_TO_REMOVE = new String[] {"Connection", "Keep-Alive",
			"Proxy-Authenticate", "Proxy-Authorization", "TE", "Trailer", "Transfer-Encoding", "Upgrade"};

	private List<String> headers = Arrays.asList(DEFAULT_HEADERS_TO_REMOVE);

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	@Override
	public GatewayFilter apply(Tuple args) {
		//TODO: support filter args

		return (exchange, chain) -> {
			// 创建新的 ServerHttpRequest
			ServerHttpRequest request = exchange.getRequest().mutate()
					.headers(httpHeaders -> {
						for (String header : this.headers) {
							httpHeaders.remove(header); // 移除
						}
					})
					.build();

			// 创建新的 ServerWebExchange ，提交过滤器链继续过滤
			return chain.filter(exchange.mutate().request(request).build());
		};
	}
}
