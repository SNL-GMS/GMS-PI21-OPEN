import { GMSTheme } from '../../../src/ts/components/charts/victory-themes';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('GMSTheme', () => {
  it('is exported', () => {
    expect(GMSTheme).toBeDefined();
  });
  it('has values', () => {
    expect(GMSTheme).toMatchSnapshot();
  });
});
