import * as React from 'react';

export interface DottedLineIconProps {
  className: string;
  color: string;
}

/**
 * A dotted line icon made from an svg
 *
 * @param props color and className
 * @returns dotted line svg icon
 */
// eslint-disable-next-line react/function-component-definition
export const DottedLineIcon: React.FunctionComponent<DottedLineIconProps> = (
  props: DottedLineIconProps
) => {
  const { color, className } = props;
  return (
    <svg
      id="Dotted_Line_Icon_SVG"
      className={className}
      data-name="Layer 1"
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 16 16"
      style={{
        width: '1rem',
        height: '1rem'
      }}
    >
      <g id="Dotted_Line_Icon" data-name="Dotted Line Icon" transform="translate(1 1)">
        <path
          d="M4,9h-1c-.55,0-1-.45-1-1s.45-1,1-1h1c.55,0,1,.45,1,1s-.45,1-1,1Z"
          style={{ fill: color }}
        />
        <path
          d="M8.5,9h-1c-.55,0-1-.45-1-1s.45-1,1-1h1c.55,0,1,.45,1,1s-.45,1-1,1Z"
          style={{ fill: color }}
        />
        <path
          d="M13,9h-1c-.55,0-1-.45-1-1s.45-1,1-1h1c.55,0,1,.45,1,1s-.45,1-1,1Z"
          style={{ fill: color }}
        />
      </g>
    </svg>
  );
};
