package com.cecilireid.springchallenges;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Transactional
@DirtiesContext
class SpringChallengesApplicationTests {

	@LocalServerPort
	private int port;

	private WebTestClient testClient;

	@BeforeEach
	void setUp() {
		testClient = WebTestClient
				.bindToServer()
				.baseUrl(String.format("http://localhost:%s/cateringJobs", port))
				.build();
	}

	@Test
	void findByStatus_canceled_returnsAllCanceledCateringJobs() {
		testClient
				.get()
				.uri(uriBuilder -> uriBuilder
						.path("/findByStatus")
						.queryParam("status", "CANCELED")
						.build()
				)
				.accept(MediaType.APPLICATION_JSON)
			.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody().jsonPath("$.size()").isEqualTo(1);
	}

	@Test
	void findByStatus_notStarted_returnsAllNotStartedCateringJobs() {
		testClient
				.get()
				.uri(uriBuilder -> uriBuilder
						.path("/findByStatus")
						.queryParam("status", "NOT_STARTED")
						.build()
				)
				.accept(MediaType.APPLICATION_JSON)
			.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody().jsonPath("$.size()").isEqualTo(1);
	}

	@Rollback
	@Test
	void createCateringJob_newJob_returnCreatedJobWithId() {
		final CateringJob cateringJob = newCateringJob();

		testClient
				.post()
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(cateringJob), CateringJob.class)
			.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody()
					.jsonPath("id").isNotEmpty()
					.jsonPath("customerName").isEqualTo(cateringJob.getCustomerName())
					.jsonPath("email").isEqualTo(cateringJob.getEmail())
					.jsonPath("menu").isEqualTo(cateringJob.getMenu())
					.jsonPath("noOfGuests").isEqualTo(cateringJob.getNoOfGuests())
					.jsonPath("phoneNumber").isEqualTo(cateringJob.getPhoneNumber());
	}

	@Rollback
	@Test
	void updateCateringJob_jobExists_returnUpdatedJob() {
		final CateringJob cateringJob = createAndGetCateringJob();
		final int expectedNoOfGuests = cateringJob.getNoOfGuests() + 1;

		cateringJob.setNoOfGuests(expectedNoOfGuests);

		testClient
				.put()
				.uri(uriBuilder -> uriBuilder
						.path("/" + cateringJob.getId())
						.build()
				)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(cateringJob), CateringJob.class)
			.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody().jsonPath("noOfGuests").isEqualTo(expectedNoOfGuests);
	}

	@Test
	void updateCateringJob_jobDoesNotExist_returnNotFoundClientException() {
		final CateringJob cateringJob = newCateringJob();

		testClient
				.put()
				.uri(uriBuilder -> uriBuilder
						.path("/-100")
						.build()
				)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(cateringJob), CateringJob.class)
			.exchange()
				.expectStatus().isNotFound()
				.expectBody(String.class).isEqualTo("Not found: Please try again");
	}

	@Rollback
	@Test
	void patchCateringJob_updateMenu_returnUpdatedJob() {
		final CateringJob cateringJob = createAndGetCateringJob();
		final String updatedMenu = cateringJob.getMenu() + " and ketchup";

		testClient
				.patch()
				.uri(uriBuilder -> uriBuilder
						.path("/" + cateringJob.getId())
						.build()
				)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just("{ \"menu\": \"" + updatedMenu + "\" }"), String.class)
			.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody().jsonPath("menu").isEqualTo(updatedMenu);
	}

	@Rollback
	@Test
	void patchCateringJob_missingMenu_returnBadRequestClientException() {
		final CateringJob cateringJob = createAndGetCateringJob();

		testClient
				.patch()
				.uri(uriBuilder -> uriBuilder
						.path("/" + cateringJob.getId())
						.build()
				)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just("{ \"foo\": \"bar\" }"), String.class)
			.exchange()
				.expectStatus().is4xxClientError()
				.expectBody(String.class).isEqualTo("Not found: Please try again");
	}

	@Test
	void patchCateringJob_jobDoesNotExist_returnNotFoundClientException() {
		testClient
				.patch()
				.uri(uriBuilder -> uriBuilder
						.path("/-100")
						.build()
				)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just("{ \"menu\": \"Eggs, beans and chips\" }"), String.class)
			.exchange()
				.expectStatus().is4xxClientError()
				.expectBody(String.class).isEqualTo("Not found: Please try again");
	}

	private CateringJob newCateringJob() {
		CateringJob cateringJob = new CateringJob();
		cateringJob.setCustomerName("John Doe");
		cateringJob.setEmail("johndoe@noreply.com");
		cateringJob.setMenu("Hot dog and fries");
		cateringJob.setNoOfGuests(1);
		cateringJob.setPhoneNumber("0790000001");

		return cateringJob;
	}

	private CateringJob createAndGetCateringJob() {
		final CateringJob cateringJob = newCateringJob();

		return testClient
				.post()
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(cateringJob), CateringJob.class)
			.exchange()
				.returnResult(CateringJob.class)
				.getResponseBody()
					.blockFirst();
	}
}
