/* eslint-disable react/jsx-no-useless-fragment */
/* eslint-disable react/function-component-definition */
import { ConfigurationTypes } from '@gms/common-model';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import {
  useCompleteEventSDColor,
  useDefaultInteractiveAnalysisStationGroup,
  useGetAllUiThemes,
  useLegibleColorsForEventAssociations,
  useMapStationDefaultColor,
  useMapVisibleStationColor,
  useOpenEventSDColor,
  useOtherEventSDColor,
  useUiTheme,
  useUnassociatedSDColor,
  useUnassociatedSignalDetectionLengthInMeters
} from '../../../src/ts/app/hooks/processing-analyst-configuration-hooks';
import { getStore } from '../../../src/ts/app/store';

const mockData: Partial<ConfigurationTypes.ProcessingAnalystConfiguration> = {
  unassociatedSignalDetectionLengthMeters: 10,
  defaultInteractiveAnalysisStationGroup: 'test',
  uiThemes: [
    {
      colors: ConfigurationTypes.defaultColorTheme,
      display: {
        edgeEventOpacity: 0.35,
        edgeSDOpacity: 0.2,
        predictionSDOpacity: 0.1
      },
      name: 'GMS Dark Theme'
    }
  ]
};

let data = {};

jest.mock(
  '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice',
  () => {
    const actual = jest.requireActual(
      '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice'
    );
    return {
      ...actual,
      useGetProcessingAnalystConfigurationQuery: jest.fn(() => ({
        data
      }))
    };
  }
);

jest.mock('../../../src/ts/app/api/user-manager/user-manager-api-slice', () => {
  const actual = jest.requireActual('../../../src/ts/app/api/user-manager/user-manager-api-slice');
  return {
    ...actual,
    useGetUserProfileQuery: jest.fn(() => ({
      data: { currentTheme: 'GMS Dark Theme' }
    }))
  };
});

describe('processing analyst configuration hooks', () => {
  it('exists', () => {
    expect(useUnassociatedSDColor).toBeDefined();
    expect(useCompleteEventSDColor).toBeDefined();
    expect(useOpenEventSDColor).toBeDefined();
    expect(useOtherEventSDColor).toBeDefined();
    expect(useUnassociatedSignalDetectionLengthInMeters).toBeDefined();
    expect(useDefaultInteractiveAnalysisStationGroup).toBeDefined();
    expect(useMapStationDefaultColor).toBeDefined();
    expect(useMapVisibleStationColor).toBeDefined();
    expect(useUiTheme).toBeDefined();
    expect(useGetAllUiThemes).toBeDefined();
  });

  it('can use default interactive analysis station group', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const result = useDefaultInteractiveAnalysisStationGroup();
      return <>{result}</>;
    };

    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use unassociated signal detection color', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const result = useUnassociatedSDColor();
      return <>{result}</>;
    };

    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use associated open signal detection color', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const result = useOpenEventSDColor();
      return <>{result}</>;
    };

    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use map default (non-visible) station color', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const result = useMapStationDefaultColor();
      return <>{result}</>;
    };

    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use visible station color', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const result = useMapVisibleStationColor();
      return <>{result}</>;
    };

    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use associated complete signal detection color', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const result = useCompleteEventSDColor();
      return <>{result}</>;
    };

    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use associated other signal detection color', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const result = useOtherEventSDColor();
      return <>{result}</>;
    };

    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use unassociated signal detection length in meters', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const result = useUnassociatedSignalDetectionLengthInMeters();
      return <>{result}</>;
    };

    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use ui theme', () => {
    const store = getStore();
    const Component: React.FC = () => {
      const [result] = useUiTheme();
      const resultString = JSON.stringify(result);
      return <>{resultString}</>;
    };
    data = undefined;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can retrieve all UI themes', () => {
    expect(useGetAllUiThemes()).toMatchSnapshot();
  });

  it('creates the expected custom css vars for our event association colors', () => {
    const Component: React.FC = () => {
      const result = useLegibleColorsForEventAssociations();
      const resultString = JSON.stringify(result);
      return <>{resultString}</>;
    };
    expect(
      create(
        <Provider store={getStore()}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });
});
