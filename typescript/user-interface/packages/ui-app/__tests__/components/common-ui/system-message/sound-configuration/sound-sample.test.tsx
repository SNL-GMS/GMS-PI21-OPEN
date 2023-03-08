import { render } from '@testing-library/react';
import * as React from 'react';

import { SoundSample } from '../../../../../src/ts/components/common-ui/components/system-message/sound-configuration/sound-sample';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Sound Sample Component', () => {
  const { container } = render(<SoundSample soundToPlay="test.mp3" />);

  it('should be defined', () => {
    expect(SoundSample).toBeDefined();
    expect(container).toBeDefined();
  });

  it('matches snapshot', () => {
    expect(container).toMatchSnapshot();
  });

  const noneSound = render(<SoundSample soundToPlay="None" />);

  it("validates 'None' sound", () => {
    expect(noneSound.container).toMatchSnapshot();
  });
});
