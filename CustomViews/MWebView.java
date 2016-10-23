package com.slim.widget;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.xikang.android.slimcoach.cfg.Constant;
import com.xikang.android.slimcoach.ui.operation.OperWebActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebStorage.QuotaUpdater;

/**
 * 
 * 首先要在 manifest.main 文件中创建一个 webview， 
然后再 activity 中定义这个 webview 然后 进行以下相关操作。 
1、添加权限：AndroidManifest.xml 中必须使用许可"android.permission.INTERNET",否则会出 Web page not available 错误。 
2、在要Activity 中生成一个 WebView 组件：WebView webView = new WebView(this); 
3、设置WebView 基本信息： 
如果访问的页面中有 Javascript，则 webview 必须设置支持 Javascript。 
webview.getSettings().setJavaScriptEnabled(true); 
触摸焦点起作用 requestFocus(); 取消滚动条 this.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY); 
4 如果希望点击链接由自己处理，而不是新开 Android 的系统 browser 中响应该链接。 
给 WebView添加一个事件监听对象（ WebViewClient)并重写其中的一些方法: 
shouldOverrideUrlLoading：对网页中超链接按钮的响应。当按下某个连接时 WebViewClient会调用这个方法， 
并传递参数：按下的 url onLoadResource onPageStart onPageFinish onReceiveError onReceivedHttpAuthRequest 
5、如果访问的页面中有 Javascript，则 webview 必须设置支持 Javascript ，否则显示空白页面。 
Java 代码 webview.getSettings().setJavaScriptEnabled(true); 
6、如果页面中链接，如果希望点击链接继续在当前 browser 中响应，而不是新开 Android 的系统 browser
 中响应该链接，必须覆盖 webview 的 WebViewClient 对象： Java 代码 1.mWebView.setWebViewClient(new WebViewClient(){ 2. 3. 4. 5. 6. }); 
上述方法告诉系统由我这个 WebViewClient 处理这个 Intent,我来加载 URL。 点击一个链接的 Intent 是向上冒泡的， 
shouldOverrideUrlLoading 方法 return true 表示我加载后这个 Intent 就消费了， 不再向上冒泡了。 
7、 如果不做任何处理， 在显示你的 Brower UI 时， 点击系统“Back”键， 整个 Browser 会作为一个整体“Back" } 
public boolean shouldOverrideUrlLoading(WebView view, String url) { view.loadUrl(url); return true; 
到其他 Activity 中，而不是希望的在 Browser 的历史页面中 Back。
 * 
 * 常用属性、状态的方法 WebSettings常用方法： setAllowFileAccess 启用或禁止WebView访问文件数据
 * setBlockNetworkImage 是否开启网络图像加载阻塞 setBuiltInZoomControls 设置是否支持缩放
 * setCacheMode 设置缓冲的模式 setDefaultFontSize 设置默认的字体大小 setDefaultTextEncodingName
 * 设置在解码时使用的默认编码 setFixedFontFamily 设置固定使用的字体 setJavaSciptEnabled
 * 设置是否支持Javascript setLayoutAlgorithm 设置布局方式 setLightTouchEnabled 设置用鼠标激活被选项
 * setSupportZoom 设置是否支持变焦
 * 
 * WebViewClient常用方法： doUpdate VisitedHistory 更新历史记录 onFormResubmission
 * 应用程序重新请求网页数据 onLoadResource 加载指定地址提供的资源 onPageFinished 网页加载完毕 onPageStarted
 * 网页开始加载 onReceivedError 报告错误信息 onScaleChanged WebView发生改变
 * shouldOverrideUrlLoading 控制新的连接在当前WebView中打开
 * 
 * WebChromeClient常用方法： onCloseWindow 关闭WebView onCreateWindow 创建WebView
 * onJsAlert 处理Javascript中的Alert对话框 onJsConfirm处理Javascript中的Confirm对话框
 * onJsPrompt处理Javascript中的Prompt对话框 onProgressChanged 加载进度条改变 onReceivedlcon
 * 网页图标更改 onReceivedTitle 网页Title更改 onRequestFocus WebView显示焦点
 * 
 * @author Hulk
 * 
 */
public class MWebView extends WebView {

	public interface OnWebListener {

		void onWebPageStarted(WebView view, String url, Bitmap favicon);

		/**
		 * page load over , but progress , 100%
		 * 
		 * @param view
		 * @param url
		 */
		void onWebPageFinished(WebView view, String url, int errorCode, String errorMsg, String titleText);
		
		void onWebReceivedError(WebView view, int errorCode, String errorMsg, String failingUrl);

