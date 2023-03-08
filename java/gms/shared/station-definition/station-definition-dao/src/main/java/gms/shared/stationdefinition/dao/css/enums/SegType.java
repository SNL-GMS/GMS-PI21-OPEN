package gms.shared.stationdefinition.dao.css.enums;


/**
 * Segment type. This column indicates if a waveform is:
 * <ul>
 *   <li>o (original)</li>
 *   <li>v (virtual)</li>
 *   <li>s (segmented)</li>
 *   <li>d (duplicate)</li>
 *   <li>c (calibration pulse)</li>
 *   <li>f (flat non-zero segments)</li>
 *   <li>g (glitch)</li>
 *   <li>A (acceleration)</li>
 *   <li>V (velocity)</li>
 *   <li>D (deplacement)</li>
 *   <li>n (no data; at most some bit noise; essentially a dead channel)</li>
 *   <li>t (step)</li>
 *   <li>u (under-resolved; data are live but largest signals have at most a few bits of resolution)</li>
 *   <li>x (bad data; unknown serious instrument malfunction)</li>
 * </ul>
 */
public enum SegType {

  NA("-"),
  ORIGINAL("o"),
  VIRTUAL("v"),
  SEGMENTED("s"),
  DUPLICATE("d"),
  CALIBRATION_PULSE("c"),
  FLAT("f"),
  GLITCH("g"),
  ACCELERATION("A"),
  VELOCITY("V"),
  DEPLACEMENT("D"),
  NO_DATA("n"),
  STEP("t"),
  UNDER_RESOLVED("u"),
  BAD_DATA("x");

  private final String name;

  SegType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
