/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.taobao.weex;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.weex.common.WXConfig;
import com.taobao.weex.utils.FontDO;
import com.taobao.weex.utils.LogLevel;
import com.taobao.weex.utils.TypefaceUtil;
import com.taobao.weex.utils.WXFileUtils;
import com.taobao.weex.utils.WXLogUtils;
import com.taobao.weex.utils.WXSoInstallMgrSdk;
import com.taobao.weex.utils.WXUtils;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.PathClassLoader;

public class WXEnvironment {

  public static final String OS = "android";
  public static String SYS_VERSION = android.os.Build.VERSION.RELEASE;
  static{
    if(SYS_VERSION != null && SYS_VERSION.toUpperCase().equals("P")){
        SYS_VERSION = "9.0.0";
    }
  }
  public static final String SYS_MODEL = android.os.Build.MODEL;
  public static final String ENVIRONMENT = "environment";
  public static final String WEEX_CURRENT_KEY = "wx_current_url";
  /*********************
   * Global config
   ***************************/

  public static String JS_LIB_SDK_VERSION = BuildConfig.buildJavascriptFrameworkVersion;

  public static String WXSDK_VERSION = BuildConfig.buildVersion;
  public static Application sApplication;
  public static final String DEV_Id = getDevId();
  @Deprecated
  public static int sDefaultWidth = 750;
  public volatile static boolean JsFrameworkInit = false;

  public static final String SETTING_EXCLUDE_X86SUPPORT = "env_exclude_x86";

  public static boolean SETTING_FORCE_VERTICAL_SCREEN = false;
  /**
   * Debug model
   */
  public static boolean sDebugMode = false;
  public static final boolean sForceEnableDevTool = true;
  public static String sDebugWsUrl = "";
  public static boolean sDebugServerConnectable = false;
  public static boolean sRemoteDebugMode = false;
  public static String sRemoteDebugProxyUrl = "";
  public static boolean sDebugNetworkEventReporterEnable = false;//debugtool network switch
  public static long sJSLibInitTime = 0;

  public static long sSDKInitStart = 0;// init start timestamp
  public static long sSDKInitInvokeTime = 0;//time cost to invoke init method
  public static long sSDKInitExecuteTime = 0;//time cost to execute init job
  /** from init to sdk-ready **/
  public static long sSDKInitTime =0;

  /**
   * component and modules ready
   * */
  public static long sComponentsAndModulesReadyTime = 0;

  public static boolean sInAliWeex = false;

  public static LogLevel sLogLevel = LogLevel.DEBUG;
  private static boolean isApkDebug = true;
  public static boolean isPerf = false;

  private static boolean openDebugLog = false;

  private static String sGlobalFontFamily;

  public static final String CORE_SO_NAME = "weexcore";
  public static final String CORE_JSS_SO_NAME = "weexjss";
  /**
   * this marked jsb.so's version, Change this if we want to update jsb.so
   */
  public static final int CORE_JSB_SO_VERSION = 1;

  private static  String CORE_JSS_SO_PATH = null;

  private static Map<String, String> options = new HashMap<>();
  static {
    options.put(WXConfig.os, OS);
    options.put(WXConfig.osName, OS);
  }

  /**
   * dynamic
   */
  public static boolean sDynamicMode = false;
  public static String sDynamicUrl = "";

  /**
   * Fetch system information.
   * @return map contains system information.
   */
  public static Map<String, String> getConfig() {
    Map<String, String> configs = new HashMap<>();
    configs.put(WXConfig.os, OS);
    configs.put(WXConfig.appVersion, getAppVersionName());
    configs.put(WXConfig.cacheDir, getAppCacheFile());
    configs.put(WXConfig.devId, DEV_Id);
    configs.put(WXConfig.sysVersion, SYS_VERSION);
    configs.put(WXConfig.sysModel, SYS_MODEL);
    configs.put(WXConfig.weexVersion, String.valueOf(WXSDK_VERSION));
    configs.put(WXConfig.logLevel,sLogLevel.getName());
    try {
      if (isApkDebugable()) {
        options.put(WXConfig.debugMode, "true");
      }
      options.put(WXConfig.scale, Float.toString(sApplication.getResources().getDisplayMetrics().density));
    }catch (NullPointerException e){
      //There is little chance of NullPointerException as sApplication may be null.
      WXLogUtils.e("WXEnvironment scale Exception: ", e);
    }
    configs.putAll(options);
    if(configs!=null&&configs.get(WXConfig.appName)==null && sApplication!=null){
      configs.put(WXConfig.appName, sApplication.getPackageName());
    }
    return configs;
  }

