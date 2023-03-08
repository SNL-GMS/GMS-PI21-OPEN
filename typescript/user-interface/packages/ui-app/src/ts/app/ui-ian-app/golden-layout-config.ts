import { Displays, UserProfileTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import { withReduxProvider } from '@gms/ui-state';
import { isCypress, isElectron } from '@gms/ui-util';

import { AnalystUiComponents } from '~components/analyst-ui';
import type {
  GLComponentConfig,
  GLComponentConfigList,
  GLMap,
  GoldenLayoutContextData
} from '~components/workspace';
import type { GLComponentValue } from '~components/workspace/components/golden-layout/types';

/**
 * Wraps the component for a golden layout panel.
 * Provides the required context providers to the component.
 *
 * @param Component the golden layout component
 */
// eslint-disable-next-line
const wrap = (Component: any) => withReduxProvider(Component);

// ! CAUTION: when changing the golden-layout component name
// The route paths must match the `golden-layout` component name for popout windows
// For example, the component name `signal-detections` must have the route path of `signal-detections`

const commonUIComponents: Map<string, GLComponentConfig> = new Map([
  [
    Displays.CommonDisplays.SYSTEM_MESSAGES,
    {
      type: 'react-component',
      title: 'System Messages',
      component: Displays.CommonDisplays.SYSTEM_MESSAGES
    }
  ]
]);

const ianComponents: Map<string, GLComponentConfig> = new Map([
  [
    Displays.IanDisplays.EVENTS,
    {
      type: 'react-component',
      title: Displays.toDisplayTitle(Displays.IanDisplays.EVENTS),
      component: Displays.IanDisplays.EVENTS
    }
  ],
  [
    Displays.IanDisplays.MAP,
    {
      type: 'react-component',
      title: Displays.toDisplayTitle(Displays.IanDisplays.MAP),
      component: Displays.IanDisplays.MAP
    }
  ],
  [
    Displays.IanDisplays.SIGNAL_DETECTIONS,
    {
      type: 'react-component',
      title: Displays.toDisplayTitle(Displays.IanDisplays.SIGNAL_DETECTIONS),
      component: Displays.IanDisplays.SIGNAL_DETECTIONS
    }
  ],
  [
    Displays.IanDisplays.STATION_PROPERTIES,
    {
      type: 'react-component',
      title: Displays.toDisplayTitle(Displays.IanDisplays.STATION_PROPERTIES),
      component: Displays.IanDisplays.STATION_PROPERTIES
    }
  ],
  [
    Displays.IanDisplays.WORKFLOW,
    {
      type: 'react-component',
      title: Displays.toDisplayTitle(Displays.IanDisplays.WORKFLOW),
      component: Displays.IanDisplays.WORKFLOW
    }
  ],
  [
    Displays.IanDisplays.WAVEFORM,
    {
      type: 'react-component',
      title: Displays.toDisplayTitle(Displays.IanDisplays.WAVEFORM),
      component: Displays.IanDisplays.WAVEFORM
    }
  ],
  [
    Displays.IanDisplays.FILTERS,
    {
      type: 'react-component',
      title: Displays.toDisplayTitle(Displays.IanDisplays.FILTERS),
      component: Displays.IanDisplays.FILTERS
    }
  ]
]);

// Adding all components
const components: GLComponentConfigList = {};
[...ianComponents, ...commonUIComponents].forEach(([componentName, glComponent]) => {
  components[componentName] = glComponent;
});

const defaultGoldenLayoutConfig: GoldenLayout.Config = {
  settings: {
    showPopoutIcon: Boolean(isElectron() && !isCypress()),
    showMaximiseIcon: true,
    showCloseIcon: true
  },
  content: [
    {
      type: 'row',
      content: [
        {
          type: 'row',
          content: [
            {
              type: 'column',
              content: [
                {
                  ...ianComponents.get(Displays.IanDisplays.WORKFLOW)
                }
              ]
            }
          ]
        },
        {
          type: 'row',
          content: [
            {
              type: 'column',
              content: [
                {
                  ...ianComponents.get(Displays.IanDisplays.MAP)
                }
              ]
            }
          ]
        },
        {
          type: 'row',
          content: [
            {
              type: 'column',
              content: [
                {
                  ...ianComponents.get(Displays.IanDisplays.SIGNAL_DETECTIONS)
                }
              ]
            }
          ]
        },
        {
          type: 'row',
          content: [
            {
              type: 'column',
              content: [
                {
                  ...ianComponents.get(Displays.IanDisplays.STATION_PROPERTIES)
                }
              ]
            }
          ]
        },
        {
          type: 'column',
          content: [
            {
              ...ianComponents.get(Displays.IanDisplays.WAVEFORM),
              height: 70
            }
          ]
        }
      ]
    }
  ],
  dimensions: {
    borderWidth: 2,
    minItemHeight: 30,
    minItemWidth: 30,
    headerHeight: 30
  }
};

/**
 * The Golden Layout context for the IAN UI.
 * Note: Defines the Application Menu structure.
 */
const glComponents = (): GLMap =>
  new Map<string, GLComponentValue>([
    [
      'Analyst Displays',
      new Map([
        [
          components[Displays.IanDisplays.EVENTS].component,
          {
            id: components[Displays.IanDisplays.EVENTS],
            value: wrap(AnalystUiComponents.Events)
          }
        ],
        [
          components[Displays.IanDisplays.MAP].component,
          {
            id: components[Displays.IanDisplays.MAP],
            value: wrap(AnalystUiComponents.IANMap)
          }
        ],
        [
          components[Displays.IanDisplays.SIGNAL_DETECTIONS].component,
          {
            id: components[Displays.IanDisplays.SIGNAL_DETECTIONS],
            value: wrap(AnalystUiComponents.SignalDetectionsComponent)
          }
        ],
        [
          components[Displays.IanDisplays.STATION_PROPERTIES].component,
          {
            id: components[Displays.IanDisplays.STATION_PROPERTIES],
            value: wrap(AnalystUiComponents.StationPropertiesComponent)
          }
        ],
        [
          components[Displays.IanDisplays.WAVEFORM].component,
          {
            id: components[Displays.IanDisplays.WAVEFORM],
            value: wrap(AnalystUiComponents.WaveformDisplay)
          }
        ],
        [
          components[Displays.IanDisplays.WORKFLOW].component,
          {
            id: components[Displays.IanDisplays.WORKFLOW],
            value: wrap(AnalystUiComponents.Workflow)
          }
        ],
        [
          components[Displays.IanDisplays.FILTERS].component,
          {
            id: components[Displays.IanDisplays.FILTERS],
            value: wrap(AnalystUiComponents.Filters)
          }
        ]
      ])
    ]
  ]);

/** The Golden Layout context */
export const glContextData = (): GoldenLayoutContextData => ({
  glComponents: glComponents(),
  gl: undefined,
  glRef: undefined,
  config: {
    components,
    workspace: defaultGoldenLayoutConfig
  },
  supportedUserInterfaceMode: UserProfileTypes.UserMode.IAN
});
