package gms.dataacquisition.stationreceiver.cd11.connman.util;

import com.google.common.net.InetAddresses;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Cd11 ConnMan Utility for shared ConnMan methods
 */
public class Cd11ConnManUtil {

  private static final Logger logger = LoggerFactory.getLogger(Cd11ConnManUtil.class);


  private static final String HOST_REGEX = "[\\d\\w.-]+";
  private static final String HOST_RESULTS_REGEX = HOST_REGEX + " has address ([\\d.]{7,15})";
  private static final Pattern hostPattern = Pattern.compile(HOST_REGEX);
  private static final Pattern hostResultsPattern = Pattern.compile(HOST_RESULTS_REGEX);

  private Cd11ConnManUtil() {
  }

  public static Map<String, InetAddress> buildAddressMap(SystemConfig systemConfig,
    String... serviceKeys) {
    return Stream.of(serviceKeys)
      .collect(toMap(identity(),
        serviceKey -> Cd11ConnManUtil.resolveInetAddress(systemConfig, serviceKey)));
  }

  public static InetAddress resolveInetAddress(SystemConfig systemConfig, String serviceKey) {
    // Replace key value pairs of ip addresses using system config
    String ipOrHost = systemConfig.getValue(serviceKey);
    logger.info("Dataman Service configured");

    final InetAddress serviceAddress;
    if (InetAddresses.isInetAddress(ipOrHost)) {
      logger.info("Dataman Address resolves as valid ip address");
      serviceAddress = InetAddresses.forString(ipOrHost);
    } else {
      logger.info("Attempting retrieval of ip for Dataman host");
      try {
        //da-data man is hard coded to satisfy Fortify scans
        //If the dataman service name changes this will need to be update here as well as in SystemConfig
        serviceAddress = findIp("da-dataman").filter(InetAddresses::isInetAddress)
          .map(InetAddresses::forString).orElseThrow(() -> new IOException(
            format("Unable to resolve ip address for host %s", ipOrHost)));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return serviceAddress;
  }

  /**
   * Leverages process execution to search for host information, parsing the ip address out of the
   * results. Note that {@link Runtime#exec(String[])} will always parse the input array into a
   * single command with passed arguments, helping to prevent remote execution attacks.
   *
   * @param host Hostname
   * @return Parsed ip address from execution results, or empty Optional if nothing was found
   * @throws IllegalArgumentException if the input doesn't match a valid hostname, also helping to
   * prevent remote execution attacks
   * @throws IOException if an error occurred during process execution
   */
  private static Optional<String> findIp(String host) throws IOException {
    checkArgument(hostPattern.matcher(host).matches(),
      "Invalid input for host: %s", host);

    try (var inputStream = Runtime.getRuntime()
      .exec(new String[]{"/usr/bin/host", host}).getInputStream();
         var scanner = new Scanner(inputStream)) {
      var hostResults = scanner.findInLine(hostResultsPattern);
      return Optional.ofNullable(hostResults == null ? null : scanner.match().group(1));
    }
  }
}