  /**
   * Get the version of the current app.
   */
  private static String getAppVersionName() {
    String versionName = "";
    PackageManager manager;
    PackageInfo info = null;
    try {
      manager = sApplication.getPackageManager();
      info = manager.getPackageInfo(sApplication.getPackageName(), 0);
      versionName = info.versionName;
    } catch (Exception e) {
      WXLogUtils.e("WXEnvironment getAppVersionName Exception: ", e);
    }
    return versionName;
  }

  /**
   *
   * @return string cache file
   */
  private static String getAppCacheFile() {
    String cache = "";
    try {
      cache = sApplication.getApplicationContext().getCacheDir().getPath();
    } catch (Exception e) {
      WXLogUtils.e("WXEnvironment getAppCacheFile Exception: ", e);
    }
    return cache;
  }


  public static Map<String, String> getCustomOptions() {
    return options;
  }

  public static void addCustomOptions(String key, String value) {
    options.put(key, value);
  }

  @Deprecated
  /**
   * Use {@link #isHardwareSupport()} if you want to see whether current hardware support Weex.
   */
  public static boolean isSupport() {
    boolean isInitialized = WXSDKEngine.isInitialized();
    if(!isInitialized){
      WXLogUtils.e("WXSDKEngine.isInitialized():" + isInitialized);
    }
    return isHardwareSupport() && isInitialized;
  }

  /**
   * Tell whether Weex can run on current hardware.
   * @return true if weex can run on current hardware, otherwise false.
   * Weex has removed the restrictions on the tablet, please use {@link #isCPUSupport()}
   */
  @Deprecated
  public static boolean isHardwareSupport() {
    if (WXEnvironment.isApkDebugable()) {
      WXLogUtils.d("isTableDevice:" + WXUtils.isTabletDevice());
    }
    return isCPUSupport() && !WXUtils.isTabletDevice();
  }

  /**
   * Determine whether Weex supports the current CPU architecture
   * @return true when support
   */
  public static boolean isCPUSupport(){
    boolean excludeX86 = "true".equals(options.get(SETTING_EXCLUDE_X86SUPPORT));
    boolean isX86AndExcluded = WXSoInstallMgrSdk.isX86() && excludeX86;
    boolean isCPUSupport = WXSoInstallMgrSdk.isCPUSupport() && !isX86AndExcluded;
    if (WXEnvironment.isApkDebugable()) {
      WXLogUtils.d("WXEnvironment.sSupport:" + isCPUSupport
              + "isX86AndExclueded: "+ isX86AndExcluded);
    }
    return isCPUSupport;
  }

  public static boolean isApkDebugable() {
    if (sApplication == null) {
      return false;
    }

    if (isPerf) {
      return false;
    }

    if (!isApkDebug) {
      return false;
    }
    try {
      ApplicationInfo info = sApplication.getApplicationInfo();
      isApkDebug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
      return isApkDebug;
    } catch (Exception e) {
      /**
       * Don't call WXLogUtils.e here,will cause stackoverflow
       */
      e.printStackTrace();
    }
    return true;
  }

  public static boolean isPerf() {
    return isPerf;
  }

