package com.reactnativedigitalink;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.reactnativedigitalink.StrokeManager.DownloadedModelsChangedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DigitalInkModule extends ReactContextBaseJavaModule implements DownloadedModelsChangedListener {
  @VisibleForTesting
  public static final StrokeManager strokeManager = new StrokeManager();
  private static final String DURATION_SHORT_KEY = "SHORT";
  private static final String DURATION_LONG_KEY = "LONG";

  private static final String TAG = "MLKDI.Activity";
  private static final ImmutableMap<String, String> NON_TEXT_MODELS =
    ImmutableMap.of(
      "zxx-Zsym-x-autodraw",
      "Autodraw",
      "zxx-Zsye-x-emoji",
      "Emoji",
      "zxx-Zsym-x-shapes",
      "Shapes");
  private static ReactApplicationContext reactContext;
  private final ArrayList<ModelLanguageContainer> mLanguagesList;

  DigitalInkModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    strokeManager.setDownloadedModelsChangedListener(this);
    strokeManager.setClearCurrentInkAfterRecognition(true);
    strokeManager.setTriggerRecognitionAfterInput(false);
    mLanguagesList = populateLanguages();
    strokeManager.refreshDownloadedModelsStatus();
    strokeManager.reset();
  }

  public static StrokeManager getStrokeManager() {
    return strokeManager;
  }

  @Override
  public String getName() {
    return "DigitalInk";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
    constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
    return constants;
  }

  @ReactMethod
  public void multiply(int x, int y, Promise promise) {
    promise.resolve(x * y);
  }

  @ReactMethod
  public void show(String message, int duration) {
    Toast.makeText(getReactApplicationContext(), message, duration).show();
  }

  @ReactMethod
  public void clear(Promise promise) {
    getReactApplicationContext().getCurrentActivity().runOnUiThread(() -> {
      DigitalInkView digitalInkView = DigitalInkViewManager.getViewInstance();
      boolean isNull = digitalInkView == null;
      promise.resolve("isNull: " + isNull + " " + R.id.digital_ink_view);
      if (!isNull) digitalInkView.clear();
      strokeManager.reset();
    });
  }

  @ReactMethod
  public void getLanguages(Promise promise) {
    WritableArray languages = Arguments.createArray();
    for (int i = 0; i < mLanguagesList.size(); i++) {
      ModelLanguageContainer container = mLanguagesList.get(i);
      WritableMap languageItem = Arguments.createMap();
      languageItem.putString("label", container.label);
      languageItem.putString("languageTag", container.languageTag);
      languageItem.putBoolean("downloaded", ((Boolean) container.downloaded));
      languages.pushMap(languageItem);
    }
    promise.resolve(languages);
  }

  @ReactMethod
  public void setModel(String languageTag, Promise promise) {
    promise.resolve(strokeManager.getModelManager().setModel(languageTag));
  }

  @ReactMethod
  public void getDownloadedModelLanguages(Promise promise) {
    strokeManager.getModelManager()
      .getDownloadedModelLanguages()
      .addOnSuccessListener(
        downloadedLanguageTags -> {
          WritableArray downloadedModelLanguages = Arguments.createArray();
          Object[] languageTags = downloadedLanguageTags.toArray();
          for (int i = 0; i < downloadedLanguageTags.size(); i++) {
            downloadedModelLanguages.pushString(languageTags[i].toString());
          }
          promise.resolve(downloadedModelLanguages);
        });
  }

  @ReactMethod
  public void downloadModel(String languageTag, Promise promise) {
    strokeManager.getModelManager().setModel(languageTag);
    strokeManager.download(promise);
  }

  @ReactMethod
  public Task<String> recognize(Promise promise) {
    return strokeManager.recognize(promise);
  }

  @ReactMethod
  public void loadLocalModels(Promise promise) {
    LocalModelManager localModelManager = new LocalModelManager();
    localModelManager.setContext(reactContext);
    String result = localModelManager.loadModels();
    promise.resolve(result);
  }

  @ReactMethod
  public void deleteDownloadedModel(String languageTag, Promise promise) {
    strokeManager.setActiveModel(languageTag);
    strokeManager.deleteActiveModel(promise);
  }

  @Override
  public void onDownloadedModelsChanged(Set<String> downloadedLanguageTags) {
    for (int i = 0; i < mLanguagesList.size(); i++) {
      ModelLanguageContainer container = mLanguagesList.get(i);
      container.setDownloaded(downloadedLanguageTags.contains(container.languageTag));
    }
  }

  private ArrayList<ModelLanguageContainer> populateLanguages() {
    ArrayList<ModelLanguageContainer> languagesList = new ArrayList<>();
    languagesList.add(ModelLanguageContainer.createLabelOnly("Select language"));
    languagesList.add(ModelLanguageContainer.createLabelOnly("Non-text Models"));

    // Manually add non-text models first
    for (String languageTag : NON_TEXT_MODELS.keySet()) {
      languagesList.add(
        ModelLanguageContainer.createModelContainer(
          NON_TEXT_MODELS.get(languageTag), languageTag));
    }
    languagesList.add(ModelLanguageContainer.createLabelOnly("Text Models"));

    ImmutableSortedSet.Builder<ModelLanguageContainer> textModels =
      ImmutableSortedSet.naturalOrder();
    for (DigitalInkRecognitionModelIdentifier modelIdentifier :
      DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
      if (NON_TEXT_MODELS.containsKey(modelIdentifier.getLanguageTag())) {
        continue;
      }

      StringBuilder label = new StringBuilder();
      label.append(new Locale(modelIdentifier.getLanguageSubtag()).getDisplayName());
      if (modelIdentifier.getRegionSubtag() != null) {
        label.append(" (").append(modelIdentifier.getRegionSubtag()).append(")");
      }

      if (modelIdentifier.getScriptSubtag() != null) {
        label.append(", ").append(modelIdentifier.getScriptSubtag()).append(" Script");
      }
      textModels.add(
        ModelLanguageContainer.createModelContainer(
          label.toString(), modelIdentifier.getLanguageTag()));
    }
    languagesList.addAll(textModels.build());
    return languagesList;
  }

  private static class ModelLanguageContainer implements Comparable<ModelLanguageContainer> {
    private final String label;
    @Nullable
    private final String languageTag;
    private boolean downloaded;

    private ModelLanguageContainer(String label, @Nullable String languageTag) {
      this.label = label;
      this.languageTag = languageTag;
    }

    /**
     * Populates and returns a real model identifier, with label, language tag and downloaded
     * status.
     */
    public static ModelLanguageContainer createModelContainer(String label, String languageTag) {
      // Offset the actual language labels for better readability
      return new ModelLanguageContainer(label, languageTag);
    }

    /**
     * Populates and returns a label only, without a language tag.
     */
    public static ModelLanguageContainer createLabelOnly(String label) {
      return new ModelLanguageContainer(label, null);
    }

    public String getLanguageTag() {
      return languageTag;
    }

    public void setDownloaded(boolean downloaded) {
      this.downloaded = downloaded;
    }

    @NonNull
    @Override
    public String toString() {
      if (languageTag == null) {
        return label;
      } else if (downloaded) {
        return "   [D] " + label;
      } else {
        return "   " + label;
      }
    }

    @Override
    public int compareTo(ModelLanguageContainer o) {
      return label.compareTo(o.label);
    }
  }

}
