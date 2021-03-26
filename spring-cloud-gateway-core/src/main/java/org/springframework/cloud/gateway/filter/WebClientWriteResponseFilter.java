package org.springframework.cloud.gateway.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CLIENT_RESPONSE_ATTR;

/**
 * Http 路由网关过滤器。
 * 其根据 【http:// 或 https://】 前缀(Scheme)过滤处理，
 * 使用基于 org.springframework.cloud.gateway.filter.WebClient 实现的 HttpClient 请求后端 Http 服务。
 *
 * 与 {@link WebClientHttpRoutingFilter} 成对使用
 * 服务的响应写回客户端。
 *
 * TODO Q：与NettyRoutingFilter有什么区别吗？
 * @author karen
 */
public class WebClientWriteResponseFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(WebClientWriteResponseFilter.class);

	public static final int WRITE_RESPONSE_FILTER_ORDER = -1;

	@Override
	public int getOrder() {
		return WRITE_RESPONSE_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// NOTICE: nothing in "pre" filter stage as CLIENT_RESPONSE_ATTR is not added
		// until the WebHandler is run
		return chain.filter(exchange).then(Mono.defer(() -> {
		    // 获得 Response
			ClientResponse clientResponse = exchange.getAttribute(CLIENT_RESPONSE_ATTR);
			if (clientResponse == null) {
				return Mono.empty();
			}
			log.trace("WebClientWriteResponseFilter start");
			ServerHttpResponse response = exchange.getResponse();

			return response.writeWith(clientResponse.body(BodyExtractors.toDataBuffers())).log("webClient response");
		}));
	}

}
