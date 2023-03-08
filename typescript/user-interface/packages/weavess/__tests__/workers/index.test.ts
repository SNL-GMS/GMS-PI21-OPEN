import { rpcProvider } from '../../src/ts/workers';

describe('Weavess Workers', () => {
  it('Weavess workers to be defined', () => {
    expect(rpcProvider).toBeDefined();
    const WeavessWorker = require('worker-loader?inline&fallback=false!../../workers'); // eslint-disable-line
    expect(WeavessWorker).toBeDefined();
  });
});
