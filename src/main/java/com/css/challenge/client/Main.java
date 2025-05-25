package com.css.challenge.client;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.css.challenge.harness.KitchenSimulator;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "challenge", showDefaultValues = true)
public class Main implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  static {
    System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT: %5$s %n");
  }

  @Option(names = "--endpoint", description = "Problem server endpoint")
  String endpoint = "https://api.cloudkitchens.com";

  @Option(names = "--auth", description = "Authentication token (required)")
  String auth = "4k8hr9shc9hs";

  @Option(names = "--name", description = "Problem name. Leave blank (optional)")
  String name = "";

  @Option(names = "--seed", description = "Problem seed (random if zero)")
  long seed = 0;

  @Option(names = "--rate", description = "Inverse order rate")
  Duration rate = Duration.ofMillis(500);

  @Option(names = "--min", description = "Minimum pickup time")
  Duration min = Duration.ofSeconds(4);

  @Option(names = "--max", description = "Maximum pickup time")
  Duration max = Duration.ofSeconds(8);

  @Autowired
  private ActionLogger log;
  
  @Override
  public void run() {
    KitchenManager kitchen = new KitchenManager(log);
    KitchenSimulator simulator = new KitchenSimulator(kitchen, rate, min, max);
    try {
      Client client = new Client(endpoint, auth);
      Problem problem = client.newProblem(name, seed);

      // ------ Simulation harness logic goes here using rate, min and max ----
      simulator.runSimulation(problem.getOrders());

      String result = client.solveProblem(problem.getTestId(), rate, min, max, log.getActions());
      LOGGER.info("Result: {}", result);

    } 
    catch (IOException | InterruptedException e) {
      LOGGER.error("Simulation failed: {}", e.getMessage());
    }
    finally {
    	simulator.killExecutor();
    }
  }
}
