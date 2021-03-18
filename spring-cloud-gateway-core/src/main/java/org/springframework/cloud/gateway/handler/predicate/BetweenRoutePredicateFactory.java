package org.springframework.cloud.gateway.handler.predicate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.Predicate;

import org.springframework.tuple.Tuple;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

/**
 * 请求时间满足在配置时间之间。
 *
 * @author karen
 */
public class BetweenRoutePredicateFactory implements RoutePredicateFactory {

	public static final String DATETIME1_KEY = "datetime1";
	public static final String DATETIME2_KEY = "datetime2";

	/**
	 * args is datetime1 / datetime2
	 */
	@Override
	public Predicate<ServerWebExchange> apply(Tuple args) {
		//TODO: is ZonedDateTime the right thing to use?
		final ZonedDateTime dateTime1 = getZonedDateTime(args.getValue(DATETIME1_KEY));
		final ZonedDateTime dateTime2 = getZonedDateTime(args.getValue(DATETIME2_KEY));
		Assert.isTrue(dateTime1.isBefore(dateTime2), args.getValue(DATETIME1_KEY) +
				" must be before " + args.getValue(DATETIME2_KEY));

		return exchange -> {
			final ZonedDateTime now = ZonedDateTime.now();
			return now.isAfter(dateTime1) && now.isBefore(dateTime2);
		};
	}

	/**
	 * 判断DATETIME1_KEY的类型，进而进行不同的处理
	 */
	public static ZonedDateTime getZonedDateTime(Object value) {
		ZonedDateTime dateTime;
		if (value instanceof ZonedDateTime) {
			dateTime = ZonedDateTime.class.cast(value);
		} else {
			dateTime = parseZonedDateTime(value.toString());
		}
		return dateTime;
	}

	/**
	 * 当值类型为 Long，例如配置文件 1511795602765
	 * 或者 当值类型为 String 。例如配置文件里 2017-01-20T17:42:47.789-07:00[America/Denver]
	 */
	public static ZonedDateTime parseZonedDateTime(String dateString) {
		ZonedDateTime dateTime;
		try {
			long epoch = Long.parseLong(dateString);
			dateTime = Instant.ofEpochMilli(epoch).atOffset(ZoneOffset.ofTotalSeconds(0))
					.toZonedDateTime();
		} catch (NumberFormatException e) {
			dateTime = ZonedDateTime.parse(dateString);
		}

		return dateTime;
	}

}
