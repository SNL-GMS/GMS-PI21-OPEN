/**
 * This odd way of exporting an SVG is a result of the current version of AG-Grid expecting a string for a custom
 * header template. We are working on upgrading to the latest version soon, where we will instead use this as jsx in
 * a custom header component, after which we will replace this temporary stopgap.
 */
export const unsavedChangesIcon = `
<svg xmlns="http://www.w3.org/2000/svg" width="22.627" height="22.628" viewBox="0 0 22.627 22.628">
  <defs>
    <style>
      .cls-1 {
        fill: var(--gms-main, f5f8fa);
      }
    </style>
  </defs>
  <path id="Unsaved" class="cls-1" d="M400.253-4231.333H383.961a1,1,0,0,1-1-1v-16.292l-1.96-1.96,1.414-1.414,21.213,21.213-1.414,1.415Zm-14.293-9v8h13.292l-9-9H386.96A1,1,0,0,0,385.96-4240.333Zm17,6.29-3-3v-3.292a1,1,0,0,0-1-1h-3.293l-3-3h5.293v-7h1a.99.99,0,0,1,.71.291l3,3a.992.992,0,0,1,.29.709v13.293Zm-6-11.29h-3v-5h3v5h0Zm-9-3.709-2.291-2.292h2.293v2.293Z" transform="translate(-381 4252)"/>
</svg>`;
