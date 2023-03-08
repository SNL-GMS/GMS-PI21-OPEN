// __mocks__/style-mock.js

module.exports = {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  process(sourceText, sourcePath, options) {
    return { code: 'module.exports = {};' };
  }
};
