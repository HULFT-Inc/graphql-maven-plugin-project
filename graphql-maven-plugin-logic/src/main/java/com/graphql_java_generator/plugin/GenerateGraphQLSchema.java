/**
 * 
 */
package com.graphql_java_generator.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.plugin.conf.CommonConfiguration;
import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;
import com.graphql_java_generator.util.GraphqlUtils;

/**
 * This class merges one or more given GraphQL schema files into a new GraphQL schema, that is written in the given
 * target schema file. If {@link CommonConfiguration#isAddRelayConnections()} is true, then the generated schema is
 * updated to be conform to the <A HREF="https://relay.dev/graphql/connections.htm">relay connection
 * specification</A>.<BR/>
 * The job is done by using this class as a Spring bean, and calling its {@link #generateGraphQLSchema()} method.
 * 
 * @author etienne-sf
 *
 */
@Component
public class GenerateGraphQLSchema {

	DocumentParser documentParser;

	GraphqlUtils graphqlUtils;

	/**
	 * This instance is responsible for providing all the configuration parameter from the project (Maven, Gradle...)
	 */
	GenerateGraphQLSchemaConfiguration configuration;

	/** The Velocity engine used to generate the target file */
	VelocityEngine velocityEngine = null;

	/**
	 * The constructor that Spring will use to load this Spring bean
	 * 
	 * @param documentParser
	 *            The document parser, that loads the GraphQL schema into memory, and prepares the data for the schema
	 *            generation.
	 * @param graphqlUtils
	 *            A runtime utility class
	 * @param configuration
	 *            The configuration for the <I>generateGraphQLSchema</I> task/goal
	 */
	@Autowired
	public GenerateGraphQLSchema(GenerateGraphQLSchemaDocumentParser documentParser, GraphqlUtils graphqlUtils,
			GenerateGraphQLSchemaConfiguration configuration) {
		this.documentParser = documentParser;
		this.graphqlUtils = graphqlUtils;
		this.configuration = configuration;
	}

	/**
	 * A constructor that can be called by other tasks/goals. For instance, the {@link GenerateCodeGenerator} class
	 * creates an instance of this class, when in server mode and addRelayConnections is true, to generate the GraphQL
	 * schema, as it is necessary for the graphql-java at runtime.
	 * 
	 * @param documentParser
	 * @param graphqlUtils
	 * @param configuration
	 */
	public GenerateGraphQLSchema(DocumentParser documentParser, GraphqlUtils graphqlUtils,
			GenerateGraphQLSchemaConfiguration configuration) {
		this.documentParser = documentParser;
		this.graphqlUtils = graphqlUtils;
		this.configuration = configuration;
	}

	/** This method is the entry point, for the generation of the schema that merges the GraphQL source schema files */
	public void generateGraphQLSchema() {

		String msg = null;
		try {
			File targetFile = new File(configuration.getTargetFolder(), configuration.getTargetSchemaFileName());
			msg = "Generating relay schema in this file: " + targetFile.getAbsolutePath();
			configuration.getPluginLogger().debug(msg);

			VelocityContext context = new VelocityContext();
			context.put("newline", "\n");
			context.put("space", " ");
			context.put("documentParser", documentParser);
			context.put("graphqlUtils", graphqlUtils);
			//
			context.put("customScalars", documentParser.customScalars);
			context.put("directives", documentParser.directives);
			context.put("enumTypes", documentParser.enumTypes);
			context.put("interfaceTypes", documentParser.interfaceTypes);
			context.put("mutationType", documentParser.mutationType);
			context.put("objectTypes", documentParser.objectTypes);
			context.put("queryType", documentParser.queryType);
			context.put("subscriptionType", documentParser.subscriptionType);
			context.put("unionTypes", documentParser.unionTypes);
			Template template = getVelocityEngine().getTemplate(resolveTemplate(CodeTemplate.RELAY_SCHEMA), "UTF-8");

			targetFile.getParentFile().mkdirs();
			Writer writer = new FileWriterWithEncoding(targetFile,
					Charset.forName(configuration.getResourceEncoding()));
			template.merge(context, writer);
			writer.flush();
			writer.close();
		} catch (ResourceNotFoundException | ParseErrorException | TemplateInitException | MethodInvocationException
				| IOException e) {
			throw new RuntimeException("Error when " + msg, e);
		}
	}

	/**
	 * Resolves the template for the given key
	 * 
	 * @param templateKey
	 * @param defaultValue
	 * @return
	 */
	protected String resolveTemplate(CodeTemplate template) {
		if (configuration.getTemplates().containsKey(template.name())) {
			return configuration.getTemplates().get(template.name());
		} else {
			return template.getDefaultValue();
		}
	}

	private VelocityEngine getVelocityEngine() {
		if (velocityEngine == null) {
			// Initialization for Velocity
			velocityEngine = new VelocityEngine();
			velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			velocityEngine.init();
		}
		return velocityEngine;
	}
}
