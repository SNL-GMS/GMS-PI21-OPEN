package gms.dataacquisition.stationreceiver.cd11.common;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Cd11Validator {

  private Cd11Validator() {

  }

  // currently only two station types...
  private static final Set<String> KNOWN_STATION_TYPES = Set.of("IDC", "IMS", "");
  private static final Set<String> KNOWN_SERVICE_TYPES = Set.of("UDP", "TCP");

  /**
   * Validates a network port number.
   *
   * @param port Port number.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static void validPortNumber(int port) {
    checkArgument(0 <= port && port <= 65535, "Port number is out of range.");
  }

  /**
   * Validates a network port number, and does not accept 0 as valid.
   *
   * @param port Port number.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static void validNonZeroPortNumber(int port) {
    checkArgument(1 <= port && port <= 65535, "Port number is out of range.");
  }

  /**
   * Validates the Frame Creator value. (CD 1.1 Frame Header)
   *
   * @param frameCreator Frame Creator value.
   * @return the frameCreator, if valid
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validFrameCreator(String frameCreator) {
    checkNotNull(frameCreator);
    checkArgument(!frameCreator.isEmpty(), "Frame Creator is empty or null");
    checkArgument(frameCreator.length() <= 8,
      "Frame Creator must be of length less than or equal to 8");
    return frameCreator;
  }

  /**
   * Validates the Frame Destination value. (CD 1.1 Frame Header)
   *
   * @param frameDestination Frame Destination value (CD 1.1 Frame Header).
   * @return the frameDestination, if valid
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validFrameDestination(String frameDestination) {
    checkNotNull(frameDestination);
    checkArgument(frameDestination.length() <= 8,
      "Frame Destination must be of length less than or equal to 8");
    return frameDestination;
  }

  /**
   * Validates the Station Name / Responder Name value. (CD 1.1 Connection Request / Connection
   * Response frames)
   *
   * @param name Station Name or Responder Name.
   * @return the station name or responder name, if valid
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validStationOrResponderName(String name) {
    return ofMaxLength(name, 8);
  }

  public static String validStationOrResponderType(String name) {
    checkArgument(KNOWN_STATION_TYPES.contains(name.trim()),
      "Unknown station or responder type %s (only know %s)", name, KNOWN_STATION_TYPES);
    return name;
  }

  private static String ofMaxLength(String s, int length) {
    checkNotNull(s);
    checkArgument(s.length() <= length, "String is too long; max %d but was %d",
      length, s.length());
    return s;
  }

  /**
   * Validates the Service Type value.
   *
   * @param serviceType Service Type.
   * @return the service type, if valid
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validServiceType(String serviceType) {
    checkNotNull(serviceType);
    checkArgument(!"UDP".equals(serviceType), "UDP is not yet implemented.");
    checkArgument(serviceType.length() >= 3,
      "Invalid Service Type value received.");
    checkArgument(KNOWN_SERVICE_TYPES.contains(serviceType.substring(0, 3).trim()),
      "Invalid Service Type value received.");
    return serviceType;
  }

  /**
   * Validates a network IP Address.
   *
   * @param ipAddress IP Address.
   * @return Integer representation of the IP Address. Non null.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static int validIpAddress(InetAddress ipAddress) {
    checkNotNull(ipAddress, "IP Address empty or null.");

    try {
      return InetAddresses.coerceToInteger(ipAddress);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid IP Address received.", e);
    }
  }

  public static int validIpAddress(int ipAddress) {
    try {
      InetAddresses.fromInteger(ipAddress);
      return ipAddress;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid IP Address received: %1$s.", e);
    }
  }

  /**
   * Validates a "frame set acked" value. (CD 1.1 Acknack frame)
   *
   * @param frameSetAcked Full name of the frame set being acknowledged (for example, "SG7:0").
   * @return the frameSetAcked, if valid
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validFrameSetAcked(String frameSetAcked) {
    checkNotNull(frameSetAcked);
    checkArgument(!frameSetAcked.isEmpty(), "Frame Set Acked Empty!");
    checkArgument(frameSetAcked.length() <= 20,
      "Frame Set Acked is too long (20-byte maximum).");
    return frameSetAcked;
  }

}
