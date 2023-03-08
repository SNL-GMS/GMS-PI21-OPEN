import type { StationTypes, WaveformTypes } from '@gms/common-model';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import cloneDeep from 'lodash/cloneDeep';
import uniq from 'lodash/uniq';

import { KeyDirection } from '~analyst-ui/components/waveform/types';
import { systemConfig } from '~analyst-ui/config';

import { createUnfilteredWaveformFilter } from './instance-of-util';

/**
 * Returns the waveform for the provided mode.
 *
 * @param mode the mode
 * @param sampleRate the sampleRate of the channel
 * @param defaultWaveformFilters default waveform filters
 *
 * @returns filter of type WaveformFilter
 */
export function getWaveformFilterForMode(
  mode: AnalystWorkspaceTypes.WaveformDisplayMode,
  sampleRate: number,
  defaultWaveformFilters: WaveformTypes.WaveformFilter[]
): WaveformTypes.WaveformFilter {
  let waveformFilter: WaveformTypes.WaveformFilter;
  if (mode === AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT) {
    waveformFilter = defaultWaveformFilters.find(
      filter =>
        filter.filterType.includes(systemConfig.measurementMode.amplitudeFilter.filterType) &&
        filter.filterPassBandType.includes(
          systemConfig.measurementMode.amplitudeFilter.filterPassBandType
        ) &&
        filter.lowFrequencyHz === systemConfig.measurementMode.amplitudeFilter.lowFrequencyHz &&
        filter.highFrequencyHz === systemConfig.measurementMode.amplitudeFilter.highFrequencyHz &&
        filter.sampleRate === sampleRate
    );
  }
  return waveformFilter;
}

/**
 * Returns the selected Waveform Filter based on the mode, station id and
 * the channel filters.
 *
 * @param mode the mode
 * @param id id of channel filter
 * @param sampleRate the sampleRate of the channel
 * @param channelFilters channel filters
 * @param defaultWaveformFilters default waveform filters
 *
 * @returns filter of type WaveformFilter
 */
export function getSelectedWaveformFilter(
  mode: AnalystWorkspaceTypes.WaveformDisplayMode,
  id: string,
  sampleRate: number,
  channelFilters: Record<string, WaveformTypes.WaveformFilter>,
  defaultWaveformFilters: WaveformTypes.WaveformFilter[]
): WaveformTypes.WaveformFilter {
  let selectedFilter: WaveformTypes.WaveformFilter;
  if (mode !== AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT) {
    selectedFilter = getWaveformFilterForMode(mode, sampleRate, defaultWaveformFilters);
  } else {
    selectedFilter = channelFilters[id] ? channelFilters[id] : createUnfilteredWaveformFilter();
  }
  return selectedFilter;
}

/**
 * Find the selected filter for the channel
 *
 * @param sampleRate Sample rate of the filter
 * @param selectedFilterIndex index of the current selected filter
 * @param filterNames names of all the filters
 * @param defaultWaveformFilters list of filters
 *
 * @returns the waveformFilter requested
 */
export function findWaveformFilter(
  sampleRate: number,
  selectedFilterIndex: number,
  filterNames: string[],
  defaultWaveformFilters: WaveformTypes.WaveformFilter[]
): WaveformTypes.WaveformFilter {
  // If no filter selected, return unfiltered
  if (selectedFilterIndex === -1) {
    return createUnfilteredWaveformFilter();
  }
  const selectedFilterName = filterNames[selectedFilterIndex];

  const filters = defaultWaveformFilters;
  let filter = filters.find(
    filt => filt.name === selectedFilterName && filt.sampleRate === sampleRate
  );
  if (!filter) {
    filter = filters.find(filt => filt.name === selectedFilterName);
  }
  return filter;
}

/**
 * Event handler for when a key is pressed
 *
 * @param e mouse event as React.MouseEvent<HTMLDivElement>
 * @param selectedChannels selected channels to be filtered
 * @param defaultWaveformFilters the default waveform filters
 * @param selectedFilterIndex the selected filter index
 * @param channelFilters map of channel filters
 * @returns an object {channelFilters, newFilterIndex}
 */
export function toggleWaveformChannelFilters(
  direction: KeyDirection,
  selectedChannels: string[],
  defaultWaveformFilters: WaveformTypes.WaveformFilter[],
  defaultStations: StationTypes.Station[],
  selectedFilterIndex: number,
  channelFilters: Record<string, WaveformTypes.WaveformFilter>
): { channelFilters: Record<string, WaveformTypes.WaveformFilter>; newFilterIndex: number } {
  const filterNames = uniq(defaultWaveformFilters.map(filter => filter.name));
  if (!selectedChannels || filterNames.length === 0) {
    return undefined;
  }

  const waveformFilterLength = filterNames.length;
  const tempChannelFilters = cloneDeep(channelFilters);
  let tempSelectedFilterIndex = selectedFilterIndex;
  tempSelectedFilterIndex =
    direction === KeyDirection.UP ? tempSelectedFilterIndex + 1 : tempSelectedFilterIndex - 1;
  if (tempSelectedFilterIndex >= waveformFilterLength) tempSelectedFilterIndex = -1;
  if (tempSelectedFilterIndex < -1) tempSelectedFilterIndex = waveformFilterLength - 1;

  /** Callback function to run on each station in {@link defaultStations} */
  const processStation = (station: StationTypes.Station, selectedId: string) => {
    // if the selected id is a default station,
    // set the filter on all of its non-default stations
    if (station.name === selectedId) {
      tempChannelFilters[station.name] = findWaveformFilter(
        station.allRawChannels[0].nominalSampleRateHz,
        tempSelectedFilterIndex,
        filterNames,
        defaultWaveformFilters
      );
    } else {
      // check each station's child channels to see if the id
      // matches one of them, if so, apply
      station.allRawChannels.forEach(childChannel => {
        if (childChannel.name === selectedId) {
          tempChannelFilters[childChannel.name] = findWaveformFilter(
            childChannel.nominalSampleRateHz,
            tempSelectedFilterIndex,
            filterNames,
            defaultWaveformFilters
          );
        }
      });
    }
  };

  if (selectedChannels !== undefined && selectedChannels.length > 0) {
    // for every id check to see if a default station matches
    // if none match, add to id list for every channel
    selectedChannels.forEach(selectedId => {
      defaultStations.forEach(station => processStation(station, selectedId));
    });
  } else {
    // no selected channels, apply filter to all
    defaultStations.forEach(station => {
      tempChannelFilters[station.name] = findWaveformFilter(
        // FIXME: Need a better way of looking up sample rate
        station.allRawChannels[0].nominalSampleRateHz,
        tempSelectedFilterIndex,
        filterNames,
        defaultWaveformFilters
      );
    });
  }

  return { channelFilters: tempChannelFilters, newFilterIndex: tempSelectedFilterIndex };
}
