package org.springframework.cloud.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.tuple.Tuple;

/**
 * 根据配置的正则表达式 regexp ，使用配置的 replacement 重写请求 Path，有点类似于{@see Module ngx_http_rewrite_module}
 *
 * @author karen
 */
public class RewritePathGatewayFilterFactory implements GatewayFilterFactory {

	public static final String REGEXP_KEY = "regexp";
	public static final String REPLACEMENT_KEY = "replacement";

	@Override
	public List<String> argNames() {
		return Arrays.asList(REGEXP_KEY, REPLACEMENT_KEY);
	}

	@Override
	public GatewayFilter apply(Tuple args) {
		final String regex = args.getString(REGEXP_KEY);
		// `$\` 用于替代 `$` ，避免和 YAML 语法冲突。
		String replacement = args.getString(REPLACEMENT_KEY).replace("$\\", "$");

		return (exchange, chain) -> {
			ServerHttpRequest req = exchange.getRequest();
			// 添加 原始请求URI 到 GATEWAY_ORIGINAL_REQUEST_URL_ATTR
			addOriginalRequestUrl(exchange, req.getURI());
			// 重写 Path
			String path = req.getURI().getPath();
			String newPath = path.replaceAll(regex, replacement);

			// 创建新的 ServerHttpRequest
			ServerHttpRequest request = req.mutate()
					.path(newPath) // 设置 Path
					.build();

			// 添加 请求URI 到 GATEWAY_REQUEST_URL_ATTR
			exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());

			// 创建新的 ServerWebExchange ，提交过滤器链继续过滤
			return chain.filter(exchange.mutate().request(request).build());
		};
	}
}
