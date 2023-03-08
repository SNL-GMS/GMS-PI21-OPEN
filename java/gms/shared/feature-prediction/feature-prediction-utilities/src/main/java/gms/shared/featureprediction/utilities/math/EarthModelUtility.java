package gms.shared.featureprediction.utilities.math;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.exception.InsufficientDataException;

public class EarthModelUtility {

  private static final int MAX_DIST_SAMPLES = 7;
  private static final int MAX_DEPTH_SAMPLES = 4;
  private static final int MIN_NUM_DIST_SAMPLES = 3;
  private static final double DISTANCE_PROXIMITY_TOLERANCE = 0.00001;
  private static final double DEPTH_PROXIMITY_TOLERANCE = 0.001;

  private final double[] firstModelAxis;

  private final double[] secondModelAxis;

  private final double[][] modelTable;

  private final CustomBicubicSplineInterpolator bicubicSplineInterpolator;

  private final boolean extrapolateGridpoints;

  boolean wasExtrapolated;

  private int zTop;
  private int zBottom;
  private int iDist;
  private int iDepth;

  private int nxReq;

  private int nzReq;
  private int xLow;
  private int xHigh;

  public EarthModelUtility(
    double[] firstModelAxis,
    double[] secondModelAxis,
    double[][] modelTable,
    boolean extrapolateGridpoints
  ) {
    this.firstModelAxis = firstModelAxis;
    this.secondModelAxis = secondModelAxis;
    this.modelTable = modelTable;
    this.extrapolateGridpoints = extrapolateGridpoints;

    this.bicubicSplineInterpolator = new CustomBicubicSplineInterpolator();
  }

  public boolean wasExtrapolated() {
    return wasExtrapolated;
  }

  public double[] interpolateEarthModel(double firstModelValue, double secondModelValue) {

    var miniTableTriple = generateMinitable(firstModelValue, secondModelValue);

    return bicubicSplineInterpolator.getFunctionAndDerivatives(
      miniTableTriple.getLeft(),
      miniTableTriple.getMiddle(),
      miniTableTriple.getRight()
    ).apply(firstModelValue, secondModelValue);
  }

