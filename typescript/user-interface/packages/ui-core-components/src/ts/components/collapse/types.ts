export interface CollapseButtonProps {
  // Button text can be either a string of text, or a function
  // that returns the desired text that will appear on the button
  buttonText: string | ((isVisible: boolean) => string);
  isLoading?: boolean;
  isCollapsed?: boolean;
  onClick?(isVisible: boolean): void;
}
