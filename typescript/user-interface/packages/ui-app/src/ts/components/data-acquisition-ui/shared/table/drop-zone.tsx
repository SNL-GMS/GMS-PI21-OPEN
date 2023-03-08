/* eslint-disable react/destructuring-assignment */
import { dragEventIsOfType, DragEventType, getDragData } from '@gms/ui-util';
// eslint-disable-next-line no-restricted-imports
import type { Cancelable } from 'lodash';
import debounce from 'lodash/debounce';
import * as React from 'react';

export interface DropZoneProps<Payload> {
  className?: string;
  onDrop(payload: Payload): void;
}

export interface DropZoneState {
  isHighlighted: boolean;
}

export class DropZone<Payload> extends React.Component<
  React.PropsWithChildren<DropZoneProps<Payload>>,
  DropZoneState
> {
  private readonly setStateTryMs: number = 200;

  private readonly debouncedSetStateHighlight: (() => void) & Cancelable = debounce(
    () => {
      this.setState({ isHighlighted: true });
    },
    this.setStateTryMs,
    { leading: true }
  );

  public constructor(props) {
    super(props);
    this.state = {
      isHighlighted: false
    };
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div
        className={`drop-zone
          ${this.state.isHighlighted ? 'drop-zone--highlighted' : ''}
          ${this.props.className ?? ''}`}
        onDragOver={this.cellDragOver}
        onDrop={this.cellDrop}
      >
        {this.props.children}
      </div>
    );
  }

  /**
   * Cell drag over drop zone logic, checks if supported, data in transfer object and sets drop effect
   *
   * @param event React.DragEvent<HTMLDivElement>
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly cellDragOver = (e: React.DragEvent<HTMLDivElement>): void => {
    const event = e.nativeEvent;
    if (dragEventIsOfType(e, DragEventType.SOH_ACKNOWLEDGEMENT)) {
      event.stopPropagation();
      event.preventDefault();
    }
  };

  /**
   * @param event React.DragEvent<HTMLDivElement>
   */

  /**
   * Cell drag logic, gets data from transfer object and calls acknowledgeSohStatus
   *
   * @param event React.DragEvent<HTMLDivElement>
   * @param context context data for an soh panel
   */
  private readonly cellDrop = (event: React.DragEvent<HTMLDivElement>): void => {
    const payload = getDragData<Payload>(event, DragEventType.SOH_ACKNOWLEDGEMENT);
    this.debouncedSetStateHighlight.cancel();
    this.setState({ isHighlighted: false });
    this.props.onDrop(payload);
  };
}
