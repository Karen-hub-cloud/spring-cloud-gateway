package org.springframework.cloud.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

/**
 * 与{@link WebClientWriteResponseFilter}成对使用
 *
 * @author karen
 */
public class WebClientHttpRoutingFilter implements GlobalFilter, Ordered {

	private final WebClient webClient;

	public WebClientHttpRoutingFilter(WebClient webClient) {
		this.webClient = webClient;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
	    // 获得 requestUrl
		URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);

        // 判断是否能够处理
        String scheme = requestUrl.getScheme();
		if (isAlreadyRouted(exchange) || (!scheme.equals("http") && !scheme.equals("https"))) {
			return chain.filter(exchange);
		}

        // 设置已经路由
		setAlreadyRouted(exchange);

		ServerHttpRequest request = exchange.getRequest();

		//TODO: support forms
        // Request Method
		HttpMethod method = request.getMethod();

		// Request
		RequestBodySpec bodySpec = this.webClient.method(method)
				.uri(requestUrl)
				.headers(httpHeaders -> {
					httpHeaders.addAll(request.getHeaders());
					httpHeaders.remove(HttpHeaders.HOST);
				});

		// Request Body
		RequestHeadersSpec<?> headersSpec;
		if (requiresBody(method)) {
			headersSpec = bodySpec.body(BodyInserters.fromDataBuffers(request.getBody()));
		} else {
			headersSpec = bodySpec;
		}

		return headersSpec.exchange()
				// .log("webClient route")
				.flatMap(res -> {
					ServerHttpResponse response = exchange.getResponse();

					// Response Header
					response.getHeaders().putAll(res.headers().asHttpHeaders());

					// Response Status
					response.setStatusCode(res.statusCode());

                    // 设置 Response 到 CLIENT_RESPONSE_ATTR
					// Defer committing the response until all route filters have run
					// Put client response as ServerWebExchange attribute and write response later NettyWriteResponseFilter
					exchange.getAttributes().put(CLIENT_RESPONSE_ATTR, res);
					return chain.filter(exchange);
				});
	}

	private boolean requiresBody(HttpMethod method) {
		switch (method) {
			case PUT:
			case POST:
			case PATCH:
				return true;
			default:
				return false;
		}
	}
}
