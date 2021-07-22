import * as React from 'react';
import { useRef } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import DigitalInk, { DigitalInkView } from 'react-native-digital-ink';

export default function App() {
  const [result, setResult] = React.useState<void>();
  const [status, setStatus] = React.useState(true);
  const digitalInkView1: any = useRef();

  const onClick = (e: any) => {
    setStatus(!status);
    console.log('onClick', e.nativeEvent);
  };

  const onDrawStart = (e: any) => {
    console.log('onDrawStart', e.nativeEvent);
  };

  const onDrawEnd = (e: any) => {
    console.log('onDrawEnd', e.nativeEvent);
  };

  React.useEffect(() => {
    // DigitalInk.multiply(3, 70).then(setResult);
    // DigitalInk.show('Hello', DigitalInk.SHORT);
  }, []);

  const clearDigitalInkViewHandler = async () => {
    const languageTag = 'af';
    // const result = await DigitalInk.downloadModel(languageTag);
    // const result = await DigitalInk.getDownloadedModelLanguages();
    // await DigitalInk.setModel(languageTag);
    const result = await DigitalInk.downloadModel(languageTag);
    setResult(result);
    console.log('result: ', languageTag, result);
    // const result: any = await DigitalInk.recognize();
    // await DigitalInk.clear();
    // setResult(result);

    //
    // const result = await DigitalInk.loadLocalModels();
    // console.log('result', result);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.resultText}>Result: {result}</Text>
      <DigitalInkView
        ref={digitalInkView1}
        status={status}
        onClick={onClick}
        onDrawStart={onDrawStart}
        onDrawEnd={onDrawEnd}
        style={{
          width: '90%',
          height: 400,
          backgroundColor: '#dcdcdc',
        }}
      />
      <TouchableOpacity
        style={styles.button}
        onPress={clearDigitalInkViewHandler}
      >
        <Text style={styles.buttonText}>Recognize2</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    width: '80%',
    height: 45,
    marginTop: 20,
    backgroundColor: '#0387AC',
    borderRadius: 15,
    justifyContent: 'center',
    alignItems: 'center',
  },
  buttonText: {
    fontSize: 20,
    color: 'white',
  },
  resultText: {
    fontSize: 30,
    marginBottom: 15,
  },
});
