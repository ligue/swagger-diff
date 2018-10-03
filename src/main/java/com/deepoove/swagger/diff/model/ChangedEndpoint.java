package com.deepoove.swagger.diff.model;

import java.util.Map;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;

public class ChangedEndpoint implements Changed, Comparable<ChangedEndpoint>{

	private String pathUrl;

	private Map<HttpMethod, Operation> newOperations;
	private Map<HttpMethod, Operation> missingOperations;

	private Map<HttpMethod, ChangedOperation> changedOperations;

	public Map<HttpMethod, Operation> getNewOperations() {
		return newOperations;
	}

	public void setNewOperations(Map<HttpMethod, Operation> newOperations) {
		this.newOperations = newOperations;
	}

	public Map<HttpMethod, Operation> getMissingOperations() {
		return missingOperations;
	}

	public void setMissingOperations(
			Map<HttpMethod, Operation> missingOperations) {
		this.missingOperations = missingOperations;
	}
	

	public Map<HttpMethod, ChangedOperation> getChangedOperations() {
		return changedOperations;
	}

	public void setChangedOperations(
			Map<HttpMethod, ChangedOperation> changedOperations) {
		this.changedOperations = changedOperations;
	}

	public String getPathUrl() {
		return pathUrl;
	}

	public void setPathUrl(String pathUrl) {
		this.pathUrl = pathUrl;
	}

	public boolean isDiff() {
//		newOperations.isEmpty() 
//		|| !missingOperations.isEmpty()
//		|| 
		return !changedOperations.isEmpty();
	}

	@Override
	public int compareTo(ChangedEndpoint o) {
		if(o == null)
			return 1;
		if(this.pathUrl == null)
			return -1;
		
		return this.pathUrl.compareTo(o.pathUrl);
	}
}
