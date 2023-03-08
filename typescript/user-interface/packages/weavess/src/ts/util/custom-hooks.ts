import * as React from 'react';

/**
 * Hook that keeps track of mouse location and initial mouse x
 *
 * @returns mouse information and setter for mouseX
 */
export const useFollowMouse = (): {
  initialMouseX: number | undefined;
  onMouseMove: (event: MouseEvent) => void;
  setMouseX: React.Dispatch<React.SetStateAction<number>>;
  mouseX: number;
  mouseY: number;
} => {
  const initialMouseX = React.useRef<number | undefined>(undefined);
  const [mouseX, setMouseX] = React.useState(0);
  const [mouseY, setMouseY] = React.useState(0);
  const onMouseMove = React.useCallback((event: MouseEvent) => {
    setMouseX(event.clientX);
    setMouseY(event.clientY);
  }, []);

  return {
    initialMouseX: initialMouseX.current,
    onMouseMove,
    setMouseX,
    mouseX,
    mouseY
  };
};
