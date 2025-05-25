package com.css.challenge.client;

import java.io.IOException;
import java.time.Duration;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.css.challenge.client.shelf.ShelfManager;
import com.css.challenge.client.shelf.Temparature;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "challenge", showDefaultValues = true)
public class Main implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  static {
    org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
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

  @Override
  public void run() {
	ActionLogger log = new ActionLogger();
	ShelfManager shelfManager = new ShelfManager(log);
	shelfManager.addShelf(Temparature.HOT, 6);
	shelfManager.addShelf(Temparature.COLD, 6);
	shelfManager.addShelf(Temparature.ROOM, 12);
    KitchenManager kitchen = new KitchenManager(min.toNanos()/1000, max.toNanos()/1000, shelfManager, log);
    try {
      Client client = new Client(endpoint, auth);
      Problem problem = client.newProblem(name, seed);

      // ------ Simulation harness logic goes here using rate, min and max ----
      
      for (Order order : problem.getOrders()) {
        LOGGER.info("Received: {}", order);
        kitchen.placeOrder(order);
        Thread.sleep(rate.toMillis());
      }

      Thread.sleep(max.toMillis() + 1000);
      // ----------------------------------------------------------------------

      String result = client.solveProblem(problem.getTestId(), rate, min, max, log.getActions());
      LOGGER.info("Result: {}", result);

    } 
    catch (IOException | InterruptedException e) {
      LOGGER.error("Simulation failed: {}", e.getMessage());
    }
    finally {
    	kitchen.killExecutor();
    }
  }

  public static void main(String[] args) {
    new CommandLine(new Main()).execute(args);
  }
}
