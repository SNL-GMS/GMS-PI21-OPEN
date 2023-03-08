/* eslint-disable react/function-component-definition */
/* eslint-disable @typescript-eslint/no-empty-function */
import { isCustomToolbarItem } from '@gms/ui-core-components/lib/components/ui-widgets/toolbar/toolbar-item/custom-item';
import { processingAnalystConfiguration } from '@gms/ui-state/__tests__/__data__/processing-analyst-configuration';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import * as React from 'react';

import {
  AmplitudeScalingOptions,
  useScalingOptions
} from '../../../../../../../../src/ts/components/analyst-ui/components/waveform/components/waveform-controls/scaling-options';
import { useQueryStateResult } from '../../../../../../../__data__/test-util-data';

const processingAnalystConfigurationQuery = cloneDeep(useQueryStateResult);
processingAnalystConfigurationQuery.data = {
  ...processingAnalystConfiguration,
  fixedAmplitudeScaleValues: [1, 2]
};

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetProcessingAnalystConfigurationQuery: jest.fn(() => processingAnalystConfigurationQuery)
  };
});

describe('Scaling Options Toolbar Item', () => {
  it('should exist', () => {
    expect(useScalingOptions).toBeDefined();
  });
  it('should return a ToolbarItem element that matches a snapshot', () => {
    const ScalingOptionWrapper: React.FC = () => {
      const newScalingOptionToolbarItem = useScalingOptions(
        AmplitudeScalingOptions.AUTO,
        1,
        () => {},
        () => {},
        'testscaling'
      );
      const { props: itemBase } = newScalingOptionToolbarItem.toolbarItem;
      return isCustomToolbarItem(itemBase) ? itemBase.element : <div />;
    };
    const { container } = render(<ScalingOptionWrapper />);
    expect(container).toMatchSnapshot();
  });
});
