package org.springframework.cloud.gateway.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.LinkedHashSet;

/**
 * @author Spencer Gibb
 */
public class ServerWebExchangeUtils {

	private static final Log logger = LogFactory.getLog(ServerWebExchangeUtils.class);

	public static final String URI_TEMPLATE_VARIABLES_ATTRIBUTE = qualify("uriTemplateVariables");

	public static final String CLIENT_RESPONSE_ATTR = qualify("webHandlerClientResponse");
	public static final String GATEWAY_ROUTE_ATTR = qualify("gatewayRoute");

	public static final String GATEWAY_REQUEST_URL_ATTR = qualify("gatewayRequestUrl");
	public static final String GATEWAY_ORIGINAL_REQUEST_URL_ATTR = qualify("gatewayOriginalRequestUrl");

	public static final String GATEWAY_HANDLER_MAPPER_ATTR = qualify("gatewayHandlerMapper");

	/**
	 * Used when a routing filter has been successfully call. Allows users to write custom
	 * routing filters that disable built in routing filters.
	 */
	public static final String GATEWAY_ALREADY_ROUTED_ATTR = qualify("gatewayAlreadyRouted");

	private static String qualify(String attr) {
		return ServerWebExchangeUtils.class.getName() + "." + attr;
	}

	/**
	 * GATEWAY_ALREADY_ROUTED_ATTR：true，表示该请求已被处理
	 * @param exchange
	 */
	public static void setAlreadyRouted(ServerWebExchange exchange) {
		exchange.getAttributes().put(GATEWAY_ALREADY_ROUTED_ATTR, true);
	}

	/**
	 * 判断该请求暂未被其他 Routing 网关处理，拿这个值：GATEWAY_ALREADY_ROUTED_ATTR
	 * @param exchange
	 * @return
	 */
	public static boolean isAlreadyRouted(ServerWebExchange exchange) {
		return exchange.getAttributeOrDefault(GATEWAY_ALREADY_ROUTED_ATTR, false);
	}

	public static boolean setResponseStatus(ServerWebExchange exchange, HttpStatus httpStatus) {
		boolean response = exchange.getResponse().setStatusCode(httpStatus);
		if (!response && logger.isWarnEnabled()) {
			logger.warn("Unable to set status code to "+ httpStatus + ". Response already committed.");
		}
		return response;
	}

	public static HttpStatus parse(String statusString) {
		HttpStatus httpStatus;

		try {
			int status = Integer.parseInt(statusString);
			httpStatus = HttpStatus.valueOf(status);
		} catch (NumberFormatException e) {
			// try the enum string
			httpStatus = HttpStatus.valueOf(statusString.toUpperCase());
		}
		return httpStatus;
	}

	/**
	 * 添加 原始请求URI 到 GATEWAY_ORIGINAL_REQUEST_URL_ATTR
	 * @param exchange
	 * @param url
	 */
	public static void addOriginalRequestUrl(ServerWebExchange exchange, URI url) {
		/**
		 * 为什么使用 LinkedHashSet ？
		 * 因为可以使用 RewritePathGatewayFilterFactory / PrefixPathGatewayFilterFactory 多次重写。
		 */
		exchange.getAttributes().computeIfAbsent(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, s -> new LinkedHashSet<>());
		LinkedHashSet<URI> uris = exchange.getRequiredAttribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
		uris.add(url);
	}
}
