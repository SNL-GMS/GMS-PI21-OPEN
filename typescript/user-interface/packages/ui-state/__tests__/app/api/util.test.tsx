/* eslint-disable @typescript-eslint/no-magic-numbers */
import { resetApiState } from '../../../src/ts/app/api';

describe('api util', () => {
  it('should reset all apis and the waveform cache', () => {
    const dispatch = jest.fn();

    resetApiState(dispatch);

    expect(dispatch.mock.calls[0][0]).toEqual({
      payload: undefined,
      type: 'eventManagerApi/resetApiState'
    });
    expect(dispatch.mock.calls[1][0]).toEqual({
      payload: undefined,
      type: 'workflowApi/resetApiState'
    });
    expect(dispatch.mock.calls[2][0]).toEqual({
      payload: undefined,
      type: 'ssamControlApi/resetApiState'
    });
    expect(dispatch.mock.calls[3][0]).toEqual({
      payload: undefined,
      type: 'userManagerApi/resetApiState'
    });
    expect(dispatch.mock.calls[4][0]).toEqual({
      payload: undefined,
      type: 'processingConfigurationApi/resetApiState'
    });
    expect(dispatch.mock.calls[5][0]).toEqual({
      payload: undefined,
      type: 'systemMessageDefinitionApi/resetApiState'
    });
    expect(dispatch.mock.calls[6][0]).toEqual({
      payload: undefined,
      type: 'data/clearAll'
    });
  });
});
