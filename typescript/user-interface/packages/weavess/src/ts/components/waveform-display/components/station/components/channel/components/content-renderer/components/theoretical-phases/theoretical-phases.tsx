/* eslint-disable react/destructuring-assignment */
import type { WeavessTypes } from '@gms/weavess-core';
import isEqual from 'lodash/isEqual';
import memoizeOne from 'memoize-one';
import React from 'react';

import { calculateLeftPercent, calculateRightPercent } from '../../../../../../../../utils';
import { TheoreticalPhaseWindow } from '../../../../../../../theoretical-phase-window';
import type { TheoreticalPhasesProps, TheoreticalPhasesState } from './types';

export class TheoreticalPhases extends React.PureComponent<
  TheoreticalPhasesProps,
  TheoreticalPhasesState
> {
  /**
   * A memoized function for creating the theoretical phase window elements.
   * The memoization function caches the results using
   * the most recent argument and returns the results.
   *
   * @param startTimeSecs epoch seconds start time
   * @param endTimeSecs epoch seconds end time
   * @param theoreticalPhaseWindows the theoretical phase windows
   *
   * @returns an array JSX elements
   */
  private readonly memoizedCreateTheoreticalPhaseWindowElements: (
    startTimeSecs: number,
    endTimeSecs: number,
    theoreticalPhaseWindows: WeavessTypes.TheoreticalPhaseWindow[] | undefined
  ) => JSX.Element[];

  /**
   * Constructor
   *
   * @param props Waveform props as TheoreticalPhasesProps
   */
  public constructor(props: TheoreticalPhasesProps) {
    super(props);
    this.memoizedCreateTheoreticalPhaseWindowElements = memoizeOne(
      TheoreticalPhases.createTheoreticalPhaseWindowElements,
      /* tell memoize to use a deep comparison for complex objects */
      isEqual
    );
    this.state = {};
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  public render(): JSX.Element {
    return (
      <>
        {this.memoizedCreateTheoreticalPhaseWindowElements(
          this.props.displayInterval.startTimeSecs,
          this.props.displayInterval.endTimeSecs,
          this.props.theoreticalPhaseWindows
        )}
      </>
    );
  }

  /**
   * Creates the theoretical phase window elements.
   *
   * @param startTimeSecs epoch seconds start time
   * @param endTimeSecs epoch seconds end time
   * @param theoreticalPhaseWindows the theoretical phase windows
   *
   * @returns an array of theoretical phase elements as JSX.Element
   */
  // eslint-disable-next-line react/sort-comp
  private static readonly createTheoreticalPhaseWindowElements = (
    startTimeSecs: number,
    endTimeSecs: number,
    theoreticalPhaseWindows: WeavessTypes.TheoreticalPhaseWindow[] | undefined
  ): JSX.Element[] => {
    if (!theoreticalPhaseWindows) return [];

    return theoreticalPhaseWindows.map(theoreticalPhaseWindow => {
      const leftPos = calculateLeftPercent(
        theoreticalPhaseWindow.startTimeSecs,
        startTimeSecs,
        endTimeSecs
      );

      const rightPos = calculateRightPercent(
        theoreticalPhaseWindow.endTimeSecs,
        startTimeSecs,
        endTimeSecs
      );

      return (
        <TheoreticalPhaseWindow
          id={theoreticalPhaseWindow.id}
          key={theoreticalPhaseWindow.id}
          color={theoreticalPhaseWindow.color}
          left={leftPos}
          right={rightPos}
          label={theoreticalPhaseWindow.label}
        />
      );
    });
  };
}
