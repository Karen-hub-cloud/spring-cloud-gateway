package org.springframework.cloud.gateway.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.tuple.Tuple;

/**
 * RequestRateLimiterGatewayFilter 使用【Redis + Lua】实现分布式限流。
 * 而限流的粒度，例如 URL / 用户 / IP 等，
 * 通过 {@link KeyResolver}实现类决定，
 *
 * @author karen
 */
public class RequestRateLimiterGatewayFilterFactory implements GatewayFilterFactory {

	public static final String KEY_RESOLVER_KEY = "keyResolver";

	private final RateLimiter rateLimiter;
	private final KeyResolver defaultKeyResolver;

	public RequestRateLimiterGatewayFilterFactory(RateLimiter rateLimiter,
			KeyResolver defaultKeyResolver) {
		this.rateLimiter = rateLimiter;
		this.defaultKeyResolver = defaultKeyResolver;
	}

	/**
	 * 定义了 Tuple 参数的 Key 为 replenishRate / burstCapacity / keyResolver
	 */
	@Override
	public List<String> argNames() {
		return Arrays.asList(
				RedisRateLimiter.REPLENISH_RATE_KEY,
				RedisRateLimiter.BURST_CAPACITY_KEY,
				KEY_RESOLVER_KEY
		);
	}

	/**
	 * RouteDefinitionRouteLocator中用到了，表示无需校验tuple
	 */
	@Override
	public boolean validateArgs() {
		return false;
	}

	/**
	 * 创建RequestRateLimiterGatewayFilter对象
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public GatewayFilter apply(Tuple args) {
		//校验 Tuple 参数至少有两个元素，即 replenishRate 和 burstCapacity。
		validateMin(2, args);

		//keyResolver 是选填，为空时，使用默认值 defaultKeyResolver 。
		KeyResolver keyResolver;
		if (args.hasFieldName(KEY_RESOLVER_KEY)) {
			keyResolver = args.getValue(KEY_RESOLVER_KEY, KeyResolver.class);
		} else {
			keyResolver = defaultKeyResolver;
		}

		/**
		 * 获得keyResolver。通过它，获得请求的限流键，例如URL / 用户 / IP 等。
		 */
		// 1.调用 KeyResolver#resolve(ServerWebExchange) 方法，获得请求的限流键。
		// 2.注意下，这里未处理限流键为空的情况(TODO: if key is empty?)。所以，当限流键为空时，过滤器链不会继续向下执行，也就是说，不会请求后端 Http / Websocket 服务，并且最终返回客户端 200 状态码，内容为空。
		return (exchange, chain) -> keyResolver.resolve(exchange).flatMap(key ->

				//调用 RateLimiter#isAllowed(ServerWebExchange, Tuple) 方法，判断是否被限流。
				rateLimiter.isAllowed(key, args).flatMap(response -> {
					// 允许访问
					if (response.isAllowed()) {
						return chain.filter(exchange);
					}
					// 被限流，不允许访问,设置响应 429 状态码，并回写客户端响应,exchange.getResponse().setComplete()
					exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
					return exchange.getResponse().setComplete();
				}));
	}

}
