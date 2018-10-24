package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.deepoove.swagger.diff.Settings;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.Endpoint;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;

/**
 * compare two Swagger
 * 
 * @author Sayi
 *
 */
public class SpecificationDiff {

	private List<Endpoint> newEndpoints;
	private List<Endpoint> missingEndpoints;
	private List<ChangedEndpoint> changedEndpoints;
	
	private SpecificationDiff() {
	}

	public static SpecificationDiff diff(Swagger oldSpec, Swagger newSpec, Settings settings) {
		SpecificationDiff instance = new SpecificationDiff();
		if (null == oldSpec || null == newSpec) {
			throw new IllegalArgumentException("cannot diff null spec.");
		}
		String basePath = settings.getOldPrefix();
		if(basePath != null) {
			addUrlPrefix(oldSpec.getPaths(), basePath);	
		}
		basePath = settings.getNewPrefix();
		if(basePath != null) {
			addUrlPrefix(newSpec.getPaths(), basePath);	
		}
		
		Map<String, Path> oldPaths = oldSpec.getPaths();
		excludePrefix(oldPaths, settings.getExcludedPathPrefixes());
		
		Map<String, Path> newPaths = newSpec.getPaths();
		excludePrefix(newPaths, settings.getExcludedPathPrefixes());
		
		MapKeyDiff<String, Path> pathDiff = MapKeyDiff.diff(oldPaths, newPaths);
		instance.newEndpoints = convert2EndpointList(pathDiff.getIncreased());
		instance.missingEndpoints = convert2EndpointList(pathDiff.getMissing());
		instance.changedEndpoints = new ArrayList<ChangedEndpoint>();

		List<String> sharedKey = pathDiff.getSharedKey();
		ChangedEndpoint changedEndpoint = null;
		for (String pathUrl : sharedKey) {
			changedEndpoint = new ChangedEndpoint();
			changedEndpoint.setPathUrl(pathUrl);
			Path oldPath = oldPaths.get(pathUrl);
			Path newPath = newPaths.get(pathUrl);
			
			Map<HttpMethod, Operation> oldOperationMap = oldPath.getOperationMap();
			Map<HttpMethod, Operation> newOperationMap = newPath.getOperationMap();
			MapKeyDiff<HttpMethod, Operation> operationDiff = MapKeyDiff.diff(oldOperationMap, newOperationMap);
			Map<HttpMethod, Operation> increasedOperation = operationDiff.getIncreased();
			Map<HttpMethod, Operation> missingOperation = operationDiff.getMissing();
			changedEndpoint.setNewOperations(increasedOperation);
			changedEndpoint.setMissingOperations(missingOperation);
			
			List<HttpMethod> sharedMethods = operationDiff.getSharedKey();
			Map<HttpMethod, ChangedOperation> operas = new HashMap<HttpMethod, ChangedOperation>();
			ChangedOperation changedOperation = null;
			for (HttpMethod method : sharedMethods) {
				changedOperation = new ChangedOperation();
				Operation oldOperation = oldOperationMap.get(method);
				Operation newOperation = newOperationMap.get(method);
				changedOperation.setSummary(newOperation.getSummary());
				changedOperation.setOldOperation(oldOperation);
				changedOperation.setNewOperation(newOperation);
				
				List<Parameter> oldParameters = oldOperation.getParameters();
				// Exclude Headers
				excludeParametersOfType(oldParameters, HeaderParameter.class, settings.getExcludedHeaders());
				// Exclude Query Parameters
				excludeParametersOfType(oldParameters, QueryParameter.class, settings.getExcludedQueryParameters());
				
				List<Parameter> newParameters = newOperation.getParameters();
				// Exclude Headers
				excludeParametersOfType(newParameters, HeaderParameter.class, settings.getExcludedHeaders());
				// Exclude Query Parameters
				excludeParametersOfType(newParameters, QueryParameter.class, settings.getExcludedQueryParameters());
				
				ParameterDiff parameterDiff = ParameterDiff
						.buildWithDefinition(oldSpec.getDefinitions(), newSpec.getDefinitions())
						.diff(oldParameters, newParameters, settings);
				changedOperation.setAddParameters(parameterDiff.getIncreased());
				changedOperation.setMissingParameters(parameterDiff.getMissing());
				changedOperation.setChangedParameter(parameterDiff.getChanged());
				
				Property oldResponseProperty = getResponseProperty(oldOperation);
				Property newResponseProperty = getResponseProperty(newOperation);
				PropertyDiff propertyDiff = PropertyDiff.buildWithDefinition(oldSpec.getDefinitions(),
						newSpec.getDefinitions());
				propertyDiff.diff(oldResponseProperty, newResponseProperty);
				changedOperation.setAddProps(propertyDiff.getIncreased());
				changedOperation.setMissingProps(propertyDiff.getMissing());
				changedOperation.setChangedProps(propertyDiff.getChanged());

				if (changedOperation.isDiff() || changedOperation.isDiffDeprecated()) {
					operas.put(method, changedOperation);
				}
			}
			changedEndpoint.setChangedOperations(operas);

			instance.newEndpoints
					.addAll(convert2EndpointList(changedEndpoint.getPathUrl(), changedEndpoint.getNewOperations()));
			instance.missingEndpoints
					.addAll(convert2EndpointList(changedEndpoint.getPathUrl(), changedEndpoint.getMissingOperations()));

			if (changedEndpoint.isDiff()) {
				instance.changedEndpoints.add(changedEndpoint);
			}
		}

		return instance;
	}
	
