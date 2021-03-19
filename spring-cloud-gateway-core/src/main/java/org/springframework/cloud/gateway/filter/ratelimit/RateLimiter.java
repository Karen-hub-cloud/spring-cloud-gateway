package org.springframework.cloud.gateway.filter.ratelimit;

import org.springframework.tuple.Tuple;

import reactor.core.publisher.Mono;

/**
 * 限流器接口
 *
 * @author karen
 */
public interface RateLimiter {

	/**
	 * 判断是否被限流。
	 */
	Mono<Response> isAllowed(String id, Tuple args);

	/**
	 * Question：接口中还能写类？
	 * Answer：接口中可以写，但是不建议。这样的默认都是public static，另外，还可以有方法的具体实现。
	 * Java 8使我们能够通过使用 default 关键字向接口添加非抽象方法实现。 此功能也称为虚拟扩展方法。
	 */
	class Response {
		/**
		 * 是否允许访问(未被限流)
		 */
		private final boolean allowed;
		/**
		 * 令牌桶剩余数量
		 */
		private final long tokensRemaining;

		public Response(boolean allowed, long tokensRemaining) {
			this.allowed = allowed;
			this.tokensRemaining = tokensRemaining;
		}

		public boolean isAllowed() {
			return allowed;
		}

		public long getTokensRemaining() {
			return tokensRemaining;
		}

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer("Response{");
			sb.append("allowed=").append(allowed);
			sb.append(", tokensRemaining=").append(tokensRemaining);
			sb.append('}');
			return sb.toString();
		}
	}
}
