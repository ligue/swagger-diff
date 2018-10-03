package com.deepoove.swagger.diff.model;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;

public class Endpoint implements Comparable<Endpoint> {

	private String pathUrl;
	private HttpMethod method;
	private String summary;

	private Path path;
	private Operation operation;

	public String getPathUrl() {
		return pathUrl;
	}

	public void setPathUrl(String pathUrl) {
		this.pathUrl = pathUrl;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		if(summary == null)
			this.summary = "";
		else
			this.summary = summary;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public boolean isDeprecated() {
		Boolean newDep = operation.isDeprecated();
		return (newDep != null && newDep);
	}

	@Override
	public int compareTo(Endpoint o) {
		if(o == null)
			return 1;
		
		if(this.pathUrl == null) 
			return -1;
		
		String otherUrl = o.pathUrl;
		
		return this.pathUrl.compareTo(otherUrl);
	}
}
