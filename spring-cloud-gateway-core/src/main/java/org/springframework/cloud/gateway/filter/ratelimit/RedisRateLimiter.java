package org.springframework.cloud.gateway.filter.ratelimit;

import static org.springframework.tuple.TupleBuilder.tuple;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.tuple.Tuple;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 基于 Redis 的分布式限流器实现类。
 *
 * @author karen
 */
public class RedisRateLimiter implements RateLimiter {
	/**
	 * replenishRate 令牌桶填充平均速率/秒
	 * burstCapacity 令牌桶上限。
	 */
	public static final String REPLENISH_RATE_KEY = "replenishRate";
	public static final String BURST_CAPACITY_KEY = "burstCapacity";

	private Log log = LogFactory.getLog(getClass());

	private final ReactiveRedisTemplate<String, String> redisTemplate;
	/**
	 * script 属性，Lua 脚本
	 */
	private final RedisScript<List<Long>> script;

	public RedisRateLimiter(ReactiveRedisTemplate<String, String> redisTemplate,
			RedisScript<List<Long>> script) {
		this.redisTemplate = redisTemplate;
		this.script = script;
	}

	public static Tuple args(int replenishRate, int burstCapacity) {
		return tuple().of(REPLENISH_RATE_KEY, replenishRate, BURST_CAPACITY_KEY, burstCapacity);
	}

	/**
	 * @param id 令牌桶编号，在本文场景中为请求限流键。
	 * @param args replenishRate/burstCapacity
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Mono<Response> isAllowed(String id, Tuple args) {
		int replenishRate = args.getInt(REPLENISH_RATE_KEY);
		int burstCapacity;
		if (args.hasFieldName(BURST_CAPACITY_KEY)) {
			burstCapacity = args.getInt(BURST_CAPACITY_KEY);
		} else {
			burstCapacity = 0;
		}

		try {
			// 令牌桶前缀，唯一。
			String prefix = "request_rate_limiter." + id;

			// prefix + ".tokens"：令牌桶剩余令牌数。
			// prefix + ".timestamp"：令牌桶最后填充令牌时间，单位：秒。
			List<String> keys = Arrays.asList(prefix + ".tokens", prefix + ".timestamp");

			// 获得 Lua 脚本参数：replenishRate、burstCapacity 前面有解释
			// Instant.now().getEpochSecond()：得到从 1970-01-01 00:00:00 开始的秒数，
			// 第四个参数 ：消耗令牌数量，默认 1
			// @TODO Question：为什么在 Java 代码里获取，而不使用 Lua 在 Reids 里获取？
			// Answer：因为 Redis 的限制（ Lua中有写操作不能使用带随机性质的读操作，如TIME ）不能在 Redis Lua中 使用 TIME 获取时间戳，因此只好从应用获取然后传入，在某些极端情况下（机器时钟不准的情况下），限流会存在一些小问题。
			List<String> scriptArgs = Arrays.asList(replenishRate + "", burstCapacity + "",
					Instant.now().getEpochSecond() + "", "1");

			// @TODO 执行 Redis Lua 脚本，获取令牌。返回结果为 [是否获取令牌成功, 剩余令牌数] ，
			// 其中，1 代表获取令牌成功，0 代表令牌获取失败。
			Flux<List<Long>> flux = this.redisTemplate.execute(this.script, keys, scriptArgs);
			return flux
					//当 Redis Lua 脚本过程中发生异常，忽略异常，返回 Flux.just(Arrays.asList(1L, -1L)) ，即认为获取令牌成功。
					// @TODO Question 为什么？在 Redis 发生故障时，我们不希望限流器对 Reids 是强依赖，并且 Redis 发生故障的概率本身就很低。
					.onErrorResume(throwable -> Flux.just(Arrays.asList(1L, -1L)))
					// 将 Flux<List<Long>> => Mono<List<Long>>
					.reduce(new ArrayList<Long>(), (longs, l) -> {
						longs.addAll(l);
						return longs;
					})
					// 将 Mono<List<Long>> => Mono<Response>
					.map(results -> {
						boolean allowed = results.get(0) == 1L;
						Long tokensLeft = results.get(1);

						Response response = new Response(allowed, tokensLeft);

						if (log.isDebugEnabled()) {
							log.debug("response: " + response);
						}
						return response;
					})
					;
		} catch (Exception e) {
			/*
			 * We don't want a hard dependency on Redis to allow traffic. Make sure to set
			 * an alert so you know if this is happening too much. Stripe's observed
			 * failure rate is 0.01%.
			 */
			log.error("Error determining if user allowed from redis", e);
		}
		//发生异常时，例如 Redis 挂了，返回 Flux.just(Arrays.asList(1L, -1L)) ，即认为获取令牌成功。
		return Mono.just(new Response(true, -1));
	}
}
