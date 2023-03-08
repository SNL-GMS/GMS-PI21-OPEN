/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { CreateRecordSectionLineParams } from '../../src/ts/workers/create-record-section-line';
import { createRecordSectionPositionBuffer } from '../../src/ts/workers/create-record-section-line';

describe('Create Record Section Line', () => {
  it('Create Record Section Line to be defined', () => {
    expect(createRecordSectionPositionBuffer).toBeDefined();
  });

  it('Create Record Section Line called', () => {
    const params: CreateRecordSectionLineParams = {
      cameraXRange: 100,
      defaultCameraLeft: 50,
      distance: 80,
      data: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
    };
    expect(createRecordSectionPositionBuffer(params)).toMatchInlineSnapshot(`
      Float32Array [
        50,
        35599.85546875,
        0,
        57.14285659790039,
        35657,
        0,
        64.28571319580078,
        35714.140625,
        0,
        71.42857360839844,
        35771.28515625,
        0,
        78.57142639160156,
        35828.42578125,
        0,
        85.71428680419922,
        35885.5703125,
        0,
        92.85713958740234,
        35942.71484375,
        0,
        100,
        35999.85546875,
        0,
        107.14286041259766,
        36057,
        0,
        114.28571319580078,
        36114.140625,
        0,
        121.42857360839844,
        36171.28515625,
        0,
        128.57142639160156,
        36228.42578125,
        0,
        135.7142791748047,
        36285.5703125,
        0,
        142.85714721679688,
        36342.71484375,
        0,
        150,
        36399.85546875,
        0,
      ]
    `);
  });
});
