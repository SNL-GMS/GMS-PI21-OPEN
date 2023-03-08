import { Logger } from '@gms/common-util';

import { glContextData } from '../../../src/ts/app/ui-ian-app/golden-layout-config';

const logger = Logger.create('GMS_LOG_JEST', process.env.GMS_LOG_JEST);

jest.mock('../../../src/ts/components/analyst-ui/components', () => {
  return { IANMap: () => logger.debug('hi') };
});

jest.mock('@gms/common-util', () => {
  const actual = jest.requireActual('@gms/common-util');
  return {
    ...actual,
    IS_MODE_SOH: false,
    IS_MODE_IAN: true
  };
});

describe('Root IAN app', () => {
  it('matches a snapshot', () => {
    expect(glContextData()).toMatchSnapshot();
  });
});
