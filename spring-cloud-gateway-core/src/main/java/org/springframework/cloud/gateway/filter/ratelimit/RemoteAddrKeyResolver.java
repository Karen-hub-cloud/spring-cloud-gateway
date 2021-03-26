package org.springframework.cloud.gateway.filter.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 第一步：自定义一个使用请求 IP 作为限流键的 KeyResolver
 * 第二步：配置 RemoteAddrKeyResolver Bean 对象 {@link #remoteAddrKeyResolver()}
 * 第三步，配置 RouteDefinition 路由配置
 *
 * @author Karen
 * Created on 2021-03-19
 */
public class RemoteAddrKeyResolver implements KeyResolver {

	public static final String BEAN_NAME = "remoteAddrKeyResolver";

	@Override
	public Mono<String> resolve(ServerWebExchange exchange) {
		return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
	}

	@Bean(name = RemoteAddrKeyResolver.BEAN_NAME)
	@ConditionalOnBean(RateLimiter.class)
	public RemoteAddrKeyResolver remoteAddrKeyResolver() {
		return new RemoteAddrKeyResolver();
	}

}
