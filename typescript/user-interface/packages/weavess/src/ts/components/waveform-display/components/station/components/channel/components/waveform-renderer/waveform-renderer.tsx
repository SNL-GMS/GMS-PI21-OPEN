/* eslint-disable react/destructuring-assignment */
import { recordLength } from '@gms/common-util';
import { blendColors, UILogger } from '@gms/ui-util';
import { WeavessConstants, WeavessTypes, WeavessUtil } from '@gms/weavess-core';
import * as d3 from 'd3';
import debounce from 'lodash/debounce';
import isEqual from 'lodash/isEqual';
import sortBy from 'lodash/sortBy';
import memoizeOne from 'memoize-one';
import React from 'react';
import * as THREE from 'three';

import { clearThree } from '../../../../../../utils';
import type { Float32ArrayData, WaveformRendererProps, WaveformRendererState } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

/**
 * This override is to allow THREE to support 2d array buffers.
 * It assumes 3 points (x, y, and z) by default in an array buffer at
 * arr[0], arr[1], and arr[2], respectively. This allows us to override arr[2], because
 * it would not be present in a 2d buffer. By doing this, we are able to eliminate 1/3 of
 * the points in the buffer, since they are all 0 anyway.
 * TODO: If this causes an error, delete it and change the array buffer to expect 3 points, the
 * TODO: third of which is set to 0;
 * eg: geometry.addAttribute('position', new THREE.BufferAttribute(float32Array, 3));
 * https://github.com/mrdoob/three.js/issues/19735
 *
 * @param index
 */
// eslint-disable-next-line func-names, no-invalid-this
THREE.BufferAttribute.prototype.getZ = function (index) {
  return this.array[index * this.itemSize + 2] || 0;
};

const getLineMaterial = memoizeOne(
  (materialColor: string) =>
    new THREE.LineBasicMaterial({
      color: materialColor,
      linewidth: 1
    })
);
/**
 * Waveform component. Renders and displays waveform graphics data.
 */
export class WaveformRenderer extends React.PureComponent<
  WaveformRendererProps,
  WaveformRendererState
