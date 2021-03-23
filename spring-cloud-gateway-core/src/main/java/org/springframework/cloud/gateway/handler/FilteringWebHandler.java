package org.springframework.cloud.gateway.handler;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;

import reactor.core.publisher.Mono;

/**
 * 获得 Route 的 GatewayFilter 数组，创建 GatewayFilterChain 处理请求。
 *
 * @author karen
 */
public class FilteringWebHandler implements WebHandler {
	protected static final Log logger = LogFactory.getLog(FilteringWebHandler.class);

	/**
	 * 全局过滤器
	 * 在 FilteringWebHandler 初始化时，将 GlobalFilter 委托成 GatewayFilterAdapter，
	 * 通过{@link GatewayFilterAdapter}
	 */
	private final List<GatewayFilter> globalFilters;

	public FilteringWebHandler(List<GlobalFilter> globalFilters) {
		this.globalFilters = loadFilters(globalFilters);
	}

	private static List<GatewayFilter> loadFilters(List<GlobalFilter> filters) {
		return filters.stream()
				.map(filter -> {
					GatewayFilterAdapter gatewayFilter = new GatewayFilterAdapter(filter);
					//当 GlobalFilter 子类实现了 org.springframework.core.Ordered 接口，
					// 在委托一层 OrderedGatewayFilter 。
					// 这样 AnnotationAwareOrderComparator#sort(List) 方法好排序。
					if (filter instanceof Ordered) {
						int order = ((Ordered) filter).getOrder();
						return new OrderedGatewayFilter(gatewayFilter, order);
					}
					//当 GlobalFilter 子类没有实现了 org.springframework.core.Ordered 接口，
					// 在 AnnotationAwareOrderComparator#sort(List) 排序时，顺序值为 Integer.MAX_VALUE
					return gatewayFilter;
				}).collect(Collectors.toList());
	}


	@Override
	public Mono<Void> handle(ServerWebExchange exchange) {
		// 获得 Route
		Route route = exchange.getRequiredAttribute(GATEWAY_ROUTE_ATTR);
		// 获得 GatewayFilter 数组
		List<GatewayFilter> gatewayFilters = route.getFilters();
		List<GatewayFilter> combined = new ArrayList<>(this.globalFilters);
		combined.addAll(gatewayFilters);

		// 排序
		//TODO: needed or cached?
		AnnotationAwareOrderComparator.sort(combined);
		logger.debug("Sorted gatewayFilterFactories: " + combined);

		// 创建 DefaultGatewayFilterChain
		return new DefaultGatewayFilterChain(combined).filter(exchange);
	}

	/**
	 * 网关过滤器链默认实现类
	 */
	private static class DefaultGatewayFilterChain implements GatewayFilterChain {

		private int index;
		private final List<GatewayFilter> filters;

		public DefaultGatewayFilterChain(List<GatewayFilter> filters) {
			this.filters = filters;
		}

		@Override
		public Mono<Void> filter(ServerWebExchange exchange) {
			if (this.index < filters.size()) {
				GatewayFilter filter = filters.get(this.index++);
				return filter.filter(exchange, this);
			} else {
				return Mono.empty(); // complete
			}
		}
	}

	/**
	 * 网关过滤器适配器。在 GatewayFilterChain 使用 GatewayFilter 过滤请求，
	 * 所以通过 GatewayFilterAdapter 将 GlobalFilter 适配成 GatewayFilter
	 */
	private static class GatewayFilterAdapter implements GatewayFilter {

		private final GlobalFilter delegate;

		public GatewayFilterAdapter(GlobalFilter delegate) {
			this.delegate = delegate;
		}

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
			return this.delegate.filter(exchange, chain);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("GatewayFilterAdapter{");
			sb.append("delegate=").append(delegate);
			sb.append('}');
			return sb.toString();
		}
	}

}
