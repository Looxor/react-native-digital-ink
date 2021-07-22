package com.reactnativedigitalink;

import android.content.Context;
import android.view.LayoutInflater;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class DigitalInkViewManager extends SimpleViewManager<DigitalInkView> {

  public static final String REACT_CLASS = "RCTDigitalInkView";
  public static DigitalInkView mView = null;

  public static DigitalInkView getViewInstance() {
    return DigitalInkViewManager.mView;
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public DigitalInkView createViewInstance(ThemedReactContext context) {
    LayoutInflater inflater = (LayoutInflater)
      context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    DigitalInkView view = (DigitalInkView) inflater.inflate(R.layout.digital_ink_view, null);
    DigitalInkViewManager.mView = view;
    return view;
  }

  @ReactProp(name = "status")
  public void setStatus(DigitalInkView view, Boolean status) {
    view.setStatus(status);
  }

  @Override
  public Map getExportedCustomBubblingEventTypeConstants() {
    return MapBuilder.builder()
      .put(
        "onDrawStart",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onDrawStart"))
      )
      .put(
        "onDrawEnd",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onDrawEnd"))
      )
      .build();
  }

}
