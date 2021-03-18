package org.springframework.cloud.gateway.handler.predicate;

import static org.springframework.cloud.gateway.handler.support.RoutePredicateFactoryUtils.traceMatch;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
import static org.springframework.http.server.PathContainer.parsePath;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.PathContainer;
import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPattern.PathMatchInfo;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 请求 Path 匹配指定值。注意path=/foo/123 <=> /foo/{segment}的解析
 *
 * @author karen
 */
public class PathRoutePredicateFactory implements RoutePredicateFactory {

	private PathPatternParser pathPatternParser = new PathPatternParser();

	public void setPathPatternParser(PathPatternParser pathPatternParser) {
		this.pathPatternParser = pathPatternParser;
	}

	@Override
	public List<String> argNames() {
		return Collections.singletonList(PATTERN_KEY);
	}

	@Override
	public Predicate<ServerWebExchange> apply(Tuple args) {
		// 解析 Path ，创建对应的 PathPattern
		String unparsedPattern = args.getString(PATTERN_KEY);
		PathPattern pattern;
		//考虑到解析过程中的线程安全，此处使用 synchronized 修饰符，
		synchronized (this.pathPatternParser) {
			pattern = this.pathPatternParser.parse(unparsedPattern);
		}

		return exchange -> {
			PathContainer path = parsePath(exchange.getRequest().getURI().getPath());

			// 匹配
			boolean match = pattern.matches(path);
			traceMatch("Pattern", pattern.getPatternString(), path, match);
			if (match) {
				// 解析 路径参数，例如 path=/foo/123 <=> /foo/{segment}
				PathMatchInfo uriTemplateVariables = pattern.matchAndExtract(path);
				exchange.getAttributes().put(URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVariables);
				return true;
			} else {
				return false;
			}
		};
	}
}
