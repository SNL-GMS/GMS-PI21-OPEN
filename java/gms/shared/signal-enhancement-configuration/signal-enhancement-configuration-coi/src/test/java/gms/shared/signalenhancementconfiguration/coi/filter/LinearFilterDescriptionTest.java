package gms.shared.signalenhancementconfiguration.coi.filter;

import gms.shared.signalenhancementconfiguration.coi.types.FilterType;
import gms.shared.signalenhancementconfiguration.coi.types.PassBandType;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LinearFilterDescriptionTest {

  private final String comments = "test comment";

  @Test
  void serialization() {
    TestUtilities.assertSerializes(FilterFixtures.LINEAR_HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION, FilterDescription.class);
  }

  @Test
  void serializationErrorForFilterType() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, this::executeErrorFilterType);

    Assertions.assertEquals("Linear filter are only of the FIR_HAMMING type or the IRR_BUTTERWORTH type", thrown.getMessage());
  }

  @Test
  void serializationErrorForFilterOrder() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, this::executeErrorForFilterOrder);

    Assertions.assertEquals("Filter order must be at greater or equal than one", thrown.getMessage());
  }

  @Test
  void checkForFilterBandPassReject() {
    LinearFilterDescription linearFilterDescription = LinearFilterDescription.from(Optional.of(comments), true, FilterType.FIR_HAMMING, Optional.of(3.0), Optional.of(3.0), 1, false, PassBandType.BAND_REJECT, Optional.of(FilterFixtures.LINEAR_FILTER_PARAMETERS));
    Assertions.assertTrue(linearFilterDescription.getHighFrequency().isEmpty());
    Assertions.assertTrue(linearFilterDescription.getLowFrequency().isEmpty());
  }

  @Test
  void serializationErrorForFilterLowFrequencies() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, this::executeErrorForFilterLowFrequencies);

    Assertions.assertEquals("Frequency values must be positive", thrown.getMessage());
  }

  @Test
  void serializationErrorForFilterHighFrequencies() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, this::executeErrorForFilterHighFrequencies);

    Assertions.assertEquals("Frequency values must be positive", thrown.getMessage());
  }

  @Test
  void causalIsFalseWhenZeroPhaseIsTrue() {
    LinearFilterDescription linearFilterDescription = LinearFilterDescription.from(Optional.of(comments), true, FilterType.FIR_HAMMING, Optional.of(3.0), Optional.of(3.0), 1, true, PassBandType.BAND_REJECT, Optional.of(FilterFixtures.LINEAR_FILTER_PARAMETERS));
    Assertions.assertFalse(linearFilterDescription.isCausal());
  }

  @Test
  void whenPassBandIsNotGivenErrorOccurs() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, this::executeErrorBandIsNotGiven);

    Assertions.assertEquals("Filter Pass Band type must be provided", thrown.getMessage());
  }

  private void executeErrorFilterType() {
    LinearFilterDescription.from(Optional.of(comments), true, FilterType.CASCADE, Optional.of(3.0), Optional.of(3.0), 1, false, PassBandType.BAND_PASS, Optional.of(FilterFixtures.LINEAR_FILTER_PARAMETERS));
  }

  private void executeErrorForFilterOrder() {
    LinearFilterDescription.from(Optional.of(comments), true, FilterType.FIR_HAMMING, Optional.of(3.0), Optional.of(3.0), 0, false, PassBandType.BAND_PASS, Optional.of(FilterFixtures.LINEAR_FILTER_PARAMETERS));
  }

  private void executeErrorForFilterLowFrequencies() {
    LinearFilterDescription.from(Optional.of(comments), true, FilterType.FIR_HAMMING, Optional.of(-1.0), Optional.of(3.0), 1, false, PassBandType.BAND_PASS, Optional.of(FilterFixtures.LINEAR_FILTER_PARAMETERS));
  }

  private void executeErrorForFilterHighFrequencies() {
    LinearFilterDescription.from(Optional.of(comments), true, FilterType.FIR_HAMMING, Optional.of(3.0), Optional.of(-3.0), 1, false, PassBandType.BAND_PASS, Optional.of(FilterFixtures.LINEAR_FILTER_PARAMETERS));
  }

  private void executeErrorBandIsNotGiven() {
    LinearFilterDescription.from(Optional.of(comments), true, FilterType.FIR_HAMMING, Optional.of(3.0), Optional.of(-3.0), 1, false, null, Optional.of(FilterFixtures.LINEAR_FILTER_PARAMETERS));
  }
}