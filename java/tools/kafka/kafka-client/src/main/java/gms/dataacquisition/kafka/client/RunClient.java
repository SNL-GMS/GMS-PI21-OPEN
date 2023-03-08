package gms.dataacquisition.kafka.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunClient {

  private static final Logger logger = LoggerFactory.getLogger(RunClient.class);

  public static void main(String[] args) {
    // defaults
    var totalNumberOfMessagesToConsume = 0;
    var topicName = "soh.extract"; // may need to make this a list
    var parser = new DefaultParser();
    var options = new Options();
    options.addOption("h", "help", false, "print help dialog");
    options.addOption("m", "messages", true, "total number of messages to consume");
    options.addOption("t", "topic", true, "topic from which to consume");

    try {
      CommandLine commandLine = parser.parse(options, args);
      if (commandLine.hasOption("h")) {
        logger.info("-h| --help         print help dialog");
        logger.info("-m| --messages     total number of messages to consume");
        logger.info("-t| --topic        topic from which to consume");
        System.exit(0);
      }
      if (commandLine.hasOption("m")) {
        totalNumberOfMessagesToConsume = Integer.parseInt(commandLine.getOptionValue("m"));
      }
      if (commandLine.hasOption("t")) {
        topicName = commandLine.getOptionValue("t");
      }
    } catch (ParseException e) {
      logger.error("Problem parsing arguments", e);
    }

    logger.info("Starting client, consuming topic {}", topicName);

    var kafkaConsumer = new Consumer(topicName, totalNumberOfMessagesToConsume);
    kafkaConsumer.run();
  }

}
