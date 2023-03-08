/**
 * A NonIdealStateDefinition for a given type T pairs the non-ideal state component
 * with a condition that determines whether to render it.
 */
export interface NonIdealStateDefinition<PropsType, ChildPropsType = PropsType> {
  // The JSX to render for the non ideal state
  element: JSX.Element;
  // take in the component's props and return true if the non ideal state should be rendered.
  condition(props: PropsType): boolean;
  // An optional function that converts the props passed in into a format desired for the child component.
  // This is basically a way to transform the props, for example, to filter out query metadata when the
  // child component only cares about the query data.
  converter?: (props: PropsType) => ChildPropsType;
}
