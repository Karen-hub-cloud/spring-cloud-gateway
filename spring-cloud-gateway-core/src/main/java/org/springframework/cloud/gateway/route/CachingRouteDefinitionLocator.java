package org.springframework.cloud.gateway.route;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;

/**
 * 从缓存中读取RouteDefinition？
 * @author Karen
 */
public class CachingRouteDefinitionLocator implements RouteDefinitionLocator {

	private final RouteDefinitionLocator delegate;
	private final AtomicReference<List<RouteDefinition>> cachedRoutes = new AtomicReference<>();

	public CachingRouteDefinitionLocator(RouteDefinitionLocator delegate) {
		this.delegate = delegate;
		this.cachedRoutes.compareAndSet(null, collectRoutes());
	}

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		return Flux.fromIterable(this.cachedRoutes.get());
	}

	/**
	 * Sets the new routes
	 * @return old routes
	 */
	public Flux<RouteDefinition> refresh() {
		return Flux.fromIterable(this.cachedRoutes.getAndUpdate(
				routes -> CachingRouteDefinitionLocator.this.collectRoutes()));
	}

	private List<RouteDefinition> collectRoutes() {
		return this.delegate.getRouteDefinitions().collectList().block();
	}

	@EventListener(RefreshRoutesEvent.class)
    /* for testing */ void handleRefresh() {
        refresh();
    }
}
