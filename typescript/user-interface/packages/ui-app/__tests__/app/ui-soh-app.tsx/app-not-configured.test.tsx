import { Logger } from '@gms/common-util';
import { render } from '@testing-library/react';
import * as React from 'react';

import { App } from '../../../src/ts/app/ui-soh-app/app';

const logger = Logger.create('GMS_LOG_JEST', process.env.GMS_LOG_JEST);

jest.mock('../../../src/ts/components/data-acquisition-ui/components/soh-map', () => {
  return { SOHMap: () => logger.debug('map') };
});

jest.mock('@gms/common-util', () => {
  const actual = jest.requireActual('@gms/common-util');
  return { ...actual, IS_MODE_SOH: false, IS_MODE_IAN: true, IS_MODE_LEGACY: true };
});

describe('SOH App - Not Configured', () => {
  const { container } = render(<App />);
  it('matches a snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
