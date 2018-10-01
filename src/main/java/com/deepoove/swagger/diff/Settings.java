package com.deepoove.swagger.diff;

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
