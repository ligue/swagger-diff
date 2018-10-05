package com.deepoove.swagger.diff.output;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.*;
import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import j2html.tags.ContainerTag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static j2html.TagCreator.*;

public class HtmlRender implements Render {

	private String title;
	private String linkCss;
	private static final String REMOVED_ID = "removed";
	private static final String NEW_ID = "new";
	private static final String DEPRECATED_ID = "deprecated";
	private static final String CHANGED_ID = "";

	public HtmlRender() {
		this("Api Change Log", "http://deepoove.com/swagger-diff/stylesheets/demo.css");
	}

	public HtmlRender(String title, String linkCss) {
		this.title = title;
		this.linkCss = linkCss;
	}

	public String render(SwaggerDiff diff) {
		StringBuffer sb = new StringBuffer();
		try {
			render(diff, sb);
		} catch (IOException e) {
		}
		return sb.toString();
	}

	public void render(SwaggerDiff diff, Appendable writer) throws IOException {
		List<Endpoint> endpoints = diff.getNewEndpoints();
		ContainerTag ol_newEndpoint = null;
		if (endpoints != null && !endpoints.isEmpty()) {
			Collections.sort(endpoints);
			ol_newEndpoint = ol_newEndpoint(endpoints);
		}

		ContainerTag ol_missingEndpoint = null;
		endpoints = diff.getMissingEndpoints();
		if (endpoints != null && !endpoints.isEmpty()) {
			Collections.sort(endpoints);
			ol_missingEndpoint = ol_emptyEndpoint(endpoints, REMOVED_ID);
		}

		ContainerTag ol_deprecatedEndpoint = null;
		endpoints = diff.GetDeprecatedEndpoints();
		if (endpoints != null && !endpoints.isEmpty()) {
			Collections.sort(endpoints);
			ol_deprecatedEndpoint = ol_emptyEndpoint(endpoints, DEPRECATED_ID);
		}

		ContainerTag ol_changed = null;
		List<ChangedEndpoint> changedEndpoints = diff.getChangedEndpoints();
		if (changedEndpoints != null && !changedEndpoints.isEmpty()) {
			Collections.sort(changedEndpoints);
			ol_changed = ol_changed(changedEndpoints);
		}
		
		ContainerTag p_versions = p_versions(diff.getOldVersion(), diff.getNewVersion());

		renderHtml(ol_newEndpoint, ol_missingEndpoint, ol_changed, ol_deprecatedEndpoint, p_versions, writer);
	}

	public String renderHtml(ContainerTag ol_new, ContainerTag ol_miss, ContainerTag ol_changed,
			ContainerTag ol_deprecated, ContainerTag p_versions) {
		StringBuffer sb = new StringBuffer();
		try {
			renderHtml(ol_new, ol_miss, ol_changed, ol_deprecated, p_versions, sb);
		} catch (IOException e) {
		}
		return sb.toString();
	}

	public void renderHtml(ContainerTag ol_new, ContainerTag ol_miss, ContainerTag ol_changed,
			ContainerTag ol_deprecated, ContainerTag p_versions, Appendable writer) throws IOException {
		ContainerTag html = prepareRender(ol_new, ol_miss, ol_changed, ol_deprecated, p_versions);
		document().render(writer);
		html.render(writer);
	}

	private ContainerTag prepareRender(ContainerTag ol_new, ContainerTag ol_miss, ContainerTag ol_changed,
			ContainerTag ol_deprecated, ContainerTag p_versions) {
		
		List<ContainerTag> tags = new ArrayList<ContainerTag>();
		tags.add(div_headArticle("Versions", "versions", p_versions));
		
		if(ol_new != null) tags.add(div_headArticle("New Endpoints", NEW_ID, ol_new));
		if(ol_miss != null) tags.add(div_headArticle("Removed Endpoints", REMOVED_ID, ol_miss));
		if(ol_deprecated != null) tags.add(div_headArticle("Deprecated Endpoints", DEPRECATED_ID, ol_deprecated));
		if(ol_changed != null) tags.add(div_headArticle("Changed Endpoints", CHANGED_ID, ol_changed));
		
		
		ContainerTag html = html().attr("lang", "en")
				.with(head().with(meta().withCharset("utf-8"), title(title), script(rawHtml(
						"function showHide(id){if(document.getElementById(id).style.display==\'none\'){document.getElementById(id).style.display=\'block\';document.getElementById(\'btn_\'+id).innerHTML=\'&uArr;\';}else{document.getElementById(id).style.display=\'none\';document.getElementById(\'btn_\'+id).innerHTML=\'&dArr;\';}return true;}"))
								.withType("text/javascript"),
						link().withRel("stylesheet").withHref(linkCss)),
						body().with(header().with(h1(title)),
								div().withClass("article").with(
										tags.toArray(new ContainerTag[tags.size()]))));
		return html;
	}

	private ContainerTag div_headArticle(final String title, final String type, final ContainerTag ol) {
		return div().with(h2(title).with(a(rawHtml("&uArr;")).withId("btn_" + type).withClass("showhide").withHref("#")
				.attr("onClick", "javascript:showHide('" + type + "');")), hr(), ol);
	}

	private ContainerTag p_versions(String oldVersion, String newVersion) {
		ContainerTag p = p().withId("versions");
		p.withText("Changes from " + oldVersion + " to " + newVersion + ".");
		return p;
	}

