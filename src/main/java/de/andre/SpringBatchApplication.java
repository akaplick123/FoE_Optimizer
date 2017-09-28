package de.andre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.andre.process.DeepSearchPlacement;
import de.andre.process.EndlessConstruction;
import de.andre.process.RandomDeepSearchPlacement;
import de.andre.process.RandomDeepSearchPlacement2;

@SpringBootApplication
public class SpringBatchApplication implements CommandLineRunner {
    @Value("${exp.clazz}")
    private String experimentName;

    @Autowired
    private EndlessConstruction endlessConstruction;
    @Autowired
    private RandomDeepSearchPlacement randomDeepSearchPlacement;
    @Autowired
    private RandomDeepSearchPlacement2 randomDeepSearchPlacement2;
    @Autowired
    private DeepSearchPlacement deepSearchPlacement;

    @Override
    public void run(String... args) throws Exception {
	switch (experimentName) {
	case "EndlessConstruction":
	    new Thread() {
		public void run() {
		    endlessConstruction.runDBExperiment();
		};
	    }.start();
	    break;
	case "RandomDeepSearchPlacement":
	    new Thread() {
		public void run() {
		    randomDeepSearchPlacement.runDBExperiment();
		};
	    }.start();
	    break;
	case "RandomDeepSearchPlacement2":
	    new Thread() {
		public void run() {
		    randomDeepSearchPlacement2.runDBExperiment();
		};
	    }.start();
	    break;
	case "DeepSearchPlacement":
	    new Thread() {
		public void run() {
		    deepSearchPlacement.runDBExperiment();
		};
	    }.start();
	    break;
	default:
	    System.err.println("Unknown experiment '" + experimentName + "'.");
	    System.exit(1);
	    break;
	}
    }

    public static void main(String[] args) throws Exception {
	SpringApplication app = new SpringApplication(SpringBatchApplication.class);
	app.run(args);
    }
}
