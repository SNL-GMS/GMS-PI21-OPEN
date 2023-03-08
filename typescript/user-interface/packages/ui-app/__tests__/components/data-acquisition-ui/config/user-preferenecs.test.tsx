import { dataAcquisitionUserPreferences } from '../../../../src/ts/components/data-acquisition-ui/config/user-preferences';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('User Preferences Config', () => {
  it('is exported', () => {
    expect(dataAcquisitionUserPreferences).toBeDefined();
  });

  it('is config defined', () => {
    expect(JSON.stringify(dataAcquisitionUserPreferences)).toMatchSnapshot();
  });
});
