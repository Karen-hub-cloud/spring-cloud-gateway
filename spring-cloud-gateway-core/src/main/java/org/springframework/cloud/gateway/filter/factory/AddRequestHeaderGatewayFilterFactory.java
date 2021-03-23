package org.springframework.cloud.gateway.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.tuple.Tuple;

/**
 * Filter：添加指定请求 Header 为指定值。
 * 配置：
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *       # =====================================
 *       - id: add_request_header_route
 *         uri: http://example.org
 *         filters:
 *         - AddRequestHeader=X-Request-Foo, Bar
 *
 * @author karen
 */
public class AddRequestHeaderGatewayFilterFactory implements GatewayFilterFactory {

	@Override
	public List<String> argNames() {
		return Arrays.asList(NAME_KEY, VALUE_KEY);
	}

	/**
	 *
	 * @param args Tuple 参数 ：name / value
	 * @return
	 */
	@Override
	public GatewayFilter apply(Tuple args) {
		String name = args.getString(NAME_KEY);
		String value = args.getString(VALUE_KEY);

		//注意：每个GatewayFilterFactory 实现类的 #apply(Tuple) 方法里，都声明了一个实现 GatewayFilter 的内部类
		return (exchange, chain) -> {
			// 创建新的 ServerHttpRequest
			ServerHttpRequest request = exchange.getRequest().mutate()
					.header(name, value)
					.build();

			// 创建新的 ServerWebExchange ，提交过滤器链继续过滤
			return chain.filter(exchange.mutate().request(request).build());
		};
	}
}