  /**
   * Constructs a minitable around the given depth and distance from the given table of values.
   * Holes in the table are extrapolated.
   */
  private Triple<double[], double[], double[][]> generateMinitable(double depth,
    double distance) {
    
    // Keeping the original terminology of depths and distances for now, as
    // changing them might be more work then its worth.
    // firstModelAxis and secondModelAxis are so named because, depending on 
    // how a model is stored, theres no guarentee of order of depth and distance. 
    // (As long as the ordering is consistent with the table, it should not make
    // a difference.)
    var depths = this.firstModelAxis;
    var distances = this.secondModelAxis;
    var values = this.modelTable;

    // Required # of samples in x-direction
    nxReq = Math.min(MAX_DIST_SAMPLES, distances.length);

    // Required # of samples in z-direction
    nzReq = Math.min(MAX_DEPTH_SAMPLES, depths.length);

    var ttcsInHoleDist = new double[]{181., -1., 0.};

    setDistInHole(ttcsInHoleDist, distances, values);

    boolean inHole = isInHole(distance, ttcsInHoleDist);

    // ====================================================================
    // Set Depth Range
    // ====================================================================
    if( !determineDepthRange(depths, depth)) {
      return null;
    }


    // ====================================================================
    // Set Distance Range
    // ====================================================================
    // --------------------------------------------------------------------
    // Preliminary Bracketing
    // --------------------------------------------------------------------
    setPreliminaryDistanceRange(distances, distance);

    // --------------------------------------------------------------------
    // Final Adjustment of Distance Range Bounds
    //
    // If requested distance sample is within table bounds, then we
    // need to find as many valid samples as possible. If none exists
    // shift xLow and xHigh closest to a valid curve. On the other
    // hand, if the requested distance sample is located clearly
    // outside the valid table region, create an artificial mini-table
    // surrounding the requested sample distance value.
    // --------------------------------------------------------------------
    makeFinalAdjustmenDistanceRange(values, inHole);

    // ====================================================================
    // Construct Mini Table
    //
    // Up to now we have only inspected the 1st depth component on the
    // distance vector. Now we will build a complete mini-table which
    // will be used for actual inter/extrapolation using rational
    // function and bi-cubic spline interpolation routines.
    // ====================================================================

    var miniTable = new double[nzReq][];
    var miniDist = extract(distances, xLow, nxReq);
    var miniDepth = extract(depths, zTop, nzReq);

    if (!extrapolateGridpoints) {
      extractMinitableEntries(miniTable, values);
      return Triple.of(miniDepth, miniDist, miniTable);
    }

    // --------------------------------------------------------------------
    // First, construct mini-table assuming no depth extrapolation is
    // needed. All distance extrapolation will be handled in this master
    // "for loop".
    // --------------------------------------------------------------------
    for (int k = 0, kk = zTop; k < nzReq; k++, kk++) {
      // First fill miniTable assuming all values[][] values are valid
      miniTable[k] = extract(values[kk], xLow, nxReq);
      // ------------------------------------------------------------------
      // Check the distance value with respect to range of distances
      // ------------------------------------------------------------------
      if (inHole || iDist > 0) {

        // ----------------------------------------------------------------
        // Case 1: Off high end of distance curve -OR-
        // in a hole
        // ----------------------------------------------------------------
        var ii = checkDistValueHighOrHole(distances, distance, values[kk], k, miniDist, miniTable);
        //**** added two rows below to get valid rational spline interpolant
        //****  (jrh 12-18-2018)
        var minTableRowEx = extract(values[kk], ii, nxReq);
        var miniDistEx = extract(distances, ii, nxReq);


        // At this depth (k) in the mini-table, extrapolate any missing
        // values along the distance direction
        for (var j = 0; j < nxReq; j++) {
          miniTable[k][j] = checkForInvalidMiniTableEntry(miniTable[k][j], miniDistEx, minTableRowEx, miniDist[j]);
        }

      } // End if inHole || iDist > 0

      else if (iDist < 0) {
        // ----------------------------------------------------------------
        // Case 2: Off low end of distance curve
        // ----------------------------------------------------------------
        shiftDistancesToRequested(distances, distance, miniDist, miniTable, k);
      } else {
        // ----------------------------------------------------------------
        // Case 3: Distance is at a valid range in the distance vector
        //
        // Make sure there are no single BAD_SAMPLE entries. If so,
        // extrapolate as necessary.
        // ----------------------------------------------------------------

        validRangeVectorCalc(miniTable, distances, miniDist, values[kk], k);
      }
    }

    // Now that the distance component of the mini-table is secure,
    // perform any necessary extrapolation for the depth component by
    // re-constructing the mini-table. Also, build transposed mini-
    // table, miniTableTrans[][], to obtain distance derivatives
    // from spline routines below.

    reconstructMiniTable(miniTable, depths, depth, miniDepth);

    wasExtrapolated = wasExtrapolated || inHole;

    return Triple.of(miniDepth, miniDist, miniTable);
  }

  private void reconstructMiniTable(double[][] miniTable, double[] depths, double depth, double[] miniDepth) {
    var miniTableTrans = new double[nxReq][nzReq];

    for (var j = 0; j < nxReq; j++) {
      // Fill miniTableTrans[][] assuming all values from array,
      // miniTable[][], are valid

      for (var i = 0; i < nzReq; i++) {
        miniTableTrans[j][i] = miniTable[i][j];
      }

      // Are we below the lowest depth component in the curve
      if (iDepth > 0) {
        calculateDepthRange(depth, depths, miniDepth,zBottom - ((nzReq - 1) / 2), j);
        // Extrapolate a new set of depths bracketing the requested
        // depth
        extrapolateRequestedDepth(miniTable, depths, miniTableTrans, j, miniDepth);

      } else if (iDepth < 0) {
        calculateDepthRange(depth, depths, miniDepth,zTop + ((nzReq - 1) / 2), j);
        // Extrapolate a new set of depths bracketing the requested
        // depth
        extrapolateRequestedDepth(miniTable, depths, miniTableTrans, j, miniDepth);
      }
    }
  }

  private void shiftDistancesToRequested(double[] distances, double distance, double[] miniDist,
    double[][] miniTable, int k) {
    var minTableRow = miniTable[k].clone();
    if (distance < distances[xLow]) {
      // Shift the distances associated with the mini table down
      // to a region centered about the requested distance
      double xShift = distance - distances[xLow + ((nxReq - 1) / 2)];
      for (var j = 0; j < nxReq; j++) {
        if (k < 1) {
          miniDist[j] = miniDist[j] + xShift;
        }
        miniTable[k][j] = Double.NaN;
      }
    }

    // At this depth (k) in the mini-table, interpolate any missing
    // values along the distance direction
    for (var j = 0; j < nxReq; j++) {
      miniTable[k][j] = checkForInvalidMiniTableEntry(miniTable[k][j], miniDist, minTableRow, miniDist[j]);
    }
  }

