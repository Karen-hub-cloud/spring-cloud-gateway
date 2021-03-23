package org.springframework.cloud.gateway.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CLIENT_RESPONSE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.isAlreadyRouted;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setAlreadyRouted;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.NettyPipeline;
import reactor.ipc.netty.http.client.HttpClient;
import reactor.ipc.netty.http.client.HttpClientRequest;

/**
 * Netty 路由网关过滤器。其根据 http:// 或 https:// 前缀( Scheme )过滤处理，
 * 使用基于 Netty 实现的 HttpClient 请求后端 Http 服务。
 * 与{@link NettyWriteResponseFilter}成对出现
 * @author karen
 */
public class NettyRoutingFilter implements GlobalFilter, Ordered {

	//基于 Netty 实现的 HttpClient 。通过该属性，请求后端的 Http 服务。
	private final HttpClient httpClient;

	public NettyRoutingFilter(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 获得 requestUrl
		URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);

		// scheme：前缀，判断是否能够处理
		String scheme = requestUrl.getScheme();
		if (isAlreadyRouted(exchange) || (!scheme.equals("http") && !scheme.equals("https"))) {
			return chain.filter(exchange);
		}

		// 设置已经路由
		setAlreadyRouted(exchange);

		ServerHttpRequest request = exchange.getRequest();

		// Request Method
		final HttpMethod method = HttpMethod.valueOf(request.getMethod().toString());

		// 获得 url
		final String url = requestUrl.toString();

		// Request Header
		final DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
		//这个写法很独特啊
		request.getHeaders().forEach(httpHeaders::set);

		// 调用 HttpClient.request(HttpMethod, String, Function) 方法，请求后端 Http 服务。
		// TODO 没看懂
		return this.httpClient.request(method, url, req -> {
			final HttpClientRequest proxyRequest = req.options(NettyPipeline.SendOptions::flushOnEach) // 【】
					.failOnClientError(false) // 是否请求失败，抛出异常
					.headers(httpHeaders);

			// Request Form
			if (MediaType.APPLICATION_FORM_URLENCODED.includes(request.getHeaders().getContentType())) {
				return exchange.getFormData()
						.flatMap(map -> proxyRequest.sendForm(form -> {
							for (Map.Entry<String, List<String>> entry : map.entrySet()) {
								for (String value : entry.getValue()) {
									form.attr(entry.getKey(), value);
								}
							}
						}).then())
						.then(chain.filter(exchange));
			}

			// Request Body
			return proxyRequest.sendHeaders() //I shouldn't need this
					.send(request.getBody()
							.map(DataBuffer::asByteBuffer) // Flux<DataBuffer> => ByteBuffer
							.map(Unpooled::wrappedBuffer)); // ByteBuffer => Flux<DataBuffer>
			//请求后端 Http 服务完成，将 Netty Response 赋值给响应 response
		}).doOnNext(res -> {
			ServerHttpResponse response = exchange.getResponse();
			// Response Header
			// put headers and status so filters can modify the response
			HttpHeaders headers = new HttpHeaders();
			res.responseHeaders().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
			response.getHeaders().putAll(headers);

			// Response Status
			response.setStatusCode(HttpStatus.valueOf(res.status().code()));

			// 设置 Response 到 CLIENT_RESPONSE_ATTR
			// Defer committing the response until all route filters have run
			// Put client response as ServerWebExchange attribute and write response later NettyWriteResponseFilter
			exchange.getAttributes().put(CLIENT_RESPONSE_ATTR, res);
		}).then(chain.filter(exchange));
	}
}
