package com.deepoove.swagger.diff.output;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.ChangedParameter;
import com.deepoove.swagger.diff.model.Endpoint;
import com.deepoove.swagger.diff.model.ElProperty;

import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;
import j2html.tags.ContainerTag;

public class MarkdownRender implements Render {

	final String H3 = "### ";
	final String H2 = "## ";
	final String BLOCKQUOTE = "> ";
	final String CODE = "`";
	final String PRE_CODE = "    ";
	final String PRE_LI = "    ";
	final String LI = "* ";
	final String HR = "---\n";
	final String STRIKE = "~~";

	public MarkdownRender() {
	}

	public void render(SwaggerDiff diff, Appendable writer) throws IOException {
		List<Endpoint> endpoints = diff.getNewEndpoints();
		if(endpoints != null)
			Collections.sort(endpoints);
		String ol_newEndpoint = ol_newEndpoint(endpoints);

		endpoints = diff.getMissingEndpoints();
		if(endpoints != null)
			Collections.sort(endpoints);
		String ol_missingEndpoint = ol_missingEndpoint(endpoints);

		endpoints = diff.GetDeprecatedEndpoints();
		if(endpoints != null)
			Collections.sort(endpoints);
		String ol_depEndpoint = ol_missingEndpoint(endpoints);
		
		List<ChangedEndpoint> changedEndpoints = diff.getChangedEndpoints();
		if(changedEndpoints != null)
			Collections.sort(changedEndpoints);
		String ol_changed = ol_changed(changedEndpoints);

		renderHtml(diff.getOldVersion(), diff.getNewVersion(), ol_newEndpoint,
				ol_missingEndpoint, ol_changed, ol_depEndpoint, writer);
	}

	public String render(SwaggerDiff diff) {
		StringBuffer sb = new StringBuffer();
		try {
			render(diff, sb);
		} catch (IOException e) {
		}
		return sb.toString();
	}

	public String renderHtml(String oldVersion, String newVersion, String ol_new, String ol_miss, String ol_changed, String ol_depEndpoint) {
		StringBuffer sb = new StringBuffer();
		try {
			renderHtml(oldVersion, newVersion, ol_new, ol_miss, ol_changed, ol_depEndpoint, sb);
		} catch (IOException e) {
		}
		return sb.toString();
	}

	public void renderHtml(String oldVersion, String newVersion, String ol_new, String ol_miss, String ol_changed, String ol_depEndpoint, Appendable sb)
			throws IOException {
		sb.append(H2).append("Version " + oldVersion + " to " + newVersion).append("\n").append(HR);
		if(ol_new != null && !ol_new.isEmpty())
			sb.append(H3).append("New Endpoints").append("\n").append(HR).append(ol_new).append("\n");
		
		if(ol_miss != null && !ol_miss.isEmpty())
			sb.append(H3).append("Removed Endpoints").append("\n").append(HR)
			.append(ol_miss).append("\n");
		
		if(ol_depEndpoint != null && !ol_depEndpoint.isEmpty())
			sb.append(H3).append("Deprecated Endpoints").append("\n").append(HR)
			.append(ol_depEndpoint).append("\n");
		
		if(ol_changed != null && !ol_changed.isEmpty())
			sb.append(H3).append("Changed Endpoints").append("\n").append(HR)
			.append(ol_changed);
	}

