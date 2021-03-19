package org.springframework.cloud.gateway.filter.ratelimit;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 实现KeyResolver接口，使用请求认证的 java.security.Principal 作为限流键。
 * 也可以改造一下，如下
 * @author karen
 */
public class PrincipalNameKeyResolver implements KeyResolver {

	public static final String BEAN_NAME = "principalNameKeyResolver";

	@Override
	public Mono<String> resolve(ServerWebExchange exchange) {
//		return exchange.getPrincipal().map(Principal::getName).switchIfEmpty(Mono.empty()); // TODO 临时注释
//		return  Mono.just(exchange.getRequest().getQueryParams().getFirst("user")).defaultIfEmpty("123");
        return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
	}
}
