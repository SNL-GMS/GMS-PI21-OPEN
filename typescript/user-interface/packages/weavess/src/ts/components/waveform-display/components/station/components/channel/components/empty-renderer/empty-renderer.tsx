/* eslint-disable react/destructuring-assignment */
import React from 'react';
import * as THREE from 'three';

import type { EmptyRendererProps, EmptyRendererState } from './types';

/**
 * Empty component. Renders and displays an empty graphics data.
 */
export class EmptyRenderer extends React.PureComponent<EmptyRendererProps, EmptyRendererState> {
  /** THREE.Scene for this channel */
  public scene: THREE.Scene;

  /** Orthographic camera used to zoom/pan around the spectrogram */
  public camera: THREE.OrthographicCamera;

  /** Current min in gl units */
  private readonly glMin = 0;

  /** Current max in gl units */
  private readonly glMax = 100;

  /**
   * Constructor
   *
   * @param props props as SpectrogramRendererProps
   */
  public constructor(props: EmptyRendererProps) {
    super(props);
    this.state = {};
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Called immediately after a component is mounted.
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount(): void {
    this.scene = new THREE.Scene();
    const cameraZDepth = 5;
    this.camera = new THREE.OrthographicCamera(
      this.glMin,
      this.glMax,
      1,
      -1,
      cameraZDepth,
      -cameraZDepth
    );
    this.camera.position.z = 0;
  }

  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, react/sort-comp
  public render() {
    return null;
  }
}
