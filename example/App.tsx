import { default as React, Fragment } from 'react'
import { StyleSheet, View, StatusBar } from 'react-native'
import { Coil, CoilCachePolicy } from 'react-native-coil'

export const App: React.FunctionComponent = props => {
  return (
    <Fragment>
      <StatusBar barStyle="dark-content" backgroundColor="white" />
      <View style={styles.container}>
        <Coil 
          style={styles.coil}
          onSuccess={event => console.log(event.nativeEvent)}
          onCancel={undefined}
          memoryCacheKey="wwkland"
          source={
            {
              uri: 'https://www.andreasreiterer.at/wp-content/uploads/2017/11/react-logo-825x510.jpg',
              memoryCachePolicy: CoilCachePolicy.ENABLED
            }
          }
        />
        <Coil 
          style={styles.coil}
          onSuccess={event => console.log(event.nativeEvent)}
          onCancel={undefined}
          // memoryCacheKey="wwkland"
          source={
            {
              uri: 'https://www.andreasreiterer.at/wp-content/uploads/2017/11/react-logo-825x510.jpg',
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