import { default as React, forwardRef, PropsWithoutRef, RefAttributes, useMemo } from 'react'
import { View, StyleSheet, NativeSyntheticEvent, ViewProps, requireNativeComponent, NativeModules } from 'react-native'

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
}

export type CoilComponentType = React.ForwardRefExoticComponent<PropsWithoutRef<CoilProps> & RefAttributes<View>> & CoilStatic

export interface CoilProps extends Partial<CoilEvent>, ViewProps, CoilCommon  {
  source: CoilSource
  transforms?: CoilTransform[]
  resizeMode?: CoilResizeMode
  scale?: CoilScale
  crossfade?: number
  size?: [number, number]
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
    const computedStyle = useMemo(
      () => StyleSheet.flatten([styles.wrapper, props.style]), [props.style]
    )

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
          onCoilStart={props.onStart}
          onCoilError={props.onError}
          onCoilSuccess={props.onSuccess}
          onCoilCancel={props.onCancel}
        />
      </View>
    )
  }
)

export const Coil: CoilComponentType = Object.assign(
  CoilBase, CoilModule
)

const styles = StyleSheet.create(
  {
    wrapper: {
      overflow: 'hidden'
    }
  }
)