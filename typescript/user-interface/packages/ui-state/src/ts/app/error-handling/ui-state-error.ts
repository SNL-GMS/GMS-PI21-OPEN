export class UIStateError extends Error {
  public name: string;

  public constructor(message: string) {
    super(message);
    this.name = this.constructor.name;
  }
}
