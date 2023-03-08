import { Logger } from '@gms/common-util';
import { act, render } from '@testing-library/react';
import * as React from 'react';

import { App } from '../../../src/ts/app/ui-soh-app/app';

const logger = Logger.create('GMS_LOG_JEST', process.env.GMS_LOG_JEST);

jest.mock('../../../src/ts/components/data-acquisition-ui/components/soh-map', () => {
  return { SOHMap: () => logger.debug('map') };
});

jest.mock('@gms/common-util', () => {
  const actual = jest.requireActual('@gms/common-util');
  return {
    ...actual,
    IS_MODE_SOH: true,
    IS_MODE_IAN: false,
    IS_MODE_LEGACY: false
  };
});

describe('SOH App - Not Logged In', () => {
  // TODO Unskip and fix
  it('matches a snapshot', () => {
    act(() => {
      const { container } = render(<App />);
      expect(container).toMatchSnapshot();
    });
  });
});
