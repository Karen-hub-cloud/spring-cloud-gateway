package org.springframework.cloud.gateway.handler.predicate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;

/**
 * 请求 QueryParam 匹配指定值。
 *
 * @author karen
 */
public class QueryRoutePredicateFactory implements RoutePredicateFactory {

	public static final String PARAM_KEY = "param";
	public static final String REGEXP_KEY = "regexp";

	@Override
	public List<String> argNames() {
		return Arrays.asList(PARAM_KEY, REGEXP_KEY);
	}

	@Override
	public boolean validateArgs() {
		return false;
	}

	/**
	 * args：param ( 必填 ) / regexp ( 选填 )
	 * @param args
	 * @return
	 */
	@Override
	public Predicate<ServerWebExchange> apply(Tuple args) {
		validateMin(1, args);
		String param = args.getString(PARAM_KEY);


		return exchange -> {
			// REGEXP_KEY为空时，校验 param 对应的 QueryParam 存在。
			if (!args.hasFieldName(REGEXP_KEY)) {
				// check existence of header
				return exchange.getRequest().getQueryParams().containsKey(param);
			}

			// 当 regexp 非空时，请求 param 对应的 QueryParam 正则匹配指定值
			String regexp = args.getString(REGEXP_KEY);
			List<String> values = exchange.getRequest().getQueryParams().get(param);
			for (String value : values) {
				if (value.matches(regexp)) {
					return true;
				}
			}
			return false;
		};
	}
}
