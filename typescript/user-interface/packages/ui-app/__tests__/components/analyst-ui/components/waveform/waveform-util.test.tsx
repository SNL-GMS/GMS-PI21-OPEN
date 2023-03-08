import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';

import { getChannelLabelAndToolTipFromSignalDetections } from '../../../../../src/ts/components/analyst-ui/components/waveform/utils';

describe('Waveform utils', () => {
  describe('getChannelLabelAndToolTipFromSignalDetections', () => {
    test('same channel names', () => {
      signalDetectionsData[0].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ/beam,fk,coherent',
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ/beam,fk,coherent',
        effectiveAt: 0
      };
      signalDetectionsData[2].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ/beam,fk,coherent',
        effectiveAt: 0
      };
      const res = getChannelLabelAndToolTipFromSignalDetections(signalDetectionsData);
      expect(res.channelLabel).toMatch('beam.SHZ');
      expect(res.tooltip).toBeUndefined();
    });

    test('mixed channel orientations', () => {
      signalDetectionsData[0].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ/beam,fk,coherent',
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.BHZ/beam,fk,coherent',
        effectiveAt: 0
      };
      signalDetectionsData[2].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.BHZ/beam,fk,coherent',
        effectiveAt: 0
      };
      const res = getChannelLabelAndToolTipFromSignalDetections(signalDetectionsData);
      expect(res.channelLabel).toMatch('beam.*');
      expect(res.tooltip).toMatch('Multiple channels');
    });

    test('mixed beams', () => {
      signalDetectionsData[0].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ/beam,fk,coherent',
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ/beam,event,coherent',
        effectiveAt: 0
      };
      signalDetectionsData[2].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ/beam,detection,coherent',
        effectiveAt: 0
      };
      const res = getChannelLabelAndToolTipFromSignalDetections(signalDetectionsData);
      expect(res.channelLabel).toMatch('*.SHZ');
      expect(res.tooltip).toMatch('Multiple beam types');
    });

    test('mixed beams and channel orientations', () => {
      signalDetectionsData[0].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ/beam,event,coherent',
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.BHZ/beam,fk,coherent',
        effectiveAt: 0
      };
      const res = getChannelLabelAndToolTipFromSignalDetections(signalDetectionsData);
      expect(res.channelLabel).toMatch('*');
      expect(res.tooltip).toMatch('Multiple beam types and channels');
    });

    test('missing channel name', () => {
      signalDetectionsData[0].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: undefined,
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: undefined,
        effectiveAt: 0
      };
      signalDetectionsData[2].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: undefined,
        effectiveAt: 0
      };
      const res = getChannelLabelAndToolTipFromSignalDetections(signalDetectionsData);
      expect(res.channelLabel).toMatch('');
      expect(res.tooltip).toBeUndefined();
    });

    test('bad channel name', () => {
      signalDetectionsData[0].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: '',
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'foo',
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'bar',
        effectiveAt: 0
      };
      const res = getChannelLabelAndToolTipFromSignalDetections(signalDetectionsData);
      expect(res.channelLabel).toMatch('');
      expect(res.tooltip).toBeUndefined();
    });

    test('not same station throws error', () => {
      signalDetectionsData[0].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ASAR.beam.SHZ',
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'AAK.beam.SHZ',
        effectiveAt: 0
      };
      signalDetectionsData[2].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ARCES.beam.SHZ',
        effectiveAt: 0
      };
      expect(() => {
        getChannelLabelAndToolTipFromSignalDetections(signalDetectionsData);
      }).toThrow();
    });

    test('not 3 channel elements', () => {
      signalDetectionsData[0].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ele1.ele2.ele3.ele4',
        effectiveAt: 0
      };
      signalDetectionsData[1].signalDetectionHypotheses[0].featureMeasurements[0].channel = {
        name: 'ele1.ele2.ele3.ele4',
        effectiveAt: 0
      };
      const res = getChannelLabelAndToolTipFromSignalDetections(signalDetectionsData);
      expect(res.channelLabel).toMatch('');
      expect(res.tooltip).toBeUndefined();
    });

    test('null signal detection list', () => {
      const res = getChannelLabelAndToolTipFromSignalDetections(null);
      expect(res.channelLabel).toMatch('');
      expect(res.tooltip).toBeUndefined();
    });

    test('empty signal detection list', () => {
      const res = getChannelLabelAndToolTipFromSignalDetections([]);
      expect(res.channelLabel).toMatch('');
      expect(res.tooltip).toBeUndefined();
    });
  });
});
