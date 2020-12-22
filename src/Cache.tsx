export type CoilCacheKey = CoilCacheKeySimple | CoilCacheKeyComplex

export interface CoilCacheKeyBase {
  type: 'simple' | 'complex'
}

export interface CoilCacheKeyComplex extends CoilCacheKeyBase {
  base: string
  transformations: string[]
  size: {
    width: number,
    height: number
  }
  parameters: Record<string, string>
}

export interface CoilCacheKeySimple extends CoilCacheKeyBase {
  value: string
}

export function createCacheKey(value: string): CoilCacheKeySimple {
  return Object.freeze({ type: 'simple', value })
}

export function cacheKeyIsSimple(key: CoilCacheKey): key is CoilCacheKeySimple {
  return key.type == 'simple'
}