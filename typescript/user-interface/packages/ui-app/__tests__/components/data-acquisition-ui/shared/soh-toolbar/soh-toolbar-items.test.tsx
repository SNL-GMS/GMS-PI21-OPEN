import {
  makeLagSortingDropdown,
  makeMissingSortingDropdown,
  SOHLagOptions,
  SOHMissingOptions
} from '../../../../../src/ts/components/data-acquisition-ui/shared/toolbars/soh-toolbar-items';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Soh toolbar items', () => {
  it('should be defined', () => {
    expect(SOHLagOptions).toBeDefined();
    expect(SOHMissingOptions).toBeDefined();
    expect(makeLagSortingDropdown).toBeDefined();
    expect(makeMissingSortingDropdown).toBeDefined();
  });

  it('function makeLagSortingDropdown should create a label 01', () => {
    expect(makeLagSortingDropdown(SOHLagOptions.CHANNEL_FIRST, jest.fn())).toMatchSnapshot();
  });

  it('function makeMissingSortingDropdown should create a label 02', () => {
    expect(
      makeMissingSortingDropdown(SOHMissingOptions.CHANNEL_FIRST, jest.fn())
    ).toMatchSnapshot();
  });
});
