package com.dxpfc.thematic.components;

import java.security.cert.Extension;

public class ThematicUtils {
  public static String getThemeNameFromURL(String baseUrl, String prefix, String extension) {
    String splitUrl[] = baseUrl.split("/");
    int urlLength = splitUrl.length;
    String themeBlock = splitUrl[urlLength-1];
    if (themeBlock.startsWith(prefix)) {
      themeBlock = themeBlock.replaceFirst(prefix,"");
    }
    if (themeBlock.endsWith(extension)) {
      themeBlock = themeBlock.replaceAll(extension+"$", "");
    }
    return themeBlock;
  }
}
