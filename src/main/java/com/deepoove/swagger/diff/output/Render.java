package com.deepoove.swagger.diff.output;

import java.io.IOException;

import com.deepoove.swagger.diff.SwaggerDiff;

public interface Render {
	
	String render(SwaggerDiff diff);
	public void render(SwaggerDiff diff, Appendable writer) throws IOException;
}
