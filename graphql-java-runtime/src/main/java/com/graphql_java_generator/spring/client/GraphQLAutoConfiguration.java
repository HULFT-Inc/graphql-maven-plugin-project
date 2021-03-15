/**
 * 
 */
package com.graphql_java_generator.spring.client;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import com.graphql_java_generator.client.OAuthTokenExtractor;
import com.graphql_java_generator.client.QueryExecutor;
import com.graphql_java_generator.client.QueryExecutorSpringReactiveImpl;

import reactor.netty.http.client.HttpClient;

/**
 * This classes allows to autoconfigure Spring, with a full default behavior, ready to use. This can be overridden by
 * Spring {@link Bean} or {@link Component}, in the application configuration.<BR/>
 * <B>Important notice:</B> This class must not be the target of component scanning. See <a href=
 * "https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-developing-auto-configuration">https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-developing-auto-configuration</a>
 * for more information
 * 
 * @author etienne-sf
 */
@Configuration
public class GraphQLAutoConfiguration {

	@Value(value = "${graphql.endpoint.url}")
	private String graphqlEndpointUrl;

	@Value("${graphql.endpoint.subscriptionUrl:${graphql.endpoint.url}}")
	@Deprecated
	private String graphqlEndpointSubscriptionUrl;

	/**
	 * This beans defines the GraphQL endpoint, as a {@link String}
	 * 
	 * 
	 * @return Returns the value of the <I>graphql.endpoint.url</I> application property.
	 * @see https://docs.spring.io/spring-boot/docs/2.3.3.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config
	 */
	@Bean
	String graphqlEndpoint() {
		return graphqlEndpointUrl;
	}

	/**
	 * This beans defines the GraphQL endpoint for subscriptions, as a {@link String}. If null, then the
	 * {@link #graphqlEndpoint()} url is used, which is the default.<BR/>
	 * If the subscription is exposed on a different url, then this bean can be used. This is the case for Java, which
	 * has a limitation which prevents to expose web socket (needed for subscription) on the same path that is
	 * accessible with GET or POST.
	 * 
	 * @return Returns the value of the <I>graphql.endpoint.subscriptionUrl</I> application property.
	 * @see https://docs.spring.io/spring-boot/docs/2.3.3.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config
	 */
	@Bean
	@Deprecated
	String graphqlSubscriptionEndpoint() {
		return graphqlEndpointSubscriptionUrl;
	}

	/**
	 * This bean provides a default implementation of the {@link QueryExecutor}. It will be used to execute
	 * query/mutation/subscription against the GraphQL server.<BR/>
	 * Applications can provides there own Spring bean of this type. In which case, this default implementation is not
	 * used. applications.
	 * 
	 * @param graphqlEndpoint
	 *            A <I>graphqlEndpoint</I> Spring bean, of type String, must be provided, with the URL of the GraphQL
	 *            endpoint, for instance <I>https://my.serveur.com/graphql</I>
	 * @param graphqlSubscriptionEndpoint
	 *            If the subscription is on a different endpoint than the main GraphQL endpoint, thant you can define a
	 *            <I>graphqlSubscriptionEndpoint</I> Spring bean, of type String, with this specific URL, for instance
	 *            <I>https://my.serveur.com/graphql/subscription</I>. For instance, Java servers suffer from a
	 *            limitation which prevent to server both GET/POST HTTP verbs and WebSockets on the same URL.<BR/>
	 *            If no bean <I>graphqlSubscriptionEndpoint</I> Spring bean is defined, then the <I>graphqlEndpoint</I>
	 *            URL is also used for subscriptions (which is the standard case).
	 * @param webClient
	 *            The Spring reactive {@link WebClient} that will execute the HTTP requests for GraphQL queries and
	 *            mutations.
	 * @param webSocketClient
	 *            The Spring reactive {@link WebSocketClient} web socket client, that will execute HTTP requests to
	 *            build the web sockets, for GraphQL subscriptions.<BR/>
	 *            This is mandatory if the application latter calls subscription. It may be null otherwise.
	 * @param serverOAuth2AuthorizedClientExchangeFilterFunction
	 *            The {@link ServerOAuth2AuthorizedClientExchangeFilterFunction} is responsible for getting OAuth token
	 *            from the OAuth authorization server. It is optional, and may be provided by the App's spring config.
	 *            If it is not provided, then there is no OAuth authentication on client side. If provided, then the
	 *            client uses it to provide the OAuth2 authorization token, when accessing the GraphQL resource server
	 *            for queries/mutations/subscriptions.
	 * @param oAuthTokenRetriever
	 *            This class is responsible for extracting the OAuth token, once the
	 *            {@link ServerOAuth2AuthorizedClientExchangeFilterFunction} has done its job, and added the OAuth2
	 *            token into the request, in the Authorization header. See the {@link OAuthTokenExtractor} doc for more
	 *            information.
	 */
	@Bean
	@ConditionalOnMissingBean
	QueryExecutor queryExecutor(//
			String graphqlEndpoint, //
			@Autowired(required = false) String graphqlSubscriptionEndpoint, //
			WebClient webClient, //
			@Autowired(required = false) WebSocketClient webSocketClient,
			@Autowired(required = false) ServerOAuth2AuthorizedClientExchangeFilterFunction serverOAuth2AuthorizedClientExchangeFilterFunction,
			@Autowired(required = false) OAuthTokenExtractor oAuthTokenRetriever) {
		return new QueryExecutorSpringReactiveImpl(graphqlEndpoint, graphqlSubscriptionEndpoint, webClient,
				webSocketClient, serverOAuth2AuthorizedClientExchangeFilterFunction, oAuthTokenRetriever);
	}

	/**
	 * The Spring reactive {@link WebClient} that will execute the HTTP requests for GraphQL queries and mutations.
	 */
	@Bean
	@ConditionalOnMissingBean
	public WebClient webClient(String graphqlEndpoint, @Autowired(required = false) HttpClient httpClient,
			@Autowired(required = false) ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter) {
		Builder webClientBuilder = WebClient.builder()//
				.baseUrl(graphqlEndpoint)//
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultUriVariables(Collections.singletonMap("url", graphqlEndpoint));

		if (httpClient != null) {
			webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
		}
		if (oauthFilter != null) {
			webClientBuilder.filter(oauthFilter);
		}

		return webClientBuilder.build();
	}

	/**
	 * The Spring reactive {@link WebSocketClient} web socket client, that will execute HTTP requests to build the web
	 * sockets, for GraphQL subscriptions.<BR/>
	 * This is mandatory if the application latter calls subscription. It may be null otherwise.
	 */
	@Bean
	@ConditionalOnMissingBean
	public WebSocketClient webSocketClient(@Autowired(required = false) HttpClient httpClient) {
		if (httpClient == null) {
			return new ReactorNettyWebSocketClient(HttpClient.create());
		} else {
			return new ReactorNettyWebSocketClient(httpClient);
		}
	}
}
