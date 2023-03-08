export interface TextAreaProps {
  defaultValue: string;
  title: string;
  maxChar?: number;
  // data field for cypress testing
  'data-cy'?: string;

  onMaybeValue(value: any);
}