	private String ol_newEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints)
			return "";
		StringBuffer sb = new StringBuffer();
		for (Endpoint endpoint : endpoints) {
			sb.append(li_newEndpoint(endpoint));
		}
		return sb.toString();
	}

	private String li_newEndpoint(Endpoint endpoint) {
		StringBuffer sb = new StringBuffer();
		sb.append(LI).append(CODE).append(endpoint.getMethod().toString())
		.append(CODE);
		if(endpoint.isDeprecated()) {
			sb.append(" ").append(STRIKE).append(endpoint.getPathUrl())
			.append(removeLines(endpoint.getSummary())).append(STRIKE).append('\n');
		} else {
			sb.append(" ").append(endpoint.getPathUrl()).append(" ").append(removeLines(endpoint.getSummary())).append('\n');
		}
		return sb.toString();
	}

	private String ol_missingEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints || endpoints.isEmpty())
			return "";
		StringBuffer sb = new StringBuffer();
		for (Endpoint endpoint : endpoints) {
			sb.append(li_newEndpoint(endpoint));
		}
		return sb.toString();
	}
	
	private String ol_changed(List<ChangedEndpoint> changedEndpoints) {
		if (null == changedEndpoints)
			return "";
		StringBuffer sb = new StringBuffer();
		for (ChangedEndpoint changedEndpoint : changedEndpoints) {
			String pathUrl = changedEndpoint.getPathUrl();
			Map<HttpMethod, ChangedOperation> changedOperations = changedEndpoint.getChangedOperations();
			for (Entry<HttpMethod, ChangedOperation> entry : changedOperations.entrySet()) {
				ChangedOperation changedOperation = entry.getValue();
				if(!changedOperation.isDiff())
					continue; // At this point the operation must be just deprecated
				
				String method = entry.getKey().toString();
				String desc = removeLines(changedOperation.getSummary());
				
				StringBuffer ul_detail = new StringBuffer();
				if (changedOperation.isDiffParam()) {
					ul_detail.append(PRE_LI).append("Parameter").append(ul_param(changedOperation));
				}
				if (changedOperation.isDiffProp()) {
					ul_detail.append(PRE_LI).append("Return Type").append(ul_response(changedOperation));
				}
				sb.append(LI).append(CODE).append(method).append(CODE).append(" ");
				
				if(changedOperation.isDepreacted()) {
					sb.append("~~").append(pathUrl).append("~~");
				} else {
					sb.append(pathUrl);
				}
				
				sb.append(" ").append(desc).append("  \n").append(ul_detail);
			}
		}
		return sb.toString();
	}

	private String ul_response(ChangedOperation changedOperation) {
		List<ElProperty> addProps = changedOperation.getAddProps();
		List<ElProperty> delProps = changedOperation.getMissingProps();
		StringBuffer sb = new StringBuffer("\n\n");
		for (ElProperty prop : addProps) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_addProp(prop) + "\n");
		}
		for (ElProperty prop : delProps) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_missingProp(prop) + "\n");
		}
		return sb.toString();
	}

	private String li_missingProp(ElProperty prop) {
		Property property = prop.getProperty();
		StringBuffer sb = new StringBuffer("");
		sb.append("Delete ").append(prop.getEl())
				.append(removeLines(null == property.getDescription() ? "" : (" //" + property.getDescription())));
		return sb.toString();
	}

	private String li_addProp(ElProperty prop) {
		Property property = prop.getProperty();
		StringBuffer sb = new StringBuffer("");
		sb.append("Add ").append(prop.getEl())
				.append(removeLines(null == property.getDescription() ? "" : (" //" + property.getDescription())));
		return sb.toString();
	}

	private String ul_param(ChangedOperation changedOperation) {
		List<Parameter> addParameters = changedOperation.getAddParameters();
		List<Parameter> delParameters = changedOperation.getMissingParameters();
		List<ChangedParameter> changedParameters = changedOperation.getChangedParameter();
		
		StringBuffer sb = new StringBuffer("\n\n");
		for (Parameter param : addParameters) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_addParam(param) + "\n");
		}
		for (ChangedParameter param : changedParameters) {
			List<ElProperty> increased = param.getIncreased();
			for (ElProperty prop : increased) {
				sb.append(PRE_LI).append(PRE_CODE).append(li_addProp(prop) + "\n");
			}
		}
		for (ChangedParameter param : changedParameters) {
			boolean changeRequired = param.isChangeRequired();
			boolean changeDescription = param.isChangeDescription();
			if (changeRequired || changeDescription)
				sb.append(PRE_LI).append(PRE_CODE).append(li_changedParam(param) + "\n");
		}
		for (ChangedParameter param : changedParameters) {
			List<ElProperty> missing = param.getMissing();
			for (ElProperty prop : missing) {
				sb.append(PRE_LI).append(PRE_CODE).append(li_missingProp(prop) + "\n");
			}
		}
		for (Parameter param : delParameters) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_missingParam(param) + "\n");
		}
		return sb.toString();
	}

	private String li_addParam(Parameter param) {
		StringBuffer sb = new StringBuffer("");
		sb.append("Add ").append(param.getName())
				.append(removeLines(null == param.getDescription() ? "" : (" //" + param.getDescription())));
		return sb.toString();
	}

	private String li_missingParam(Parameter param) {
		StringBuffer sb = new StringBuffer("");
		sb.append("Delete ").append(param.getName())
				.append(removeLines(null == param.getDescription() ? "" : (" //" + param.getDescription())));
		return sb.toString();
	}

	private String li_changedParam(ChangedParameter changeParam) {
		boolean changeRequired = changeParam.isChangeRequired();
		boolean changeDescription = changeParam.isChangeDescription();
		Parameter rightParam = changeParam.getRightParameter();
		Parameter leftParam = changeParam.getLeftParameter();
		StringBuffer sb = new StringBuffer("");
		sb.append(rightParam.getName());
		if (changeRequired) {
			sb.append(" change into " + (rightParam.getRequired() ? "required" : "not required"));
		}
		if (changeDescription) {
			sb.append(" Notes ").append(removeLines(leftParam.getDescription())).append(" change into ")
					.append(removeLines(rightParam.getDescription()));
		}
		return sb.toString();
	}
	
	private String removeLines(String value) {
		return replaceAll(value, "\n", "");
	}
	
	private String replaceAll(String value, String olds, String news) {
		if(value == null || value.isEmpty()) return value;
		return value.replaceAll(olds, news);
	}
}
