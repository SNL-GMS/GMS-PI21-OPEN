/* eslint-disable react/destructuring-assignment */
import { DragEventType, overrideDragCursor, storeDragData } from '@gms/ui-util';
import * as React from 'react';

export interface DragInitiatorProps<Payload> {
  getDragPayload(event: React.DragEvent<HTMLDivElement>): Payload;
  onDragStart?(event: React.DragEvent<HTMLDivElement>): void;
  onDragEnd?(event: React.DragEvent<HTMLDivElement>): void;
  getDragImage?(event: React.DragEvent<HTMLDivElement>): Element;
}

export class DragInitiator<Payload> extends React.PureComponent<
  React.PropsWithChildren<DragInitiatorProps<Payload>>
> {
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div draggable onDragStart={this.onDragStart} onDragEnd={this.props.onDragEnd}>
        {this.props.children}
      </div>
    );
  }

  /**
   * onCellDragStart event
   *
   * @param event the drag event
   */
  private readonly onDragStart = (event: React.DragEvent<HTMLDivElement>) => {
    const dragData: Payload = this.props.getDragPayload(event);
    const dragImage: Element = this.props.getDragImage(event);
    if (dragImage) {
      overrideDragCursor(event, dragImage);
    }
    storeDragData(event, dragData, DragEventType.SOH_ACKNOWLEDGEMENT);
    if (this.props.onDragStart) {
      this.props.onDragStart(event);
    }
  };
}
