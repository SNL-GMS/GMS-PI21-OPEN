/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable import/namespace */
/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable @typescript-eslint/no-unused-vars */

import * as Cesium from 'cesium';

import * as IanMapTooltipUtils from '../../../../../src/ts/components/analyst-ui/components/map/ian-map-tooltip-utils';
import * as IanMapUtils from '../../../../../src/ts/components/analyst-ui/components/map/ian-map-utils';
import { mockSd } from './map-sd-mock-data';

jest.mock('@blueprintjs/core', () => {
  const actualBlueprint = jest.requireActual('@blueprintjs/core');
  return {
    ...actualBlueprint,
    ContextMenu: {
      show: jest.fn()
    }
  };
});
// Mock console.warn so they are not getting out put to the test log
// several tests are unhappy path tests and will console warn
// eslint-disable-next-line no-console
console.warn = jest.fn();
describe('Ian map tooltip utils', () => {
  test('are defined', () => {
    expect(IanMapTooltipUtils.ianMapStationTooltipLabel).toBeDefined();
    expect(IanMapTooltipUtils.ianMapEventTooltipLabel).toBeDefined();
    expect(IanMapTooltipUtils.ianMapTooltipHandleMouseMove).toBeDefined();
    expect(IanMapTooltipUtils.ianMapTooltipHandleAltClick).toBeDefined();
    expect(IanMapTooltipUtils.clearEventTooltip).toBeDefined();
    expect(IanMapTooltipUtils.clearHoverTooltip).toBeDefined();
    expect(IanMapTooltipUtils.formatEntityAsTooltip).toBeDefined();
  });
  test('ianMapStationTooltipLabel should match snapshot', () => {
    expect(IanMapTooltipUtils.ianMapStationTooltipLabel).toMatchSnapshot();
  });

  describe('ianMapTooltipHandleMouseMove', () => {
    const movement: any = {
      endPosition: 'position'
    };

    const tooltipDataSource = {
      name: 'Tooltip',
      entities: {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        getById: jest.fn(id => {
          return {
            id: 'hoverLabelEntity',
            label: {
              text: 'default'
            }
          };
        }),
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        add: jest.fn(incomingEntity => {
          return IanMapTooltipUtils.ianMapStationTooltipLabelOptions;
        })
      }
    };
    const viewer = {
      dataSources: {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        getByName: jest.fn(name => {
          return [tooltipDataSource];
        }),
        entities: {
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          getById: jest.fn(id => {
            return undefined;
          }),

          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          add: jest.fn(incomingEntity => {
            return IanMapTooltipUtils.ianMapStationTooltipLabelOptions;
          })
        }
      },
      scene: {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        pickPosition: jest.fn(endPosition => {
          return 'myPosition';
        }),
        requestRender: jest.fn(),
        globe: { ellipsoid: undefined }
      },
      camera: {
        pickEllipsoid: jest.fn(endPosition => {
          return 'myPosition';
        })
      }
    };

    const stationProperties = {
      coordinates: {
        _value: {
          latitude: 100,
          longitude: 100,
          elevation: 100
        }
      },
      statype: {
        _value: 'SEISMIC_3_COMPONENT'
      },
      type: 'Station'
    };

    const channelGroupProperties = {
      coordinates: {
        _value: {
          latitude: 100,
          longitude: 100,
          elevation: 100
        }
      },
      type: 'ChannelGroup'
    };

    const signalDetectionProperties = {
      phaseValue: {
        value: 'P'
      },
      stationName: 'station',
      type: 'Signal detection'
    };

    const eventProperties = {
      type: 'Event location',
      event: {
        id: 'eventId',
        time: 0,
        latitudeDegrees: 1,
        longitudeDegrees: 2,
        depthKm: 3
      }
    };

    beforeEach(() => {
      jest.clearAllMocks();
      // reassign getObjectFromPoint to get a empty entity back
      // like we would if nothing is below the hovered point
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return {};
        }
      });
    });

    test('should handle updating the labels for a Station', async () => {
      const selectedEntityStation = {
        name: 'AAK',
        properties: { getValue: jest.fn(() => stationProperties) },
        position: {
          getValue: jest.fn(() => {
            return { x: 1, y: 2, z: 3 };
          })
        }
      };

      const defaultLabelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );
      // make sure our ID is correct
      expect(defaultLabelEntity.id).toEqual('hoverLabelEntity');
      expect(defaultLabelEntity).toMatchSnapshot();
      expect(defaultLabelEntity.label.show._value).toBeFalsy();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(1);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);

      // reassign getObjectFromPoint to get a correct entity back
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return selectedEntityStation;
        }
      });

      const labelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );
      // make sure our ID is correct
      expect(labelEntity.id).toEqual('hoverLabelEntity');
      expect(labelEntity.label.show._value).toBeTruthy();
      expect(labelEntity).toMatchSnapshot();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(2);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);
    });

    test('should handle updating the labels for a Channel Group', async () => {
      const selectedEntityChannelGroup = {
        name: 'AAK0',
        properties: { getValue: jest.fn(() => channelGroupProperties) },
        position: {
          getValue: jest.fn(() => {
            return { x: 1, y: 2, z: 3 };
          })
        }
      };

      const defaultLabelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );
      // make sure our ID is correct
      expect(defaultLabelEntity.id).toEqual('hoverLabelEntity');
      expect(defaultLabelEntity.label.show._value).toBeFalsy();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(1);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);

      // reassign getObjectFromPoint to get a correct entity back
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return selectedEntityChannelGroup;
        }
      });

      const labelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );
      // make sure our ID is correct
      expect(labelEntity.id).toEqual('hoverLabelEntity');
      expect(labelEntity).toMatchSnapshot();
      expect(labelEntity.label.show._value).toBeTruthy();

      expect(tooltipDataSource.entities.getById).toBeCalledTimes(2);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);
    });

    test('should handle updating the labels for a signal detection', async () => {
      const selectedEntitySignalDetection = {
        properties: { getValue: jest.fn(() => signalDetectionProperties) },
        position: {
          getValue: jest.fn(() => {
            return { x: 1, y: 2, z: 3 };
          })
        }
      };

      const defaultLabelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );
      // make sure our ID is correct
      expect(defaultLabelEntity.id).toEqual('hoverLabelEntity');
      expect(defaultLabelEntity.label.show._value).toBeFalsy();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(1);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);

      // reassign getObjectFromPoint to get a correct entity back
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return selectedEntitySignalDetection;
        }
      });

      const labelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );
      // make sure our ID is correct
      expect(labelEntity.id).toEqual('hoverLabelEntity');
      expect(labelEntity).toMatchSnapshot();
      expect(labelEntity.label.show._value).toBeTruthy();

      expect(tooltipDataSource.entities.getById).toBeCalledTimes(2);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);
    });

    test('should handle updating the labels for an Event', async () => {
      const selectedEvent = {
        properties: { getValue: jest.fn(() => eventProperties) },
        position: {
          getValue: jest.fn(() => {
            return { x: 1, y: 2, z: 3 };
          })
        }
      };

      const defaultLabelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );

      // make sure our ID is correct
      expect(defaultLabelEntity.id).toEqual('hoverLabelEntity');
      expect(defaultLabelEntity.label.show._value).toBeFalsy();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(1);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);

      // reassigned getObjectFromPoint to get a correct entity back
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => selectedEvent
      });

      const labelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );

      // make sure our ID is correct
      expect(labelEntity.id).toEqual('hoverLabelEntity');
      expect(labelEntity).toMatchSnapshot();
      expect(labelEntity.label.show._value).toBeTruthy();

      expect(tooltipDataSource.entities.getById).toBeCalledTimes(2);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);
    });

    test('should not show a tooltip if the hovered entity has no properties', async () => {
      const selectedEntityChannelGroupNoProperties = {
        name: 'AAK0'
      };

      // reassign getObjectFromPoint to get a correct entity back
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return selectedEntityChannelGroupNoProperties;
        }
      });

      const labelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );
      // make sure our ID is correct
      expect(labelEntity.id).toEqual('hoverLabelEntity');
      expect(labelEntity.label.show._value).toBe(false);
    });

    test('should not show a tooltip if the hovered entity is not a station or channel group', async () => {
      const selectedEntityWrongType =
        // reassign getObjectFromPoint to get a correct entity back
        Object.assign(IanMapTooltipUtils, {
          ...IanMapTooltipUtils,
          getObjectFromPoint: () => {
            return selectedEntityWrongType;
          }
        });

      const labelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(
        movement,
        viewer as any
      );
      // make sure our ID is correct
      expect(labelEntity.id).toEqual('hoverLabelEntity');
      expect(labelEntity.label.show._value).toBe(false);
    });

    test('should not show a tooltip if the tooltip datasource is not present', async () => {
      const labelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(movement, {
        dataSources: { getByName: jest.fn(x => undefined) }
      } as any);
      // should return undefined
      expect(labelEntity).not.toBeDefined();
    });

    test('should not show a tooltip if there is no label entity', async () => {
      const labelEntity: any = await IanMapTooltipUtils.ianMapTooltipHandleMouseMove(movement, {
        dataSources: {
          getByName: jest.fn(x => [{ entities: { getById: jest.fn(y => undefined) } }])
        }
      } as any);
      // should return undefined
      expect(labelEntity).not.toBeDefined();
    });
  });

  describe('ianMapTooltipHandleAltClick', () => {
    const movement = {
      position: 'position'
    };

    const tooltipDataSource = {
      name: 'Tooltip',
      entities: {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        getById: jest.fn(id => {
          return {
            id: 'eventLabelEntity',
            label: {
              text: 'default',
              show: new Cesium.ConstantProperty(false)
            }
          };
        }),
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        add: jest.fn(incomingEntity => {
          return IanMapTooltipUtils.ianMapEventTooltipLabelOptions;
        })
      }
    };
    const viewer = {
      dataSources: {
        getByName: jest.fn(() => {
          return [tooltipDataSource];
        }),
        entities: {
          getById: jest.fn(() => {
            return undefined;
          }),

          add: jest.fn(() => {
            return IanMapTooltipUtils.ianMapEventTooltipLabelOptions;
          })
        }
      },
      scene: {
        pickPosition: jest.fn(() => {
          return 'myPosition';
        }),
        requestRender: jest.fn()
      }
    };

    const eventProperties = {
      type: 'Event location',
      event: {
        id: 'eventId',
        time: 0,
        latitudeDegrees: 1,
        longitudeDegrees: 2,
        depthKm: 3
      }
    };
    // set up canvas focus so we dont get any errors

    beforeEach(() => {
      jest.clearAllMocks();
      // reassign getObjectFromPoint to get a empty entity back
      // like we would if nothing is below the hovered point
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return {};
        }
      });
    });

    test('should handle updating the labels for an event', () => {
      const selectedEntityEvent = {
        properties: { getValue: jest.fn(() => eventProperties) },
        position: {
          getValue: jest.fn(() => {
            return { x: 4, y: 5, z: 6 };
          })
        }
      };

      const defaultLabelEntity: any = IanMapTooltipUtils.ianMapTooltipHandleAltClick(
        movement,
        viewer as any
      );
      // should not receive a label back from this non-event entity mock
      expect(defaultLabelEntity).not.toBeDefined();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(0);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);

      // reassign getObjectFromPoint to get a correct entity back
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return selectedEntityEvent;
        }
      });

      const labelEntity: any = IanMapTooltipUtils.ianMapTooltipHandleAltClick(
        movement,
        viewer as any
      );
      // ensure undefined, no longer displaying details in tooltip
      expect(labelEntity).not.toBeDefined();
      expect(labelEntity).toMatchSnapshot();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(0);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);
    });

    test('clicking an sd does not inhibit updating event labels', () => {
      const sdProperties = mockSd;
      const selectedEntitySd = {
        properties: { getValue: jest.fn(() => sdProperties) },
        position: {
          getValue: jest.fn(() => {
            return { x: 4, y: 5, z: 6 };
          })
        }
      };

      const defaultLabelEntity: any = IanMapTooltipUtils.ianMapTooltipHandleAltClick(
        movement,
        viewer as any
      );
      // should not receive a label entity back for a non-event entity
      expect(defaultLabelEntity).not.toBeDefined();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(0);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return selectedEntitySd;
        }
      });
      const labelEntity: any = IanMapTooltipUtils.ianMapTooltipHandleAltClick(
        movement,
        viewer as any
      );
      // should not get a label entity back when clicking on a SD
      expect(labelEntity).not.toBeDefined();
      expect(tooltipDataSource.entities.getById).toBeCalledTimes(0);
      expect(tooltipDataSource.entities.add).toBeCalledTimes(0);
    });

    test('should not show a tooltip if the hovered entity has no properties', () => {
      const selectedEntityEventNoProperties = {};

      // reassign getObjectFromPoint to get a correct entity back
      Object.assign(IanMapUtils, {
        ...IanMapUtils,
        getObjectFromPoint: () => {
          return selectedEntityEventNoProperties;
        }
      });

      const labelEntity: any = IanMapTooltipUtils.ianMapTooltipHandleAltClick(
        movement,
        viewer as any
      );
      // should not receive a label entity back
      expect(labelEntity).not.toBeDefined();
    });

    test('should not show a tooltip if the hovered entity is not an event location', () => {
      const selectedEntityWrongType =
        // reassign getObjectFromPoint to get a correct entity back
        Object.assign(IanMapTooltipUtils, {
          ...IanMapTooltipUtils,
          getObjectFromPoint: () => {
            return selectedEntityWrongType;
          }
        });

      const labelEntity: any = IanMapTooltipUtils.ianMapTooltipHandleAltClick(
        movement,
        viewer as any
      );
      // should not receive a label entity back
      expect(labelEntity).not.toBeDefined();
    });

    test('should not show a tooltip if the tooltip datasource is not present', () => {
      const labelEntity: any = IanMapTooltipUtils.ianMapTooltipHandleAltClick(movement, {
        dataSources: { getByName: jest.fn(x => undefined) }
      } as any);
      // should return undefined
      expect(labelEntity).not.toBeDefined();
    });

    test('should not show a tooltip if there is no label entity', () => {
      const labelEntity: any = IanMapTooltipUtils.ianMapTooltipHandleAltClick(movement, {
        dataSources: {
          getByName: jest.fn(x => [{ entities: { getById: jest.fn(y => undefined) } }])
        }
      } as any);
      // should return undefined
      expect(labelEntity).not.toBeDefined();
    });
  });

  describe('clearEventTooltip', () => {
    it('handles the escape key', () => {
      const mockTooltipEntity: any = { label: { show: new Cesium.ConstantProperty(true) } };

      const mockTooltipGetById: any = jest.fn(() => mockTooltipEntity);

      const mockTooltipDataSource: any = {
        entities: {
          getById: mockTooltipGetById
        }
      };

      const mockRender: any = jest.fn();

      IanMapTooltipUtils.setViewer({
        dataSources: { getByName: jest.fn(() => [mockTooltipDataSource]) },
        scene: {
          requestRender: mockRender
        }
      } as any);

      const mockEscapeEvent: any = { key: 'Escape' };

      IanMapTooltipUtils.clearEventTooltip(mockEscapeEvent);

      expect(mockTooltipGetById).toHaveBeenCalledWith('eventLabelEntity');

      expect(mockTooltipEntity.label.show._value).toBeFalsy();
      expect(mockRender).toBeCalled();
    });
    it('handles a non escape key', () => {
      const mockTooltipEntity: any = { label: { show: new Cesium.ConstantProperty(true) } };

      const mockTooltipGetById: any = jest.fn(() => mockTooltipEntity);

      const mockTooltipDataSource: any = {
        entities: {
          getById: mockTooltipGetById
        }
      };

      const mockRender: any = jest.fn();

      IanMapTooltipUtils.setViewer({
        dataSources: { getByName: jest.fn(() => [mockTooltipDataSource]) },
        scene: {
          requestRender: mockRender
        }
      } as any);

      const mockNonEscapeEvent: any = { key: 'E' };

      IanMapTooltipUtils.clearEventTooltip(mockNonEscapeEvent);

      expect(mockTooltipGetById).toHaveBeenCalledTimes(0);

      expect(mockTooltipEntity.label.show._value).toBeTruthy();
      expect(mockRender).toHaveBeenCalledTimes(0);
    });
  });

  describe('clearHoverTooltip', () => {
    it('handles the escape key', () => {
      const mockTooltipEntity: any = { label: { show: new Cesium.ConstantProperty(true) } };

      const mockTooltipGetById: any = jest.fn(() => mockTooltipEntity);

      const mockTooltipDataSource: any = {
        entities: {
          getById: mockTooltipGetById
        }
      };

      const mockRender: any = jest.fn();

      IanMapTooltipUtils.setViewer({
        dataSources: { getByName: jest.fn(() => [mockTooltipDataSource]) },
        scene: {
          requestRender: mockRender
        }
      } as any);

      const mockEscapeEvent: any = { key: 'Escape' };

      IanMapTooltipUtils.clearHoverTooltip(mockEscapeEvent);

      expect(mockTooltipGetById).toHaveBeenCalledWith('hoverLabelEntity');

      expect(mockTooltipEntity.label.show._value).toBeFalsy();
      expect(mockRender).toBeCalled();
    });
    it('handles a non escape key', () => {
      const mockTooltipEntity: any = { label: { show: new Cesium.ConstantProperty(true) } };

      const mockTooltipGetById: any = jest.fn(() => mockTooltipEntity);

      const mockTooltipDataSource: any = {
        entities: {
          getById: mockTooltipGetById
        }
      };

      const mockRender: any = jest.fn();

      IanMapTooltipUtils.setViewer({
        dataSources: { getByName: jest.fn(() => [mockTooltipDataSource]) },
        scene: {
          requestRender: mockRender
        }
      } as any);

      const mockNonEscapeEvent: any = { key: 'E' };

      IanMapTooltipUtils.clearHoverTooltip(mockNonEscapeEvent);

      expect(mockTooltipGetById).toHaveBeenCalledTimes(0);

      expect(mockTooltipEntity.label.show._value).toBeTruthy();
      expect(mockRender).toHaveBeenCalledTimes(0);
    });
  });
});
