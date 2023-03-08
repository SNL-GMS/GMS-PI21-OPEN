import { CommonTypes, LegacyEventTypes } from '@gms/common-model';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';

import { semanticColors } from '~scss-config/color-preferences';

import { QcMaskCategory } from './system-config';

export interface MaskDisplayFilter {
  color: string;
  visible: boolean;
  name: string;
}

export interface QcMaskDisplayFilters {
  ANALYST_DEFINED: MaskDisplayFilter;
  CHANNEL_PROCESSING: MaskDisplayFilter;
  DATA_AUTHENTICATION: MaskDisplayFilter;
  REJECTED: MaskDisplayFilter;
  STATION_SOH: MaskDisplayFilter;
  WAVEFORM_QUALITY: MaskDisplayFilter;
}
export interface AnalystWorkSpaceColorPreferences {
  maskDisplayFilters: QcMaskDisplayFilters;
}

export const analystWorkspaceColors: AnalystWorkSpaceColorPreferences = {
  maskDisplayFilters: {
    ANALYST_DEFINED: {
      color: semanticColors.qcAnalystDefined,
      visible: true,
      name: QcMaskCategory.ANALYST_DEFINED
    },
    CHANNEL_PROCESSING: {
      color: semanticColors.qcChannelProcessing,
      visible: true,
      name: QcMaskCategory.CHANNEL_PROCESSING
    },
    DATA_AUTHENTICATION: {
      color: semanticColors.qcDataAuthentication,
      visible: true,
      name: QcMaskCategory.DATA_AUTHENTICATION
    },
    REJECTED: {
      color: semanticColors.qcRejected,
      visible: false,
      name: QcMaskCategory.REJECTED
    },
    STATION_SOH: {
      color: semanticColors.qcStationSOH,
      visible: true,
      name: QcMaskCategory.STATION_SOH
    },
    WAVEFORM_QUALITY: {
      color: semanticColors.qcWaveformQuality,
      visible: true,
      name: QcMaskCategory.WAVEFORM_QUALITY
    }
  }
};

/** Initial Mag type */
const initialMagType: AnalystWorkspaceTypes.DisplayedMagnitudeTypes = {};
initialMagType[LegacyEventTypes.MagnitudeType.MB] = true;
initialMagType[LegacyEventTypes.MagnitudeType.MBMLE] = true;
initialMagType[LegacyEventTypes.MagnitudeType.MS] = true;
initialMagType[LegacyEventTypes.MagnitudeType.MSMLE] = true;
Object.freeze(initialMagType);
export interface UserPreferences {
  azimuthSlowness: {
    defaultLead: number;
    defaultLength: number;
    defaultStepSize: number;
    // Minimum height and width of fk rendering
    minFkLengthPx: number;
    // Maximum height and width of fk rendering
    maxFkLengthPx: number;
  };
  initialMagType: AnalystWorkspaceTypes.DisplayedMagnitudeTypes;
  colors: AnalystWorkSpaceColorPreferences;
  map: {
    icons: {
      event: string;
      eventScale: number;
      station: string;
      stationScale: number;
      scaleFactor: number;
      displayDistance: number;
      pixelOffset: number;
    };
    widths: {
      unselectedSignalDetection: number;
      selectedSignalDetection: number;
    };
    defaultTo3D: boolean;
  };
  defaultSignalDetectionPhase: CommonTypes.PhaseType;
  signalDetectionList: {
    autoFilter: boolean;
    showIds: boolean;
  };
  eventList: {
    showIds: boolean;
    showAssocIds: boolean;
  };
  location: {
    preferredLocationSolutionRestraintOrder: LegacyEventTypes.DepthRestraintType[];
  };
  list: {
    minWidthPx: number;
    widthOfTableMarginsPx: number;
  };
  distanceUnits: CommonTypes.DistanceUnits;
}
export const userPreferences: UserPreferences = {
  azimuthSlowness: {
    defaultLead: 1,
    defaultLength: 4,
    defaultStepSize: 5,
    // Minimum height and width of fk rendering
    minFkLengthPx: 265,
    // Maximum height and width of fk rendering
    maxFkLengthPx: 500
  },
  colors: analystWorkspaceColors,
  initialMagType,
  map: {
    icons: {
      event: 'circle-transition.png',
      eventScale: 0.07,
      station: 'outlined-triangle.png',
      stationScale: 0.12,
      scaleFactor: 1.5,
      displayDistance: 1e6,
      pixelOffset: 15
    },
    widths: {
      unselectedSignalDetection: 1,
      selectedSignalDetection: 3
    },
    defaultTo3D: false
  },
  defaultSignalDetectionPhase: CommonTypes.PhaseType.P,
  signalDetectionList: {
    autoFilter: true,
    showIds: true
  },
  eventList: {
    showIds: true,
    showAssocIds: false
  },
  location: {
    preferredLocationSolutionRestraintOrder: [
      LegacyEventTypes.DepthRestraintType.UNRESTRAINED,
      LegacyEventTypes.DepthRestraintType.FIXED_AT_SURFACE,
      LegacyEventTypes.DepthRestraintType.FIXED_AT_DEPTH
    ]
  },
  list: {
    minWidthPx: 60,
    widthOfTableMarginsPx: 16
  },
  distanceUnits: CommonTypes.DistanceUnits.degrees
};
