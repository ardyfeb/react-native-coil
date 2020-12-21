import { default as React, Fragment, useEffect, useState } from 'react'
import { StyleSheet, View, StatusBar } from 'react-native'
import { Coil, CoilCachePolicy, CoilScale, createBlurTransform } from 'react-native-coil'

export const App: React.FunctionComponent = props => {
  return (
    <Fragment>
      <StatusBar barStyle="dark-content" backgroundColor="white" />
      <View style={styles.container}>
        <Coil 
          style={styles.coil}
          memoryCacheKey={Coil.createCacheKey('fi')}
          crossfade={200}
          source={
            {
              uri: 'https://www.inovex.de/blog/wp-content/uploads/2022/01/one-year-of-react-native.png',
              diskCachePolicy: CoilCachePolicy.DISABLED
            }
          }
        />
      </View>
    </Fragment>
  )
}

const styles = StyleSheet.create(
  {
    container: {
      flex: 1,
      backgroundColor: 'white'
    },
    coil: {
      flex: 1,
    }
  }
)