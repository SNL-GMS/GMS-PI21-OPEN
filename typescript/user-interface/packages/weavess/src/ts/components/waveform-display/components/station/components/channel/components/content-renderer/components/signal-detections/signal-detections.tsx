import type { WeavessTypes } from '@gms/weavess-core';
import isEqual from 'lodash/isEqual';
import memoizeOne from 'memoize-one';
import React from 'react';

import { calculateLeftPercent } from '../../../../../../../../utils';
import { PickMarker, UncertaintyMarker } from '../../../../../../../markers';
import type { SignalDetectionsProps, SignalDetectionsState } from './types';

export class SignalDetections extends React.PureComponent<
  SignalDetectionsProps,
  SignalDetectionsState
> {
  /**
   * A memoized function for creating the signal detection elements.
   * The memoization function caches the results using
   * the most recent argument and returns the results.
   *
   * @param props the signal detection props
   *
   * @returns an array JSX elements
   */
  private readonly memoizedCreateSignalDetectionElements: (
    props: SignalDetectionsProps
  ) => JSX.Element[];

  /**
   * Constructor
   *
   * @param props Waveform props as SignalDetectionsProps
   */
  public constructor(props: SignalDetectionsProps) {
    super(props);
    this.memoizedCreateSignalDetectionElements = memoizeOne(
      SignalDetections.createSignalDetectionElements,
      /* tell memoize to use a deep comparison for complex objects */
      isEqual
    );
    this.state = {};
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  public render(): JSX.Element {
    return <>{this.memoizedCreateSignalDetectionElements(this.props)}</>;
  }

  /**
   * Creates SignalDetection components
   *
   * @param props the signal detection props
   *
   * @returns an array of signal detection elements as JSX.Element
   */
  // eslint-disable-next-line react/sort-comp
  private static readonly createSignalDetectionElements = (
    props: SignalDetectionsProps
  ): JSX.Element[] => {
    if (!props.signalDetections) return [];
    return props.signalDetections.map(signalDetection => {
      const signalDetectionPosition = calculateLeftPercent(
        signalDetection.timeSecs,
        props.displayInterval.startTimeSecs,
        props.displayInterval.endTimeSecs
      );

      const sdUncertaintyLeftPosition = calculateLeftPercent(
        signalDetection.timeSecs - signalDetection.uncertaintySecs,
        props.displayInterval.startTimeSecs,
        props.displayInterval.endTimeSecs
      );

      const sdUncertaintyRightPosition = calculateLeftPercent(
        signalDetection.timeSecs + signalDetection.uncertaintySecs,
        props.displayInterval.startTimeSecs,
        props.displayInterval.endTimeSecs
      );

      const leftUncertaintyElement = SignalDetections.createUncertaintyElement(
        sdUncertaintyLeftPosition,
        signalDetectionPosition,
        `${signalDetection.id}-uncert-left`,
        signalDetection,
        true
      );
      const rightUncertaintyElement = SignalDetections.createUncertaintyElement(
        sdUncertaintyRightPosition,
        signalDetectionPosition,
        `${signalDetection.id}-uncert-right`,
        signalDetection,
        false
      );

      return (
        <div className="signal-detection__pick" key={`${signalDetection.id}-top`}>
          {leftUncertaintyElement}
          <PickMarker
            key={signalDetection.id}
            channelId={props.channelId}
            predicted={false}
            isSelected={signalDetection.isSelected}
            isSelectable
            startTimeSecs={props.displayInterval.startTimeSecs}
            endTimeSecs={props.displayInterval.endTimeSecs}
            // eslint-disable-next-line react/jsx-props-no-spreading
            {...signalDetection}
            position={signalDetectionPosition}
            /* eslint-disable @typescript-eslint/unbound-method */
            getTimeSecsForClientX={props.getTimeSecsForClientX}
            /* eslint-disable @typescript-eslint/unbound-method */
            onClick={
              props.events?.onSignalDetectionClick ? props.events.onSignalDetectionClick : undefined
            }
            onContextMenu={
              props.events?.onSignalDetectionContextMenu
                ? props.events.onSignalDetectionContextMenu
                : undefined
            }
            /* eslint-disable @typescript-eslint/unbound-method */
            onDragEnd={
              props.events?.onSignalDetectionDragEnd
                ? props.events.onSignalDetectionDragEnd
                : undefined
            }
            toggleDragIndicator={props.toggleDragIndicator}
            positionDragIndicator={props.positionDragIndicator}
          />
          {rightUncertaintyElement}
        </div>
      );
    });
  };

  /**
   * Creates the left or right JSX.Element uncertainty marker
   *
   * @param position Creates the left or right JSX.Element uncertainty marker
   * @param key
   * @param signalDetection
   * @returns JSX.Element or null if showUncertaintyBars is false
   */
  private static readonly createUncertaintyElement = (
    position: number,
    signalDetectionPosition: number,
    key: string,
    signalDetection: WeavessTypes.PickMarker,
    isLeftUncertaintyBar: boolean
  ): JSX.Element | null => {
    if (signalDetection.showUncertaintyBars) {
      return (
        <UncertaintyMarker
          key={key}
          id={signalDetection.id}
          color={signalDetection.color}
          position={position}
          pickMarkerPosition={signalDetectionPosition}
          isLeftUncertaintyBar={isLeftUncertaintyBar}
        />
      );
    }
    return null;
  };
}
