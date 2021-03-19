package org.springframework.cloud.gateway.filter.ratelimit;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 请求键解析器接口 Resolver：解析器
 *
 * @author karen
 */
public interface KeyResolver {
	/**
	 * 通过实现 KeyResolver 接口，实现获得不同的请求的限流键，例如URL / 用户 / IP 等。
	 * 目前实现类只有PrincipalNameKeyResolver：获得请求的IP
	 */
	Mono<String> resolve(ServerWebExchange exchange);
}
