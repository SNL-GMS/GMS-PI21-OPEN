import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import type { AcknowledgeFormProps } from '../../../../../src/ts/components/data-acquisition-ui/shared/acknowledge/acknowledge-form';
import { AcknowledgeForm } from '../../../../../src/ts/components/data-acquisition-ui/shared/acknowledge/acknowledge-form';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

describe('Acknowledge Form', () => {
  it('should be defined', () => {
    expect(AcknowledgeForm).toBeDefined();
  });

  class TestClass extends React.PureComponent {
    public render() {
      return <div>Test</div>;
    }
  }

  const acknowledgeFormProps: AcknowledgeFormProps = {
    classNames: 'test-class',
    onClose: jest.fn(),
    stationNames: ['TEST'],
    acknowledgeStationsByName: jest.fn()
  };

  it('matches a snapshot', () => {
    const { container } = render(
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <AcknowledgeForm {...acknowledgeFormProps}>
          <TestClass />
        </AcknowledgeForm>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