  private boolean determineDepthRange(double[] depths, double depth) {
    if (depths.length == 1) {
      // ------------------------------------------------------------------
      // CASE A: Only 1 depth sample available
      // ------------------------------------------------------------------
      zTop = 0;
      zBottom = 0;
      //TODO: tolerance?
      if (depth != depths[0])
      //TODO: handle this
      {
        return false;
      }
    } else {
      setDepthRange(depths, depth);
    }
    return true;
  }

  private void validRangeVectorCalc(double[][] miniTable, double[] distances, double[] miniDist,
    double[] valueRow, int k) {
    for (var j = 0; j < nxReq; j++) // Scan distances (j) in the mini
    { // table at this depth (k)
      if (invalid(miniTable[k][j])) {
        wasExtrapolated = true;
        if (j > 0) {
          // Go back and get as many valid samples for this
          // depth as is possible for a good sample space.

          getValidSamplesForDepth(valueRow, miniTable[k], distances, miniDist, nxReq, j);
        } else {
          // Advance in distance and get as many valid samples
          // for this depth as is possible for a good sample space.
          getValidSamplesAdvanceDistance(valueRow, miniTable[k], distances, miniDist);

        }
        break;
      }
    }
  }

  private void getValidSamplesAdvanceDistance(double[] valueRow, double[] miniTableRow, double[] distances,
    double[] miniDist) {
    var numExtrap = 0;
    var numSamp = 0;
    int i = xLow;
    for (var n = 0; i < xHigh; i++, n++) {
      if (valid(valueRow[i])) {
        xLow = i;
        numExtrap = n;
        for (var nn = 0; nn < nxReq; nn++) {
          if (valid(valueRow[xLow + nn])) {
            ++numSamp;
          }
        }

        // check for minimum sample number
        checkForMinSampleDataException(numSamp < MIN_NUM_DIST_SAMPLES);
        break;
      }
    }

    // Check for at least 1 sample
    checkForMinSampleDataException(i == xHigh);

    // create mini table

    for (var n = 0; n < numExtrap; n++) {
      miniTableRow[n] = checkForInvalidMiniTableEntryExtracted(miniTableRow[n], distances,
        valueRow, numSamp, miniDist[n], i);
    }
  }

  private void getValidSamplesForDepth(double[] valueRow, double[] miniTableRow, double[] distances, double[] miniDist,
    int numSamp, int j) {
    int numExtrap = nxReq - j;
    int i = xLow - numExtrap;
    while (i < 0 || invalid(valueRow[i])) {
      ++i;
      --numSamp;
      // check for minimum sample number
      checkForMinSampleDataException(numSamp < MIN_NUM_DIST_SAMPLES);
    }

    // Extrapolate a valid traveltime for the mini table
    for (var n = 0; n < numExtrap; n++) {
      int m = j + n;
      miniTableRow[m] = checkForInvalidMiniTableEntryExtracted(miniTableRow[m], distances,
        valueRow, numSamp, miniDist[m], i);

    }
  }

  private void makeFinalAdjustmenDistanceRange(double[][] values, boolean inHole) {
    if (inHole) {
      // ------------------------------------------------------------------
      // Case 1: Requested Distance is in a Hole
      // ------------------------------------------------------------------

      // Check outer distance value
      checkOuterDistanceValue(values);

    } else if (iDist == 0) {
      // ------------------------------------------------------------------
      // Case 2: Distance w/in Table, but not in a hole
      // ------------------------------------------------------------------

      checkDistanceInTableNotHole(values);
    }
  }

  private void setPreliminaryDistanceRange(double[] distances, double distance) {
    int xLeft = hunt(distances, distance);

    if (xLeft < 0) {
      // Case 1: distance < minimum table distance
      // Check if exactly equal
      checkDistanceProximity(distances, distance);

      xLow = 0;
      xHigh = nxReq - 1;
    } else if (xLeft >= distances.length - 1) {
      // Case 2: distance > maximum table distance
      iDist++;
      xLow = distances.length - nxReq;
      xHigh = distances.length - 1;
    } else {
      // Case 3: distance within valid table region

      // Distance is within a valid table region, but may not have a
      // valid value. Interogate table in order to obtain as many
      // valid values as possible for either direct interpolation or
      // eventual extrapolation. This is determined by the xLow and
      // xHigh settings.

      // Make sure that high and low end requested does not run us
      // off one side of the distance curve or the other. We need
      // to do this even before we check the actual values contained
      // in the 2-D (x-z) array.

      xHigh = Math.min(xLeft + (nxReq / 2), distances.length - 1);
      xLow = Math.max(xHigh - nxReq + 1, 0);
      if (xLow == 0) {
        xHigh = nxReq - 1;
      }
    }
  }

