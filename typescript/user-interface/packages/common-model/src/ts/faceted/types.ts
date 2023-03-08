export interface Faceted {
  name: string;
  effectiveAt?: number;
}

export type EntityReference<T extends Faceted> = Pick<T, 'name'>;
export type VersionReference<T extends Faceted> = Pick<T, 'name' | 'effectiveAt'>;
