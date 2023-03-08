import { buildPredictedDropdown } from '../../../../../../../../src/ts/components/analyst-ui/components/waveform/components/waveform-controls/predicted-control';

describe('build predicted dropdown', () => {
  it('is defined', () => {
    expect(buildPredictedDropdown).toBeDefined();
  });

  it('Has menu label Hide predicted phases when passed param as false and no currentOpenEventId', () => {
    const shouldShowPredictedPhases = false;
    const setShouldShowPredictedPhases = jest.fn();
    const currentOpenEventId = '';
    const key = 'wfpredicted';
    expect(
      buildPredictedDropdown(
        shouldShowPredictedPhases,
        setShouldShowPredictedPhases,
        currentOpenEventId,
        key
      )
    ).toMatchSnapshot();
  });
});
