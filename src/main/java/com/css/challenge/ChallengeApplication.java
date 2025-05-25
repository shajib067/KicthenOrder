package com.css.challenge;

import com.css.challenge.client.Main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.CommandLineRunner;
import picocli.CommandLine;

@SpringBootApplication
public class ChallengeApplication implements CommandLineRunner {

    private final ApplicationContext context;

    public ChallengeApplication(ApplicationContext context) {
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication.run(ChallengeApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Main main = context.getBean(Main.class);
        CommandLine cmd = new CommandLine(main);
        cmd.setUnmatchedOptionsArePositionalParams(true);
        cmd.setUnmatchedArgumentsAllowed(true);
        cmd.execute(args);
    }
}
