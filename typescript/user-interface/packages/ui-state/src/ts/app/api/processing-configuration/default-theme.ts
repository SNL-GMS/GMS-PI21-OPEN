import { Colors } from '@blueprintjs/core';
import type { ConfigurationTypes } from '@gms/common-model';

export const defaultTheme: ConfigurationTypes.UITheme = {
  name: 'GMS Default (dark)',
  isDarkMode: true,
  display: {
    edgeEventOpacity: 0.35,
    edgeSDOpacity: 0.2,
    predictionSDOpacity: 0.1
  },
  colors: {
    gmsMain: '#f5f8fa',
    gmsMainInverted: '#10161a',
    gmsBackground: '#182026',
    gmsSelection: '#1589d1',
    gmsTableSelection: '#f5f8fa',
    mapVisibleStation: '#D9822B',
    mapStationDefault: '#6F6E74',
    waveformDimPercent: 0.75,
    waveformRaw: Colors.COBALT4,
    waveformFilterLabel: Colors.LIGHT_GRAY5,
    unassociatedSDColor: Colors.RED3,
    openEventSDColor: Colors.ORANGE3,
    completeEventSDColor: Colors.FOREST5,
    otherEventSDColor: Colors.WHITE,
    predictionSDColor: '#C58C1B'
  }
};