  private static String getDevId() {
    return sApplication == null ? "" : ((TelephonyManager) sApplication
            .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
  }

  public static Application getApplication() {
    return sApplication;
  }

  public void initMetrics() {
    if (sApplication == null) {
      return;
    }
  }

  public static String getDiskCacheDir(Context context) {
    if (context == null) {
      return null;
    }
    String cachePath = null;
    try {
      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
              || !Environment.isExternalStorageRemovable()) {
        cachePath = context.getExternalCacheDir().getPath();
      } else {
        cachePath = context.getCacheDir().getPath();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return cachePath;
  }

  public static String getFilesDir(Context context) {
    if (context == null) {
      return "";
    }
    File filesDir = context.getFilesDir();
    String path = "";
    if (filesDir != null) {
      path = filesDir.getPath();
    } else {
      path = WXEnvironment.getApplication().getApplicationInfo().dataDir;
      path += File.separator;
      path += "files";
    }

    return path;
  }

  public static String getCrashFilePath(Context context) {
    if (context == null) {
        return "";
    }

    File dir = context.getDir("crash", Context.MODE_PRIVATE);
    if (dir == null)
        return "";

    String crashDir = dir.getAbsolutePath();

    return crashDir;
  }

  public static String getGlobalFontFamilyName() {
    return sGlobalFontFamily;
  }

  public static void setGlobalFontFamily(String fontFamilyName, Typeface typeface) {
    WXLogUtils.d("GlobalFontFamily", "Set global font family: " + fontFamilyName);
    sGlobalFontFamily = fontFamilyName;
    if (!TextUtils.isEmpty(fontFamilyName)) {
      if (typeface == null) {
        TypefaceUtil.removeFontDO(fontFamilyName);
      } else {
        FontDO nativeFontDO = new FontDO(fontFamilyName, typeface);
        TypefaceUtil.putFontDO(nativeFontDO);
        WXLogUtils.d("TypefaceUtil", "Add new font: " + fontFamilyName);
      }
    }
  }

  public static boolean isOpenDebugLog() {
    return openDebugLog;
  }

  public static void setOpenDebugLog(boolean openDebugLog) {
    WXEnvironment.openDebugLog = openDebugLog;
  }

  public static void  setApkDebugable(boolean debugable){
    isApkDebug  = debugable;
    if(!isApkDebug){
      openDebugLog = false;
    }
  }

  public static String findSoPath(String libName) {
    final String libPath = ((PathClassLoader) (WXEnvironment.class.getClassLoader())).findLibrary(libName);
    WXLogUtils.e(libName + "'s Path is" + libPath);
    return libPath;
  }

  public static String getCacheDir() {
    final Application application = getApplication();
    if (application == null || application.getApplicationContext() == null)
      return null;
    return application.getApplicationContext().getCacheDir().getPath();
  }

  public static boolean extractSo() {
    File sourceFile = new File(getApplication().getApplicationContext().getApplicationInfo().sourceDir);
    final String cacheDir = getCacheDir();
    if (sourceFile.exists() && !TextUtils.isEmpty(cacheDir)) {
      try {
        WXFileUtils.extractSo(sourceFile.getAbsolutePath(), cacheDir);
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }
    return false;
  }

  private static String findLibJssRealPath() {
    String soPath = findSoPath(CORE_JSS_SO_NAME);
    String realName = "lib" + CORE_JSS_SO_NAME + ".so";
    if (TextUtils.isEmpty(soPath)) {
      String cacheDir = getCacheDir();
      if (TextUtils.isEmpty(cacheDir)) {
        return "";
      }
      if (cacheDir.indexOf("/cache") > 0) {
        soPath = new File(cacheDir.replace("/cache", "/lib"), realName).getAbsolutePath();
      }
    }
    final File soFile = new File(soPath);
    if (soFile.exists())
      return soPath;
    else {
      //unzip from apk file
      final boolean success = extractSo();
      if (success)
        return new File(getCacheDir(), realName).getAbsolutePath();
    }
    return "";
  }

  public static String getLibJssRealPath() {
    if(TextUtils.isEmpty(CORE_JSS_SO_PATH)) {
      CORE_JSS_SO_PATH = findLibJssRealPath();
      WXLogUtils.e("findLibJssRealPath " + CORE_JSS_SO_PATH);
    }

    return CORE_JSS_SO_PATH;
  }
}