  private void calculateDepthRange(double depth, double[] depths, double[] miniDepth, int index, int j) {
    wasExtrapolated = true;
    // Case 1: Off the deep end of the depth range
    double zShift;
    zShift = depth - depths[index];
    if (j < 1) {
      for (var i = 0; i < nzReq; i++) {
        miniDepth[i] = miniDepth[i] + zShift;
      }
    }
  }

  private void extrapolateRequestedDepth(double[][] miniTable, double[] depths, double[][] miniTableTrans, int j,
    double[] miniDepth) {
    for (var i = 0; i < nzReq; i++) {
      miniTable[i][j] = RationalFunctionInterpolator.interpolate(extract(depths, zTop, nzReq),
        extract(miniTableTrans[j], 0, nzReq),
        miniDepth[i]);
    }
  }

  private void checkDistanceProximity(double[] distances, double distance) {
    if (Math.abs(distance - distances[0]) >= DISTANCE_PROXIMITY_TOLERANCE) {
      iDist--;
    }
  }

  private double checkForInvalidMiniTableEntryExtracted(double miniTableEntry, double[] distances, double[] value,
    int numSamp, double miniDistEntry, int i) {
    if (invalid(miniTableEntry)) {
      wasExtrapolated = true;
      return RationalFunctionInterpolator.interpolate(
        extract(distances, i, numSamp),
        extract(value, i, numSamp),
        miniDistEntry);
    }
    return miniTableEntry;
  }

  private double checkForInvalidMiniTableEntry(double miniTableEntry, double[] miniDist, double[] minTableRow, double miniDistEntry) {
    if (invalid(miniTableEntry)) {
      wasExtrapolated = true;
      return RationalFunctionInterpolator.interpolate(miniDist, minTableRow, miniDistEntry);
    }
    return miniTableEntry;
  }

  private void checkForMinSampleDataException(boolean testThis) throws InsufficientDataException {
    if (testThis) {
      throw new InsufficientDataException();
    }
  }

  private int checkDistValueHighOrHole(double[] distances, double distance, double[] valueArray, int index,
    double[] miniDist, double[][] miniTable) {
    int ii = -1;
    double diff = distance - distances[xHigh];
    if (iDist > 0 && diff > 1e-9) {
      // Case A: Off the high end of the distance curve
      //
      // Shift the distances associated with the mini table out to
      // a region centered about the requested distance
      double xShift = distance - distances[xHigh - ((nxReq - 1) / 2)];
      for (var j = 0; j < nxReq; j++) {
        if (index < 1) {
          miniDist[j] = miniDist[j] + xShift;
        }
        miniTable[index][j] = Double.NaN;
      }
      ii = xLow;
    } else {
      // Case B: In a hole in the distance curve

      //**** Added check for xHigh or xLow having an invalid value. It is
      //**** possible when the interpolation point is in a hole, but not
      //**** shifted left or right, to have either end with invalid points.
      //**** (jrh 12-18-2018)
      if (valid(valueArray[xHigh])) {
        ii = findIndex(valueArray, distances, ii, xLow);

      } else {
        // Scanning downward in distance, look for valid values in
        // the table to use for extrapolation fill-in of the mini table
        ii = findIndex(valueArray, distances, ii, xHigh);
        ii = ii - nxReq + 1;
      }
    }
    return ii;
  }

  private int findIndex(double[] valueArray, double[] distances, int ii, int border) {
    for (var i = border; i < distances.length; i++) {
      // Look for the first good value scanning upward from
      // the lower distance bound
      if (valid(valueArray[i])) {
        return i;
      }
    }
    return ii;
  }

  private void extractMinitableEntries(double[][] miniTable, double[][] values) {
    for (int k = 0, kk = zTop; k < nzReq; k++, kk++) {
      miniTable[k] = extract(values[kk], xLow, nxReq);
    }
  }

