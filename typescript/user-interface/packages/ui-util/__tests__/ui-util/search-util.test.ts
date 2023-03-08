/* eslint-disable @typescript-eslint/unbound-method */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import includes from 'lodash/includes';

import type { RangeTuple, SearchSet, SelectionManager } from '../../src/ts/ui-util/search-util';
import {
  highlightRanges,
  useSearch,
  useSearchResultSelectionManager,
  useVanillaSearch,
  vanillaSearch,
  wrapSelectedIndices
} from '../../src/ts/ui-util/search-util';
import { renderHook } from './utils/render-hook-util';

const matches = (element: string, searchTerm: string) => includes(element, searchTerm);
const searchSet: SearchSet<string> = [
  'the cat jumped the moon',
  'the chicken crossed the road',
  'the sun is bright'
];

describe('Search utils', () => {
  it('to be defined', () => {
    expect(highlightRanges).toBeDefined();
    expect(vanillaSearch).toBeDefined();
    expect(useSearchResultSelectionManager).toBeDefined();
    expect(useSearch).toBeDefined();
    expect(useVanillaSearch).toBeDefined();
    expect(wrapSelectedIndices).toBeDefined();
  });

  it('vanilla search', () => {
    expect(vanillaSearch(undefined, 'ball', matches)).toBeUndefined();
    expect(vanillaSearch(searchSet, undefined, matches)).toBeUndefined();
    expect(vanillaSearch(searchSet, 'ball', undefined)).toBeUndefined();

    expect(vanillaSearch(searchSet, 'hi', matches)).toMatchInlineSnapshot(`
      Array [
        Object {
          "item": "the chicken crossed the road",
          "refIndex": 1,
        },
      ]
    `);

    expect(vanillaSearch(searchSet, 'ball', matches)).toMatchInlineSnapshot(`Array []`);
  });

  it('useSearch', () => {
    const [searchResults, setSearchTerm, getSearchTerm] = renderHook(() =>
      useSearch((searchTerm: string) => vanillaSearch(searchSet, searchTerm, matches))
    );

    expect(searchResults).not.toBeDefined();
    expect(setSearchTerm).toBeDefined();
    expect(getSearchTerm).toBeDefined();

    setSearchTerm('ball');
  });

  it('useVanillaSearch', () => {
    const [searchResult, setSearchResult, fnc] = renderHook(() =>
      useVanillaSearch(searchSet, matches)
    );

    expect(searchResult).not.toBeDefined();
    expect(setSearchResult).toBeDefined();
    expect(fnc).toBeDefined();
  });

  it('useSearchResultSelectionManager', () => {
    const result = vanillaSearch(searchSet, 'h', matches);
    const selectionManager: SelectionManager<string> = renderHook(() =>
      useSearchResultSelectionManager(result)
    ) as any;

    expect(selectionManager.selectPrevious).toBeDefined();
    expect(selectionManager.selectNext).toBeDefined();
    expect(selectionManager.resetSelection).toBeDefined();
    expect(selectionManager.getSelectedResult).toBeDefined();

    expect(selectionManager.getSelectedResult()).toMatchInlineSnapshot(`
      Object {
        "item": "the cat jumped the moon",
        "refIndex": 0,
      }
    `);
    selectionManager.selectNext();
    expect(selectionManager.getSelectedResult()).toMatchInlineSnapshot(`
      Object {
        "item": "the cat jumped the moon",
        "refIndex": 0,
      }
    `);
    selectionManager.selectPrevious();
    expect(selectionManager.getSelectedResult()).toMatchInlineSnapshot(`
      Object {
        "item": "the cat jumped the moon",
        "refIndex": 0,
      }
    `);
    selectionManager.resetSelection();
    expect(selectionManager.getSelectedResult()).toMatchInlineSnapshot(`
      Object {
        "item": "the cat jumped the moon",
        "refIndex": 0,
      }
    `);
  });

  it('highlight ranges', () => {
    const str = 'the chicken crossed the road';
    let tuple: RangeTuple[] = [[0, str.length]];

    expect(highlightRanges(str, tuple)).toMatchInlineSnapshot(`
      <React.Fragment>
        
        <span
          className="is-highlighted"
        >
          the chicken crossed the road
        </span>
        
      </React.Fragment>
    `);

    tuple = [
      [0, 2],
      [str.length - 4, str.length]
    ];
    expect(highlightRanges(str, tuple)).toMatchInlineSnapshot(`
      <React.Fragment>
        <React.Fragment>
          
          <span
            className="is-highlighted"
          >
            
          </span>
          
        </React.Fragment>
        <span
          className="is-highlighted"
        >
          the
        </span>
         chicken crossed the road
      </React.Fragment>
    `);
  });

  it('wrapSelectedIndices', () => {
    expect(wrapSelectedIndices(0, 0, 0)).toEqual(0);
    expect(wrapSelectedIndices(0, 1, 10)).toEqual(10);
    expect(wrapSelectedIndices(5, 5, 12)).toEqual(5);
    expect(wrapSelectedIndices(13, 8, 12)).toEqual(8);
  });
});
