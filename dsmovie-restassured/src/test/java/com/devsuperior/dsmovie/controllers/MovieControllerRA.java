package com.devsuperior.dsmovie.controllers;

import com.devsuperior.dsmovie.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class MovieControllerRA {

	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;

	private Long existingMovieId, nonExistingMovieId;
	private String movieTitle;

	private Map<String, Object> postMovieInstance;

	@BeforeEach
	public void setUp() throws JSONException {
		baseURI = "http://localhost:8080";

		clientUsername = "alex@gmail.com";
		clientPassword = "123456";
		adminUsername = "maria@gmail.com";
		adminPassword = "123456";

		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		invalidToken = adminToken + "xpto";

		movieTitle = "Vingadores";

		existingMovieId = 16L;
		nonExistingMovieId = 99L;

		postMovieInstance = new HashMap<>();
		postMovieInstance.put("title", "Test Movie");
		postMovieInstance.put("score", 0.0);
		postMovieInstance.put("count", 0);
		postMovieInstance.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");

	}


	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
		given()
				.get("/movies")
			.then()
				.statusCode(200);
	}
	
	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {
		given()
				.get("/movies?title={movieTitle}", movieTitle)
			.then()
			.statusCode(200)
				.body("content.id[0]", is(13))
				.body("content.title[0]", equalTo("Vingadores: Ultimato"))
				.body("content.score[0]", equalTo(0.0F))
				.body("content.count[0]", equalTo(0))
				.body("content.image[0]", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg"));
	}
	
	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		given()
				.get("/movies/{id}", existingMovieId)
			.then()
			.statusCode(200)
				.body("id", is(16))
				.body("title", equalTo("O Silêncio dos Inocentes"))
				.body("score", equalTo(0.0F))
				.body("count", equalTo(0))
				.body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/mfwq2nMBzArzQ7Y9RKE8SKeeTkg.jpg"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		given()
				.get("/movies/{id}", nonExistingMovieId)
			.then()
				.statusCode(404);
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {

		postMovieInstance.put("title", "    ");
		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + adminToken)
					.body(newMovie)
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
				.when()
					.post("/movies")
				.then()
				.statusCode(422);
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + clientToken)
					.body(newMovie)
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
				.when()
					.post("/movies")
				.then()
				.statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + invalidToken)
					.body(newMovie)
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
				.when()
					.post("/movies")
				.then()
				.statusCode(401);
	}
}
