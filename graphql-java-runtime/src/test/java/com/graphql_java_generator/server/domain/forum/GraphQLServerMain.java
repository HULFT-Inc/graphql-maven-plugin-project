/** Generated by the default template from graphql-java-generator */
package com.graphql_java_generator.server.domain.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.graphql_java_generator.GraphqlUtils;

/**
 * @author generated by graphql-java-generator
 * @see <a href="https://github.com/graphql-java-generator/graphql-java-generator">https://github.com/graphql-java-generator/graphql-java-generator</a>
 */
@SpringBootApplication(scanBasePackages = { "org.forum.server.graphql", "com.graphql_java_generator" ,"org.forum.server.jpa","org.forum.server.specific_code" })
@EnableJpaRepositories(basePackages = { "org.forum.server.graphql", "com.graphql_java_generator" ,"org.forum.server.jpa","org.forum.server.specific_code" })
@EntityScan(basePackages = { "org.forum.server.graphql", "com.graphql_java_generator" ,"org.forum.server.jpa","org.forum.server.specific_code" })
@EnableConfigurationProperties
public class GraphQLServerMain {


	public static void main(String[] args) {
		SpringApplication.run(GraphQLServerMain.class, args);
	}

}
