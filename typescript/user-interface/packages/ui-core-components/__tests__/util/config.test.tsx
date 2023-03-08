describe('jest config', () => {
  it('Should set timezone to UTC', () => {
    expect(new Date().getTimezoneOffset()).toBe(0);
  });

  it('Should mock system time', () => {
    jest.useFakeTimers();
    jest.setSystemTime(1000);
    expect(new Date().getTimezoneOffset()).toBe(0);
    expect(Date.now()).toEqual(1000);
  });
});
