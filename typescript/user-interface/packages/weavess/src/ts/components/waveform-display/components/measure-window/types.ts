import type { WeavessTypes } from '@gms/weavess-core';

import type { WaveformPanelProps } from '../../types';
import type { WaveformPanel } from '../../waveform-panel';

export interface MeasureWindowProps extends WaveformPanelProps {
  /**
   * The information about the selected measure window
   */
  measureWindowSelection: WeavessTypes.MeasureWindowSelection | undefined;

  /**
   * A function that is passed the underlying WaveformPanel ref to expose the
   * WaveformPanel's public functions and DOM info
   */
  setMeasureWindowRef: (ref: WaveformPanel | null) => void;

  /**
   * The height of the measure window, including the x-axis.
   */
  measureWindowHeightPx: number;
}
