package org.springframework.cloud.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.tuple.Tuple;

import reactor.core.publisher.Mono;

/**
 * 状态码修改
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *       # =====================================
 *       - id: setstatusstring_route
 *         uri: http://example.org
 *         filters:
 *         - SetStatus=BAD_REQUEST
 *       - id: setstatusint_route
 *         uri: http://example.org
 *         filters:
 *         - SetStatus=401
 * @author karen
 */
public class SetStatusGatewayFilterFactory implements GatewayFilterFactory {

	public static final String STATUS_KEY = "status";

	@Override
	public List<String> argNames() {
		return Arrays.asList(STATUS_KEY);
	}

	@Override
	public GatewayFilter apply(Tuple args) {
		String status = args.getRawString(STATUS_KEY);
		final HttpStatus httpStatus = ServerWebExchangeUtils.parse(status);

		return (exchange, chain) -> {

			// option 1 (runs in filter order)
			/*exchange.getResponse().beforeCommit(() -> {
				exchange.getResponse().setStatusCode(finalStatus);
				return Mono.empty();
			});
			return chain.filter(exchange);*/

			// option 2 (runs in reverse filter order)
			return chain.filter(exchange)
					.then(Mono.fromRunnable(() -> { // 将一个Runnable转换为Observable，当一个订阅者订阅时，它执行这个Runnable并发射Runnable的返回值
						// check not really needed, since it is guarded in setStatusCode,
						// but it's a good example
						if (!exchange.getResponse().isCommitted()) {
							setResponseStatus(exchange, httpStatus);
						}
					}));
		};
	}

}
