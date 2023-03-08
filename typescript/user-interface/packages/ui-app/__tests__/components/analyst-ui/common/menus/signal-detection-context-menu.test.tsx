import { ContextMenu } from '@blueprintjs/core';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import React from 'react';

import type { SignalDetectionContextMenuProps } from '../../../../../src/ts/components/analyst-ui/common/menus/signal-detection-context-menu';
import { SignalDetectionContextMenu } from '../../../../../src/ts/components/analyst-ui/common/menus/signal-detection-context-menu';

const contextMenuMock = {
  show: jest.fn()
};
Object.assign(ContextMenu, contextMenuMock);

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

function flushPromises(): any {
  return new Promise(resolve => {
    setTimeout(resolve, 0);
  });
}

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();
/**
 * Tests the signal detection context menu component
 */
describe('signal-detection-context-menu', () => {
  // eslint-disable-next-line one-var
  const props: SignalDetectionContextMenuProps = {
    signalDetections: signalDetectionsData,
    selectedSds: [signalDetectionsData[0]],
    sdIdsToShowFk: [],
    currentOpenEventId: undefined,
    changeAssociation: jest.fn(),
    associateToNewEvent: jest.fn(),
    rejectDetections: jest.fn(),
    updateDetections: jest.fn(),
    measurementMode: {
      mode: AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT,
      entries: {
        [signalDetectionsData[0].id]: true
      }
    },
    setSelectedSdIds: jest.fn(),
    setSdIdsToShowFk: jest.fn(),
    setMeasurementModeEntries: jest.fn()
  };
  const wrapper: any = Enzyme.mount(
    <SignalDetectionContextMenu
      signalDetections={props.signalDetections}
      selectedSds={props.selectedSds}
      sdIdsToShowFk={props.sdIdsToShowFk}
      currentOpenEventId={props.currentOpenEventId}
      // eslint-disable-next-line @typescript-eslint/unbound-method
      changeAssociation={props.changeAssociation}
      // eslint-disable-next-line @typescript-eslint/unbound-method
      associateToNewEvent={props.associateToNewEvent}
      // eslint-disable-next-line @typescript-eslint/unbound-method
      rejectDetections={props.rejectDetections}
      // eslint-disable-next-line @typescript-eslint/unbound-method
      updateDetections={props.updateDetections}
      measurementMode={props.measurementMode}
      // eslint-disable-next-line @typescript-eslint/unbound-method
      setSelectedSdIds={props.setSelectedSdIds}
      // eslint-disable-next-line @typescript-eslint/unbound-method
      setSdIdsToShowFk={props.setSdIdsToShowFk}
      // eslint-disable-next-line @typescript-eslint/unbound-method
      setMeasurementModeEntries={props.setMeasurementModeEntries}
    />
  );

  it('should have a consistent snapshot on mount', () => {
    expect(wrapper.render()).toMatchSnapshot();
    flushPromises();
  });

  it('should have basic props built on render', () => {
    const buildProps = wrapper.props() as SignalDetectionContextMenuProps;
    expect(buildProps).toMatchSnapshot();
  });

  it('should have a function for checking if we can generate fk', () => {
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'canGenerateFk');
    expect(instance.canGenerateFk(signalDetectionsData[0])).toMatchSnapshot();
    flushPromises();
    expect(spy).toHaveBeenCalledWith(signalDetectionsData[0]);
  });

  it('should have a function for showing showSdDetailsPopover', () => {
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'showSdDetailsPopover');
    expect(() => instance.showSdDetailsPopover(signalDetectionsData[0], 100, 100)).not.toThrow();
    flushPromises();
    expect(spy).toHaveBeenCalledWith(signalDetectionsData[0], 100, 100);
  });

  it('should have a function for setSdIdsToShowFk', () => {
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'setSdIdsToShowFk');
    instance.setSdIdsToShowFk();
    flushPromises();
    expect(spy).toHaveBeenCalled();
    // if we call setSdIdsToShowFk it should call set selected sd ids
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(props.setSelectedSdIds).toHaveBeenCalledWith([signalDetectionsData[0].id]);
  });

  it('should build measure window menu item buildMeasurementModeContextMenuItem', () => {
    const sdIds = signalDetectionsData.map(sd => sd.id);
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'buildMeasurementModeContextMenuItem');
    expect(instance.buildMeasurementModeContextMenuItem(sdIds)).toMatchSnapshot();
    expect(spy).toHaveBeenCalled();
  });

  it('should be able to show measurement mode entries showMeasurementModeEntries', () => {
    const sdhypIds: string[] = [];
    signalDetectionsData.forEach(sd => {
      sd.signalDetectionHypotheses.forEach(sdhypo => {
        sdhypIds.push(sdhypo.id.id);
      });
    });
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'showMeasurementModeEntries');
    expect(() => instance.showMeasurementModeEntries(sdhypIds)).not.toThrow();
    expect(spy).toHaveBeenCalled();
  });

  it('should be able to hide measurement mode entries hideMeasurementModeEntries', () => {
    const sdhypIds: string[] = [];
    signalDetectionsData.forEach(sd => {
      sd.signalDetectionHypotheses.forEach(sdhypo => {
        sdhypIds.push(sdhypo.id.id);
      });
    });
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'hideMeasurementModeEntries');
    expect(() => instance.hideMeasurementModeEntries(sdhypIds)).not.toThrow();
    expect(spy).toHaveBeenCalled();
  });
});
