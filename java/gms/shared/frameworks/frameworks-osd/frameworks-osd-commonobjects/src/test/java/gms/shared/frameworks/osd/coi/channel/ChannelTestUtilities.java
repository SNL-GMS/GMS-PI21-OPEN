package gms.shared.frameworks.osd.coi.channel;

class ChannelTestUtilities {

  /**
   * Obtains the hash portion of a {@link Channel#getName()}.  The returned string begins with the
   * first character of the hash.
   *
   * @param channel {@link Channel}, not null
   * @return name substring containing the Channel's hash
   * @throws IllegalArgumentException if the Channel name does not contain a hash component (e.g.
   * does not have a trailing {@link Channel#COMPONENT_SEPARATOR} followed by a non-empty string)
   */
  static String extractHash(Channel channel) {
    final String name = channel.getName();

    final int beginHash = name.lastIndexOf(Channel.COMPONENT_SEPARATOR) + 1;
    if (0 == beginHash || name.length() == beginHash) {
      throw new IllegalArgumentException("name '" + name + "' does not include a non-empty hash");
    }

    return name.substring(beginHash);
  }

  /**
   * Obtains the attributes portion of a {@link Channel#getName()}.  The returned string begins and
   * ends with {@link Channel#COMPONENT_SEPARATOR}.
   *
   * @param channel {@link Channel}, not null
   * @return name substring containing the Channel's attributes
   * @throws IllegalArgumentException if the Channel name does not contain an attributes section
   * (e.g. does not have separate leading and trailing {@link Channel#COMPONENT_SEPARATOR}s
   * separated by a non-empty string)
   */
  static String extractAttributes(Channel channel) {
    final String name = channel.getName();

    final int beginAttributes = name.indexOf(Channel.COMPONENT_SEPARATOR);
    final int endAttributes = name.lastIndexOf(Channel.COMPONENT_SEPARATOR) + 1;
    if (-1 == beginAttributes || 0 == endAttributes || ((beginAttributes + 2) >= endAttributes)) {
      throw new IllegalArgumentException(
        "name '" + name + "' does not include non-empty attributes");
    }

    return name.substring(beginAttributes, endAttributes);
  }
}
