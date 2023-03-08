import { CellImage } from '../../../../../src/ts/components/data-acquisition-ui/shared/table/cell-image';

describe('Cell Image', () => {
  it('should be defined', () => {
    expect(CellImage).toBeDefined();
  });

  it('can be created with as not a background', () => {
    const cell = new CellImage(0, 'test', false);
    expect(cell.getElement()).toBeDefined();
  });

  it('can be created as a background', () => {
    const cell = new CellImage(0, 'test', true);
    expect(cell.getElement()).toBeDefined();
  });
});
