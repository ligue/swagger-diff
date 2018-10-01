package com.deepoove.swagger.diff;

import java.util.HashSet;
import java.util.Set;

import com.beust.jcommander.Parameter;

public class Settings {
	
	@Parameter(names = "-include-comments", description = "if true it will include comments in diff", required = false, order = 2)
	private boolean includeComments = false;

	@Parameter(names = "-ignore-form-request", description = "if true it will ignore the handling of form parameter request body", required = false, order = 3)
	private boolean ignoreFormParameter = false;
	
	@Parameter(names = "-old-prefix", description = "if defined it will add in the old specs a prefix", required = false, order = 4)
	private String oldPrefix;
	
	@Parameter(names = "-new-prefix", description = "if defined it will in the new specs a prefix", required = false, order = 5)
	private String newPrefix;
	
	@Parameter(names = "-exclude-headers", description = "comma separated, if defined it will exclude headers from diff", required = false, order = 6)
	private String excludeHeaders;
	
	@Parameter(names = "-exclude-path-prefixes", description = "comma separated, if defined it will exclude paths with the specified paths from diff", required = false, order = 7)
	private String excludePathPrefixes;
	
	@Parameter(names = "-exclude-parameters", description = "comma separated, if defined it will exclude headers from diff", required = false, order = 8)
	private String excludeQueryParameters;
	
	public Set<String> getExcludedHeaders() {
		return readArray(excludeHeaders);
	}
	
	public Set<String> getExcludedPathPrefixes() {
		return readArray(excludePathPrefixes);
	}

	public Set<String> getExcludedQueryParameters() {
		return readArray(excludeQueryParameters);
	}
	
	private static Set<String> readArray(String value) {
		Set<String> set = new HashSet<String>();
		if(value == null) {
			return set;
		}
		value = value.trim();
		if(value.isEmpty()) {
			return set;
		}
		String[] array = value.split(",");
		for(String str : array) {
			str = str.trim();
			if(!str.isEmpty()) {
				set.add(str);
			}
		}
		return set;
	}
	
	public String getOldPrefix() {
		return oldPrefix;
	}

	public void setOldPrefix(String oldPrefix) {
		this.oldPrefix = oldPrefix;
	}

	public String getNewPrefix() {
		return newPrefix;
	}

	public void setNewPrefix(String newPrefix) {
		this.newPrefix = newPrefix;
	}

	public boolean isIgnoreFormParameter() {
		return ignoreFormParameter;
	}

	public void setIgnoreFormParameter(boolean ignoreFormParameter) {
		this.ignoreFormParameter = ignoreFormParameter;
	}

	public boolean isIncludeComments() {
		return includeComments;
	}
	
	public void setIncludeComments(boolean includeComments) {
		this.includeComments = includeComments;
	}
}
