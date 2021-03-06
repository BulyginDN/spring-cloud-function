/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.function.rsocket;

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;


/**
 *
 * @author Oleg Zhurakousky
 * @since 3.1
 */
public class RSocketAutoConfigurationTests {
	@Test
	public void testImperativeFunctionAsRequestReply() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		new SpringApplicationBuilder(SampleFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=uppercase",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + port);

		RSocket socket = RSocketConnectionUtils.createClientSocket(InetSocketAddress.createUnresolved("localhost", port), null);
		Mono<String> result = socket.requestResponse(DefaultPayload.create("\"hello\"")).map(Payload::getDataUtf8);

		StepVerifier
			.create(result)
			.expectNext("\"HELLO\"")
			.expectComplete()
			.verify();
	}

	@Test
	public void testImperativeFunctionAsRequestStream() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		new SpringApplicationBuilder(SampleFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=uppercase",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + port);

		RSocket socket = RSocketConnectionUtils.createClientSocket(InetSocketAddress.createUnresolved("localhost", port), null);
		Flux<String> result = socket.requestStream(DefaultPayload.create("\"hello\"")).map(Payload::getDataUtf8);

		StepVerifier
			.create(result)
			.expectNext("\"HELLO\"")
			.expectComplete()
			.verify();
	}

	@Test
	public void testImperativeFunctionAsRequestChannel() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		new SpringApplicationBuilder(SampleFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=uppercase",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + port);

		RSocket socket = RSocketConnectionUtils.createClientSocket(InetSocketAddress.createUnresolved("localhost", port), null);
		Flux<String> result = socket.requestChannel(Flux.just(
				DefaultPayload.create("\"Ricky\""),
				DefaultPayload.create("\"Julien\""),
				DefaultPayload.create("\"Bubbles\""))
		)
		.map(Payload::getDataUtf8);

		StepVerifier.create(result)
			.expectNext("\"RICKY\"")
			.expectNext("\"JULIEN\"")
			.expectNext("\"BUBBLES\"")
			.expectComplete()
			.verify();
	}

	@Test
	public void testReactiveFunctionAsRequestReply() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		new SpringApplicationBuilder(SampleFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=uppercaseReactive",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + port);

		RSocket socket = RSocketConnectionUtils.createClientSocket(InetSocketAddress.createUnresolved("localhost", port), null);

		Mono<String> result = socket.requestResponse(DefaultPayload.create("\"hello\"")).map(Payload::getDataUtf8);

		StepVerifier
			.create(result)
			.expectNext("\"HELLO\"")
			.expectComplete()
			.verify();
	}

	@Test
	public void testReactiveFunctionAsRequestStream() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		new SpringApplicationBuilder(SampleFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=uppercaseReactive",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + port);

		RSocket socket = RSocketConnectionUtils.createClientSocket(InetSocketAddress.createUnresolved("localhost", port), null);

		Flux<String> result = socket.requestStream(DefaultPayload.create("\"hello\"")).map(Payload::getDataUtf8);

		StepVerifier
			.create(result)
			.expectNext("\"HELLO\"")
			.expectComplete()
			.verify();
	}

	@Test
	public void testReactiveFunctionAsRequestChannel() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		new SpringApplicationBuilder(SampleFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=uppercaseReactive",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + port);

		RSocket socket = RSocketConnectionUtils.createClientSocket(InetSocketAddress.createUnresolved("localhost", port), null);

		Flux<String> result = socket.requestChannel(Flux.just(
				DefaultPayload.create("\"Ricky\""),
				DefaultPayload.create("\"Julien\""),
				DefaultPayload.create("\"Bubbles\""))
		)
		.map(Payload::getDataUtf8);

		StepVerifier
			.create(result)
			.expectNext("\"RICKY\"")
			.expectNext("\"JULIEN\"")
			.expectNext("\"BUBBLES\"")
			.expectComplete()
			.verify();
	}

	@Test
	public void testRequestReplyFunctionWithComposition() throws Exception {
		int portA = SocketUtils.findAvailableTcpPort();
		int portB = SocketUtils.findAvailableTcpPort();
		new SpringApplicationBuilder(SampleFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=uppercase|concat",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + portA);

		new SpringApplicationBuilder(AdditionalFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=reverse>localhost:" + portA + "|wrap",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + portB);

		RSocket socket = RSocketConnectionUtils.createClientSocket(InetSocketAddress.createUnresolved("localhost", portB), null);
		Mono<String> result = socket.requestResponse(DefaultPayload.create("\"hello\"")).map(Payload::getDataUtf8);
		StepVerifier
			.create(result)
			.expectNext("\"(OLLEHOLLEH)\"")
			.expectComplete()
			.verify();
	}

	@Test
	public void testRequestChannelFunction() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		new SpringApplicationBuilder(SampleFunctionConfiguration.class).web(WebApplicationType.NONE).run(
				"--logging.level.org.springframework.cloud.function=DEBUG",
				"--spring.cloud.function.definition=uppercaseReactive",
				"--spring.cloud.function.rsocket.bind-address=localhost",
				"--spring.cloud.function.rsocket.bind-port=" + port);

		RSocket socket = RSocketConnectionUtils.createClientSocket(InetSocketAddress.createUnresolved("localhost", port), null);

		Flux<String> result = socket.requestChannel(Flux.just(
				DefaultPayload.create("\"Ricky\""),
				DefaultPayload.create("\"Julien\""),
				DefaultPayload.create("\"Bubbles\""))
		)
		.map(Payload::getDataUtf8);

		StepVerifier
			.create(result)
			.expectNext("\"RICKY\"")
			.expectNext("\"JULIEN\"")
			.expectNext("\"BUBBLES\"")
			.expectComplete()
			.verify();
	}



//	@Test
//	public void testFireAndForgetConsumer() throws Exception {
//		new SpringApplicationBuilder(SampleFunctionConfiguration.class)
//				.run("--logging.level.org.springframework.cloud.function=DEBUG",
//					 "--spring.cloud.function.definition=log");
//
//		RSocket socket = RSocketConnector.connectWith(TcpClientTransport.create("localhost", 7000))
//				.log()
//				.retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
//				.block();
//		socket.fireAndForget(DefaultPayload.create("Hello"))
//			.log()
//			.onErrorContinue((e, x) -> {
//				System.out.println(e);
//			})
//			.block();
//		Thread.sleep(2000);
//		System.out.println();
//	}

	@EnableAutoConfiguration
	@Configuration
	public static class SampleFunctionConfiguration {
		@Bean
		public Function<String, String> uppercase() {
			return v -> {
				return v.toUpperCase();
			};
		}

		@Bean
		public Function<String, String> concat() {
			return v -> {
				return v + v;
			};
		}

		@Bean
		public Function<String, String> echo() {
			return v -> v;
		}

		@Bean
		public Function<Flux<String>, Flux<String>> uppercaseReactive() {
			return flux -> flux.map(v -> {
				System.out.println("Uppercasing: " + v);
				return v.toUpperCase();
			});
		}

		@Bean
		public Consumer<byte[]> log() {
			return v -> {
				System.out.println("==> In Consumer: " + new String(v));
			};
		}
	}

	@EnableAutoConfiguration
	@Configuration
	public static class AdditionalFunctionConfiguration {
		@Bean
		public Function<String, String> reverse() {
			return v -> {
				return new StringBuilder(v).reverse().toString();
			};
		}

		@Bean
		public Function<String, String> wrap() {
			return v -> {
				return "(" + v + ")";
			};
		}
	}
}
