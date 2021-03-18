package org.springframework.cloud.gateway.handler.predicate;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;

/**
 * 请求时间满足在配置时间之前，见{@link AfterRoutePredicateFactory}
 *
 * @author karen
 */
public class BeforeRoutePredicateFactory implements RoutePredicateFactory {

	public static final String DATETIME_KEY = "datetime";

	@Override
	public List<String> argNames() {
		return Collections.singletonList(DATETIME_KEY);
	}

	@Override
	public Predicate<ServerWebExchange> apply(Tuple args) {
		Object value = args.getValue(DATETIME_KEY);
		final ZonedDateTime dateTime = BetweenRoutePredicateFactory.getZonedDateTime(value);

		return exchange -> {
			final ZonedDateTime now = ZonedDateTime.now();
			return now.isBefore(dateTime);
		};
	}

}
