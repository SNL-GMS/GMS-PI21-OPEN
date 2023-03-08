/* eslint-disable max-len */
import cloneDeep from 'lodash/cloneDeep';

import type { Command } from '../../../../../src/ts/components/common-ui/components/command-palette/types';
import { CommandType } from '../../../../../src/ts/components/common-ui/components/command-palette/types';
import {
  commandMatchesSearchTerm,
  executeCommand,
  getCommandId,
  hasCommandListChanged,
  searchCommandsForTerms
} from '../../../../../src/ts/components/common-ui/components/command-palette/utils';

describe('Command palette utils', () => {
  const testCommand: Command = {
    action: jest.fn(),
    commandType: CommandType.LOG_OUT,
    displayText: 'Test Command',
    priority: 1,
    searchTags: ['test', 'command']
  };
  const testCommand2: Command = {
    action: jest.fn(),
    commandType: CommandType.OPEN_DISPLAY,
    displayText: 'Second Command',
    priority: 1,
    searchTags: ['two', 'command']
  };

  it('calls the command action executeCommand is called', () => {
    executeCommand(testCommand);
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(testCommand.action).toHaveBeenCalled();
  });

  it("can check if a search term is a match for a command's tags", () => {
    expect(commandMatchesSearchTerm(testCommand, 'test')).toBeTruthy();
    expect(commandMatchesSearchTerm(testCommand, 'command')).toBeTruthy();
  });

  it('does not match a command if searched for a term that is not present', () => {
    expect(commandMatchesSearchTerm(testCommand, 'miss')).toBeFalsy();
  });

  it('search finds a command if term is found in command display text', () => {
    expect(commandMatchesSearchTerm(testCommand, 'Command')).toBeTruthy();
  });

  it('search finds a command regardless of case', () => {
    expect(commandMatchesSearchTerm(testCommand, 'command')).toBeTruthy();
    expect(commandMatchesSearchTerm(testCommand, 'COMMAND')).toBeTruthy();
  });

  it('can find one command from a list of commands', () => {
    const foundSingleCommand = searchCommandsForTerms(['test'], [testCommand, testCommand2]);
    expect(foundSingleCommand).toHaveLength(1);
  });

  it('can find multiple command from a list of commands', () => {
    const foundMultipleCommands = searchCommandsForTerms(
      ['test', 'command'],
      [testCommand, testCommand2]
    );
    expect(foundMultipleCommands).toHaveLength(2);
  });

  it('can tell that a command list has changed when a new command is added', () => {
    const prevCommandList = [testCommand];
    const nextCommandList = [cloneDeep(testCommand), cloneDeep(testCommand2)];
    expect(hasCommandListChanged(prevCommandList, nextCommandList)).toBeTruthy();
  });

  it('can tell that a command list has not changed', () => {
    const prevCommandList = [testCommand, testCommand2];
    const nextCommandList = [cloneDeep(testCommand), cloneDeep(testCommand2)];
    expect(hasCommandListChanged(prevCommandList, nextCommandList)).toBeFalsy();
  });

  it('can tell that a command list has changed when display text changes', () => {
    const prevCommandList = [testCommand, testCommand2];
    const changedCommand = cloneDeep(testCommand);
    changedCommand.displayText = 'new display text';
    const nextCommandList = [changedCommand, cloneDeep(testCommand2)];
    expect(hasCommandListChanged(prevCommandList, nextCommandList)).toBeFalsy();
  });

  it('can tell that a command list has changed when command type changes', () => {
    const prevCommandList = [testCommand, testCommand2];
    const changedCommand = cloneDeep(testCommand);
    changedCommand.commandType = CommandType.CLEAR_LAYOUT;
    const nextCommandList = [changedCommand, cloneDeep(testCommand2)];
    expect(hasCommandListChanged(prevCommandList, nextCommandList)).toBeFalsy();
  });

  it('generates the same command id for the same command (pure function)', () => {
    const commandId = getCommandId(testCommand);
    const commandId2 = getCommandId(cloneDeep(testCommand));
    expect(commandId).toEqual(commandId2);
  });

  it('generates different command ids for different commands', () => {
    const commandId = getCommandId(testCommand);
    const commandId2 = getCommandId(testCommand2);
    expect(commandId === commandId2).toBeFalsy();
  });
});
