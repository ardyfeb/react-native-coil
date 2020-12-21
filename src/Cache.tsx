export interface CoilCacheKey {
  type: 'simple' | 'complex'
}

export interface CoilCacheKeyComplex extends CoilCacheKey {
  base: string
  transformations: string[]
  size: {
    width: number,
    height: number
  }
  parameters: Record<string, string>
}

export interface CoilCacheKeySimple extends CoilCacheKey {
  value: string
}

export function createCacheKey(value: string): CoilCacheKeySimple {
  return Object.freeze({ type: 'simple', value })
}