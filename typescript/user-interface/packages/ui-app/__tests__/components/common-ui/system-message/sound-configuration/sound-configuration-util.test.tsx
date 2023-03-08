import { filterRowsByType } from '../../../../../src/ts/components/common-ui/components/system-message/sound-configuration/sound-configuration-util';
import type { SoundConfigurationRow } from '../../../../../src/ts/components/common-ui/components/system-message/sound-configuration/types';

describe('Sound Configuration Util', () => {
  const row: SoundConfigurationRow = {
    category: 'SOH',
    hasNotificationStatusError: false,
    id: '1',
    message: 'hello',
    severity: 'CRITICAL',
    sound: {
      availableSounds: { 'test.mp3': 'test.mp3' },
      onSelect: jest.fn(),
      selectedSound: 'test.mp3'
    },
    subcategory: 'USER'
  };
  const rows = [row, row, row];

  it('should filter no rows when everything matches', () => {
    const result = filterRowsByType('CRITICAL', rows, 'SOH', 'USER');

    expect(result).toHaveLength(rows.length);
  });

  it('should filter all rows when nothing matches severity', () => {
    const result = filterRowsByType('WARNING', rows, 'SOH', 'USER');

    expect(result).toHaveLength(0);
  });
});
