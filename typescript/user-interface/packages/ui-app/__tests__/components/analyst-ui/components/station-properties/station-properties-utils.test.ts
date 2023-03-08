// eslint-disable @typescript-eslint/no-magic-numbers
import { ChannelTypes } from '@gms/common-model';

import {
  formatTimeShift,
  getChannelDataTypeForDisplay,
  getChannelGroupTypeForDisplay
} from '../../../../../src/ts/components/analyst-ui/components/station-properties/station-properties-utils';
import { messageConfig } from '../../../../../src/ts/components/analyst-ui/config/message-config';

describe('Station Properties Utils', () => {
  const expectedResultForInvalidInput = messageConfig.invalidCellText;

  describe('Cell value getter/helper functions', () => {
    describe('formatTimeShift', () => {
      test('returns Unknown when given null input', () => {
        const result = formatTimeShift(null);
        expect(result).toEqual(expectedResultForInvalidInput);
      });
      test('returns Unknown when given undefined input', () => {
        const result = formatTimeShift(undefined);
        expect(result).toEqual(expectedResultForInvalidInput);
      });
      test('returns Unknown when given input string with no digits', () => {
        const result = formatTimeShift('oetnuidh oi oeui');
        expect(result).toEqual(expectedResultForInvalidInput);

        const result2 = formatTimeShift('eou');
        expect(result2).toEqual(expectedResultForInvalidInput);

        const result3 = formatTimeShift('@#$%@#');
        expect(result3).toEqual(expectedResultForInvalidInput);

        const result4 = formatTimeShift('@#$%eoui$%^');
        expect(result4).toEqual(expectedResultForInvalidInput);
      });
      test('returns only digits when given a mixed string', () => {
        const expectedResult = '2';
        const result: string = formatTimeShift('PT2S');
        expect(result).toEqual(expectedResult);

        const result2: string = formatTimeShift('P$T2@%S');
        expect(result2).toEqual(expectedResult);

        const result3: string = formatTimeShift('P$ T 2@ % S');
        expect(result3).toEqual(expectedResult);
      });

      test('returns only the first contiguous digits when given "32t234t234t5"', () => {
        const expectedResult = '32';
        const result: string = formatTimeShift('32t234t234t5');
        expect(result).toEqual(expectedResult);
      });
      test('returns number string when given input integer in a string', () => {
        const integer = Math.floor(Math.random() * Number.MAX_SAFE_INTEGER).toString();
        const expectedResult = `${integer}`;
        const result = formatTimeShift(integer);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getChannelGroupTypeForDisplay', () => {
      test('returns correct entry for valid inputs as strings', () => {
        const expectedOutputPG = 'Processing Group';
        const expectedOutputSG = 'Site Group';

        const resultPG = getChannelGroupTypeForDisplay('PROCESSING_GROUP');
        const resultSG = getChannelGroupTypeForDisplay('SITE_GROUP');

        expect(resultPG).toEqual(expectedOutputPG);
        expect(resultSG).toEqual(expectedOutputSG);
      });

      test('returns correct entry for valid inputs', () => {
        const expectedOutputPG = 'Processing Group';
        const expectedOutputSG = 'Site Group';

        const resultPG = getChannelGroupTypeForDisplay(
          ChannelTypes.ChannelGroupType.PROCESSING_GROUP
        );
        const resultSG = getChannelGroupTypeForDisplay(ChannelTypes.ChannelGroupType.SITE_GROUP);

        expect(resultPG).toEqual(expectedOutputPG);
        expect(resultSG).toEqual(expectedOutputSG);
      });

      test('returns Unknown for invalid string inputs', () => {
        const result1 = getChannelGroupTypeForDisplay('eoui');
        const result2 = getChannelGroupTypeForDisplay('SITE GROUP');
        const result3 = getChannelGroupTypeForDisplay('site_group');

        expect(result1).toEqual(expectedResultForInvalidInput);
        expect(result2).toEqual(expectedResultForInvalidInput);
        expect(result3).toEqual(expectedResultForInvalidInput);
      });

      test('returns Unknown for null input', () => {
        const result = getChannelGroupTypeForDisplay(null);
        expect(result).toEqual(expectedResultForInvalidInput);
      });

      test('returns Unknown for undefined input', () => {
        const result = getChannelGroupTypeForDisplay(undefined);
        expect(result).toEqual(expectedResultForInvalidInput);
      });

      test('returns Unknown for empty input', () => {
        const result = getChannelGroupTypeForDisplay('');
        expect(result).toEqual(expectedResultForInvalidInput);
      });
    });

    describe('getChannelDataTypeForDisplay', () => {
      test('returns correct entry for valid inputs as strings', () => {
        const expectedOutputS = 'Seismic';
        const expectedOutputHA = 'Hydroacoustic';

        const resultS = getChannelDataTypeForDisplay('SEISMIC');
        const resultHA = getChannelDataTypeForDisplay('HYDROACOUSTIC');

        expect(resultS).toEqual(expectedOutputS);
        expect(resultHA).toEqual(expectedOutputHA);
      });

      test('returns correct entry for valid inputs', () => {
        const expectedOutputIS = 'Infrasound';
        const expectedOutputW = 'Weather';

        const resultIS = getChannelDataTypeForDisplay(ChannelTypes.ChannelDataType.INFRASOUND);
        const resultW = getChannelDataTypeForDisplay(ChannelTypes.ChannelDataType.WEATHER);

        expect(resultIS).toEqual(expectedOutputIS);
        expect(resultW).toEqual(expectedOutputW);
      });

      test('returns Unknown for invalid string inputs', () => {
        const result1 = getChannelDataTypeForDisplay('eoui');
        const result2 = getChannelDataTypeForDisplay('infrasound');
        const result3 = getChannelDataTypeForDisplay('DIAGNOSTIC SOH');

        expect(result1).toEqual(expectedResultForInvalidInput);
        expect(result2).toEqual(expectedResultForInvalidInput);
        expect(result3).toEqual(expectedResultForInvalidInput);
      });

      test('returns Unknown for null input', () => {
        const result = getChannelDataTypeForDisplay(null);
        expect(result).toEqual(expectedResultForInvalidInput);
      });

      test('returns Unknown for undefined input', () => {
        const result = getChannelDataTypeForDisplay(undefined);
        expect(result).toEqual(expectedResultForInvalidInput);
      });

      test('returns Unknown for empty input', () => {
        const result = getChannelDataTypeForDisplay('');
        expect(result).toEqual(expectedResultForInvalidInput);
      });
    });
  });
});
