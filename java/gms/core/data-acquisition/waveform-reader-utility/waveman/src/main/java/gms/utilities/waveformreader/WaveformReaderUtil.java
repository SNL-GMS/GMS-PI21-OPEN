package gms.utilities.waveformreader;

public class WaveformReaderUtil {

  /**
   * The samples extracted from each record will need to be integrated to reflect the number of
   * times the data was differenced and the last sample checked. NOTE: this is an in-place process,
   * data param will be altered as a result of this call
   *
   * @param data Data to integrate
   * @param start Start of the integration
   * @param end End of the integration
   */
  public static void integrate(double[] data, int start, int end) {
    if (start >= data.length) {
      return;
    }

    double prev = data[start];
    for (int i = start + 1; i < end; i++) {
      prev += data[i];
      data[i] = prev;
    }
  }
}
