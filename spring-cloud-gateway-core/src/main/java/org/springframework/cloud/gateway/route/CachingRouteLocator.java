package org.springframework.cloud.gateway.route;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 缓存路由的 RouteLocator 实现类。
 * RoutePredicateHandlerMapping 调用 CachingRouteLocator 的 RouteLocator#getRoutes() 方法，获取路由。
 *
 * @author karen
 */
public class CachingRouteLocator implements RouteLocator {

	private final RouteLocator delegate;
	/**
	 * 路由缓存
	 */
	private final AtomicReference<List<Route>> cachedRoutes = new AtomicReference<>();

	/**
	 * 构造方法，调用 #collectRoutes() 方法获得路由，并缓存到 cachedRoutes 属性
	 * @param delegate
	 */
	public CachingRouteLocator(RouteLocator delegate) {
		this.delegate = delegate;
		this.cachedRoutes.compareAndSet(null, collectRoutes());
	}

	/**
	 * 返回路由缓存
	 * @return
	 */
	@Override
	public Flux<Route> getRoutes() {
		return Flux.fromIterable(this.cachedRoutes.get());
	}

	/**
	 * 刷新缓存 cachedRoutes 属性
	 *
	 * @return old routes
	 */
	public Flux<Route> refresh() {
		return Flux.fromIterable(this.cachedRoutes.getAndUpdate(
				routes -> CachingRouteLocator.this.collectRoutes()));
	}

	/**
	 * 从 delegate 获取路由数组。
	 * @return
	 */
	private List<Route> collectRoutes() {
		List<Route> routes = this.delegate.getRoutes().collectList().block();
		// 排序
		AnnotationAwareOrderComparator.sort(routes);
		return routes;
	}

	/**
	 * 监听 org.springframework.context.ApplicationEvent.RefreshRoutesEvent 事件，刷新缓存。
	 */
	@EventListener(RefreshRoutesEvent.class)
		/* for testing */ void handleRefresh() {
		refresh();
	}
}
