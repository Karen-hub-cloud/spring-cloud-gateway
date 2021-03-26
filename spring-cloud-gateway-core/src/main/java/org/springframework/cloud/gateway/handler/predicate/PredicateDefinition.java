package org.springframework.cloud.gateway.handler.predicate;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.validation.annotation.Validated;

/**
 * 谓语定义
 * 同{@link org.springframework.cloud.gateway.filter.FilterDefinition}
 * 解析配置中的Predicate，绑定到一个PredicateDefinition对象
 *
 * @author Karen
 */
@Validated
public class PredicateDefinition {

	/**
	 * 谓语定义名字
	 */
	@NotNull
	private String name;
	/**
	 * 参数数组
	 */
	private Map<String, String> args = new LinkedHashMap<>();

	public PredicateDefinition() {
	}

	/**
	 * 根据 text 创建 PredicateDefinition
	 *
	 * @param text 格式 ${name}=${args[0]},${args[1]}...${args[n]}
	 * 例如 Host=iocoder.cn
	 */
	public PredicateDefinition(String text) {
		int eqIdx = text.indexOf("=");
		if (eqIdx <= 0) {
			throw new ValidationException("Unable to parse PredicateDefinition text '" + text + "'" +
					", must be of the form name=value");
		}
		// name
		setName(text.substring(0, eqIdx));
		// args
		String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");
		for (int i = 0; i < args.length; i++) {
			this.args.put(NameUtils.generateName(i), args[i]);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getArgs() {
		return args;
	}

	public void setArgs(Map<String, String> args) {
		this.args = args;
	}

	public void addArg(String key, String value) {
		this.args.put(key, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PredicateDefinition that = (PredicateDefinition) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(args, that.args);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, args);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PredicateDefinition{");
		sb.append("name='").append(name).append('\'');
		sb.append(", args=").append(args);
		sb.append('}');
		return sb.toString();
	}

	public static void main(String[] args) {
		new PredicateDefinition("Host=iocoder.cn");
	}

}
