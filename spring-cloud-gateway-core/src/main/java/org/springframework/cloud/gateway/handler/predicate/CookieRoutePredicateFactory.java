package org.springframework.cloud.gateway.handler.predicate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.HttpCookie;
import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;

/**
 * 请求指定 Cookie 正则匹配指定值。
 *
 * @author karen
 */
public class CookieRoutePredicateFactory implements RoutePredicateFactory {

	public static final String NAME_KEY = "name";
	public static final String REGEXP_KEY = "regexp";

	@Override
	public List<String> argNames() {
		return Arrays.asList(NAME_KEY, REGEXP_KEY);
	}

	/**
	 * args is name / regexp
	 * @param args
	 * @return
	 */
	@Override
	public Predicate<ServerWebExchange> apply(Tuple args) {
		String name = args.getString(NAME_KEY);
		String regexp = args.getString(REGEXP_KEY);

		return exchange -> {
			List<HttpCookie> cookies = exchange.getRequest().getCookies().get(name);
			for (HttpCookie cookie : cookies) {
				// 正则匹配
				if (cookie.getValue().matches(regexp)) {
					return true;
				}
			}
			return false;
		};
	}
}
