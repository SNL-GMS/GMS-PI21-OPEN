import type { CommandPaletteContextData } from '../../../src/ts/components/common-ui/components/command-palette/command-palette-context';

export const commandPaletteContextData: CommandPaletteContextData = {
  commandPaletteIsVisible: false,
  registerCommands: jest.fn(),
  setCommandPaletteVisibility: jest.fn()
};
