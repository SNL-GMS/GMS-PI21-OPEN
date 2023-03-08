import { CommonTypes } from '@gms/common-model';
import { PhaseType } from '@gms/common-model/lib/common/types';
import { sleep } from '@gms/common-util';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import * as React from 'react';
import { act } from 'react-test-renderer';

import { AlignmentMenu } from '../../../../../src/ts/components/analyst-ui/common/dialogs';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');
// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('Alignment Menu', () => {
  it('Renders', () => {
    const wrapper = Enzyme.mount(
      <AlignmentMenu
        alignedOn={AlignWaveformsOn.PREDICTED_PHASE}
        phaseAlignedOn={CommonTypes.PhaseType.LR}
        defaultPhaseAlignment={CommonTypes.PhaseType.P}
        sdPhases={[CommonTypes.PhaseType.LR, CommonTypes.PhaseType.Lg]}
        prioritySdPhases={[CommonTypes.PhaseType.P]}
        onSubmit={() => {
          jest.fn();
        }}
      />
    );
    expect(wrapper.render()).toMatchSnapshot();
    expect(wrapper.instance().getState()).toEqual({
      alignedOn: 'Predicted',
      phaseAlignedOn: 'LR',
      predictedOrObservedVal: 'Predicted'
    });
    wrapper.unmount();
  });

  it('un-mounts', async () => {
    const onSubmit = jest.fn();
    const wrapper = Enzyme.mount(
      <AlignmentMenu
        alignedOn={AlignWaveformsOn.PREDICTED_PHASE}
        phaseAlignedOn={CommonTypes.PhaseType.LR}
        defaultPhaseAlignment={CommonTypes.PhaseType.P}
        sdPhases={[CommonTypes.PhaseType.LR, CommonTypes.PhaseType.Lg]}
        prioritySdPhases={[CommonTypes.PhaseType.P]}
        onSubmit={onSubmit}
      />
    );
    wrapper.instance().onPhaseSelection(PhaseType.Lg);
    await act(async () => {
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      await sleep(2000);
      wrapper.unmount();
    });

    expect(onSubmit).toHaveBeenCalledWith('Predicted', 'Lg');
  });

  it('handles radio on change events', () => {
    const onSubmit = jest.fn();
    const wrapper = Enzyme.mount(
      <AlignmentMenu
        alignedOn={AlignWaveformsOn.PREDICTED_PHASE}
        phaseAlignedOn={CommonTypes.PhaseType.LR}
        defaultPhaseAlignment={CommonTypes.PhaseType.P}
        sdPhases={[CommonTypes.PhaseType.LR, CommonTypes.PhaseType.Lg]}
        prioritySdPhases={[CommonTypes.PhaseType.P]}
        onSubmit={onSubmit}
      />
    );
    const mockEvent: any = { currentTarget: { value: 'time' } };
    wrapper.find('input[data-cy="time"]').first().props().onChange(mockEvent);
    expect(onSubmit).toHaveBeenCalledWith('Time');

    onSubmit.mockClear();

    mockEvent.currentTarget.value = 'phase';

    wrapper.find('[data-cy="phase"]').first().props().onChange(mockEvent);
    expect(onSubmit).toHaveBeenCalledTimes(0);

    wrapper.find('[data-cy="filterable-option-Lg"]').simulate('click');
    expect(onSubmit).toHaveBeenCalledWith('Predicted', 'Lg');
    expect(wrapper.instance().getState()).toEqual({
      alignedOn: 'Predicted',
      phaseAlignedOn: 'Lg',
      predictedOrObservedVal: 'Predicted'
    });
  });
});
