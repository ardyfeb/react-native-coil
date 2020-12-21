import { default as React, forwardRef, PropsWithoutRef, RefAttributes, useMemo } from 'react'
import { View, StyleSheet, NativeSyntheticEvent, ViewProps, requireNativeComponent, NativeModules } from 'react-native'

import { CoilCacheKeyComplex, CoilCacheKeySimple, createCacheKey } from './Cache'

export interface CoilOptions extends CoilCommon, CoilCache {
  availableMemoryPercentage?: number
  allowHardware?: boolean
  allowRgba565?: boolean
  crossfade?: boolean | number
}

export interface CoilCommon {
  placeholder?: string
  fallback?: string
  error?: string
}

export interface CoilStatic {
  setLoaderOptions: (options: CoilOptions) => void
  prefetch: (sources: string[], loadTo: 'DISK' | 'MEMOR') => void 
  clearAllCache: () => void
  clearMemoryCache: () => void
  clearDiskCache: () => void
  createCacheKey: (value: string) => CoilCacheKeySimple
}

export type CoilComponentType = React.ForwardRefExoticComponent<PropsWithoutRef<CoilProps> & RefAttributes<View>> & CoilStatic

export interface CoilProps extends Partial<CoilEvent>, ViewProps, CoilCommon  {
  source: CoilSource
  transforms?: CoilTransform[]
  resizeMode?: CoilResizeMode
  scale?: CoilScale
  crossfade?: number
  size?: [number, number]
  memoryCacheKey?: CoilCacheKeySimple
  placeholderMemoryCacheKey?: string
  videoFrameMilis?: number
  videoFrameMicro?: number
}

export interface CoilSource extends CoilCache {
  uri: string
  headers?: Record<string, string>
}

export enum CoilResizeMode {
  CONTAIN = 'contain',
  COVER = 'cover',
  STRETCH = 'stretch',
  CENTER = 'center'
}

export enum CoilScale {
  FILL = 'fill',
  FIT = 'fit'
}

export type CoilCache = Partial< 
  Record<'diskCachePolicy' | 'memoryCachePolicy' | 'networkCachePolicy', CoilCachePolicy>
>

export enum CoilCachePolicy {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
  WRITE_ONLY = 'WRITE_ONLY',
  READ_ONLY = 'READ_ONLY'
}

export interface CoilEvent {
  onStart: (event: NativeSyntheticEvent<null>) => void
  onCancel: (event: NativeSyntheticEvent<null>) => void
  onError: (event: NativeSyntheticEvent<null>) => void
  onSuccess: (event: NativeSyntheticEvent<CoilSuccessEvent>) => void
}

export interface CoilSuccessEvent {
  isSampled: boolean
  dataSource: 'MEMORY' | 'DISK' | 'NETWORK'
  cachedInMemory: boolean
  memoryCacheKey: CoilCacheKeySimple | CoilCacheKeyComplex
  isPlaceholderMemoryCacheKeyPresent: boolean
}

export interface CoilTransform {
  className: string
  args: any[]
}

export function createBlurTransform(radius: number, sampling: number = 1): CoilTransform {
  return { className: 'blur', args: [radius, sampling] }
}

export function createCircleTransform(): CoilTransform {
  return { className: 'circle', args: [] }
}

export function createGrayscaleTransform(): CoilTransform {
  return { className: 'grayscale', args: [] }
}

export type RoundedTransformEdge = [number, number, number, number]

export function createRoundedTransform(radius?: number | RoundedTransformEdge): CoilTransform {
  let args: RoundedTransformEdge = [0, 0, 0, 0]

  if (typeof radius == 'number') {
    args.fill(radius)
  } else {
    args = radius as RoundedTransformEdge
  }

  return { className: 'rounded', args }
}

const CoilNative = requireNativeComponent<any>('RCTCoilView')
const CoilModule = NativeModules.CoilModule

const CoilBase = forwardRef<View, CoilProps>(
  (props, ref) => {
    if (props.transforms?.some(transform => transform.className == 'rounded')) {
      if (!props.size) {
        console.warn(
          'props `size` is empty while `rounded` is used, please specify `size` to prevent unexpected behavior'
        )
      }
    }

    if (props.videoFrameMilis && props.videoFrameMicro) {
      console.warn(
        'You have both `videoFrameMilis` and `videoFrameMicro` props, please select one of that'
      )
    }

    const computedStyle = useMemo(
      () => StyleSheet.flatten([styles.wrapper, props.style]), [props.style]
    )

    const onCoilSuccess = (event: NativeSyntheticEvent<CoilSuccessEvent>): void => {
      if (typeof props.onSuccess == 'function') {
        props.onSuccess({ ...event, nativeEvent: Object.freeze(event.nativeEvent) })
      }
    }

    return (
      <View {...props} style={computedStyle} ref={ref}>
        <CoilNative 
          style={StyleSheet.absoluteFillObject}
          source={props.source}
          transform={props.transforms || []}
          resizeMode={props.resizeMode || CoilResizeMode.CENTER}
          crossfade={props.crossfade}
          size={props.size}
          placeholder={props.placeholder}
          error={props.error}
          fallback={props.fallback}
          memoryCacheKey={props.memoryCacheKey}
          placeholderMemoryCacheKey={props.placeholderMemoryCacheKey}
          videoFrameMilis={props.videoFrameMilis}
          videoFrameMicro={props.videoFrameMicro}
          onCoilStart={props.onStart}
          onCoilError={props.onError}
          onCoilSuccess={onCoilSuccess}
          onCoilCancel={props.onCancel}
        />
      </View>
    )
  }
)

export const Coil: CoilComponentType = Object.assign(
  CoilBase, CoilModule, { createCacheKey }
)

const styles = StyleSheet.create(
  {
    wrapper: {
      overflow: 'hidden'
    }
  }
)