> {
  /**
   * Flag to ensure that deprecated messages are only logged once in the logger
   * note: will only log when NODE_ENV is set to `development`
   */
  private static shouldLogDeprecated: boolean = process.env.NODE_ENV === 'development';

  /** Default channel props, if not provided */
  // eslint-disable-next-line react/static-property-placement
  public static readonly defaultProps: WeavessTypes.ChannelDefaultConfiguration = {
    displayType: [WeavessTypes.DisplayType.LINE],
    pointSize: 2,
    color: '#4580E6'
  };

  /** THREE.Scene which holds the waveforms for this channel */
  public scene: THREE.Scene;

  /** Orthographic camera used to zoom/pan around the waveform */
  public camera: THREE.OrthographicCamera;

  /** Shutting down stop and calls */
  private shuttingDown = false;

  /** References to the masks drawn on the scene. */
  private renderedMaskRefs: THREE.Mesh[] = [];

  /** Camera max top value for specific channel. */
  private cameraTopMax = -Infinity;

  /** Camera max bottom value for specific channel */
  private cameraBottomMax = Infinity;

  /** The manual amplitude scaled value to set on channel */
  private manualAmplitudeScaledValue = 0;

  /** Manual amplitude scale is set */
  private isManualAmplitudeScaleSet = false;

  /** Map from waveform filter id to processed data segments */
  private processedSegmentCache: Map<string, Float32ArrayData[]> = new Map();

  /** Map from channel segment id to pre-calculated boundaries */
  private channelSegmentBoundaries: Map<
    string,
    WeavessTypes.ChannelSegmentBoundaries[]
  > = new Map();

  public updateAmplitude = debounce(
    async (timeRange: WeavessTypes.TimeRange): Promise<void> => {
      /**
       * If we are in the process of zooming, drop this call because another
       *
       * @function updateAmplitude call will be scheduled.
       */
      await this.updateBounds(timeRange);
      /**
       * If we are in the process of zooming, drop this call because another
       *
       * @function updateAmplitude call will be scheduled.
       * We add this second check here in case zooming was triggered while we awaited
       * the @function updateBounds above.
       */
      this.updateAmplitudeFromBounds();
      this.props.renderWaveforms({ shouldCallAnimationLoopEnd: false }); // false so we don't get an infinite loop of amplitude update calls
    },
    WeavessConstants.ONE_FRAME_MS,
    { leading: false, trailing: true }
  );

  /**
   * Constructor
   *
   * @param props Waveform props as WaveformRenderProps
   */
  public constructor(props: WaveformRendererProps) {
    super(props);
    this.state = {};

    // If the msr window amplitude scale adjustment (factor) is set
    // then this must be the measure window's channel so set the adjustment
    this.manualAmplitudeScaledValue = this.props.msrWindowWaveformAmplitudeScaleFactor ?? 0;
    this.isManualAmplitudeScaleSet = this.manualAmplitudeScaledValue !== 0;
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************d

  /**
   * Called immediately after a component is mounted.
   * Setting state here will trigger re-rendering.
   */
  public async componentDidMount(): Promise<void> {
    this.scene = new THREE.Scene();
    const cameraZDepth = 5;
    this.camera = new THREE.OrthographicCamera(
      this.props.glMin,
      this.props.glMax,
      1,
      -1,
      cameraZDepth,
      -cameraZDepth
    );
    this.camera.position.z = 0;
    await this.prepareWaveformData(true);
    if (this.props.masks) {
      this.renderChannelMasks(this.props.masks);
    }
    await this.updateBounds(this.props.displayInterval);
    this.updateAmplitudeFromBounds();
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public async componentDidUpdate(prevProps: WaveformRendererProps): Promise<void> {
    // if the measure window amplitude scale factor is set update camera amplitude factor
    if (
      this.props.msrWindowWaveformAmplitudeScaleFactor ||
      prevProps.msrWindowWaveformAmplitudeScaleFactor
    ) {
      this.updateMsrWindowCameraAmplitudeAdjustment(prevProps);
    }

    // Received data for the first time
    if (
      // TODO: should do a deep equal check?
      !isEqual(prevProps.channelSegmentsRecord, this.props.channelSegmentsRecord) ||
      prevProps.displayInterval !== this.props.displayInterval ||
      !isEqual(prevProps.defaultRange, this.props.defaultRange) ||
      prevProps.getBoundaries !== this.props.getBoundaries
    ) {
      this.updateCameraBounds();
      await this.prepareWaveformData(true);
    } else if (prevProps.channelSegmentId !== this.props.channelSegmentId) {
      this.updateCameraBounds();
      await this.prepareWaveformData(false);
    }
    if (this.props.masks) {
      this.renderChannelMasks(this.props.masks);
    }
  }

  /**
   * Stop any calls propagating to channel after unmount
   */
  public componentWillUnmount(): void {
    this.shuttingDown = true;
    this.processedSegmentCache = new Map();
    this.channelSegmentBoundaries = new Map();
    clearThree(this.scene);
    this.scene = undefined;
    clearThree(this.camera);
    this.camera = undefined;
    clearThree(this.renderedMaskRefs);
    this.renderedMaskRefs = [];
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Get the manual scaled amplitude if set else returns 0
   *
   * @returns camera (manual) amplitude scaled value
   */
  public getCameraManualScaleAmplitude(): number {
    return this.isManualAmplitudeScaleSet ? this.manualAmplitudeScaledValue : 0;
  }

  /**
   * Scales the amplitude of the single waveform.
   *
   * @param e The mouse event
   */
  public readonly beginScaleAmplitudeDrag = (e: React.MouseEvent<HTMLDivElement>): void => {
    // prevent propagation of these events so that the underlying channel click doesn't register
    let previousPos = e.clientY;
    let currentPos = e.clientY;
    let diff = 0;

    if (!this.isManualAmplitudeScaleSet) {
      this.isManualAmplitudeScaleSet = true;
      this.manualAmplitudeScaledValue = Math.abs(this.camera.top);
    }
    const onMouseMove = (e2: MouseEvent) => {
      currentPos = e2.clientY;
      diff = previousPos - currentPos;
      previousPos = currentPos;

      const currentCameraRange = Math.abs(this.camera.top - this.camera.bottom);

      // calculate the amplitude adjustment
      const percentDiff = 0.05;
      const amplitudeAdjustment: number = currentCameraRange * percentDiff;

      // Was mouse moving up or down
      if (diff > 0) {
        this.manualAmplitudeScaledValue += amplitudeAdjustment;
      } else if (diff < 0) {
        this.manualAmplitudeScaledValue -= amplitudeAdjustment;
      }
      // apply the any amplitude adjustment to the camera
      this.camera.top = this.manualAmplitudeScaledValue;
      this.camera.bottom = -this.manualAmplitudeScaledValue;

      this.setYAxisBounds(this.camera.bottom, this.camera.top);
      this.camera.updateProjectionMatrix();
      this.props.renderWaveforms();
    };

    const onMouseUp = () => {
      document.body.removeEventListener('mousemove', onMouseMove);
      document.body.removeEventListener('mouseup', onMouseUp);
    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  };

  /**
   * Reset the amplitude to the default.
   */
  public resetAmplitude = (): void => {
    // Clear manual scaling
    this.manualAmplitudeScaledValue = 0;
    this.isManualAmplitudeScaleSet = false;

    // Check that the amplitude needs resetting
    if (this.camera.top !== this.cameraTopMax || this.camera.bottom !== this.cameraBottomMax) {
      if (this.processedSegmentCache.size !== 0) {
        // reset the amplitude to the window default for this channel
        this.camera.top = this.cameraTopMax;
        this.camera.bottom = this.cameraBottomMax;
        this.setYAxisBounds(this.camera.bottom, this.camera.top);
        this.camera.updateProjectionMatrix();
        this.props.renderWaveforms();
      }
    }
  };

  /**
   * Gets the channel segments with the ID provided in this.props
   *
   * @returns the channel segments that matches the channelSegmentID given by props
   */
  private readonly getThisChannelSegments = () =>
    this.props.channelSegmentsRecord &&
    this.props.channelSegmentsRecord[this.props.channelSegmentId]
      ? this.props.channelSegmentsRecord[this.props.channelSegmentId]
      : undefined;

  /**
   * If the Amplitude values in the ChannelSegmentBoundaries was not already set
   * create them and set them in the channelSegmentBoundaries map for each channel segment
   *
   * * @param timeRange
   */
  private readonly updateBounds = async (timeRange: WeavessTypes.TimeRange) => {
    if (
      this.props.channelSegmentsRecord == null ||
      Object.entries(this.props.channelSegmentsRecord) == null
    ) {
      return;
    }
    // Clear the map before rebuilding the boundaries for the timeRange
    this.channelSegmentBoundaries.clear();
    await Promise.all(
      Object.entries(this.props.channelSegmentsRecord).map(async ([id, channelSegments]) => {
        if (channelSegments == null) {
          // return a promise so we can use Promise.all above.
          await Promise.resolve(undefined);
        }
        await Promise.all(
          channelSegments.map(async channelSegment => {
            await this.updateChannelSegmentBounds(timeRange, channelSegment, id);
          })
        );
      })
    );
  };

  /**
   * Updates channelSegmentBoundaries map for each channel segment
   *
   * @param timeRange
   * @param channelSegment
   * @param id
   */
  private readonly updateChannelSegmentBounds = async (
    timeRange: WeavessTypes.TimeRange,
    channelSegment: WeavessTypes.ChannelSegment,
    id: string
  ) => {
    let boundary = channelSegment.channelSegmentBoundaries;
    if (
      !boundary &&
      this.props.getBoundaries &&
      WeavessTypes.areDataSegmentsAllClaimChecks(channelSegment.dataSegments)
    ) {
      if (this.props.channelOffset) {
        const offsetTimeRange: WeavessTypes.TimeRange = {
          startTimeSecs: timeRange.startTimeSecs - this.props.channelOffset,
          endTimeSecs: timeRange.endTimeSecs - this.props.channelOffset
        };
        boundary = await this.props.getBoundaries(
          channelSegment.channelName,
          channelSegment,
          offsetTimeRange
        );
      } else {
        boundary = await this.props.getBoundaries(
          channelSegment.channelName,
          channelSegment,
          timeRange
        );
      }
    } else {
      boundary = this.createChannelSegmentBoundaries(channelSegment, id);
    }
    if (boundary) {
      if (this.channelSegmentBoundaries.has(id) && this.channelSegmentBoundaries.get(id)) {
        this.channelSegmentBoundaries.get(id).push(boundary);
      } else {
        this.channelSegmentBoundaries.set(id, [boundary]);
      }
    }
  };

  /**
   * Update the min,max in gl units where we draw waveforms, if the view bounds have changed.
   *
   * @param prevProps The previous waveform props
   */
  private readonly updateCameraBounds = () => {
    this.camera.left = this.props.glMin;
    this.camera.right = this.props.glMax;
  };

  /**
   * For measure window update the camera amplitude adjustment if adjustment changed
   * or the display time range has changed
   *
   * @param prevProps The previous waveform props
   */
  private readonly updateMsrWindowCameraAmplitudeAdjustment = (
    prevProps: WaveformRendererProps
  ): void => {
    if (
      prevProps.msrWindowWaveformAmplitudeScaleFactor !==
        this.props.msrWindowWaveformAmplitudeScaleFactor ||
      prevProps.displayInterval !== this.props.displayInterval ||
      !isEqual(prevProps.defaultRange, this.props.defaultRange)
    ) {
      this.manualAmplitudeScaledValue = this.props.msrWindowWaveformAmplitudeScaleFactor ?? 0;
      this.isManualAmplitudeScaleSet = this.manualAmplitudeScaledValue !== 0;
    }
  };

  /**
   * Prepares the waveform display for rendering.
   *
   * @param refreshVerticesCache True if the cache should be refreshed, false otherwise
   */
  private readonly prepareWaveformData = async (refreshVerticesCache: boolean) => {
    // Converts from array of floats to an array of vertices
    if (refreshVerticesCache) {
      await this.convertDataToVerticesArray();
      this.props.renderWaveforms();
    }

    // Create ThreeJS scene from vertices data
    this.setupThreeJSFromVertices();
  };

  /**
   * Updates the y axis and camera position based on the boundaries in this.channelSegmentBoundaries
   */
  // eslint-disable-next-line complexity
  private readonly updateAmplitudeFromBounds = () => {
    /**
     * If we are in the process of zooming, drop this call because another
     *
     * @function updateAmplitude call will be scheduled.
     */
    if (this.shuttingDown) {
      return;
    }

    const boundaries = this.channelSegmentBoundaries.get(this.props.channelSegmentId);
    if (!boundaries) {
      this.setYAxisBounds(undefined, undefined);
      return;
    }

    const amplitudeMin = boundaries
      .map(boundary => Math.min(boundary.bottomMax, boundary.topMax))
      .reduce(
        (previousBoundary, currentBoundary) => Math.min(previousBoundary, currentBoundary),
        Infinity
      );

    const amplitudeMax = boundaries
      .map(boundary => Math.max(boundary.bottomMax, boundary.topMax))
      .reduce(
        (previousBoundary, currentBoundary) => Math.max(previousBoundary, currentBoundary),
        -Infinity
      );
    this.updateCameraForMinMaxAmplitudes(amplitudeMin, amplitudeMax, boundaries);
  };

  /**
   * Update the camera based on the min/max amplitudes
   *
   * @param amplitudeMin
   * @param amplitudeMax
   * @param boundaries ChannelSegmentBoundaries
   */
  private readonly updateCameraForMinMaxAmplitudes = (
    amplitudeMin: number,
    amplitudeMax: number,
    boundaries: WeavessTypes.ChannelSegmentBoundaries[]
  ): void => {
    // Set channel average and set default camera top/bottom based on average
    // calculate the average using the unloaded data segments
    // and the previous loaded segments
    // Set axis offset and default view but account for the zero (empty channel)
    const axisOffset: number = Math.max(Math.abs(amplitudeMax), Math.abs(amplitudeMin));

    // account for the amplitude if it is all positive or all negative
    if (amplitudeMin < 0 && amplitudeMax > 0) {
      const channelAvg =
        boundaries
          .map(boundary => boundary.channelAvg)
          .reduce((previous, current) => previous + current, 0) / boundaries.length;
      this.cameraTopMax = channelAvg + axisOffset;
      this.cameraBottomMax = channelAvg - axisOffset;
    } else {
      this.cameraTopMax = amplitudeMax;
      this.cameraBottomMax = amplitudeMin;
    }

    // apply the default yaxis range if provided, instead of using the
    // calculated min/max for the yaxis based on the provided data
    if (this.props.defaultRange) {
      // apply the default max for the yaxis
      if (this.props.defaultRange.max) {
        this.cameraTopMax = this.props.defaultRange.max;
      }

      // apply the default min for the yaxis
      if (this.props.defaultRange.min !== undefined) {
        this.cameraBottomMax = this.props.defaultRange.min;
      }
    }

    if (this.cameraTopMax !== -Infinity && this.cameraBottomMax !== Infinity) {
      // update the camera and apply the any amplitude adjustment to the camera
      if (!this.isManualAmplitudeScaleSet) {
        this.camera.top = this.cameraTopMax;
        this.camera.bottom = this.cameraBottomMax;
      } else {
        this.camera.top = this.manualAmplitudeScaledValue;
        this.camera.bottom = -this.manualAmplitudeScaledValue;
      }

      // set amplitude for label
      this.setYAxisBounds(this.camera.bottom, this.camera.top);
      this.camera.updateProjectionMatrix();
    }
  };

  /**
   * Add line or scatter points to the scene
   *
   * @param float32ArrayWithStartTime
   * @param anySelected
   */
  private readonly addScene = (
    float32ArrayWithStartTime: Float32ArrayData,
    anySelected: boolean
  ) => {
    const color: string = float32ArrayWithStartTime.color || WaveformRenderer.defaultProps.color;
    const dimColor = blendColors(
      color,
      this.props.initialConfiguration?.backgroundColor,
      this.props.initialConfiguration?.waveformDimPercent
    );

    const { float32Array } = float32ArrayWithStartTime;
    const geometry = new THREE.BufferGeometry();
    geometry.addAttribute('position', new THREE.BufferAttribute(float32Array, 2));
    (float32ArrayWithStartTime.displayType || WaveformRenderer.defaultProps.displayType).forEach(
      displayType => {
        if (displayType === WeavessTypes.DisplayType.LINE) {
          // Default material is bright if any of the CS are selected
          // then dim all CS that are not selected
          let lineColor = color;
          if (anySelected && !float32ArrayWithStartTime.isSelected) {
            lineColor = dimColor;
          }
          const line = new THREE.Line(geometry, getLineMaterial(lineColor));
          this.scene.add(line);
        } else if (displayType === WeavessTypes.DisplayType.SCATTER) {
          const pointsMaterial = new THREE.PointsMaterial({
            color,
            size: float32ArrayWithStartTime.pointSize || WaveformRenderer.defaultProps.pointSize,
            sizeAttenuation: false
          });
          const points = new THREE.Points(geometry, pointsMaterial);
          this.scene.add(points);
        }
      }
    );
  };

  /**
   * Create two lists and to make sure the bright (selected) waveforms are more visible
   * add the bright waveforms after the dimmed waveforms
   *
   * @param processedData list Float32ArrayData for this channel
   * @param anySelected are any of the channel segments selected
   * @returns Float32ArrayData[] in order to be added to the scene
   */
  private readonly createProcessedDataList = (
    processedData: Float32ArrayData[],
    anySelected: boolean
  ): Float32ArrayData[] => {
    const brightProcessedData = [];
    const dimmedProcessedData = [];
    processedData.forEach(float32ArrayWithStartTime => {
      if (this.props.isMeasureWindow) {
        if (!anySelected || float32ArrayWithStartTime.isSelected) {
          brightProcessedData.push(float32ArrayWithStartTime);
        }
      } else if (anySelected && !float32ArrayWithStartTime.isSelected) {
        dimmedProcessedData.push(float32ArrayWithStartTime);
      } else {
        brightProcessedData.push(float32ArrayWithStartTime);
      }
    });
    return [...dimmedProcessedData, ...brightProcessedData];
  };

  /**
   * Iterates through cached vertices data in the float32 array format
   * and creates ThreeJS objects and adds them to the
   * ThreeJS scene
   */
  private readonly setupThreeJSFromVertices = () => {
    if (this.shuttingDown) {
      return;
    }
    // removed old three js objects from scene
    this.clearScene();

    if (!this.props.channelSegmentId) {
      return;
    }
    const channelSegments = this.getThisChannelSegments();
    const anySelected = channelSegments && channelSegments.find(cs => cs.isSelected) !== undefined;
    const processedData = this.processedSegmentCache.get(this.props.channelSegmentId);
    if (processedData) {
      // A list of data to add to scene in order of dimmed then bright channel segments
      // The list will always be defined
      const processedDataList = this.createProcessedDataList(processedData, anySelected);
      processedDataList.forEach(data => this.addScene(data, anySelected));
    }
    this.updateAmplitudeFromBounds();
  };

  /**
   * Converts waveform data into useable vertices
   */
  private readonly convertDataToVerticesArray = async () => {
    // determine the new data segments that need to be added to the scene
    if (this.props.channelSegmentsRecord) {
      await Promise.all(
        Object.entries(this.props.channelSegmentsRecord).map(async ([key, channelSegments]) => {
          if (channelSegments && channelSegments.length > 0) {
            const processedSegments: Float32ArrayData[] = await this.convertWaveformDataFloat32(
              channelSegments
            );
            // if all processed segments have no waveform data don't set cache
            if (processedSegments?.find((ps: Float32ArrayData) => ps.float32Array.length > 0)) {
              this.processedSegmentCache.set(key, processedSegments);
            }
          }
        })
      );
    }
  };

  /**
   * Converts a data segment into a Float32ArrayData
   * if it is dataClaimCheck or dataBySampleRate
   * if already a Float32ArrayData then just returns it
   *
   * @param dataSegment to convert
   * @returns converted Float32ArrayData  or undefined
   */
  private readonly convertDataSegmentDataFloat32 = async (
    isChannelSegmentSelected: boolean,
    dataSegment: WeavessTypes.DataSegment
  ): Promise<Float32ArrayData | void> => {
    let float32Array: Float32Array;
    if (WeavessTypes.isFloat32Array(dataSegment.data.values)) {
      float32Array = dataSegment.data.values;
    } else if (WeavessTypes.isDataClaimCheck(dataSegment.data)) {
      if (this.props.getPositionBuffer) {
        float32Array = await this.props.getPositionBuffer(
          dataSegment.data.id,
          this.props.displayInterval.startTimeSecs,
          this.props.displayInterval.endTimeSecs,
          dataSegment.data.domainTimeRange
        );
      } else {
        throw new Error(
          'Data by Claim Check needs a valid getPositionBuffer getter (passed as Weavess props)'
        );
      }
    } else if (WeavessTypes.isDataBySampleRate(dataSegment.data)) {
      if (WaveformRenderer.shouldLogDeprecated) {
        logger.warn(
          'Deprecated (data by sample rate) - recommended to pass the data in using a typed array'
        );
        WaveformRenderer.shouldLogDeprecated = false;
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const values: number[] = dataSegment.data.values as any[];
      float32Array = WeavessUtil.createPositionBufferForDataBySampleRate({
        values,
        displayStartTimeSecs: this.props.displayInterval.startTimeSecs,
        displayEndTimeSecs: this.props.displayInterval.endTimeSecs,
        glMax: this.props.glMax,
        glMin: this.props.glMin,
        sampleRate: dataSegment.data.sampleRate,
        startTimeSecs: dataSegment.data.startTimeSecs,
        endTimeSecs: dataSegment.data.endTimeSecs
      });
    } else {
      if (WaveformRenderer.shouldLogDeprecated) {
        logger.warn(
          'Deprecated (data by time) - recommended to pass the data in using a typed array'
        );
        WaveformRenderer.shouldLogDeprecated = false;
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const values: WeavessTypes.TimeValuePair[] = dataSegment.data.values as any[];
      float32Array = WeavessUtil.createPositionBufferForDataByTime({
        glMax: this.props.glMax,
        glMin: this.props.glMin,
        displayStartTimeSecs: this.props.displayInterval.startTimeSecs,
        displayEndTimeSecs: this.props.displayInterval.endTimeSecs,
        values
      });
    }

    // If values were returned then add it
    // Note: Measure Window might not be in this segments window
    if (float32Array.length > 0) {
      // Update the max / min gl units found
      return {
        isSelected: isChannelSegmentSelected,
        color: dataSegment.color,
        displayType: dataSegment.displayType,
        pointSize: dataSegment.pointSize,
        float32Array
      };
    }
    return undefined;
  };

  private readonly convertWaveformDataFloat32 = async (
    channelSegments: WeavessTypes.ChannelSegment[]
  ): Promise<Float32ArrayData[]> => {
    // Convert Waveform data to Float32ArrayData data
    const processedSegments: Float32ArrayData[] = [];
    // Build list of data segments to process
    await Promise.all(
      channelSegments.map(async cs => {
        await Promise.all(
          cs.dataSegments.map(async dataSegment => {
            const float32ArrayData = await this.convertDataSegmentDataFloat32(
              cs.isSelected,
              dataSegment
            );
            if (float32ArrayData) {
              processedSegments.push(float32ArrayData);
            }
          })
        );
      })
    );
    return processedSegments;
  };

  /**
   * Given a channel segment and id creates the Channel Segment Boundaries
   *
   * @param channelSegment
   * @param channelSegmentId
   * @returns ChannelSegmentBoundaries
   */
  private readonly createChannelSegmentBoundaries = (
    channelSegment: WeavessTypes.ChannelSegment,
    channelSegmentId: string
  ): WeavessTypes.ChannelSegmentBoundaries => {
    let topMax = -Infinity;
    let bottomMax = Infinity;
    let totalValue = 0;
    let totalValuesCount = 0;

    if (channelSegment.dataSegments) {
      channelSegment.dataSegments.forEach(dataSegment => {
        // eslint-disable-next-line no-nested-ternary
        const values = WeavessTypes.isFloat32Array(dataSegment.data.values)
          ? dataSegment.data.values.filter((element, index) => index % 2 === 1)
          : // eslint-disable-next-line no-nested-ternary
          WeavessTypes.isDataBySampleRate(dataSegment.data)
          ? dataSegment.data.values
          : WeavessTypes.isDataByTime(dataSegment.data)
          ? dataSegment.data.values.map(v => v.value)
          : [];

        if (!values || values?.length === 0) {
          // When there is no data in the channel set offset to 1 (to avoid infinity)
          this.cameraTopMax = 1;
          this.cameraBottomMax = -1;
          return;
        }
        if (values?.length > 0) {
          values.forEach(sample => {
            totalValue += sample;
            if (sample > topMax) topMax = sample;
            if (sample < bottomMax) bottomMax = sample;
          });
          totalValuesCount += values.length;
        }
      });
    }

    return {
      topMax,
      bottomMax,
      channelAvg: totalValue / totalValuesCount,
      samplesCount: totalValuesCount,
      offset: Math.max(Math.abs(topMax), Math.abs(bottomMax)),
      channelSegmentId
    };
  };

  /**
   * Render the Masks to the display.
   *
   * @param masks The masks (as Mask[]) to render
   */
  private readonly renderChannelMasks = (masks: WeavessTypes.Mask[]) => {
    // clear out any existing masks
    this.renderedMaskRefs.forEach(m => this.scene.remove(m));
    this.renderedMaskRefs.length = 0; // delete all references

    // if we're being passed empty data, don't try to add masks
    if (recordLength(this.props.channelSegmentsRecord) === 0) return;

    const timeToGlScale = d3
      .scaleLinear()
      .domain([this.props.displayInterval.startTimeSecs, this.props.displayInterval.endTimeSecs])
      .range([this.props.glMin, this.props.glMax]);

    // TODO move sorting to happen elsewhere and support re-sorting when new masks are added
    // TODO consider passing comparator for mask sorting as an argument to weavess
    sortBy(masks, (mask: WeavessTypes.Mask) => mask.endTimeSecs - mask.startTimeSecs).forEach(
      (mask, i, arr) => {
        const halfSecond = 0.5;
        let maskStartTime = mask.startTimeSecs;
        let maskEndTime = mask.endTimeSecs;
        if (mask.endTimeSecs - mask.startTimeSecs < 1) {
          maskStartTime -= halfSecond;
          maskEndTime += halfSecond;
        }
        const width = timeToGlScale(maskEndTime) - timeToGlScale(maskStartTime);
        const midpoint = timeToGlScale(maskStartTime + (maskEndTime - maskStartTime) / 2);
        const planeGeometry = new THREE.PlaneBufferGeometry(width, this.cameraTopMax * 2);
        const planeMaterial = new THREE.MeshBasicMaterial({
          color: new THREE.Color(mask.color),
          side: THREE.DoubleSide,
          transparent: true
        });
        planeMaterial.blending = THREE.CustomBlending;
        planeMaterial.blendEquation = THREE.AddEquation;
        planeMaterial.blendSrc = THREE.DstAlphaFactor;
        planeMaterial.blendDst = THREE.SrcColorFactor;
        planeMaterial.depthFunc = THREE.NotEqualDepth;

        const plane: THREE.Mesh = new THREE.Mesh(planeGeometry, planeMaterial);
        const depth = -2;
        plane.position.x = midpoint;
        plane.position.z = depth;
        plane.renderOrder = i / arr.length;

        this.renderedMaskRefs.push(plane);
      }
    );

    if (this.renderedMaskRefs.length > 0) {
      this.scene.add(...this.renderedMaskRefs);
    }
  };

  /**
   * set the y-axis bounds for a particular channel
   *
   * @param min The y minimum axis value
   * @param max The y maximum axis value
   */
  private readonly setYAxisBounds = (min: number | undefined, max: number | undefined) => {
    // don't update channel y-axis if unmount has been called
    if (!this.shuttingDown) {
      this.props.setYAxisBounds(min, max);
    }
  };

  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, react/sort-comp
  public render() {
    return null;
  }

  /**
   * Remove the scene children
   */
  private readonly clearScene = (): void => {
    while (this.scene.children.length > 0) {
      this.scene.remove(this.scene.children[0]);
    }
  };
}
