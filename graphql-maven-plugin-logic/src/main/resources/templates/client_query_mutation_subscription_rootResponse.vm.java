/** Generated by the default template from graphql-java-generator */
package ${packageUtilName};

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.graphql_java_generator.annotation.GraphQLNonScalar;
import com.graphql_java_generator.client.request.ObjectResponse;
import com.graphql_java_generator.client.response.Error;

#if(${configuration.separateUtilityClasses})
import ${configuration.packageName}.${object.classSimpleName};
#end

public class ${object.classSimpleName}RootResponse {

	@JsonProperty("data")
	@GraphQLNonScalar(fieldName = "${object.name}", graphQLTypeSimpleName = "${object.javaName}", javaClass = ${object.classSimpleName}.class)
	${object.classSimpleName} ${object.requestType};

	@JsonProperty("errors")
	@JsonDeserialize(contentAs = Error.class)
	public List<Error> errors;

	@JsonProperty("extensions")
	public JsonNode extensions;

	// This getter is needed for the Json serialization
	public ${object.classSimpleName} getData() {
		return this.${object.requestType};
	}

	// This setter is needed for the Json deserialization
	public void setData(${object.classSimpleName} ${object.requestType}) {
		this.${object.requestType} = ${object.requestType};
	}

	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

	public JsonNode getExtensions() {
		return extensions;
	}

	public void setExtensions(JsonNode extensions) {
		this.extensions = extensions;
	}

}
