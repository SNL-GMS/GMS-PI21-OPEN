package gms.core.performancemonitoring.soh.control;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;

import java.time.Instant;


/**
 * Contains a {@link WaveformSummary} and the time at which its associated waveform was received by
 * the system.
 */
@AutoValue
abstract class WaveformSummaryAndReceptionTime {


  abstract WaveformSummary getWaveformSummary();

  abstract Instant getReceptionTime();


  /**
   * Creates a new WaveformSummaryAndReceptionTime from a {@link WaveformSummary} and {@link
   * Instant} reception time.
   *
   * @param waveformSummary The WaveformSummary associated with the provided receptionTime.
   * @param receptionTime The receptionTime associated with the provided WaveformSummary.
   * @return New WaveformSummaryAndReceptionTime.
   */
  static WaveformSummaryAndReceptionTime create(WaveformSummary waveformSummary,
    Instant receptionTime) {

    return new AutoValue_WaveformSummaryAndReceptionTime(waveformSummary, receptionTime);
  }
}
