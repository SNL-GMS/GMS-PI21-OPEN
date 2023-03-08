import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import { AcknowledgeOverlay } from '../../../../../src/ts/components/data-acquisition-ui/shared/acknowledge/acknowledge-overlay';
import type { AcknowledgeOverlayProps } from '../../../../../src/ts/components/data-acquisition-ui/shared/acknowledge/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

describe('Acknowledge Overlay', () => {
  it('should be defined', () => {
    expect(AcknowledgeOverlay).toBeDefined();
  });

  class TestClass extends React.PureComponent {
    public render() {
      return <div>Test</div>;
    }
  }

  const acknowledgeOverlayProps: AcknowledgeOverlayProps = {
    isOpen: true,
    onClose: jest.fn(),
    stationNames: ['TEST'],
    acknowledgeStationsByName: jest.fn()
  };

  it('matches a snapshot', () => {
    const { container } = render(
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <AcknowledgeOverlay {...acknowledgeOverlayProps}>
          <TestClass />
        </AcknowledgeOverlay>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