	private ContainerTag ol_newEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints)
			return ol().withId(NEW_ID);
		ContainerTag ol = ol().withId(NEW_ID);
		for (Endpoint endpoint : endpoints) {
			ol.with(li_newEndpoint(endpoint));
		}
		return ol;
	}

	private ContainerTag li_newEndpoint(Endpoint endpoint) {
		String method = endpoint.getMethod().toString();
		String path = endpoint.getPathUrl();
		String desc = endpoint.getSummary();
		ContainerTag ct = li().with(span(method).withClass(method));
		if (endpoint.isDeprecated())
			ct.with(del(path + " "));
		else
			ct.withText(path + " ");
		return ct.with(span(null == desc ? "" : desc));
	}

	private ContainerTag ol_emptyEndpoint(List<Endpoint> endpoints, String headerId) {
		if (null == endpoints)
			return ol().withId(headerId);
		ContainerTag ol = ol().withId(headerId);
		for (Endpoint endpoint : endpoints) {
			ol.with(li_newEndpoint(endpoint));
		}
		return ol;
	}

	private ContainerTag ol_changed(List<ChangedEndpoint> changedEndpoints) {
		if (null == changedEndpoints)
			return ol().withId(CHANGED_ID);
		ContainerTag ol = ol().withId(CHANGED_ID);
		for (ChangedEndpoint changedEndpoint : changedEndpoints) {
			String pathUrl = changedEndpoint.getPathUrl();
			Map<HttpMethod, ChangedOperation> changedOperations = changedEndpoint.getChangedOperations();
			for (Entry<HttpMethod, ChangedOperation> entry : changedOperations.entrySet()) {
				ChangedOperation changedOperation = entry.getValue();
				if (!changedOperation.isDiff())
					continue; // At this point the operation must be just deprecated

				String method = entry.getKey().toString();
				String desc = changedOperation.getSummary();

				ContainerTag ul_detail = ul().withClass("detail");
				if (changedOperation.isDiffParam()) {
					ul_detail.with(li().with(h3("Parameter")).with(ul_param(changedOperation)));
				}
				if (changedOperation.isDiffProp()) {
					ul_detail.with(li().with(h3("Return Type")).with(ul_response(changedOperation)));
				}
				if (changedOperation.isDepreacted()) {
					ol.with(li().with(span(method).withClass(method)).with(del(pathUrl + " "))
							.with(span(null == desc ? "" : desc)).with(ul_detail));
				} else {
					ol.with(li().with(span(method).withClass(method)).withText(pathUrl + " ")
							.with(span(null == desc ? "" : desc)).with(ul_detail));
				}
			}
		}
		return ol;
	}

	private ContainerTag ul_response(ChangedOperation changedOperation) {
		List<ElProperty> addProps = changedOperation.getAddProps();
		List<ElProperty> delProps = changedOperation.getMissingProps();
		ContainerTag ul = ul().withClass("change response");
		for (ElProperty prop : addProps) {
			ul.with(li_addProp(prop));
		}
		for (ElProperty prop : delProps) {
			ul.with(li_missingProp(prop));
		}
		return ul;
	}

	private ContainerTag li_missingProp(ElProperty prop) {
		Property property = prop.getProperty();
		return li().withClass("missing").withText("Delete").with(del(prop.getEl())).with(
				span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
	}

	private ContainerTag li_addProp(ElProperty prop) {
		Property property = prop.getProperty();
		return li().withText("Add " + prop.getEl()).with(
				span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
	}

	private ContainerTag ul_param(ChangedOperation changedOperation) {
		List<Parameter> addParameters = changedOperation.getAddParameters();
		List<Parameter> delParameters = changedOperation.getMissingParameters();
		List<ChangedParameter> changedParameters = changedOperation.getChangedParameter();
		ContainerTag ul = ul().withClass("change param");
		for (Parameter param : addParameters) {
			ul.with(li_addParam(param));
		}
		for (ChangedParameter param : changedParameters) {
			List<ElProperty> increased = param.getIncreased();
			for (ElProperty prop : increased) {
				ul.with(li_addProp(prop));
			}
		}
		for (ChangedParameter param : changedParameters) {
			boolean changeRequired = param.isChangeRequired();
			boolean changeDescription = param.isChangeDescription();
			if (changeRequired || changeDescription)
				ul.with(li_changedParam(param));
		}
		for (ChangedParameter param : changedParameters) {
			List<ElProperty> missing = param.getMissing();
			for (ElProperty prop : missing) {
				ul.with(li_missingProp(prop));
			}
		}
		for (Parameter param : delParameters) {
			ul.with(li_missingParam(param));
		}
		return ul;
	}

	private ContainerTag li_addParam(Parameter param) {
		return li().withText("Add " + param.getName())
				.with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
	}

	private ContainerTag li_missingParam(Parameter param) {
		return li().withClass("missing").with(span("Delete")).with(del(param.getName()))
				.with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
	}

	private ContainerTag li_changedParam(ChangedParameter changeParam) {
		boolean changeRequired = changeParam.isChangeRequired();
		boolean changeDescription = changeParam.isChangeDescription();
		Parameter rightParam = changeParam.getRightParameter();
		Parameter leftParam = changeParam.getLeftParameter();
		ContainerTag li = li().withText(rightParam.getName());
		if (changeRequired) {
			li.withText(" change into " + (rightParam.getRequired() ? "required" : "not required"));
		}
		if (changeDescription) {
			li.withText(" Notes ").with(del(leftParam.getDescription()).withClass("comment")).withText(" change into ")
					.with(span(span(null == rightParam.getDescription() ? "" : rightParam.getDescription())
							.withClass("comment")));
		}
		return li;
	}

}
