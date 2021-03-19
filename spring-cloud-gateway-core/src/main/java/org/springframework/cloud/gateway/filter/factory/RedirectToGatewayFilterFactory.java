package org.springframework.cloud.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.parse;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.tuple.Tuple;
import org.springframework.util.Assert;

import reactor.core.publisher.Mono;

/**
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *       # =====================================
 *       - id: prefixpath_route
 *         uri: http://example.org
 *         filters:
 *         - RedirectTo=302, http://www.iocoder.cn
 * 将响应重定向到指定 URL ，并设置响应状态码为指定 Status 。注意，Status 必须为 3XX 重定向状态码。
 *
 * @author karen
 */
public class RedirectToGatewayFilterFactory implements GatewayFilterFactory {

	public static final String STATUS_KEY = "status";
	public static final String URL_KEY = "url";

	@Override
	public List<String> argNames() {
		return Arrays.asList(STATUS_KEY, URL_KEY);
	}

	@Override
	public GatewayFilter apply(Tuple args) {
		String statusString = args.getRawString(STATUS_KEY);
		String urlString = args.getString(URL_KEY);

		// 解析 status ，并判断是否是 3XX 重定向状态
		final HttpStatus httpStatus = parse(statusString);
		Assert.isTrue(httpStatus.is3xxRedirection(), "status must be a 3xx code, but was " + statusString);
		// 创建 URL
		final URL url;
		try {
			url = URI.create(urlString).toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid url " + urlString, e);
		}

		return (exchange, chain) ->
				chain.filter(exchange).then(Mono.defer(() -> { // After Filter
					if (!exchange.getResponse().isCommitted()) {
						// 设置响应 Status
						setResponseStatus(exchange, httpStatus);

						// 设置响应 Header
						final ServerHttpResponse response = exchange.getResponse();
						response.getHeaders().set(HttpHeaders.LOCATION, url.toString());
						return response.setComplete();
					}
					return Mono.empty();
				}));
	}

}
