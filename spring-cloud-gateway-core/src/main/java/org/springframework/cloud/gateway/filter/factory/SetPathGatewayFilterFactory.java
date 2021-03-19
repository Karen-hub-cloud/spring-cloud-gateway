package org.springframework.cloud.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.tuple.Tuple;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.pattern.PathPattern.PathMatchInfo;

/**
 * uriTemplateVariables重写path？
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *       # =====================================
 *       - id: setpath_route
 *         uri: http://example.org
 *         predicates:
 *         - Path=/foo/{segment}
 *         filters:
 *         - SetPath=/{segment}
 *
 * @author karen
 */
public class SetPathGatewayFilterFactory implements GatewayFilterFactory {

	public static final String TEMPLATE_KEY = "template";

	@Override
	public List<String> argNames() {
		return Arrays.asList(TEMPLATE_KEY);
	}

	@Override
	@SuppressWarnings("unchecked")
	public GatewayFilter apply(Tuple args) {
		String template = args.getString(TEMPLATE_KEY);
		UriTemplate uriTemplate = new UriTemplate(template);

		return (exchange, chain) -> {
			PathMatchInfo variables = exchange.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			ServerHttpRequest req = exchange.getRequest();
			addOriginalRequestUrl(exchange, req.getURI());
			Map<String, String> uriVariables;

			if (variables != null) {
				uriVariables = variables.getUriVariables();
			} else {
				uriVariables = Collections.emptyMap();
			}

			// 使用 路径参数进行 替换 请求Path
			URI uri = uriTemplate.expand(uriVariables);
			String newPath = uri.getPath();

			exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, uri);

			ServerHttpRequest request = req.mutate()
					.path(newPath)
					.build();

			return chain.filter(exchange.mutate().request(request).build());
		};
	}
}
