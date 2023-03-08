import { MILLISECONDS_IN_SECOND, SECONDS_IN_MINUTES } from '@gms/common-util';
import { getStore, withReduxProvider } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { waitForComponentToPaint } from '@gms/ui-state/__tests__/test-util';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import { AcknowledgeWrapper } from '../../../../../src/ts/components/data-acquisition-ui/shared/acknowledge/acknowledge-wrapper';
import type {
  AcknowledgeWrapperProps,
  WithAcknowledgeProps
} from '../../../../../src/ts/components/data-acquisition-ui/shared/acknowledge/types';
import { WithAcknowledge } from '../../../../../src/ts/components/data-acquisition-ui/shared/acknowledge/with-acknowledge';
import { stationAndStationGroupSohStatus } from '../../../../__data__/data-acquisition-ui/soh-overview-data';
import { useQueryStateResult } from '../../../../__data__/test-util-data';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

Date.now = jest.fn().mockReturnValue(() => MILLISECONDS_IN_SECOND * SECONDS_IN_MINUTES);

function flushPromises(): any {
  return new Promise(resolve => {
    setTimeout(resolve, 0);
  });
}

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
jest.setTimeout(60000);

describe('Acknowledge Wrapper', () => {
  it('should be defined', () => {
    expect(AcknowledgeWrapper).toBeDefined();
  });

  class TestClass extends React.PureComponent<Partial<WithAcknowledgeProps>> {
    public render() {
      return <div>Test acknowledge wrapper</div>;
    }
  }

  const TestWithAck = WithAcknowledge(TestClass);
  const store = getStore();

  const Wrapper = withReduxProvider(TestWithAck);

  it('matches a snapshot', () => {
    const { container } = render(<Wrapper />);
    expect(container).toBeDefined();
    expect(container).toMatchSnapshot();
  });

  const sohConfigurationQuery = useQueryStateResult;
  sohConfigurationQuery.data = sohConfiguration;
  const mockAcknowledge = jest.fn().mockReturnValue(new Promise(jest.fn()));
  const mockAckProps: AcknowledgeWrapperProps = {
    sohStatus: {
      loading: false,
      stationAndStationGroupSoh: stationAndStationGroupSohStatus,
      lastUpdated: 10,
      isStale: false
    },
    sohConfigurationQuery
  };

  const ackWrapper = Enzyme.mount(
    <Provider store={store}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <AcknowledgeWrapper {...mockAckProps}>
        <TestClass />
      </AcknowledgeWrapper>
    </Provider>
  );

  it.skip('should call mutation when acknowledgeStationsByName is called', async () => {
    await waitForComponentToPaint(ackWrapper);
    ackWrapper.update();
    ackWrapper.find('TestClass').props().acknowledgeStationsByName(['H05N', 'H06N']);
    expect(mockAcknowledge).toHaveBeenCalledTimes(1);
    expect(mockAcknowledge).toHaveBeenCalledWith({
      variables: { stationNames: ['H05N', 'H06N'] }
    });
    flushPromises();
  });

  it.skip('should log an error when mutation fails', () => {
    const spyError = jest.spyOn(console, 'error').mockImplementation();
    const errorMessage = 'got a failed promise';
    const rejection = Promise.reject(errorMessage);
    const mockReject = jest.fn().mockReturnValueOnce(rejection);
    ackWrapper.setProps({
      acknowledgeSohStatus: mockReject
    });
    ackWrapper.update();
    ackWrapper.find('TestClass').props().acknowledgeStationsByName(['H05N', 'H06N']);
    // await waitForComponentToPaint(ackWrapper);
    expect(spyError).toMatchSnapshot();
    spyError.mockRestore();
  });
});
