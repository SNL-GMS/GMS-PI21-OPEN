package gms.dataacquisition.data.preloader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Command Line Options for the DataGenerationManager, managing command-line execution of the
 * manager with the following options:
 * <pre>
 *   {@literal
 *   --dataType=(ACEI_ANALOG|ACEI_BOOLEAN|ROLLUP|RSDF|STATION_SOH)
 *   --startTime=<start_time>
 *   --sampleDuration=<duration-of-sample>
 *   --duration=<duration>
 *   --stationGroups=<set-of-station-groups>
 *   [--receptionDelay=<reception_time_seed>]
 *   }
 * </pre>
 */
public class DatasetGeneratorOptions {

  public static final String ISO_DOUBLE = "ISO Double";
  private static final String ISO_DURATION = "ISO duration";

  private static final String DATA_TYPE_NAME = "dataType";
  private static final Option dataType = Option.builder()
    .longOpt(DATA_TYPE_NAME)
    .desc("Comma separated list of data Types to generate")
    .hasArg()
    .argName("ACEI_ANALOG|ACEI_BOOLEAN|ROLLUP|RSDF|STATION_SOH")
    .required()
    .build();

  private static final String START_TIME_NAME = "startTime";
  private static final Option startTime = Option.builder()
    .longOpt(START_TIME_NAME)
    .desc("Start time of seed data, Duration string from present")
    .hasArg()
    .argName("ISO instant")
    .required()
    .build();

  private static final String SAMPLE_DURATION_NAME = "sampleDuration";
  private static final Option sampleDuration = Option.builder()
    .longOpt(SAMPLE_DURATION_NAME)
    .desc("How long each sample should be")
    .hasArg()
    .argName(ISO_DURATION)
    .required()
    .build();

  private static final String DURATION_NAME = "duration";
  private static final Option duration = Option.builder()
    .longOpt(DURATION_NAME)
    .desc("Amount of time for which to generate data as a Duration string")
    .hasArg()
    .argName(ISO_DURATION)
    .required()
    .build();

  private static final String STATION_GROUPS_NAME = "stationGroups";
  private static final Option stationGroups = Option.builder()
    .longOpt(STATION_GROUPS_NAME)
    .desc("Comma-separated station groups for which to generate data")
    .hasArgs()
    .argName("groups")
    .required()
    .build();

  private static final String RECEPTION_DELAY_NAME = "receptionDelay";
  private static final Option receptionDelay = Option.builder()
    .longOpt(RECEPTION_DELAY_NAME)
    .desc("Time delay seed object was received")
    .hasArg()
    .argName(ISO_DURATION)
    .build();

  private static final String DURATION_INCREMENT_NAME = "durationIncrement";
  private static final Option durationIncrement = Option.builder()
    .longOpt(DURATION_INCREMENT_NAME)
    .desc("Smallest amount of time between statuses")
    .hasArg()
    .argName(ISO_DURATION)
    .build();

  private static final String BOOLEAN_INITIAL_STATUS_NAME = "booleanInitialStatus";
  private static final Option booleanInitialStatus = Option.builder()
    .longOpt(BOOLEAN_INITIAL_STATUS_NAME)
    .desc("Initial environmental issue status")
    .hasArg()
    .argName("ISO Boolean")
    .build();

