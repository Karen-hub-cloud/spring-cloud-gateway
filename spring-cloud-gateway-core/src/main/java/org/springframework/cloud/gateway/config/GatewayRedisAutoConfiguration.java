package org.springframework.cloud.gateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.web.reactive.DispatcherHandler;

/**
 * Redis 相关配置类：基于 RedisRateLimiter 实现网关的限流功能
 */
@Configuration
@AutoConfigureAfter(RedisReactiveAutoConfiguration.class)
@AutoConfigureBefore(GatewayAutoConfiguration.class)
@ConditionalOnBean(ReactiveRedisTemplate.class)
@ConditionalOnClass({RedisTemplate.class, DispatcherHandler.class})
class GatewayRedisAutoConfiguration {

	/**
	 * 创建RedisScript Bean对象，加载 META-INF/scripts/request_rate_limiter.lua
	 * 路径下的 Redis Lua 脚本。该脚本使用 Redis 基于令牌桶算法实现限流。
	 * @return
	 */
	@Bean
	@SuppressWarnings("unchecked")
	public RedisScript redisRequestRateLimiterScript() {
		DefaultRedisScript redisScript = new DefaultRedisScript<>();
		redisScript.setScriptSource(
				new ResourceScriptSource(new ClassPathResource("META-INF/scripts/request_rate_limiter.lua")));
		redisScript.setResultType(List.class);
		return redisScript;
	}

	/**
	 * 创建ReactiveRedisTemplate Bean对象
	 * @param reactiveRedisConnectionFactory
	 * @param resourceLoader
	 * @return
	 */
	@Bean
	public ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate(
			ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
			ResourceLoader resourceLoader) {
		RedisSerializer<String> serializer = new StringRedisSerializer();
		RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
				.<String, String> newSerializationContext()
				.key(serializer)
				.value(serializer)
				.hashKey(serializer)
				.hashValue(serializer)
				.build();
		return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory,
				serializationContext);
	}

	/**
	 * 使用 RedisScript 和 ReactiveRedisTemplate Bean 对象，
	 * 创建 RedisRateLimiter Bean 对象。
	 * @param redisTemplate
	 * @param redisScript
	 * @return
	 */
	@Bean
	public RedisRateLimiter redisRateLimiter(ReactiveRedisTemplate<String, String> redisTemplate,
			@Qualifier("redisRequestRateLimiterScript") RedisScript<List<Long>> redisScript) {
		return new RedisRateLimiter(redisTemplate, redisScript);
	}

}
