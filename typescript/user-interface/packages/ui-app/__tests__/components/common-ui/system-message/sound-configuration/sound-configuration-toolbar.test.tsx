import { render } from '@testing-library/react';
import * as React from 'react';

import { SoundConfigurationToolbar } from '../../../../../src/ts/components/common-ui/components/system-message/sound-configuration/sound-configuration-toolbar';
import {
  ALL_CATEGORIES,
  ALL_SEVERITIES,
  ALL_SUBCATEGORIES
} from '../../../../../src/ts/components/common-ui/components/system-message/sound-configuration/types';
import { systemMessageDefinitions } from '../../../../__data__/common-ui/system-message-definition-data';
// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Sound Configuration Toolbar', () => {
  const { container } = render(
    <SoundConfigurationToolbar
      systemMessageDefinitions={systemMessageDefinitions}
      selectedOptions={{
        selectedSeverity: ALL_SEVERITIES,
        selectedCategory: ALL_CATEGORIES,
        selectedSubcategory: ALL_SUBCATEGORIES
      }}
      onChanged={jest.fn()}
    />
  );

  it('should be defined', () => {
    expect(SoundConfigurationToolbar).toBeDefined();
  });

  it('matches snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
