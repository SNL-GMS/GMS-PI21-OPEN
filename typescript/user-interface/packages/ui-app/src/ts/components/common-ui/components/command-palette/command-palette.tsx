/* eslint-disable react/destructuring-assignment */
import isEqual from 'lodash/isEqual';
import * as React from 'react';

import type { CommandPaletteContextData } from './command-palette-context';
import { CommandPaletteContext } from './command-palette-context';
import { CommandPaletteSearchManager } from './command-palette-search-manager';
import type { CommandPaletteProps, CommandPaletteState } from './types';
import { searchCommandsForTerms } from './utils';

/**
 * Command Palette creates a text input that searches a configured list of commands and presents
 * a dropdown of any commands that match the search.
 */
export class CommandPalette extends React.Component<CommandPaletteProps, CommandPaletteState> {
  /** Type for Command Palette context - provides the functions and data needed to create the commands */
  // eslint-disable-next-line react/static-property-placement
  public static contextType: React.Context<CommandPaletteContextData> = CommandPaletteContext;

  /** The Command Palette context - provides the functions and data needed to create the commands */
  // eslint-disable-next-line react/static-property-placement
  public declare context: React.ContextType<typeof CommandPaletteContext>;

  /** The previous context, used to check if the context has changed. */
  private prevContext: React.ContextType<typeof CommandPaletteContext>;

  public constructor(props) {
    super(props);
    this.state = {
      defaultSearchResults: props.defaultSearchResults ?? []
    };
  }

  /**
   * only update if the command palette is visible. Otherwise, it's a waste of resources.
   *
   * @returns boolean
   */
  public shouldComponentUpdate(nextProps: CommandPaletteProps): boolean {
    if (nextProps.isVisible) {
      return true;
    }
    return false;
  }

  /** If the context has changed, regenerate commands and default search results */
  public componentDidUpdate(): void {
    if (!isEqual(this.prevContext, this.context)) {
      this.prevContext = this.context;
      const updatedDefaultSearchResults = searchCommandsForTerms(
        this.props.defaultSearchTerms ?? [],
        this.props.commandActions
      );
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        defaultSearchResults: updatedDefaultSearchResults
      });
    }
  }

  public render(): JSX.Element {
    return (
      <CommandPaletteSearchManager
        commandActions={this.props.commandActions}
        defaultSearchResults={this.state.defaultSearchResults}
        // eslint-disable-next-line react/jsx-props-no-spreading
        {...this.props}
      />
    );
  }
}
