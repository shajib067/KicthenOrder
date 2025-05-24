package com.css.challenge.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Client is a client for fetching and solving challenge test problems. */
public class Client {
  private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

  private final String endpoint;
  private final String auth;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public Client(String endpoint, String auth) {
    this.endpoint = endpoint;
    this.auth = auth;
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
  }

  /**
   * newProblem fetches a new test problem from the server. The URL also works in a browser for
   * convenience.
   */
  public Problem newProblem(String name, long seed) throws IOException {
    if (seed == 0) {
      seed = new Random().nextLong();
    }

    String url = UriComponentsBuilder.fromHttpUrl(endpoint + "/interview/challenge/new")
        .queryParam("auth", auth)
        .queryParam("name", name)
        .queryParam("seed", seed)
        .toUriString();

    HttpHeaders headers = new HttpHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new IOException(url + ": " + response.getStatusCode());
    }

    String id = response.getHeaders().getFirst("x-test-id");
    if (id == null) {
      throw new IOException(url + ": Missing x-test-id header");
    }

    LOGGER.info("Fetched new test problem, id={}: {}", id, url);
    return new Problem(id, Order.parse(response.getBody()));
  }

  private static class Options {
    public long rate;
    public long min;
    public long max;

    Options(Duration rate, Duration min, Duration max) {
      this.rate = TimeUnit.MILLISECONDS.toMicros(rate.toMillis());
      this.min = TimeUnit.MILLISECONDS.toMicros(min.toMillis());
      this.max = TimeUnit.MILLISECONDS.toMicros(max.toMillis());
    }
  }

  private static class Solution {
    public Options options;
    public List<Action> actions;

    Solution(Options options, List<Action> actions) {
      this.options = options;
      this.actions = actions;
    }
  }

  /**
   * solveProblem submits a sequence of actions and parameters as a solution to a test problem.
   * Returns test result.
   */
  public String solveProblem(
      String testId, Duration rate, Duration min, Duration max, List<Action> actions)
      throws IOException {

    Solution solution = new Solution(new Options(rate, min, max), actions);
    String requestBody = objectMapper.writeValueAsString(solution);

    String url = UriComponentsBuilder.fromHttpUrl(endpoint + "/interview/challenge/solve")
        .queryParam("auth", auth)
        .toUriString();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("x-test-id", testId);

    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new IOException(url + ": " + response.getStatusCode());
    }

    return response.getBody();
  }
}
