package org.springframework.cloud.gateway.handler.predicate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.HttpMethod;
import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;

/**
 * 请求 Method 匹配指定值。
 *
 * @author karen
 */
public class MethodRoutePredicateFactory implements RoutePredicateFactory {

	public static final String METHOD_KEY = "method";

	@Override
	public List<String> argNames() {
		return Arrays.asList(METHOD_KEY);
	}

	/**
	 * args is method
	 * @param args
	 * @return
	 */
	@Override
	public Predicate<ServerWebExchange> apply(Tuple args) {
		String method = args.getString(METHOD_KEY);
		return exchange -> {
			HttpMethod requestMethod = exchange.getRequest().getMethod();
			// 正则匹配
			return requestMethod.matches(method);
		};
	}
}