  private static final String MEAN_OCCURRENCES_PER_YEAR_NAME = "meanOccurrencesPerYear";
  private static final Option meanOccurrencesPerYear = Option.builder()
    .longOpt(MEAN_OCCURRENCES_PER_YEAR_NAME)
    .desc("Mean number of occurrences of an event in a year")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String MEAN_HOURS_OF_PERSISTENCE_NAME = "meanHoursOfPersistence";
  private static final Option meanHoursOfPersistence = Option.builder()
    .longOpt(MEAN_HOURS_OF_PERSISTENCE_NAME)
    .desc("Mean time in hours a given event will continue to happen")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String DURATION_ANALOG_STATUS_MIN_NAME = "durationAnalogStatusMin";
  private static final Option durationAnalogStatusMin = Option.builder()
    .longOpt(DURATION_ANALOG_STATUS_MIN_NAME)
    .desc("Minimum duration in hours as a double")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String DURATION_ANALOG_STATUS_MAX_NAME = "durationAnalogStatusMax";
  private static final Option durationAnalogStatusMax = Option.builder()
    .longOpt(DURATION_ANALOG_STATUS_MAX_NAME)
    .desc("Maximum duration in hours as a double")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String DURATION_BETA0_NAME = "durationBeta0";
  private static final Option durationBeta0 = Option.builder()
    .longOpt(DURATION_BETA0_NAME)
    .desc("Constant parameter of the auto-regression time series model")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String DURATION_BETA1_NAME = "durationBeta1";
  private static final Option durationBeta1 = Option.builder()
    .longOpt(DURATION_BETA1_NAME)
    .desc("First order parameter of the auto-regression time series model")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String DURATION_STDERR_NAME = "durationStderr";
  private static final Option durationStderr = Option.builder()
    .longOpt(DURATION_STDERR_NAME)
    .desc("Standard error for the auto-regression time series model")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String DURATION_ANALOG_INITIAL_VALUE_NAME = "durationAnalogInitialValue";
  private static final Option durationAnalogInitialValue = Option.builder()
    .longOpt(DURATION_ANALOG_INITIAL_VALUE_NAME)
    .desc("Initial value for the auto-regression time series model")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String PERCENT_ANALOG_STATUS_MIN_NAME = "percentAnalogStatusMin";
  private static final Option percentAnalogStatusMin = Option.builder()
    .longOpt(PERCENT_ANALOG_STATUS_MIN_NAME)
    .desc("Minimum percent as a double between 0.0 and 1.0")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String PERCENT_ANALOG_STATUS_MAX_NAME = "percentAnalogStatusMax";
  private static final Option percentAnalogStatusMax = Option.builder()
    .longOpt(PERCENT_ANALOG_STATUS_MAX_NAME)
    .desc("Maximum percent as a double between 0.0 and 1.0")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String PERCENT_BETA0_NAME = "percentBeta0";
  private static final Option percentBeta0 = Option.builder()
    .longOpt(PERCENT_BETA0_NAME)
    .desc("Constant parameter of the auto-regression time series model")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String PERCENT_BETA1_NAME = "percentBeta1";
  private static final Option percentBeta1 = Option.builder()
    .longOpt(PERCENT_BETA1_NAME)
    .desc("First order parameter of the auto-regression time series model")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String PERCENT_STDERR_NAME = "percentStderr";
  private static final Option percentStderr = Option.builder()
    .longOpt(PERCENT_STDERR_NAME)
    .desc("Standard error for the auto-regression time series model")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String PERCENT_ANALOG_INITIAL_VALUE_NAME = "percentAnalogInitialValue";
  private static final Option percentAnalogInitialValue = Option.builder()
    .longOpt(PERCENT_ANALOG_INITIAL_VALUE_NAME)
    .desc("Initial value for the auto-regression time series model")
    .hasArg()
    .argName(ISO_DOUBLE)
    .build();

  private static final String USE_CURATED_DATA_GENERATION_NAME = "useCuratedDataGeneration";
  private static final Option useCuratedDataGeneration = Option.builder()
    .longOpt(USE_CURATED_DATA_GENERATION_NAME)
    .desc("Set true to use curated data")
    .hasArg()
    .argName("ISO Boolean")
    .build();

  public static final Options options = new Options()
    .addOption(dataType)
    .addOption(startTime)
    .addOption(sampleDuration)
    .addOption(duration)
    .addOption(stationGroups)
    .addOption(receptionDelay)
    .addOption(durationIncrement)
    .addOption(booleanInitialStatus)
    .addOption(meanOccurrencesPerYear)
    .addOption(meanHoursOfPersistence)
    .addOption(durationAnalogStatusMin)
    .addOption(durationAnalogStatusMax)
    .addOption(durationBeta0)
    .addOption(durationBeta1)
    .addOption(durationStderr)
    .addOption(durationAnalogInitialValue)
    .addOption(percentAnalogStatusMin)
    .addOption(percentAnalogStatusMax)
    .addOption(percentBeta0)
    .addOption(percentBeta1)
    .addOption(percentStderr)
    .addOption(percentAnalogInitialValue)
    .addOption(useCuratedDataGeneration);

