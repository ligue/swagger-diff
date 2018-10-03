package com.deepoove.swagger.diff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deepoove.swagger.diff.compare.SpecificationDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.Endpoint;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.SwaggerCompatConverter;
import io.swagger.parser.SwaggerParser;

public class SwaggerDiff {

    public static final String SWAGGER_VERSION_V2 = "2.0";

    private static Logger logger = LoggerFactory.getLogger(SwaggerDiff.class);

    private Swagger oldSpecSwagger;
    private Swagger newSpecSwagger;

    private List<Endpoint> newEndpoints;
    private List<Endpoint> missingEndpoints;
    private List<ChangedEndpoint> changedEndpoints;
    
    private Settings settings;

    /**
     * compare two swagger 1.x doc
     * 
     * @param oldSpec
     *            old api-doc location:Json or Http
     * @param newSpec
     *            new api-doc location:Json or Http
     * @param settings TODO
     */
    public static SwaggerDiff compareV1(String oldSpec, String newSpec, Settings settings) {
        return compare(oldSpec, newSpec, null, null, settings);
    }

    /**
     * compare two swagger v2.0 doc
     * 
     * @param oldSpec
     *            old api-doc location:Json or Http
     * @param newSpec
     *            new api-doc location:Json or Http
     * @param settings TODO
     */
    public static SwaggerDiff compareV2(String oldSpec, String newSpec, Settings settings) {
        return compare(oldSpec, newSpec, null, SWAGGER_VERSION_V2, settings);
    }

    /**
     * Compare two swagger v2.0 docs by JsonNode
     *
     * @param oldSpec
     *            old Swagger specification document in v2.0 format as a JsonNode
     * @param newSpec
     *            new Swagger specification document in v2.0 format as a JsonNode
     * @param settings TODO
     */
    public static SwaggerDiff compareV2(JsonNode oldSpec, JsonNode newSpec, Settings settings) {
        return new SwaggerDiff(oldSpec, newSpec, settings).compare();
    }

    public static SwaggerDiff compare(String oldSpec, String newSpec,
            List<AuthorizationValue> auths, String version, Settings settings) {
        return new SwaggerDiff(oldSpec, newSpec, auths, version, settings).compare();
    }

    /**
     * @param oldSpec
     * @param newSpec
     * @param auths
     * @param version
     */
    private SwaggerDiff(String oldSpec, String newSpec, List<AuthorizationValue> auths,
            String version, Settings settings) {
        if (SWAGGER_VERSION_V2.equals(version)) {
            SwaggerParser swaggerParser = new SwaggerParser();
            oldSpecSwagger = swaggerParser.read(oldSpec, auths, true);
            newSpecSwagger = swaggerParser.read(newSpec, auths, true);
        } else {
            SwaggerCompatConverter swaggerCompatConverter = new SwaggerCompatConverter();
            try {
                oldSpecSwagger = swaggerCompatConverter.read(oldSpec, auths);
                newSpecSwagger = swaggerCompatConverter.read(newSpec, auths);
            } catch (IOException e) {
                logger.error("cannot read api-doc from spec[version_v1.x]", e);
                return;
            }
        }
        this.settings = settings != null ? settings : new Settings();
        if (null == oldSpecSwagger || null == newSpecSwagger) { throw new RuntimeException(
                "cannot read api-doc from spec."); }
    }

    private SwaggerDiff(JsonNode oldSpec, JsonNode newSpec, Settings settings) {
        SwaggerParser swaggerParser = new SwaggerParser();
        oldSpecSwagger = swaggerParser.read(oldSpec, true);
        newSpecSwagger = swaggerParser.read(newSpec, true);
        this.settings = settings != null ? settings : new Settings();
        if (null == oldSpecSwagger) { throw new RuntimeException(
            "cannot read old api-doc from spec."); }
        if (null == newSpecSwagger) { throw new RuntimeException(
                "cannot read new api-doc from spec."); }
    }

    private SwaggerDiff compare() {
    	SpecificationDiff diff = SpecificationDiff.diff(oldSpecSwagger, newSpecSwagger, settings);
        this.newEndpoints = diff.getNewEndpoints();
        this.missingEndpoints = diff.getMissingEndpoints();
        this.changedEndpoints = diff.getChangedEndpoints();
        return this;
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

    /**
     * @return the endpoints which are not deprecated in the old but where marked deprecated in new, and has no other changes.
     */
    public List<Endpoint> GetDeprecatedEndpoints() {
    	List<Endpoint> endpoints = new ArrayList<Endpoint>();
    	for(ChangedEndpoint endpoint : changedEndpoints) {
    		Map<HttpMethod, ChangedOperation> ops = endpoint.getChangedOperations();
    		if(ops == null || ops.isEmpty())
    			continue;
    		for(HttpMethod method : ops.keySet()) {
    			ChangedOperation op = ops.get(method);
    			if(!op.isDiff() && op.isDiffDeprecated()) {
    				Endpoint ep = new Endpoint();
    				ep.setMethod(method);
    				ep.setOperation(op.getNewOperation());
    				ep.setPathUrl(endpoint.getPathUrl());
    				ep.setSummary(op.getSummary());
    				endpoints.add(ep);
    			}
    		}
    	}
    	return endpoints;
    }
    
    public String getOldVersion() {
        return oldSpecSwagger.getInfo().getVersion();
    }

    public String getNewVersion() {
        return newSpecSwagger.getInfo().getVersion();
    }
}
