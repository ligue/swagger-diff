package com.deepoove.swagger.diff.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;

public class ChangedOperation implements Changed {

	private String summary;

	private List<Parameter> addParameters = new ArrayList<Parameter>();
	private List<Parameter> missingParameters = new ArrayList<Parameter>();

	private List<ChangedParameter> changedParameter = new ArrayList<ChangedParameter>();

	private List<ElProperty> addProps = new ArrayList<ElProperty>();
	private List<ElProperty> missingProps = new ArrayList<ElProperty>();
	private List<ElProperty> changedProps = new ArrayList<ElProperty>();

	private Operation oldOperation;
	private Operation newOperation;
	
	public List<Parameter> getAddParameters() {
		return addParameters;
	}

	public void setAddParameters(List<Parameter> addParameters) {
		this.addParameters = addParameters;
	}

	public List<Parameter> getMissingParameters() {
		return missingParameters;
	}

	public void setMissingParameters(List<Parameter> missingParameters) {
		this.missingParameters = missingParameters;
	}

	public List<ChangedParameter> getChangedParameter() {
		return changedParameter;
	}

	public void setChangedParameter(List<ChangedParameter> changedParameter) {
		this.changedParameter = changedParameter;
	}

	public List<ElProperty> getAddProps() {
		return addProps;
	}

	public void setAddProps(List<ElProperty> addProps) {
		this.addProps = addProps;
	}

	public List<ElProperty> getMissingProps() {
		return missingProps;
	}

	public void setMissingProps(List<ElProperty> missingProps) {
		this.missingProps = missingProps;
	}

	public List<ElProperty> getChangedProps() {
		return changedProps;
	}

	public void setChangedProps(List<ElProperty> changedProps) {
		this.changedProps = changedProps;
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

	public Operation getOldOperation() {
		return oldOperation;
	}

	public void setOldOperation(Operation oldOperation) {
		this.oldOperation = oldOperation;
	}

	public Operation getNewOperation() {
		return newOperation;
	}

	public void setNewOperation(Operation newOperation) {
		this.newOperation = newOperation;
	}

	public boolean isDiff() {
		return !addParameters.isEmpty() || !missingParameters.isEmpty()
				|| !changedParameter.isEmpty() || !addProps.isEmpty()
				|| !missingProps.isEmpty()
				|| isDiffProp();
	}
	
	public boolean isDiffProp(){
		return !addProps.isEmpty()
				|| !missingProps.isEmpty()
				|| !changedProps.isEmpty();
	}
	public boolean isDiffParam(){
		return !addParameters.isEmpty() || !missingParameters.isEmpty()
				|| !changedParameter.isEmpty();
	}

	/**
	 * True if the old one was not deprecated, but the new one was marked as deprecated
	 * @return
	 */
	public boolean isDiffDeprecated() {
		Boolean oldDep = oldOperation.isDeprecated();
		Boolean newDep = newOperation.isDeprecated();
		
		return !(oldDep != null && oldDep) && (newDep != null && newDep);
	}
	
	public boolean isDepreacted() {
		Boolean newDep = newOperation.isDeprecated();
		return (newDep != null && newDep);
	}
}
