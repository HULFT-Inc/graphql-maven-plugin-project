/**
 * 
 */
package com.graphql_java_generator.mavenplugin;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.graphql_java_generator.plugin.DocumentParser;
import com.graphql_java_generator.plugin.PluginExecutor;
import com.graphql_java_generator.plugin.conf.CommonConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;

/**
 * This class is the super class of all Mojos. It contains all parameters that are common to all goals, and the
 * {@link #execute()} method.<BR/>
 * This avoids to redeclare each common parameter in each Mojo, including its comment. When a comment is updated, only
 * one update is necessary, instead of updating it in each Mojo.
 * 
 * @author etienne-sf
 */
public abstract class AbstractCommonMojo extends AbstractMojo implements CommonConfiguration {

	/**
	 * <P>
	 * True if the plugin is configured to add the Relay connection capabilities to the field marked by the
	 * <I>&#064;RelayConnection</I> directive.
	 * </P>
	 * <P>
	 * If so, the plugin reads the provided GraphQL schema file(s), and enriches them with the interfaces and types
	 * needed to respect the Relay Connection specification. The entry point for that is the
	 * <I>&#064;RelayConnection</I> directive.
	 * </P>
	 * <P>
	 * You'll find all the information on the plugin web site. Please check the <A
	 * HREF="https://graphql-maven-plugin-project.graphql-java-generator.com/client_add_relay_connection.html>client
	 * Relay capability page</A> or the <A
	 * HREF="https://graphql-maven-plugin-project.graphql-java-generator.com/server_add_relay_connection.html>server
	 * Relay capability page</A>.
	 * </P>
	 */
	@Parameter(property = "com.graphql_java_generator.mavenplugin.addRelayConnections", defaultValue = CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS)
	boolean addRelayConnections;

	/**
	 * Not available to the user: the {@link MavenProject} in which the plugin executes
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	MavenProject project;

	/** The folder where the graphql schema file(s) will be searched. The default schema is the main resource folder. */
	@Parameter(property = "com.graphql_java_generator.mavenplugin.schemaFileFolder", defaultValue = GraphQLConfiguration.DEFAULT_SCHEMA_FILE_FOLDER)
	File schemaFileFolder;

	/**
	 * <P>
	 * The pattern to find the graphql schema file(s). The default value is "/*.graphqls" meaning that the maven plugin
	 * will search all graphqls files in the "/src/main/resources" folder (please check also the <I>schemaFileFolder</I>
	 * plugin parameter).
	 * </P>
	 * <P>
	 * You can put the star (*) joker in the filename, to retrieve several files at ones, for instance
	 * <I>/myschema*.graphqls</I> will retrieve the <I>/src/main/resources/myschema.graphqls</I> and
	 * <I>/src/main/resources/myschema_extend.graphqls</I> files.
	 * <P>
	 */
	@Parameter(property = "com.graphql_java_generator.mavenplugin.schemaFilePattern", defaultValue = GraphQLConfiguration.DEFAULT_SCHEMA_FILE_PATTERN)
	String schemaFilePattern;

	/**
	 * <P>
	 * (this parameter is in beta version) If true, and the generated sources or resources are older than the GraphQL
	 * schema file(s), then there is no source or resource generation.
	 * </P>
	 * <P>
	 * For instance, the <I>generateGraphQLSchema</I> goal won't (re)generate the target schema, and the
	 * <I>generateClientCode</I> won't generate the sources.
	 * </P>
	 * <P>
	 * Please note that if your pom adds the generated source folder with the <I>build-helper-maven-plugin</I>, it seems
	 * that the compiler will always compile the sources, even if they didn't change. If you still want to use this
	 * <I>build-helper-maven-plugin</I>, you'll have to put it into a dedicated profile, so that you can activte it or
	 * not as you want. You can have a look at the
	 * <A HREF="https://github.com/graphql-java-generator/graphql-maven-plugin-project/issues/69">Issue 69</I> for a
	 * hint on this.
	 * <P>
	 * Of course, after a <I>clean</I> goal/taks execution, the target folder won't exist, and the sources or resources
	 * will be created again during the next build.
	 * </P>
	 * <P>
	 * As of 1.x version of the plugin, the default value is <I>false</I>, so that only people aware of it try it.
	 * Starting from 2.x version, its default value will be <I>true</I>.
	 * </P>
	 */
	@Parameter(property = "com.graphql_java_generator.mavenplugin.skipGenerationIfSchemaHasNotChanged", defaultValue = GraphQLConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED)
	boolean skipGenerationIfSchemaHasNotChanged;

	/**
	 * <P>
	 * Map of the code templates to be used: this allows to override the default templates, and control exactly what
	 * code is generated by the plugin.
	 * </P>
	 * <P>
	 * You can override any of the Velocity templates of the project. The list of templates is defined in the enum
	 * CodeTemplate, that you can <A HREF=
	 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-logic/src/main/java/com/graphql_java_generator/plugin/CodeTemplate.java">check
	 * here</A>.
	 * </P>
	 * <P>
	 * You can find a sample in the <A HREF=
	 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-CustomTemplates-client/pom.xml">CustomTemplates
	 * client sample</A>.
	 * </P>
	 * <P>
	 * <B>Important notice:</B> Please note that the default templates may change in the future. And some of these
	 * modifications would need to be reported into the custom templates. We'll try to better expose a stable public API
	 * in the future.
	 * </P>
	 */
	@Parameter(property = "com.graphql_java_generator.mavenplugin.templates")
	Map<String, String> templates;

	/**
	 * This class contains the Sprign configuration for the actual instance of this Mojo. It's set by subclasses,
	 * through the constructor
	 */
	protected final Class<?> springConfigurationClass;

	/** The Spring context used for the plugin execution. It contains all the beans that runs for its execution */
	protected AnnotationConfigApplicationContext ctx;

	/** The {@link DocumentParser} that contains all the data for the parsed GraphQL schema(s) */
	protected PluginExecutor executor;

	@Override
	public File getProjectDir() {
		return project.getBasedir();
	}

	@Override
	public File getSchemaFileFolder() {
		return schemaFileFolder;
	}

	@Override
	public String getSchemaFilePattern() {
		return schemaFilePattern;
	}

	@Override
	public Map<String, String> getTemplates() {
		return templates;
	}

	@Override
	public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return skipGenerationIfSchemaHasNotChanged;
	}

	AbstractCommonMojo(Class<?> springConfigurationClass) {
		this.springConfigurationClass = springConfigurationClass;
	}

	/** {@inheritDoc} */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			LoggerFactory.getLogger(getClass()).debug("Starting generation of java classes from graphqls files");

			// We'll use Spring IoC
			ctx = new AnnotationConfigApplicationContext();
			ctx.getBeanFactory().registerSingleton("mojo", this);
			ctx.register(springConfigurationClass);
			ctx.refresh();

			// Let's log the current configuration (this will do something only when in
			// debug mode)
			ctx.getBean(CommonConfiguration.class).logConfiguration();

			executor = ctx.getBean(PluginExecutor.class);
			executor.execute();

			executeSpecificJob();

			ctx.close();

		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	/**
	 * This class must be overriden by the concrete subclasses. It contains the specific work to execute for this Mojo's
	 * goal
	 * 
	 * @throws Exception
	 */
	protected abstract void executeSpecificJob() throws Exception;

}