  /**
   * Generates an initial {@link GenerationSpec} for in the incoming command line arguments,
   * containing all information provided at runtime.
   *
   * @param args Command line arguments
   * @return Initial GenerationSpec
   */
  public static GenerationSpec parse(String[] args) {
    CommandLineParser parser = new DefaultParser();

    try {
      CommandLine line = parser.parse(DatasetGeneratorOptions.options, args);
      return parseSpec(line);
    } catch (ParseException | DateTimeParseException e) {
      throw new IllegalArgumentException("Error parsing arguments", e);
    }

  }

  private static void parseSpecForDurationStatusGeneratorParameters(
    GenerationSpec.Builder specBuilder, CommandLine line) {
    Optional<Double> statusMin = getOption(line, DURATION_ANALOG_STATUS_MIN_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> statusMax = getOption(line, DURATION_ANALOG_STATUS_MAX_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> beta0 = getOption(line, DURATION_BETA0_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> beta1 = getOption(line, DURATION_BETA1_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> percentStderr = getOption(line, DURATION_STDERR_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> percentAnalogInitialValue = getOption(line, DURATION_ANALOG_INITIAL_VALUE_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));

    statusMin.ifPresent(status -> specBuilder
      .addDurationStatusGeneratorParameter(InitialCondition.DURATION_ANALOG_STATUS_MIN, status));
    statusMax.ifPresent(status -> specBuilder
      .addDurationStatusGeneratorParameter(InitialCondition.DURATION_ANALOG_STATUS_MAX, status));
    beta0.ifPresent(b0 -> specBuilder
      .addDurationStatusGeneratorParameter(InitialCondition.DURATION_BETA0, b0));
    beta1.ifPresent(b1 -> specBuilder
      .addDurationStatusGeneratorParameter(InitialCondition.DURATION_BETA1, b1));
    percentStderr.ifPresent(stdErr -> specBuilder
      .addDurationStatusGeneratorParameter(InitialCondition.DURATION_STDERR, stdErr));
    percentAnalogInitialValue.ifPresent(initialValue -> specBuilder
      .addDurationStatusGeneratorParameter(InitialCondition.DURATION_ANALOG_INITIAL_VALUE,
        initialValue));
  }

  private static void parseSpecForPercentStatusGeneratorParameters(
    GenerationSpec.Builder specBuilder, CommandLine line) {
    Optional<Double> statusMin = getOption(line, PERCENT_ANALOG_STATUS_MIN_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> statusMax = getOption(line, PERCENT_ANALOG_STATUS_MAX_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> beta0 = getOption(line, PERCENT_BETA0_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> beta1 = getOption(line, PERCENT_BETA1_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> percentStderr = getOption(line, PERCENT_STDERR_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> percentAnalogInitialValue = getOption(line, PERCENT_ANALOG_INITIAL_VALUE_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));

    statusMin.ifPresent(status -> specBuilder
      .addPercentStatusGeneratorParameter(InitialCondition.PERCENT_ANALOG_STATUS_MIN, status));
    statusMax.ifPresent(status -> specBuilder
      .addPercentStatusGeneratorParameter(InitialCondition.PERCENT_ANALOG_STATUS_MAX, status));
    beta0.ifPresent(b0 -> specBuilder
      .addPercentStatusGeneratorParameter(InitialCondition.PERCENT_BETA0, b0));
    beta1.ifPresent(b1 -> specBuilder
      .addPercentStatusGeneratorParameter(InitialCondition.PERCENT_BETA1, b1));
    percentStderr.ifPresent(stdErr -> specBuilder
      .addPercentStatusGeneratorParameter(InitialCondition.PERCENT_STDERR, stdErr));
    percentAnalogInitialValue.ifPresent(initialValue -> specBuilder
      .addPercentStatusGeneratorParameter(InitialCondition.PERCENT_ANALOG_INITIAL_VALUE,
        initialValue));
  }

  private static void parseSpecForBooleanStatusGeneratorParameters(
    GenerationSpec.Builder specBuilder, CommandLine line) {
    Optional<Duration> durationIncrement = getOption(line, DURATION_INCREMENT_NAME,
      val -> Optional.ofNullable(val).map(Duration::parse));
    Optional<Boolean> booleanInitialStatus = getOption(line, BOOLEAN_INITIAL_STATUS_NAME,
      val -> Optional.ofNullable(val).map(Boolean::parseBoolean));
    Optional<Double> meanOccurrencesPerYear = getOption(line, MEAN_OCCURRENCES_PER_YEAR_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));
    Optional<Double> meanHoursOfPersistence = getOption(line, MEAN_HOURS_OF_PERSISTENCE_NAME,
      val -> Optional.ofNullable(val).map(Double::parseDouble));

    durationIncrement.ifPresent(increment -> specBuilder
      .addBooleanStatusGeneratorParameter(InitialCondition.DURATION_INCREMENT, increment));
    booleanInitialStatus.ifPresent(status -> specBuilder
      .addBooleanStatusGeneratorParameter(InitialCondition.BOOLEAN_INITIAL_STATUS, status));
    meanOccurrencesPerYear.ifPresent(occurrences -> specBuilder
      .addBooleanStatusGeneratorParameter(InitialCondition.MEAN_OCCURRENCES_PER_YEAR,
        occurrences));
    meanHoursOfPersistence.ifPresent(hours -> specBuilder
      .addBooleanStatusGeneratorParameter(InitialCondition.MEAN_HOURS_OF_PERSISTENCE, hours));
  }

  private static GenerationSpec parseSpec(CommandLine line) {
    GenerationType dataType = getOption(line, DATA_TYPE_NAME, GenerationType::parseType);
    Instant startTime = getOption(line, START_TIME_NAME, Instant::parse);
    Duration sampleDuration = getOption(line, SAMPLE_DURATION_NAME, Duration::parse);
    Duration duration = getOption(line, DURATION_NAME, Duration::parse);

    GenerationSpec.Builder specBuilder = GenerationSpec.builder()
      .setType(dataType)
      .setStartTime(startTime)
      .setSampleDuration(sampleDuration)
      .setDuration(duration)
      .setBatchSize(100); //TODO: SHOULD THIS BE PARSED OR NOT???

    String stationGroups = line.getOptionValue(STATION_GROUPS_NAME);
    Optional<Duration> receptionDelay = getOption(line, RECEPTION_DELAY_NAME,
      val -> Optional.ofNullable(val).map(Duration::parse));
    Optional<Boolean> useCuratedDataGeneration = getOption(line, USE_CURATED_DATA_GENERATION_NAME,
      val -> Optional.ofNullable(val).map(Boolean::parseBoolean));

    specBuilder.addInitialCondition(InitialCondition.STATION_GROUPS, stationGroups);
    receptionDelay.ifPresent(reception -> specBuilder
      .addInitialCondition(InitialCondition.RECEPTION_DELAY, reception.toString()));
    useCuratedDataGeneration.ifPresent(specBuilder::setUseCuratedDataGeneration);

    parseSpecForBooleanStatusGeneratorParameters(specBuilder, line);
    parseSpecForDurationStatusGeneratorParameters(specBuilder, line);
    parseSpecForPercentStatusGeneratorParameters(specBuilder, line);

    return specBuilder.build();
  }

  private static <T> T getOption(CommandLine line, String option,
    Function<String, T> valueParser) {
    return valueParser.apply(line.getOptionValue(option));
  }

  private DatasetGeneratorOptions() {
  }
}
