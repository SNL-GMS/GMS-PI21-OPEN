import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import { EventsToolbar } from '../../../../../src/ts/components/analyst-ui/components/events/events-toolbar';
import { BaseDisplay } from '../../../../../src/ts/components/common-ui/components/base-display';
import { glContainer } from '../workflow/gl-container';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

describe('event list Toolbar', () => {
  const storeDefault = getStore();

  it('is exported', () => {
    expect(EventsToolbar).toBeDefined();
  });

  it('matches snapshot', () => {
    const { container } = render(
      <Provider store={storeDefault}>
        <BaseDisplay glContainer={glContainer}>
          <EventsToolbar
            completeEventsCount={0}
            remainingEventsCount={0}
            rejectedEventsCount={0}
            conflictsEventsCount={0}
            disableMarkSelectedComplete={false}
            handleMarkSelectedComplete={jest.fn()}
          />
        </BaseDisplay>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
