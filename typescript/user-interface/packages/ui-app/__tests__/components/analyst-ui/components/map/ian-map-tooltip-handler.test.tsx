import { render } from '@testing-library/react';
import * as React from 'react';

import { IanMapTooltipHandler } from '../../../../../src/ts/components/analyst-ui/components/map/ian-map-tooltip-handler';
import { ianMapStationTooltipLabel } from '../../../../../src/ts/components/analyst-ui/components/map/ian-map-tooltip-utils';

describe('IanMapTooltipHandler', () => {
  test('is defined', () => {
    expect(IanMapTooltipHandler).toBeDefined();
  });
  test('can create a mouse move handler', () => {
    const viewer: any = {
      scene: {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        pickPosition: jest.fn(endPosition => {
          'myPosition';
        })
      },
      entities: {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        getById: jest.fn(id => {
          return undefined;
        }),
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        add: jest.fn(incomingEntity => {
          return ianMapStationTooltipLabel;
        })
      }
    };
    const { container } = render(<IanMapTooltipHandler viewer={viewer} />);
    expect(container).toMatchSnapshot();
  });
  test('can handle an undefined viewer', () => {
    const viewer: any = undefined;
    const { container } = render(<IanMapTooltipHandler viewer={viewer} />);
    expect(container).toMatchSnapshot();
  });
});
