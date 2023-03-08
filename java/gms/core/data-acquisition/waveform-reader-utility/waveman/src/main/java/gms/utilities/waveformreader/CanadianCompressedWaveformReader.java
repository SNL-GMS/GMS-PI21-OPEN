package gms.utilities.waveformreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class CanadianCompressedWaveformReader implements WaveformReaderInterface {

  private static final Logger logger = LoggerFactory
    .getLogger(CanadianCompressedWaveformReader.class);

  public double[] read(InputStream input, int numSamples, int skip) throws IOException {
    // Extract the canadian compressed data
    // TODO: (sgk 06/23/2021) should this really read skip + numSamples of data?
    double[] data = CanadianCompression.read(input, skip + numSamples, false);

    if (skip == 0) {
      return data;
    }

    return Arrays.copyOfRange(data, skip, numSamples);

  }

  private static class CanadianCompression {

    /*
     * bit lengths for various sample codes
     */
    private static final byte[][] bitLengthsForCodes = new byte[][]
      {{4, 6, 8, 10, 12, 14, 16, 18},
        {4, 8, 12, 16, 20, 24, 28, 32}};

    /*
     * Number of groups of data samples in a data block
     */
    private static final int GROUPS_IN_BLOCK = 5;

    /*
     * Number of samples in a group
     */
    private static final int SAMPLES_IN_GROUP = 4;

    /*
     * Number of samples in a data block
     */
    private static final int SAMPLES_IN_BLOCK = GROUPS_IN_BLOCK * SAMPLES_IN_GROUP;

    /**
     * Read the compressed data stream, composed of N samples.
     *
     * @param inputStream Stream of compressed data to read from
     * @param numSamples Number of samples to read from stream
     * @return uncompressed integer samples
     */
    public static double[] read(InputStream inputStream, int numSamples, boolean interlace)
      throws IOException {
      var bitInputStream = new BitInputStream(inputStream, 8192);

      //  Determine the number of blocks
      var numBlocks = (int) Math.ceil(numSamples / ((double) SAMPLES_IN_BLOCK));
      var dataArray = new double[numSamples];

      if (interlace) {
        readInterlaced(numSamples, bitInputStream, numBlocks, dataArray);
      } else {
        readSequential(numSamples, bitInputStream, numBlocks, dataArray);
      }

      return dataArray;
    }

    private static void readSequential(int numSamples, BitInputStream bitInputStream, int numBlocks,
      double[] data) throws IOException {
      //  Read the index blocks
      var bitsDoubleArray = new byte[numBlocks][GROUPS_IN_BLOCK];
      for (var iCounter = 0; iCounter < numBlocks; iCounter++) {
        readIndexBlock(bitInputStream, bitsDoubleArray[iCounter]);
      }

      //  Read the first sample
      var first = bitInputStream.read(32, true);

      //  Read the data blocks
      var n = 0;
      var i = 0;
      while (i < numBlocks && n < numSamples) {
        n = readDataBlock(bitInputStream, bitsDoubleArray[i++], data, n);
      }

      //  undo the second difference
      WaveformReaderUtil.integrate(data, 0, numSamples);

      //  undo the first difference, shifting the samples
      for (var k = 0; k < numSamples; k++) {
        var saveDouble = data[k];
        data[k] = first;
        first += saveDouble;
      }
    }

    private static void readInterlaced(int numSamples, BitInputStream bitInputStream, int numBlocks,
      double[] data) throws IOException {
      var error = false;
      var blockBytes = new byte[GROUPS_IN_BLOCK];
      var n = 0;

      /*
       * Read each of the blocks.
       */
      for (var i = 0; i < numBlocks && n < numSamples; i++) {
        int startN = n;

        //  Read the index block
        readIndexBlock(bitInputStream, blockBytes);

        //  Read the first sample
        int firstSample = bitInputStream.read(32, true);
        if (firstSample != data[n] && n > 0) {
          error = true;
        }
        data[n++] = firstSample;

        //  Read the differentiated samples
        n = readDataBlock(bitInputStream, blockBytes, data, n);

        //  Integrate the data twice
        WaveformReaderUtil.integrate(data, startN + 1, n);
        WaveformReaderUtil.integrate(data, startN, n);

        // Back up to account for block overlap
        n--;
      }

      if (error) {
        logger.error("CanadianCompression.read:  Warning, error checking samples do not match.");
      }
    }

    /**
     * Read an index block from the provided input stream and store the number of bits in the
     * provided array.
     */
    private static void readIndexBlock(BitInputStream bitInputStream, byte[] bits)
      throws IOException {
      int lengthCode = bitInputStream.read(1, false);
      byte[] bitLengths = bitLengthsForCodes[lengthCode];

      for (var i = 0; i < GROUPS_IN_BLOCK; i++) {
        bits[i] = bitLengths[bitInputStream.read(3, false)];
      }
    }

    /**
     * Read the data block from the provided input stream and return the number of samples read
     */
    private static int readDataBlock(BitInputStream bitInputStream, byte[] bits, double[] data,
      int n) throws IOException {

      for (var j = 0; j < GROUPS_IN_BLOCK; j++) {
        int b = bits[j];

        for (var k = 0; k < SAMPLES_IN_GROUP && n < data.length; k++, n++) {
          data[n] = bitInputStream.read(b, true);
        }
      }

      return n;
    }


  }
}
