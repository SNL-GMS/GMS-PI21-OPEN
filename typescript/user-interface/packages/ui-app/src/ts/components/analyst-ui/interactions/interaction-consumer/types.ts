export interface InteractionConsumerReduxProps {
  keyPressActionQueue: Record<string, number>;
  setKeyPressActionQueue(actions: Record<string, number>): void;
}

export type InteractionConsumerProps = InteractionConsumerReduxProps;
