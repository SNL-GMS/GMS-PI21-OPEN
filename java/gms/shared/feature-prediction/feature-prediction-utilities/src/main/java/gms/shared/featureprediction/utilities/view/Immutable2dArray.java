package gms.shared.featureprediction.utilities.view;

import com.google.common.base.Preconditions;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

/**
 * A generic immutable 2D array class.  All rows in the array must be the same length.
 *
 * @param <T> The class type of the elements in the array.
 */
public class Immutable2dArray<T> {

  private final Class<T> type;
  private final T[][] values;

  private Immutable2dArray(Class<T> type, T[][] values) {
    this.type = type;
    this.values = copyOf(type, values);
  }

  public static <T> Immutable2dArray<T> from(Class<T> type, T[][] values) {
    //must have same column width
    if (0 < values.length) {
      Preconditions.checkArgument(
        Arrays.stream(values).mapToInt(r -> r.length).distinct().limit(2).count() == 1,
        "Expected identical row lengths");
    }

    return new Immutable2dArray<>(type, values);
  }

  public T getValue(int row, int column) {
    Preconditions.checkElementIndex(row, rowCount(), "Row outside of array bounds");
    Preconditions.checkElementIndex(column, columnCount(), "Column outside of array bounds");
    return values[row][column];
  }

  public int rowCount() {
    return values.length;
  }

  public int columnCount() {
    int columnCount;
    if (0 == rowCount()) {
      columnCount = 0;
    } else {
      //works since we validated same column width across all rows
      columnCount = values[0].length;
    }
    return columnCount;
  }

  public T[][] copyOf() {
    return copyOf(type, values);
  }

  // Array.newInstance() is returning the correct type here.  No need to warn.
  @SuppressWarnings("unchecked")
  private static <T> T[][] copyOf(Class<T> type, T[][] values) {
    T[][] copyValues;

    if (0 < values.length) {
      copyValues = (T[][]) Array.newInstance(type, values.length, values[0].length);
      for (var i = 0; i < values.length; i++) {
        T[] valuesRow = values[i];
        int rowLength = valuesRow.length;
        System.arraycopy(valuesRow, 0, copyValues[i], 0, rowLength);
      }
    } else {
      copyValues = (T[][]) Array.newInstance(type, 0, 0);
    }

    return copyValues;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Immutable2dArray)) {
      return false;
    }
    Immutable2dArray<?> that = (Immutable2dArray<?>) o;
    return type.equals(that.type) && Arrays.deepEquals(values, that.values);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(type);
    result = 31 * result + Arrays.deepHashCode(values);
    return result;
  }
}
