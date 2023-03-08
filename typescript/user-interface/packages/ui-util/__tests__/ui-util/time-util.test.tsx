import { TimePrecision } from '@blueprintjs/datetime';
import {
  DATE_FORMAT,
  DATE_TIME_FORMAT,
  DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  DATE_TIME_FORMAT_WITH_SECOND_PRECISION
} from '@gms/common-util';

import { convertTimeFormatToTimePrecision } from '../../src/ts/ui-util/time-util';

describe('Time Utils', () => {
  test('convertTimeFormatToTimePrecision', () => {
    expect(
      convertTimeFormatToTimePrecision(DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
    ).toEqual(TimePrecision.MILLISECOND);
    expect(convertTimeFormatToTimePrecision(DATE_TIME_FORMAT_WITH_SECOND_PRECISION)).toEqual(
      TimePrecision.SECOND
    );
    expect(convertTimeFormatToTimePrecision(DATE_TIME_FORMAT)).toEqual(TimePrecision.MINUTE);
    expect(convertTimeFormatToTimePrecision(DATE_FORMAT)).toEqual(undefined);
  });
});
