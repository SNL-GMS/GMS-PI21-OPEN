import { jsonPretty } from '../../src/ts/common-util/json-util';

describe('json formatting utils', () => {
  test('Prettify real json', () => {
    const jsonToMakePretty = {
      id: 'ea395f14-c76c-495a-9d19-fae18fabbf2d',
      metadata: {
        channelNames: ['KMBO.KMBO1.SHZ'],
        stationName: 'KMBO'
      }
    };
    expect(jsonPretty(jsonToMakePretty)).toEqual(
      `{
  "id": "ea395f14-c76c-495a-9d19-fae18fabbf2d",
  "metadata": {
    "channelNames": [
      "KMBO.KMBO1.SHZ"
    ],
    "stationName": "KMBO"
  }
}`
    );
  });

  test('Prettify empty', () => {
    const jsonToMakePretty = {};
    expect(jsonPretty(jsonToMakePretty)).toEqual('{}');
  });
});
