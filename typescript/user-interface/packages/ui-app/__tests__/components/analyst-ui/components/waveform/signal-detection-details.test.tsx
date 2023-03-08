import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import { render } from '@testing-library/react';
import React from 'react';

import { SignalDetectionDetails } from '../../../../../src/ts/components/analyst-ui/common/dialogs';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

it('SignalDetectionDetails renders & matches snapshot', () => {
  const { container } = render(
    <SignalDetectionDetails detection={signalDetectionsData[0]} color="red" />
  );
  expect(container).toMatchSnapshot();
});
