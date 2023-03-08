import React from 'react';
import ReactDOMServer from 'react-dom/server';

/**
 * Function to generate a string for Cesium to display a billboard for selected event circles
 */
export function buildSelectedEventCircle(strokeOpacity = 1, fillOpacity = 1): string {
  return `data:image/svg+xml,${encodeURIComponent(
    ReactDOMServer.renderToStaticMarkup(
      <svg
        xmlns="http://www.w3.org/2000/svg"
        id="selected_event_circle"
        data-name="Selected Event Circle"
        width="168"
        height="168"
        viewBox="0 0 168 168"
      >
        <defs>
          <filter id="Ellipse_69" x="0" y="0" width="168" height="168" filterUnits="userSpaceOnUse">
            <feOffset dy="8" />
            <feGaussianBlur stdDeviation="6" result="blur" />
            <feFlood floodOpacity="0.596" />
            <feComposite operator="in" in2="blur" />
            <feComposite in="SourceGraphic" />
          </filter>
        </defs>
        <g id="Group_704" data-name="Group 704" transform="translate(-1045 -425)">
          <g id="Group_705" data-name="Group 705" transform="translate(1063 435)">
            <g transform="matrix(1, 0, 0, 1, -18, -10)" filter="url(#Ellipse_69)">
              <circle
                id="Ellipse_69-2"
                data-name="Ellipse 69"
                cx="66"
                cy="66"
                r="66"
                transform="translate(18 10)"
                fill="#4087cb"
                style={{
                  fillOpacity: strokeOpacity
                }}
              />
            </g>
            <g id="Group_706" data-name="Group 706" transform="translate(14.839 14.839)">
              <circle
                id="Ellipse_70"
                data-name="Ellipse 70"
                cx="47"
                cy="47"
                r="47"
                transform="translate(4.161 4.161)"
                fill="#fff"
                style={{
                  fillOpacity,
                  strokeOpacity
                }}
              />
              <path
                id="Ellipse_70_-_Outline"
                data-name="Ellipse 70 - Outline"
                d="M51.111,10.222A40.888,40.888,0,1,0,92,51.111,40.935,40.935,0,0,0,51.111,10.222M51.111,0A51.111,51.111,0,1,1,0,51.111,51.111,51.111,0,0,1,51.111,0Z"
                transform="translate(0 0)"
                fill="#10161a"
              />
            </g>
          </g>
        </g>
      </svg>
    )
  )}`;
}
