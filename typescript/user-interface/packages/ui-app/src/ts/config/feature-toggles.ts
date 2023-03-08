interface ToggleOptions {
  readonly [id: string]: boolean | ToggleOptions | undefined;
}

export interface FeatureToggles extends ToggleOptions {
  readonly WAVEFORMS: undefined;
  readonly IAN_MAP_MULTI_SELECT: boolean;
}

/**
 * A collection of feature toggles that can be enabled/disabled to turn a feature on or off.
 */
export const FEATURE_TOGGLES: FeatureToggles = {
  WAVEFORMS: undefined,
  IAN_MAP_MULTI_SELECT: true
} as const;
