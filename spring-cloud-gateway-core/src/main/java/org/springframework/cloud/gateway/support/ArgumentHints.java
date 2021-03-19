package org.springframework.cloud.gateway.support;

import org.springframework.tuple.Tuple;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * @author Spencer Gibb
 */
public interface ArgumentHints {

	/**
	 * Returns hints about the number of args and the order for shortcut parsing.
	 * @return
	 */
	default List<String> argNames() {
		return Collections.emptyList();
	}

	/**
	 * Validate supplied argument size against {@see #argNames} size.
	 * Useful for variable arg predicates.
	 * @return
	 */
	default boolean validateArgs() {
		return true;
	}

	/**
	 * args长度是否为requiredSize
	 * @param requiredSize
	 * @param args
	 */
	default void validate(int requiredSize, Tuple args) {
		Assert.isTrue(args != null && args.size() == requiredSize,
				"args must have "+ requiredSize +" entry(s)");
	}

	/**
	 * args数量至少为minSize
	 * @param minSize
	 * @param args
	 */
	default void validateMin(int minSize, Tuple args) {
		Assert.isTrue(args != null && args.size() >= minSize,
				"args must have at least "+ minSize +" entry(s)");
	}
}