	private static  <T> void addUrlPrefix(Map<String, T> map, String basePath) {
		if(map == null)
			return;
		Map<String, T> temp = new HashMap<String, T>(map);
		map.clear();
		for(String key : temp.keySet()) {
			T value = temp.get(key);
			map.put(basePath + key, value);
		}
	}
	
	private static  <T> void excludePrefix(Map<String, T> map, Set<String> prefixes) {
		if(map == null || prefixes == null || prefixes.isEmpty() || map.isEmpty())
			return;
		
		Map<String, T> temp = new HashMap<String, T>(map);
		map.clear();
		for(String key : temp.keySet()) {
			T value = temp.get(key);
			boolean exclude = false;
			for(String prefix : prefixes) {
				if(key.startsWith(prefix)) {
					exclude = true;
					break;
				}
			}
			if(!exclude)
				map.put(key, value);
		}
	}
	
	private static void excludeParametersOfType(List<Parameter> parameters, Class<? extends Parameter> parameterClass, Set<String> toBeExcluded) {
		if(parameters == null || toBeExcluded == null || toBeExcluded.isEmpty() || parameters.isEmpty())
			return;
		
		List<Parameter> temp = new ArrayList<Parameter>(parameters);
		parameters.clear();
		for(Parameter key : temp) {
			boolean exclude = false;
			if(key.getClass() == parameterClass) {
				for(String prefix : toBeExcluded) {
					String name = key.getName();
					if(prefix.equals(name)) {
						exclude = true;
						break;
					}
				}
			}
			if(!exclude)
				parameters.add(key);
		}
	}

	private static Property getResponseProperty(Operation operation) {
		Map<String, Response> responses = operation.getResponses();
		// temporary workaround for missing response messages
		if (responses == null)
			return null;
		Response response = responses.get("200");
		return null == response ? null : response.getSchema();
	}

	private static List<Endpoint> convert2EndpointList(Map<String, Path> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map)
			return endpoints;
		for (Entry<String, Path> entry : map.entrySet()) {
			String url = entry.getKey();
			Path path = entry.getValue();

			Map<HttpMethod, Operation> operationMap = path.getOperationMap();
			for (Entry<HttpMethod, Operation> entryOper : operationMap.entrySet()) {
				HttpMethod httpMethod = entryOper.getKey();
				Operation operation = entryOper.getValue();

				Endpoint endpoint = new Endpoint();
				endpoint.setPathUrl(url);
				endpoint.setMethod(httpMethod);
				endpoint.setSummary(operation.getSummary());
				endpoint.setPath(path);
				endpoint.setOperation(operation);
				endpoints.add(endpoint);
			}
		}
		return endpoints;
	}

	private static Collection<? extends Endpoint> convert2EndpointList(String pathUrl, Map<HttpMethod, Operation> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map)
			return endpoints;
		for (Entry<HttpMethod, Operation> entry : map.entrySet()) {
			HttpMethod httpMethod = entry.getKey();
			Operation operation = entry.getValue();
			Endpoint endpoint = new Endpoint();
			endpoint.setPathUrl(pathUrl);
			endpoint.setMethod(httpMethod);
			endpoint.setSummary(operation.getSummary());
			endpoint.setOperation(operation);
			endpoints.add(endpoint);
		}
		return endpoints;
	}

	public List<Endpoint> getNewEndpoints() {
		return newEndpoints;
	}

	public List<Endpoint> getMissingEndpoints() {
		return missingEndpoints;
	}

	public List<ChangedEndpoint> getChangedEndpoints() {
		return changedEndpoints;
	}

}
