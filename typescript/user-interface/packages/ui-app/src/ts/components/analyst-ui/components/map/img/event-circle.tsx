import React from 'react';
import ReactDOMServer from 'react-dom/server';

/**
 * Function to generate a string for Cesium to display a billboard for event circles
 */
export function buildEventCircle(strokeOpacity = 1, fillOpacity = 1): string {
  return `data:image/svg+xml,${encodeURIComponent(
    ReactDOMServer.renderToStaticMarkup(
      <svg
        id="event_circle"
        data-name="Event Circle"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 124 124"
        height="124"
        width="124"
      >
        <circle
          cx="62.71"
          cy="62.25"
          r="57.07"
          style={{
            fill: '#fff',
            stroke: 'black',
            strokeWidth: 8,
            strokeOpacity,
            fillOpacity
          }}
        />
      </svg>
    )
  )}`;
}
