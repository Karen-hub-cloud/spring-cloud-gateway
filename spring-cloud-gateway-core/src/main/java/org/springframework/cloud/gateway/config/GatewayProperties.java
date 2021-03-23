package org.springframework.cloud.gateway.config;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.RemoveNonProxyHeadersGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.validation.annotation.Validated;

import static org.springframework.cloud.gateway.support.NameUtils.normalizeFilterName;

/**
 * 创建一个类型为 org.springframework.cloud.gateway.config.GatewayProperties 的 Bean 对象，
 * 用于加载配置文件配置的 RouteDefinition / FilterDefinition 。
 * 从配置文件读取 ：
 * 路由配置
 * 默认过滤器配置。当 RouteDefinition => Route 时，会将过滤器配置添加到每个 Route
 *
 * @author karen
 */
@ConfigurationProperties("spring.cloud.gateway")
@Validated
public class GatewayProperties {

	/**
	 * List of Routes
	 */
	@NotNull
	@Valid
	private List<RouteDefinition> routes = new ArrayList<>();

	/**
	 * List of filter definitions that are applied to every route.
	 */
	private List<FilterDefinition> defaultFilters = loadDefaults();

	private ArrayList<FilterDefinition> loadDefaults() {
		ArrayList<FilterDefinition> defaults = new ArrayList<>();
		FilterDefinition definition = new FilterDefinition();
		definition.setName(normalizeFilterName(RemoveNonProxyHeadersGatewayFilterFactory.class));
		defaults.add(definition);
		return defaults;
	}

	public List<RouteDefinition> getRoutes() {
		return routes;
	}

	public void setRoutes(List<RouteDefinition> routes) {
		this.routes = routes;
	}

	public List<FilterDefinition> getDefaultFilters() {
		return defaultFilters;
	}

	public void setDefaultFilters(List<FilterDefinition> defaultFilters) {
		this.defaultFilters = defaultFilters;
	}
}
