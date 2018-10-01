package com.deepoove.swagger.diff.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.output.HtmlRender;
import com.deepoove.swagger.diff.output.MarkdownRender;
import com.deepoove.swagger.diff.output.Render;

/**
 * $java -jar swagger-diff.jar -old http://www.petstore.com/swagger.json \n
 *  -new http://www.petstore.com/swagger_new.json \n
 *  -v 2.0 \n
 *  -output-mode markdown \n
 *  
 * @author Sayi
 * @version 
 */
public class CLI {
    
    private static final String OUTPUT_MODE_MARKDOWN = "markdown";
    
    @Parameter(names = "-old", description = "old api-doc location:Json file path or Http url", required = true, order = 0)
    private String oldSpec;
    
    @Parameter(names = "-new", description = "new api-doc location:Json file path or Http url", required = true, order = 1)
    private String newSpec;
    
    @Parameter(names = "-v", description = "swagger version:1.0 or 2.0", validateWith=  RegexValidator.class, order = 2)
    @Regex("(2\\.0|1\\.0)")
    private String version = SwaggerDiff.SWAGGER_VERSION_V2;
    
    @Parameter(names = "-output-mode", description = "render mode: markdown or html", validateWith=  RegexValidator.class, order = 3)
    @Regex("(markdown|html)")
    private String outputMode = OUTPUT_MODE_MARKDOWN;
    
    @Parameter(names = "-output-file", description = "output file: path of the file to write the reults", order = 4)
    private String outputFile;
    
    @Parameter(names = "--help", help = true, order = 5)
    private boolean help;
    
    @Parameter(names = "--version", description = "swagger-diff tool version", help = true, order = 6)
    private boolean v;
    
    public static void main(String[] args) {
        CLI cli = new CLI();
        JCommander jCommander = JCommander.newBuilder()
            .addObject(cli)
            .build();
        jCommander.parse(args);
        cli.run(jCommander);
    }

    public void run(JCommander jCommander) {
        if (help){
            jCommander.setProgramName("java -jar swagger-diff.jar");
            jCommander.usage();
            return;
        }
        if (v){
            JCommander.getConsole().println("1.3.0");
            return;
        }
        
        SwaggerDiff diff = SwaggerDiff.SWAGGER_VERSION_V2.equals(version)
                ? SwaggerDiff.compareV2(oldSpec, newSpec) : SwaggerDiff.compareV1(oldSpec, newSpec);
        
        output(diff);
    }

    private void output(SwaggerDiff diff) {
    	if(outputFile == null || outputFile.isEmpty()) {
    		String render = getRender(outputMode).render(diff);
            JCommander.getConsole().println(render);
        } else {
        	outputInFile(diff);
        }
    }
    
    private void outputInFile(SwaggerDiff diff) {
    	// Open the file
    	File file = new File(outputFile);
    	if(file.isDirectory()) {
    		JCommander.getConsole().println("Path is an existing directory");
    		return;
    	}
    	FileWriter writer = null;
    	try {
			file.createNewFile();
			writer = new FileWriter(file);
			getRender(outputMode).render(diff, writer);
		} catch (IOException e) {
			JCommander.getConsole().println("Cannot write into file, error: " + e.getMessage());
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) { }
			}
		}
    }
    
    private Render getRender(String outputMode) {
        if (OUTPUT_MODE_MARKDOWN.equals(outputMode)) return new MarkdownRender();
        return new HtmlRender("Changelog", "http://deepoove.com/swagger-diff/stylesheets/demo.css");
    }

    public String getOldSpec() {
        return oldSpec;
    }

    public String getNewSpec() {
        return newSpec;
    }

    public String getVersion() {
        return version;
    }

    public String getOutputMode() {
        return outputMode;
    }


}
