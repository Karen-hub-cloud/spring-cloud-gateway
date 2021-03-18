package org.springframework.cloud.gateway.handler.predicate;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.tuple.Tuple;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;

/**
 * 请求 Host 匹配指定值。
 * 例如：
 * id: host_route
 * uri: http://example.org
 * predicates:
 * Host=**.somehost.org
 *
 * @author karen
 */
public class HostRoutePredicateFactory implements RoutePredicateFactory {

	//路径匹配器
	private PathMatcher pathMatcher = new AntPathMatcher(".");

	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	@Override
	public List<String> argNames() {
		return Collections.singletonList(PATTERN_KEY);
	}

	/**
	 * args is pattern
	 * @param args
	 * @return
	 */
	@Override
	public Predicate<ServerWebExchange> apply(Tuple args) {
		String pattern = args.getString(PATTERN_KEY);

		return exchange -> {
			String host = exchange.getRequest().getHeaders().getFirst("Host");
			// 匹配
			return this.pathMatcher.match(pattern, host);
		};
	}
}
