package org.springframework.cloud.gateway.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CLIENT_RESPONSE_ATTR;

/**
 * 与 NettyRoutingFilter 成对使用的网关过滤器。{@link NettyRoutingFilter}
 * 其将 NettyRoutingFilter 请求后端 Http 服务的响应写回客户端。
 * @author karen
 */
public class NettyWriteResponseFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(NettyWriteResponseFilter.class);

	public static final int WRITE_RESPONSE_FILTER_ORDER = -1;

	@Override
	public int getOrder() {
		return WRITE_RESPONSE_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// NOTICE: nothing in "pre" filter stage as CLIENT_RESPONSE_ATTR is not added
		// until the WebHandler is run
		// then() 实现 After Filter 逻辑
		return chain.filter(exchange).then(Mono.defer(() -> {
		    // 获得 Netty Response
			HttpClientResponse clientResponse = exchange.getAttribute(CLIENT_RESPONSE_ATTR);
			// HttpClientResponse clientResponse = getAttribute(exchange, CLIENT_RESPONSE_ATTR, HttpClientResponse.class);
			if (clientResponse == null) {
				return Mono.empty();
			}
			log.trace("NettyWriteResponseFilter start");
			ServerHttpResponse response = exchange.getResponse();

			// 将 Netty Response 写回给客户端。
			NettyDataBufferFactory factory = (NettyDataBufferFactory) response.bufferFactory();
			//TODO: what if it's not netty
			final Flux<NettyDataBuffer> body = clientResponse.receive()
					.retain() // ByteBufFlux => ByteBufFlux
					.map(factory::wrap); // ByteBufFlux  => Flux<NettyDataBuffer>
			return response.writeWith(body);
		}));
	}

}
