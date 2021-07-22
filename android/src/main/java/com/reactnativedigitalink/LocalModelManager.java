package com.reactnativedigitalink;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

class LocalModelManager {
  private Context context;

  public void setContext(Context context) {
    this.context = context;
  }

  public String loadModels() {
    try {
      @SuppressLint("SdCardPath") String filesPath = "/data/data/" + context.getPackageName();
      ArrayList<String> results = new ArrayList<>();
      copyAssets("files", filesPath + "/", results);
      StringBuilder resultString = new StringBuilder('\n');
      for (int i = 0; i < results.size(); i++) {
        resultString.append(results.get(i));
      }
      return resultString.toString();
    } catch (Exception e) {
      return e.toString();
    }
  }

  private void copyAssets(String path, String outPath, ArrayList results) {
    AssetManager assetManager = context.getAssets();
    String assets[];
    try {
      assets = assetManager.list(path);
      if (assets.length == 0) {
        results.add(copyFile(path, outPath));
      } else {
        String fullPath = outPath + "/" + path;
        File dir = new File(fullPath);
        if (!dir.exists())
          if (!dir.mkdir()) Log.e(TAG, "No create external directory: " + dir);
        for (String asset : assets) {
          copyAssets(path + "/" + asset, outPath, results);
        }
      }
    } catch (IOException ex) {
      results.add("I/O Exception: " + ex.toString());
    }
  }

  private String copyFile(String filename, String outPath) {
    AssetManager assetManager = context.getAssets();

    InputStream in;
    OutputStream out;
    try {
      in = assetManager.open(filename);
      String newFileName = outPath + "/" + filename;
      out = new FileOutputStream(newFileName);

      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
      in.close();
      out.flush();
      out.close();
      return outPath + '/' + filename;
    } catch (Exception e) {
      return e.getMessage();
    }

  }
}
