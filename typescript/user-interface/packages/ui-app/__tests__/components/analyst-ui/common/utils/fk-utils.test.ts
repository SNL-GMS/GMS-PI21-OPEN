import type { FkTypes } from '@gms/common-model';

import { frequencyBandToString } from '../../../../../src/ts/components/analyst-ui/common/utils/fk-utils';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

/**
 * Tests the ability to check if the peak trough is in warning
 */
describe('frequencyBandToString', () => {
  test('correctly creates frequency band string', () => {
    const band: FkTypes.FrequencyBand = {
      maxFrequencyHz: 5,
      minFrequencyHz: 1
    };
    const testString = '1 - 5 Hz';
    expect(frequencyBandToString(band)).toEqual(testString);
  });
});