  private void checkDistanceInTableNotHole(double[][] values) {
    // Check to see if the upper distance bound is in a bad sample region
    // Check outer distance value
    if (valid(values[zTop][0])
      && invalid(values[zTop][xHigh])) {
      wasExtrapolated = true;
      iDist = 1;
      for (var i = 0; i < (nxReq - 1) / 2; i++) {
        // Shift upper distance bound downward until a valid table
        // entry is found
        --xHigh;
        if (valid(values[zTop][xHigh])) {
          iDist = 0;
          break;
        }
      }
      // Reset lower distance bound to reflect the new upper bound
      xLow = xHigh - nxReq + 1;
    }
    // Check to see if the lower distance bound is in a bad sample
    // region
    else if (invalid(values[zTop][xLow])) {
      wasExtrapolated = true;

      iDist = -1;
      for (var i = 0; i < (nxReq - 1) / 2; i++) {
        // Shift lower distance bound upward until a valid table
        // entry is found
        ++xLow;
        if (valid(values[zTop][xLow])) {
          iDist = 0;
          break;
        }
      }
      // Reset upper distance bound to reflect the new upper bound
      xHigh = xLow + nxReq - 1;
    }
  }

  private void checkOuterDistanceValue(double[][] values) {
    if (invalid(values[zTop][xHigh])) {
      // Case A: Outer distance sample is also in the hole
      wasExtrapolated = true;

      for (var i = 0; i < (nxReq - 1) / 2; i++) {
        // Shift upper distance bound downward until a valid table
        // entry is found
        --xHigh;
        if (valid(values[zTop][xHigh])) {
          break;
        }
      }
      // Reset lower distance bound to reflect the new upper bound
      xLow = xHigh - nxReq + 1;
    } else {
      // Case B: Outer distance sample is valid

      //**** added distance shift up, similar to distance shift down above.
      //**** Not sure why this was'nt done properly here.
      //****  (jrh 12-19-2018)
      for (var i = 0; i < (nxReq - 1) / 2; i++) {
        // Shift upper distance bound downward until a valid table
        // entry is found
        ++xLow;
        if (valid(values[zTop][xLow])) {
          break;
        }
      }
      // Reset higher distance bound to reflect the new lower bound
      xHigh = xLow + nxReq - 1;

      // Use a "safe" value from the distances before the hole
      //TODO: just use next valid value in table?
    }
  }

  private void setDepthRange(double[] depths, double depth) {
    // ------------------------------------------------------------------
    // CASE B: Table contains at least 2 depth samples
    // ------------------------------------------------------------------
    int zLeft = hunt(depths, depth);

    if (zLeft < 0) // depth < min. table depth
    {
      // Check if exactly equal
      if (Math.abs(depth - depths[0]) >= DEPTH_PROXIMITY_TOLERANCE) {
        iDepth--;
      }
      zTop = 0;
      zBottom = nzReq - 1;
    } else if (zLeft >= depths.length - 1) // depth > max. table depth
    {
      iDepth++;
      zTop = depths.length - nzReq;
      zBottom = depths.length - 1;
    } else
    // requested depth within valid range
    {
      zBottom = Math.min(zLeft + (nzReq / 2), depths.length - 1);
      zTop = Math.max(zBottom - nzReq + 1, 0);
      nzReq = zBottom - zTop + 1;
    }
  }

  /**
   * Again, something here...
   */
  private boolean isInHole(double distance, double[] ttcsInHoleDist) {
    return (distance > ttcsInHoleDist[0] && distance < ttcsInHoleDist[1]);
  }

  /**
   * Something here...
   */
  private void setDistInHole(double[] ttcsInHoleDist, double[] distances, double[][] values) {
    var okSoFar = true;
    for (var i = 1; i < distances.length; i++) {
      if (!Double.isNaN(values[0][i - 1]) && Double.isNaN(values[0][i])) {
        ttcsInHoleDist[0] = distances[i - 1];
        okSoFar = false;
      } else if (!okSoFar && !Double.isNaN(values[0][i])) {
        ttcsInHoleDist[1] = distances[i];
        break;
      }
    }
  }

  /**
   * Extract a new array from x that has size elements starting at index first. No range checking is
   * performed!
   *
   * @param x original array
   * @param first index of first element
   * @param size number of elements to extract
   */
  private static double[] extract(double[] x, int first, int size) {
    var xx = new double[size];
    System.arraycopy(x, first, xx, 0, size);
    return xx;
  }

  private static boolean valid(double value) {
    return !invalid(value);
  }

  private static boolean invalid(double value) {
    return Double.isNaN(value);
  }

  private static int hunt(double[] values, double x) {
    if (x == values[values.length - 1]) {
      return values.length - 2;
    }

    int i;
    int bot = -1;
    int top = values.length;
    while (top - bot > 1) {
      i = (top + bot) / 2;
      if (x >= values[i]) {
        bot = i;
      } else {
        top = i;
      }
    }
    return bot;
  }
}