		/**
		 * The loading progress is 100%
		 * 
		 * @param errorCode
		 * @param view
		 * @param errorMsg
		 * @param obj
		 */
		void onWebFinish(WebView view, String url, int errorCode,
				String errorMsg, Object obj);

		void onWebProgressChanged(WebView view, int newProgress);

		void onWebReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error);
		
		void onPostHtmlSource(int errorCode, String errorMsg, String html);
		
		void onOverrideUrlLoading(WebView view, String url);
	}

	private static final String TAG = "MWebView";
	public static final int TIMEOUT = 10 * 1000;
	public static final int ERROR_CUSTOM_TIMEOUT_CODE = -20;

	private Context mContext;
	private WebSettings msettings;
	private boolean debug = true;
	private boolean verbose = false;
	private int progress = 0;
	private String titleText = null;
	private String htmlSource = null;
	private int errorCode = 0;
	private String errorMsg;
	private boolean cacheMode = false;
	private boolean goBack = false;

	private OnWebListener onWebListener;
	private boolean supportZoom = false;
	private boolean builtInZoomControls = false;
	private boolean displayZoomControls = false;
	private boolean isPostHtml = true;
	private boolean isTimeout = false;
	private int timeout = TIMEOUT;
	private Timer timeoutTimer =  null;
	private TimerTask timeoutTask = null;
	
	Handler handler = new Handler();
	
	public MWebView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public MWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public MWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}
	
	public static boolean isValidHtml(String html) {
		return !TextUtils.isEmpty(html) && html.contains("html");
	}

	private void init() {
		this.setBackgroundColor(Color.TRANSPARENT);
		msettings = getSettings();
		WebSettings s = msettings;
		s.setJavaScriptEnabled(true);
		s.setDefaultTextEncodingName("UTF-8");
		s.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		
		if (Build.VERSION.SDK_INT >= 11) {
			// open image block, loading string at first,
			// when but load over, need to close it
			s.setBlockNetworkImage(true);
			s.setDisplayZoomControls(displayZoomControls);
		}
		//cache
		s.setDatabaseEnabled(true);
		if (!isConnected()) {
			s.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		} else {
			s.setCacheMode(WebSettings.LOAD_DEFAULT);
		}
		String cachePath = createDataCachePath();
		s.setDatabasePath(cachePath);
		s.setAppCacheEnabled(true);
		s.setAppCachePath(cachePath);
		s.setDomStorageEnabled(true);
		s.setAllowFileAccess(true);
		s.setAppCacheMaxSize(8 * 1024 * 1024);
		
		s.setSavePassword(true);
		s.setSaveFormData(true);
		s.setLoadWithOverviewMode(false);
		s.setUseWideViewPort(false);
		
		setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		setHorizontalScrollBarEnabled(false);
		setHorizontalScrollbarOverlay(true);
	
		setWebViewClient(new MyWebViewClient());
		setWebChromeClient(new MyWebChromeClient());
		if(isPostHtml) {
			addJavascriptInterface(new InJavaScriptPostHtml(), "post_html");
		}
	}

	private String createDataCachePath() {
		String cacheDir = getExternalStorage() + "/slim/webviewCache";
		File file = new File(cacheDir);
		boolean mkdir = false;
		try {
			mkdir = file.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(verbose) Log.v(TAG, "mkdir = " + mkdir + ", cacheDir" + cacheDir);
		if(mkdir) {
			return file.getPath();
		} else {
			return getDir("cache");
		}
	}
	
	public static String getExternalStorage() {
		String rootDir = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			rootDir = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		} else {
			rootDir = Environment.getDataDirectory().getAbsolutePath();
		}
		return rootDir;
	}
	
	private String getDir(String dirName) {
		return mContext.getDir(dirName, Context.MODE_PRIVATE).getPath();
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public Timer getTimeoutTimer() {
		return timeoutTimer;
	}

	public void setTimeoutTimer(Timer timeoutTimer) {
		this.timeoutTimer = timeoutTimer;
	}

	public TimerTask getTimeoutTask() {
		return timeoutTask;
	}

	public void setTimeoutTask(TimerTask timeoutTask) {
		this.timeoutTask = timeoutTask;
	}

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public boolean isPostHtml() {
		return isPostHtml;
	}

	public void setPostHtml(boolean isPostHtml) {
		this.isPostHtml = isPostHtml;
	}

	public String getHtmlSource() {
		return htmlSource;
	}

	public void setHtmlSource(String htmlSource) {
		this.htmlSource = htmlSource;
	}

	public boolean isCacheMode() {
		return cacheMode;
	}

	public void setCacheMode(boolean cacheMode) {
		this.cacheMode = cacheMode;
	}

	/**
	 * loadDataWithBaseURL(null,data, "text/html",  "utf-8", null);
	 * @param htmlSource
	 */
	public void loadHtmlData(String htmlSource) {
		this.htmlSource = htmlSource;
		if (!TextUtils.isEmpty(htmlSource)) {
			//loadData(htmlSource, "text/html", "UTF-8");
			if(verbose) Log.w(TAG, "load cache data htmlSource: " + htmlSource);
			cacheMode = true;
			cancelTimeoutTask();
			loadDataWithBaseURL(null, htmlSource, "text/html",  "utf-8", null);
		}
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public WebSettings getMSettings() {
		return msettings;
	}

	public void setMSettings(WebSettings settings) {
		this.msettings = settings;
		init();
	}

	public boolean isDisplayZoomControls() {
		return displayZoomControls;
	}

	public void setDisplayZoomControls(boolean displayZoomControls) {
		this.displayZoomControls = displayZoomControls;
		if (Build.VERSION.SDK_INT >= 11) {
			msettings.setDisplayZoomControls(displayZoomControls);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goBack = canGoBack();
			int repeatCount = event.getRepeatCount();
			Log.w(TAG, "onKeyDown can go Back= " + goBack + ", repeatCount= " + repeatCount);
			if(goBack/* && repeatCount == 0*/) {
				goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public OnWebListener getOnWebListener() {
		return onWebListener;
	}

	public void setOnWebListener(OnWebListener onWebListener) {
		this.onWebListener = onWebListener;
	}

	public boolean isBuiltInZoomControls() {
		return builtInZoomControls;
	}

	public void setBuiltInZoomControls(boolean builtInZoomControls) {
		this.builtInZoomControls = builtInZoomControls;
		msettings.setBuiltInZoomControls(builtInZoomControls);
	}

	public boolean isSupportZoom() {
		return supportZoom;
	}

	public void setSupportZoom(boolean supportZoom) {
		this.supportZoom = supportZoom;
		msettings.setSupportZoom(supportZoom);
	}
	
	public void initTimeoutTask(int timeout) {
		if(verbose) Log.v(TAG, "init TimeoutTask and timeoutTimer !!");
		timeoutTimer = new Timer(true);
		timeoutTask = new MyTimeoutTimerTask();
		timeoutTimer.schedule(timeoutTask, timeout);
	}
	
	public boolean cancelTimeoutTask() {
		boolean cancel = false;
		if(timeoutTask != null) {
			timeoutTask.cancel();
			timeoutTask = null;
			cancel = true;
		}
		if(timeoutTimer != null) {
			timeoutTimer.cancel();
			timeoutTimer.purge();
			timeoutTimer = null;
			cancel = true;
		}
		if(verbose && cancel) Log.v(TAG, "TimeoutTimer and timeoutTask had been canceled !!");
		return cancel;
	}

	private boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isAvailable() && ni.isConnected();
	}

	/**
	 * adapt Webview
	 * 1. onReceivedError() [if has error, such as no net, or other error, or the function will not be called]
	 * 2. onPageStarted()
	 * 3. onPageFinished()
	 * @author Hulk
	 * 
	 */
	private class MyWebViewClient extends WebViewClient {

		/**
		 * 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器
		 * 如果希望点击链接由自己处理，而不是新开 Android 的系统 browser 中响应该链接。 
		 * 在显示你的 Brower UI 时， 点击系统“Back”键， 整个 Browser 会作为一个整体“Back" } 
		 * public boolean shouldOverrideUrlLoading(WebView view, String url) { 
		 * view.loadUrl(url); return true; 
		 * 到其他 Activity 中，而不是希望的在 Browser 的历史页面中 Back。 
		 * 详细出处参考：http://www.jb51.net/article/32438.htm
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//view.loadUrl(url);
			Log.w(TAG, "OverrideUrlLoading url: " + url);
			//Endless loop eg: http://mp.weixin.qq.com/mp/redirect?url=http%3A%2F%2Fss.xikang.com%2Fweixin%2Flist%2F%23rd 
			if(url != null && !url.contains("http://mp.weixin.qq.com/mp/redirect?url=http")) {
				if(onWebListener != null) {
					onWebListener.onOverrideUrlLoading(view, url);
				}
			} else {
				Log.e(TAG, "Invalid url: " + url);
			}
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (debug) Log.i(TAG, "onPageStarted url: " + url);
			initTimeoutTask(timeout);
			if (onWebListener != null) {
				onWebListener.onWebPageStarted(view, url, favicon);
			}
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			cancelTimeoutTask();
			// load over to close BlockNetworkImage
			if (Build.VERSION.SDK_INT >= 11) {
				msettings.setBlockNetworkImage(false);
			}
			if(isPostHtml) {
				//this code is key to obtain html source in InJavaScriptPostHtml
				view.loadUrl("javascript:window.post_html.postHtml('<head>'+" +
	                    "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
			}
			super.onPageFinished(view, url);
			if(cacheMode) {
				errorCode = 0;
				errorMsg = "Load cache html source ! ";
			}
			if (onWebListener != null) {
				onWebListener.onWebPageFinished(view, url, errorCode, errorMsg, titleText);
			}
			if (debug) Log.d(TAG, "onPageFinished url: " + url +", errorCode= " + errorCode + ", errorMsg: " + errorMsg);
		}

		/**
		 * 这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
		 * The function will be called at first if has any error
		 * 
		 *  Report an error to the
		 * host application. These errors are unrecoverable (i.e. the main
		 * resource is unavailable). The errorCode parameter corresponds to one
		 * of the ERROR_* constants.
		 * 
		 * Parameters view The WebView that is initiating the callback.
		 * errorCode The error code corresponding to an ERROR_* value.
		 * description A String describing the error. failingUrl The url that
		 * failed to load.
		 * 
		 * can load local html web or error code: 
		 * String errorHtml = "<html><body><h1>Page not find!</h1></body></html>";
		 * view.loadData(errorHtml, "text/html", "UTF-8");
		 */
		@Override
		public void onReceivedError(WebView view, int errorCode1, String errorMsg1, String failingUrl) {
			super.onReceivedError(view, errorCode, errorMsg, failingUrl);
			errorCode = errorCode1;
			errorMsg = errorMsg1;
			if(errorCode < 0 && !TextUtils.isEmpty(htmlSource) && htmlSource.contains("html")) {
				Log.w(TAG, "loading local html source code: " + htmlSource);
				loadHtmlData(htmlSource);
				errorCode = 0;
			}
			if (onWebListener != null) {
				onWebListener.onWebReceivedError(view, errorCode, errorMsg, failingUrl);
			}
			Log.e(TAG, "onReceivedError errorCode=" + errorCode + ",description=" + errorMsg + ",failingUrl=" + failingUrl);
		}
		
		/**
		 * 重写此方法可以让webview处理https请求
		 */
		@SuppressLint("NewApi")
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			super.onReceivedSslError(view, handler, error);
			Log.e(TAG, "onReceivedSslError error: " + error);
			errorCode = error.getPrimaryError();
			if (onWebListener != null) {
				onWebListener.onWebReceivedError(view, errorCode, errorMsg, error.getUrl());
			}
			handler.proceed();
		}
	}

	@Override
	public void loadUrl(String url) {
		super.loadUrl(url);
	}
	
	final class InJavaScriptPostHtml {
		 public void postHtml(String html) {
	            if(verbose) Log.v(TAG, "html source code: " + html);
	            htmlSource = html;
	            if(onWebListener != null) {
	            	onWebListener.onPostHtmlSource(errorCode, errorMsg, htmlSource);
	            }
	        }
	}
	
	private class MyTimeoutTimerTask extends TimerTask {
		@Override
		public void run() {
			handler.post(new Runnable() {
				public void run() {
					if(onWebListener != null) {
						onWebListener.onWebPageFinished(MWebView.this, getUrl(), ERROR_CUSTOM_TIMEOUT_CODE, "TIMEOUT= " + timeout, titleText);
					}
				}
			});
			isTimeout = true;
			boolean canceled = cancelTimeoutTask();
			Log.w(TAG, "## timeoutTask is executed , timeout= " + timeout + ", canceled= " + canceled);
		}
	}

	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onReachedMaxAppCacheSize(long spaceNeeded,
				long totalUsedQuota, QuotaUpdater quotaUpdater) {
			if (debug)
				Log.d(TAG, "totalUsedQuota= " + totalUsedQuota);
			totalUsedQuota = totalUsedQuota * 2;
			super.onReachedMaxAppCacheSize(spaceNeeded, totalUsedQuota,
					quotaUpdater);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			titleText = title;
			if (verbose) Log.v(TAG, "WebView title: " + title);
			((Activity) mContext).setTitle(title);
		}

		/**
		 * 这里是回调加载完成的接口， 也可以根据自己的需求做一些其他的操作
		 */
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			((Activity) mContext).getWindow().setFeatureInt(
					Window.FEATURE_PROGRESS, newProgress * 100);
			progress = newProgress;
			if (verbose)
				Log.v(TAG, "current progress= " + newProgress + "%");
			if (onWebListener != null) {
				onWebListener.onWebProgressChanged(view, newProgress);
			}
		}

		@Override
		public void onGeolocationPermissionsShowPrompt(String origin,
				GeolocationPermissions.Callback callback) {
			super.onGeolocationPermissionsShowPrompt(origin, callback);
			callback.invoke(origin, true, false);
		}
	}
}
