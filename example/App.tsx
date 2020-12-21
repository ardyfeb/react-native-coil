import { default as React, Fragment, useEffect, useState } from 'react'
import { StyleSheet, View, StatusBar } from 'react-native'
import { Coil, CoilCachePolicy, CoilScale, createBlurTransform } from 'react-native-coil'

Coil.setLoaderOptions({ crossfade: 2000 })

export const App: React.FunctionComponent = props => {
  return (
    <Fragment>
      <StatusBar barStyle="dark-content" backgroundColor="white" />
      <View style={styles.container}>
        <Coil 
          style={styles.coil}
          crossfade={200}
          onStart={ev => console.log('start', ev.nativeEvent)}
          onError={ev => console.log('error', ev.nativeEvent)}
          source={
            {
              uri: 'https://i.pinimg.com/originals/60/66/ed/6066edec9178a7a2befd6bc7c5549145.gif',
              diskCachePolicy: CoilCachePolicy.ENABLED
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