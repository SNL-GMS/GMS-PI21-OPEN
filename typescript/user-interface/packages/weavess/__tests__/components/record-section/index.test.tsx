/* eslint-disable @typescript-eslint/no-magic-numbers */
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { RecordSection, workerRpc } from '../../../src/ts/components/record-section-display';

describe('Weavess Record Section', () => {
  it('Record Section to be defined', () => {
    expect(new RecordSection({})).toBeDefined();
    expect(workerRpc).toBeDefined();
  });

  it('Record Section can be shallow rendered', () => {
    const rs = Enzyme.shallow(<RecordSection />);
    expect(rs).toMatchSnapshot();
  });

  it('Record Section kilometersToDegrees', () => {
    const rs = new RecordSection({});
    expect(rs.kilometersToDegrees(100)).toEqual(0.0008993216059187306);
  });

  it('Record Section arrayMin', () => {
    const rs = new RecordSection({});
    expect(rs.arrayMin([3, 4, 5, 100, 4, 5, 2.3])).toEqual(2.3);
  });

  it('Record Section arrayMax', () => {
    const rs = new RecordSection({});
    expect(rs.arrayMax([3, 4, 5, 100, 4, 5, 2.3])).toEqual(100);
  });

  it('Record Section componentDidMount', () => {
    const rs = new RecordSection({});
    const spy = jest.spyOn(rs, 'componentDidMount');
    rs.componentDidMount();
    expect(spy).toBeCalled();
  });

  it('Record Section convertWaveformYToCanvasY', () => {
    const rs = new RecordSection({});
    expect(rs.convertWaveformYToCanvasY([33, 5, 3, 11, 87], 444)).toMatchInlineSnapshot(`
      Object {
        "yArr": Array [
          -52590.4761904762,
          -52857.14285714286,
          -52876.19047619048,
          -52800.00000000001,
          -52076.19047619048,
        ],
        "yMax": -52076.19047619048,
        "yMedian": -52800.00000000001,
        "yMin": -52876.19047619048,
      }
    `);
  });

  it('Record Section addWaveformArray', () => {
    const rs = new RecordSection({});
    const spy = jest.spyOn(rs, 'addWaveformArray');
    rs.addWaveformArray([], false);
    expect(spy).toBeCalled();
  });

  it('Record Section updateSize', () => {
    const rs = new RecordSection({});
    const spy = jest.spyOn(rs, 'updateSize');
    rs.updateSize();
    expect(spy).toBeCalled();
  });

  it('Record Section stopRender', () => {
    const rs = new RecordSection({});
    const spy = jest.spyOn(rs, 'stopRender');
    rs.stopRender();
    expect(spy).toBeCalled();
  });

  it('Record Section resumeRender', () => {
    const rs = new RecordSection({});
    const spy = jest.spyOn(rs, 'resumeRender');
    rs.resumeRender();
    rs.setState({ loaded: true });
    rs.resumeRender();
    expect(spy).toBeCalled();
  });
});
