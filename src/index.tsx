import React from 'react';

import { NativeModules, requireNativeComponent } from 'react-native';

type PropsType = {
  onClick?: any;
  onDrawEnd?: any;
  onDrawStart?: any;
  style?: any;
  status?: boolean;
};

class DigitalInkView extends React.PureComponent<PropsType> {
  RCTDigitalInkViewRef: any;
  state = {
    status: false,
  };
  _onClick = (event: any) => {
    console.log('_onClick', event.nativeEvent);
    if (!this.props.onClick) {
      return;
    }

    // process raw event
    this.props.onClick(event.nativeEvent);
  };

  _onLoad = (event: any) => {
    console.log('_onLoad', event.nativeEvent.nativeID);
  };

  _onDrawStart = (event: any) => {
    if (!this.props.onDrawStart) {
      return;
    }

    // process raw event
    this.props.onDrawStart(event.nativeEvent);
  };

  _onDrawEnd = (event: any) => {
    if (!this.props.onDrawEnd) {
      return;
    }

    // process raw event
    this.props.onDrawEnd(event.nativeEvent);
  };

  render() {
    // @ts-ignore
    return (
      <RCTDigitalInkView
        status={this.state.status}
        onClick={this._onClick}
        onDrawStart={this._onDrawStart}
        onDrawEnd={this._onDrawEnd}
        {...this.props}
      />
    );
  }
}

const RCTDigitalInkView = requireNativeComponent('RCTDigitalInkView');

type DigitalInkType = {
  multiply(a: number, b: number): Promise<number>;
  show(message: string, duration: number): void;
  clear(): void;
  getLanguages(): void;
  setModel(languageTag: string): void;
  getDownloadedModelLanguages(): void;
  downloadModel(languageTag: string): void;
  recognize(): void;
  loadLocalModels(): void;
  deleteDownloadedModel(): void;
  LONG: number;
  SHORT: number;
};

const { DigitalInk } = NativeModules;

export default DigitalInk as DigitalInkType;
export { DigitalInkView };
