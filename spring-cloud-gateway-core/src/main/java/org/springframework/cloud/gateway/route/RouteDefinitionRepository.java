package org.springframework.cloud.gateway.route;

/**
 * 存储器 RouteDefinitionLocator 接口
 * 通过实现该接口，实现从存储器( 例如，内存 / Redis / MySQL 等 )读取、保存、删除路由配置。
 * @author Karen
 */
public interface RouteDefinitionRepository extends RouteDefinitionLocator, RouteDefinitionWriter {
}
