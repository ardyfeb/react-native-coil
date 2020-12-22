import { default as React, Fragment} from 'react'
import { StyleSheet, View, StatusBar } from 'react-native'
import { Coil, CoilCachePolicy, CoilResizeMode } from 'react-native-coil'

Coil.setLoaderOptions({ crossfade: 400, diskCachePolicy: CoilCachePolicy.DISABLED })

export const App: React.FunctionComponent = props => {
  return (
    <Fragment>
      <StatusBar barStyle="dark-content" backgroundColor="white" />
      <View style={styles.container}>
        <Coil 
          style={styles.coil}
          memoryCacheKey={Coil.createCacheKey('image')}
          resizeMode={CoilResizeMode.COVER}
          source={
            {
              uri: 'https://juabali.gumlet.io/user/image/plant.jpg?w=100',
              memoryCachePolicy: CoilCachePolicy.ENABLED
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