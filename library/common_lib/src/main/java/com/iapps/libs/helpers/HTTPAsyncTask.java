package com.iapps.libs.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.X509TrustManagerExtensions;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.iapps.common_library.R;
import com.iapps.libs.objects.Response;
import com.iapps.logs.com.pascalabs.util.log.helper.Helper;
import com.iapps.logs.com.pascalabs.util.log.model.BeanLogAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;


/**
 * Abstract class to handle HTTP connection to a web server
 */
public abstract class HTTPAsyncTask
	extends AsyncTask<String, Void, Response> {

	private boolean httpsEnabled = false;
	private boolean isMultipart = false;
	private boolean isCache = false;
	private String rawResponseString = null;

	private Context context;
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}

	public String getRawResponseString() {
		return rawResponseString;
	}

	Response thisResponse = null;
	public Response getThisResponse() {
		return thisResponse;
	}

	private String rawResponse = "";

	public String getRawResponse() {
		return rawResponse;
	}

	private boolean isEnableSSLCheck = true;
	private URL url;
	private String method = BaseConstants.GET;
	private JSONObject params = new JSONObject();
	private ArrayList<LinkedHashMap<String, String>> fileParams = new ArrayList<LinkedHashMap<String, String>>();
	private LinkedHashMap<String, byte[]> bytesParams = new LinkedHashMap<String, byte[]>();
	private LinkedHashMap<String, String> filetxtParams = new LinkedHashMap<String, String>();
	private HashMap<String, String> mHeaderParams = new HashMap<String, String>();

	public void setParams(JSONObject params) {
		this.params = params;
	}

	public void setFileParams(ArrayList<LinkedHashMap<String, String>> fileParams) {
		this.fileParams = fileParams;
	}

	public void setBytesParams(LinkedHashMap<String, byte[]> bytesParams) {
		this.bytesParams = bytesParams;
	}

	public JSONObject getParams() {
		return params;
	}

	public HashMap<String, String> getmHeaderParams() {
		return mHeaderParams;
	}

	public ArrayList<LinkedHashMap<String, String>> getFileParams() {
		return fileParams;
	}

	public LinkedHashMap<String, byte[]> getBytesParams() {
		return bytesParams;
	}

	protected abstract void onPreExecute();

	protected abstract void onPostExecute(Response response);

	/**
	 * Get the URL
	 * 
	 * @return the URL being used for the end point
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Set the URL to be used to connect to the end point
	 * 
	 * @param url , the url to be used
	 */
	public void setUrl(String url) {

		try {
			this.url = new URL(url);
			if (url.startsWith("https")) {
				this.httpsEnabled = true;
			}
		}
		catch (Exception e) {}
	}

	public void setUrl(String url, Context context) {

		try {
			this.url = new URL(url);
			if (url.startsWith("https")) {
				this.httpsEnabled = true;
			}
			this.context = context;
		}
		catch (Exception e) {}
	}

	public void setGetParams(String key, String value) {

		if(url == null) return;

		if (key != null && value != null && key.trim().length() > 0 && value.trim().length() > 0) {
			String currentUrl = url.toString();
			if (currentUrl.contains("?") && currentUrl.indexOf("?") <= currentUrl.length()) {
				try {

					currentUrl += "&" + key + "=" + escapeUrlParam(value);
					this.url = new URL(currentUrl);
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					currentUrl += "?" + key + "=" + escapeUrlParam(value);
					this.url = new URL(currentUrl);
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Set the header key value pair
	 * 
	 * @param key
	 * @param value
	 */
	public void setHeader(String key, String value) {
		mHeaderParams.put(key, value);
	}

	public String escapeUrlParam(String param) {
		param = param.replace("%", "%25").replace("$", "%24").replace("`", "%60")
				.replace("<", "%3C").replace(">", "%3E").replace("=", "%3D").replace("'", "%27")
				.replace("/", "%2F").replace(":", "%3A").replace("+", "%2B").replace("\"", "%22")
				.replace(" ", "%20").replace("(", "%28").replace(")", "%29").replace("&", "%26")
				.replace("?", "	%3F");
		return param;
	}

	public void setGetParams(String key, int value) {
		String val = String.valueOf(value);
		setGetParams(key, val);
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public boolean isHttpsEnabled() {
		return httpsEnabled;
	}

	public void setHttpsEnabled(boolean httpsEnabled) {
		this.httpsEnabled = httpsEnabled;
	}

	public boolean isEnableSSLCheck() {
		return isEnableSSLCheck;
	}

	public void setEnableSSLCheck(boolean isDisableSSLCheck) {
		this.isEnableSSLCheck = isDisableSSLCheck;
	}

	public void setPostParams(String key, JSONObject value) {
		if (key == null || key.trim().length() <= 0 || value == null ) { return; }
		try {
			this.params.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.setMethod(BaseConstants.POST);
	}
	public void setPostParams(String key, JSONArray value) {
		if (key == null || key.trim().length() <= 0 || value == null ) { return; }
		try {
			this.params.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.setMethod(BaseConstants.POST);
	}

	public void setPostParams(String key, String value) {
		if (key == null || key.trim().length() <= 0 || value == null ) { return; }
		try {
			this.params.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.setMethod(BaseConstants.POST);
	}
	public void setPostParams(String key, Double value) {
		if (key == null || key.trim().length() <= 0 || value == null ) { return; }
		try {
			this.params.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.setMethod(BaseConstants.POST);
	}
	public void setPostParams(String key, int value) {
		if (key == null || key.trim().length() <= 0 ) { return; }
		try {
			this.params.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.setMethod(BaseConstants.POST);
	}

	public void setPostParams(String key, String value, boolean allowWhiteSpace) {
		if (key == null || key.trim().length() <= 0) { return; }

		if(BaseHelper.isEmpty(value)) {
			try {
				this.params.put(key, "null");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				this.params.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		this.setMethod(BaseConstants.POST);
	}


//	public void setPostParams(String key, double value) {
//		String d = String.valueOf(value);
//		this.setPostParams(key, d);
//	}
//
//	public void setPostParams(String key, int value) {
//		String d = String.valueOf(value);
//		this.setPostParams(key, d);
//	}

	public void setImageParams(String key, String absPath) {
		this.setFileParams(key, absPath, BaseConstants.MIME_PNG);
	}

	public void setCSVParams(String key, String path) {
		this.setFileParams(key, path, BaseConstants.MIME_CSV);
	}

	public void setCache(boolean isCache) {
		this.isCache = isCache;
	}

	public void setFileParams(String key, String path, String mime) {
		if (path.length() <= 0 || key.trim().length() <= 0) { return; }
		this.isMultipart = true;
		String[] q = path.split("/");
		int idx = q.length - 1;
		LinkedHashMap<String, String> file = new LinkedHashMap<String, String>();
		file.put(BaseKeys.KEY, key);
		if(!BaseHelper.isEmpty(mime)) {
			file.put(BaseKeys.NAME, q[idx]);
			file.put(BaseKeys.FILEPATH, path);
		} else {
			file.put(BaseKeys.NAME,path);
			file.put(BaseKeys.FILEPATH,"");
		}
		file.put(BaseKeys.MIME, mime);
		this.fileParams.add(file);
	}

	public void settxtFileParams(String key, String path) {
		this.isMultipart = true;

		LinkedHashMap<String, String> file = new LinkedHashMap<String, String>();
		file.put(BaseKeys.KEY, key);
		file.put(BaseKeys.NAME, path);

		this.filetxtParams.put(key,path);
	}

	public void setByteParams(String key, byte[] bytes) {

		if(url == null) return;

		if (key.trim().length() <= 0 || bytes == null || bytes.length <= 0) { return; }
		this.isMultipart = true;
		this.bytesParams.put(key, bytes);
	}

	public void execute() {
		super.execute();
	}

	/**
	 * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This
	 * has been created to aid testing on a local box, not for use on production.
	 */
	private static void disableSSLCertificateChecking() {
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1)
						throws CertificateException {
					// Not implemented
				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1)
						throws CertificateException {
					// Not implemented
				}
			}
		};

		try {
			SSLContext sc = SSLContext.getInstance("TLS");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}
		catch (KeyManagementException e) {
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public String[] getHeaders(HttpsURLConnection con, String header) {
		List<String> values = new ArrayList<String>();
		int idx = (con.getHeaderFieldKey(0) == null) ? 1 : 0;
		while (true) {
			String key = con.getHeaderFieldKey(idx);
			if (key == null)
				break;
			if (header.equalsIgnoreCase(key))
				values.add(con.getHeaderField(idx));
			++idx;
		}
		return values.toArray(new String[values.size()]);
	}

	private void validatePinning(
			X509TrustManagerExtensions trustManagerExt,
			HttpsURLConnection conn, Set<String> validPins, Set<String> validPins2)
			throws SSLException {
		String certChainMsg = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			List<X509Certificate> trustedChain =
					trustedChain(trustManagerExt, conn);
			for (X509Certificate cert : trustedChain) {
				byte[] publicKey = cert.getPublicKey().getEncoded();
				md.update(publicKey, 0, publicKey.length);
				String pin = android.util.Base64.encodeToString(md.digest(),
						android.util.Base64.NO_WRAP);
				certChainMsg += "    sha256/" + pin + " : " +
						cert.getSubjectDN().toString() + "\n";
				if (validPins.contains(pin)) {
					return;
				}
				if (validPins2.contains(pin)) {
					return;
				}
			}
		} catch (NoSuchAlgorithmException e) {
			throw new SSLException(e);
		}
		throw new SSLPeerUnverifiedException("Certificate pinning " +
				"failure\n  Peer certificate chain:\n" + certChainMsg);
	}

	private List<X509Certificate> trustedChain(
			X509TrustManagerExtensions trustManagerExt,
			HttpsURLConnection conn) throws SSLException {
		Certificate[] serverCerts = conn.getServerCertificates();
		X509Certificate[] untrustedCerts = Arrays.copyOf(serverCerts,
				serverCerts.length, X509Certificate[].class);
		String host = conn.getURL().getHost();
		try {
			return trustManagerExt.checkServerTrusted(untrustedCerts,
					"RSA", host);
		} catch (CertificateException e) {
			throw new SSLException(e);
		}
	}

	@Override
	protected Response doInBackground(String... urls) {
		// init

		HttpsURLConnection connHttps = null;
		HttpURLConnection connHttp = null;
		InputStream in = null;
		int http_status = 999;
		String responseString = null;
		Response response = null;
		int maxBufferSize = 2 * 1024 * 1024;
		try {
//			if (!isEnableSSLCheck)
//				// Trust all incoming certificates
//				disableSSLCertificateChecking();

			if (httpsEnabled) {
                if (url != null) {

					if(Build.VERSION.SDK_INT < 21) {
						try {

							SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
							TrustManager[] trustManagers = new TrustManager[] { new TrustManagerManipulator() };
							sslContext.init(null, trustManagers, new SecureRandom());
							SSLSocketFactory noSSLv3Factory = new TLSSocketFactory(sslContext.getSocketFactory());
							HttpsURLConnection.setDefaultSSLSocketFactory(noSSLv3Factory);

							String[] pins = new String[] {BaseConstants.PIN_FROM_CERT_PEM_FOR_PINNING_INFO_JSON};
//							if (context != null) {
//								connHttps = PinningHelper.getPinnedHttpsURLConnection(context, pins, url);
//							}
							if (url != null) {
								connHttps = (HttpsURLConnection) url.openConnection();
							}

						} catch (Exception e) {
							connHttps = (HttpsURLConnection) url.openConnection();
						}
					} else {

						connHttps = (HttpsURLConnection) url.openConnection();
//						try {
//							connHttps.setSSLSocketFactory(TrustKit.getInstance().getSSLSocketFactory(url.getHost()));
//						} catch (Exception e) {
//							if(!BaseConstants.IS_FOR_UNIT_TESTING)
//							e.printStackTrace();
//						}

					}

                }

			}else{
				if (url != null) {
					connHttp = (HttpURLConnection) url.openConnection();
				}
			}


			if (httpsEnabled) {
				if(connHttps == null) return null;

				connHttps.setConnectTimeout(BaseConstants.TIMEOUT);
				connHttps.setReadTimeout(BaseConstants.TIMEOUT);
				connHttps.setUseCaches(false);
				Set<Map.Entry<String, String>> header = mHeaderParams.entrySet();
				for (Map.Entry<String, String> entry : header) {
					String key = entry.getKey();
					String value = entry.getValue();
					connHttps.setRequestProperty(key, value);
				}

				// Check if the request should be cached in the network level
				if (!isCache) {
					connHttps.addRequestProperty("Cache-Control", "no-cache");
				}
				if (this.method.equalsIgnoreCase(BaseConstants.POST)) {
					// post data to server
					connHttps.setDoOutput(true);

					connHttps.setRequestMethod("POST");

					try {
						if(fileParams.size() > 0){
							isMultipart = true;
						}
					} catch (Exception e) {}

					try {
						if(bytesParams.size() > 0){
							isMultipart = true;
						}
					} catch (Exception e) {}

					if (!isMultipart) {
						try {
							String paramsStr = params.toString();
//							for (String key : params.keySet()) {
//								paramsStr += key + ":" + URLEncoder.encode(params.get(key), "utf-8") + "&";
//							}
//							paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
							connHttps.setFixedLengthStreamingMode(paramsStr.getBytes().length);
							connHttps.setRequestProperty("Connection", "Keep-Alive");
							connHttps.setRequestProperty("Content-Type", "application/json");
							PrintWriter out = new PrintWriter(connHttps.getOutputStream());

							validateCertificatePinning(connHttps);

							out.print(paramsStr);
							out.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else {
						String twoHyphens = "--";
						String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
						String lineEnd = "\r\n";
						connHttps.setRequestProperty("Connection", "Keep-Alive");
						connHttps.setRequestProperty("Content-Type", "multipart/form-data; boundary="
								+ boundary);

						DataOutputStream outputStream = new DataOutputStream(connHttps.getOutputStream());

						validateCertificatePinning(connHttps);

						for (LinkedHashMap<String, String> map : fileParams) {
							int bytesRead, bytesAvailable, bufferSize;

							outputStream.writeBytes(twoHyphens + boundary + lineEnd);
							outputStream.writeBytes("Content-Disposition: form-data; name=\""
									+ map.get(BaseKeys.KEY) + "\"; filename=\""
									+ map.get(BaseKeys.NAME) + "\"");
							outputStream.writeBytes(lineEnd);
							Log.d("Content Disposition KEY", map.get(BaseKeys.KEY)
									+": NAME "+map.get(BaseKeys.NAME)+": FILEPATH "+map.get(BaseKeys.FILEPATH));
							if(!BaseHelper.isEmpty(BaseKeys.MIME)) {
								outputStream
										.writeBytes("Content-Type: " + map.get(BaseKeys.MIME) + lineEnd);
								outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
								outputStream.writeBytes(lineEnd);


								File file = new File(map.get(BaseKeys.FILEPATH));
								FileInputStream fileInputStream = new FileInputStream(file);
								bytesAvailable = fileInputStream.available();
								bufferSize = Math.min(bytesAvailable, maxBufferSize);
								byte[] buffer = new byte[bufferSize];

								bytesRead = fileInputStream.read(buffer, 0, bufferSize);
								while (bytesRead > 0) {
									outputStream.write(buffer, 0, bufferSize);
									bytesAvailable = fileInputStream.available();
									bufferSize = Math.min(bytesAvailable, maxBufferSize);
									bytesRead = fileInputStream.read(buffer, 0, bufferSize);
								}
								fileInputStream.close();

							}
							outputStream.writeBytes(lineEnd);

						}


						for (String key : this.bytesParams.keySet()) {
							outputStream.writeBytes(twoHyphens + boundary + lineEnd);
							outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key
									+ "\"; filename=\"uploads.JPG\"");
							outputStream.writeBytes(lineEnd);
							outputStream.writeBytes("Content-Type: image/jpg" + lineEnd);
							outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
							outputStream.writeBytes(lineEnd);

							outputStream.write(bytesParams.get(key));
							Log.d("Content value", key + ":" + params.get(key));
							outputStream.writeBytes(lineEnd);

						}

						for (String key : filetxtParams.keySet()) {
							outputStream.writeBytes(twoHyphens + boundary + lineEnd);
							outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key
									+ "\"");
							outputStream.writeBytes(lineEnd);
							outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
							outputStream.writeBytes(lineEnd);
							outputStream.writeBytes(filetxtParams.get(key));
							System.out.println("filetxtParams key  " + filetxtParams.get(key));
							Log.d("filetxtParams value", key + ":" + filetxtParams.get(key));
							outputStream.writeBytes(lineEnd);

						}

						outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
						outputStream.flush();
						outputStream.close();
						PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream));

					}

				}

				if(this.method.equalsIgnoreCase(BaseConstants.GET)){
					connHttps.connect();
					validateCertificatePinning(connHttps);
				}


				http_status = connHttps.getResponseCode();



				if (http_status == BaseConstants.STATUS_SUCCESS) {
					in = connHttps.getInputStream();
				}
				else {
					in = connHttps.getErrorStream();
				}
			}else{
				if(connHttp == null) return null;

				connHttp.setConnectTimeout(BaseConstants.TIMEOUT);
				connHttp.setReadTimeout(BaseConstants.TIMEOUT);
				connHttp.setUseCaches(false);
				Set<Map.Entry<String, String>> header = mHeaderParams.entrySet();
				for (Map.Entry<String, String> entry : header) {
					String key = entry.getKey();
					String value = entry.getValue();
					connHttp.setRequestProperty(key, value);
				}

				// Check if the request should be cached in the network level
				if (!isCache) {
					connHttp.addRequestProperty("Cache-Control", "no-cache");
				}
				if (this.method.equalsIgnoreCase(BaseConstants.POST)) {
					// post data to server
					connHttp.setDoOutput(true);

					connHttp.setRequestMethod("POST");

					try {
						if(fileParams.size() > 0){
							isMultipart = true;
						}
					} catch (Exception e) {}

					try {
						if(bytesParams.size() > 0){
							isMultipart = true;
						}
					} catch (Exception e) {}

					if (!isMultipart) {
						try {
							String paramsStr = params.toString();
//							for (String key : params.keySet()) {
//								paramsStr += key + "=" + URLEncoder.encode(params.get(key), "utf-8") + "&";
//							}
//							paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
							connHttp.setFixedLengthStreamingMode(paramsStr.getBytes().length);
							connHttp.setRequestProperty("Connection", "Keep-Alive");
							connHttp.setRequestProperty("Content-Type", "application/json");
							PrintWriter out = new PrintWriter(connHttp.getOutputStream());

							out.print(paramsStr);
							out.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else {
						String twoHyphens = "--";
						String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
						String lineEnd = "\r\n";
						connHttp.setRequestProperty("Connection", "Keep-Alive");
						connHttp.setRequestProperty("Content-Type", "multipart/form-data; boundary="
								+ boundary);

						DataOutputStream outputStream = new DataOutputStream(connHttp.getOutputStream());

						for (LinkedHashMap<String, String> map : fileParams) {
							int bytesRead, bytesAvailable, bufferSize;

							outputStream.writeBytes(twoHyphens + boundary + lineEnd);
							outputStream.writeBytes("Content-Disposition: form-data; name=\""
									+ map.get(BaseKeys.KEY) + "\"; filename=\""
									+ map.get(BaseKeys.NAME) + "\"");
							outputStream.writeBytes(lineEnd);
							outputStream
									.writeBytes("Content-Type: " + map.get(BaseKeys.MIME) + lineEnd);
							outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
							outputStream.writeBytes(lineEnd);

							 Log.d("value", map.get(BaseKeys.KEY)
									 +":"+map.get(BaseKeys.NAME)+":"+map.get(BaseKeys.FILEPATH));
							File file = new File(map.get(BaseKeys.FILEPATH));
							FileInputStream fileInputStream = new FileInputStream(file);
							bytesAvailable = fileInputStream.available();
							bufferSize = Math.min(bytesAvailable, maxBufferSize);
							byte[] buffer = new byte[bufferSize];

							bytesRead = fileInputStream.read(buffer, 0, bufferSize);
							while (bytesRead > 0) {
								outputStream.write(buffer, 0, bufferSize);
								bytesAvailable = fileInputStream.available();
								bufferSize = Math.min(bytesAvailable, maxBufferSize);
								bytesRead = fileInputStream.read(buffer, 0, bufferSize);
							}

							outputStream.writeBytes(lineEnd);

							fileInputStream.close();
						}

						for (String key : this.bytesParams.keySet()) {
							outputStream.writeBytes(twoHyphens + boundary + lineEnd);
							outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key
									+ "\"; filename=\"uploads.JPG\"");
							outputStream.writeBytes(lineEnd);
							outputStream.writeBytes("Content-Type: image/jpg" + lineEnd);
							outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
							outputStream.writeBytes(lineEnd);

							outputStream.write(bytesParams.get(key));
							Log.d("value", key + ":" + params.get(key));
							outputStream.writeBytes(lineEnd);

						}

						try {
							for (String key : filetxtParams.keySet()) {
								outputStream.writeBytes(twoHyphens + boundary + lineEnd);
								outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key
										+ "\"");
								outputStream.writeBytes(lineEnd);
								outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
								outputStream.writeBytes(lineEnd);
								outputStream.writeBytes(filetxtParams.get(key));
								System.out.println("filetxtParams key  " + filetxtParams.get(key));
								Log.d("filetxtParams value", key + ":" + filetxtParams.get(key));
								outputStream.writeBytes(lineEnd);


							}
						} catch (Exception e){

						}


						outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
						outputStream.flush();
						outputStream.close();
					}

				}

				if(this.method.equalsIgnoreCase(BaseConstants.GET)){
					connHttp.connect();
				}


				http_status = connHttp.getResponseCode();



				if (http_status == BaseConstants.STATUS_SUCCESS) {
					in = connHttp.getInputStream();
				}
				else {
					in = connHttp.getErrorStream();
				}
			}


			if (in != null) {
				// read input from server
				BufferedReader br = new BufferedReader(new InputStreamReader(in));

				StringBuilder sb = new StringBuilder();

				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				responseString = sb.toString().replace("\\'", "'");

//				if(BaseConstants.IS_FOR_UNIT_TESTING)
//					printResponse(responseString, String.valueOf(http_status));
//				else{
//
					Log.v("Log ",  this.getUrl().toString()+"\nhttp status : " + http_status + "\nheader : " + mHeaderParams.toString() + "\nparams : " + params.toString() + "\n" +responseString);
				logThisApi(responseString,String.valueOf(http_status),mHeaderParams.toString());

//				}
				rawResponseString = responseString;
				rawResponse = responseString;

				response = new Response(http_status, responseString);

				if (httpsEnabled) {
					response.setHeaderContent(connHttps.getHeaderFields());
				}else{
					response.setHeaderContent(connHttp.getHeaderFields());
				}

				if(response.getContent().toString().equalsIgnoreCase("{}"))
					throw new Exception();

			}
		}
		catch (SocketTimeoutException e) {

			if (BaseConstants.IS_FOR_UNIT_TESTING){
				printResponse(responseString, String.valueOf(http_status));
				e.printStackTrace();
		     }

			response = new Response(BaseConstants.STATUS_TIMEOUT, "");

		}
		catch (EOFException e) {

			if(BaseConstants.IS_FOR_UNIT_TESTING) {
				printResponse(responseString, String.valueOf(http_status));
				e.printStackTrace();
			}

			response = new Response(BaseConstants.STATUS_TIMEOUT, "");
		}
		catch (UnknownHostException e) {
			if(BaseConstants.IS_FOR_UNIT_TESTING) {
				printResponse(responseString, String.valueOf(http_status));
				e.printStackTrace();
			}

			response = new Response(BaseConstants.STATUS_NO_CONNECTION, "");

		}
		catch (FileNotFoundException e) {

			if(BaseConstants.IS_FOR_UNIT_TESTING) {
				printResponse(responseString, String.valueOf(http_status));
				e.printStackTrace();
			}

			response = new Response(HttpsURLConnection.HTTP_INTERNAL_ERROR, "");
		}
		catch (Exception e) {

			if(BaseConstants.IS_FOR_UNIT_TESTING) {
				printResponse(responseString, String.valueOf(http_status));
				e.printStackTrace();
			}
			response = new Response(http_status, "");;
		}
		finally {
			if (httpsEnabled) {
				if (connHttps != null) {
					connHttps.disconnect();
				}
			}else{
				if (connHttp != null) {
					connHttp.disconnect();
				}
			}

		}

		return response;

	}

	public void printResponse(String responseString, String httpStatus){
		if(!url.toString().contains("current-vehicle-location")) {
			System.out.println("\n" + this.url.toString());
			System.out.println("\n" + " \n http status : " + httpStatus + " \n header : " + mHeaderParams.toString() + " \n params : " + params.toString() + " \n response : " + responseString + "\n");
		}
	}


	public void validateCertificatePinning(HttpsURLConnection conn) throws Exception{
//		TrustManagerFactory trustManagerFactory =
//				TrustManagerFactory.getInstance(
//						TrustManagerFactory.getDefaultAlgorithm());
//		trustManagerFactory.init((KeyStore) null);
//		// Find first X509TrustManager in the TrustManagerFactory
//		X509TrustManager x509TrustManager = null;
//		for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
//			if (trustManager instanceof X509TrustManager) {
//				x509TrustManager = (X509TrustManager) trustManager;
//				break;
//			}
//		}
//		X509TrustManagerExtensions trustManagerExt =
//				new X509TrustManagerExtensions(x509TrustManager);
//
//		Set<String> validPins = Collections.singleton("lK/GQSzcKnNeokATmzarhjRh9wZ43Ul+ZLSruxjSagw=");
//		Set<String> validPins2 = Collections.singleton("YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=");
//
//		validatePinning(trustManagerExt, conn, validPins, validPins2);
	}


	private Response synchronousResponse;

	public Response getSynchronousResponse() {
		return synchronousResponse;
	}

	public void executeSynchronous(){
		synchronousResponse = doInBackground();
		thisResponse = synchronousResponse;
	}

	public void logThisApi(String content, String statuscode, String headers){
		String exception = "No Exception";

		try {
				System.out.println("logThisApi "+content);


				BeanLogAPI beanApi = new BeanLogAPI();

				String Title = "";

				try {
					if(!BaseHelper.isEmpty(this.url.toString()))
						beanApi.setUrl(this.url.toString());
				} catch (Exception e) {
					exception = e.toString() ;
				}

				try {
					if(!BaseHelper.isEmpty(this.getClass().toString()))
						beanApi.setShortenClassName(this.getClass());
				} catch (Exception e) {
					exception = e.toString() ;
				}
				Title = "API ";


				try {
					String cacheDesc = "";


					try {
						if(headers != null) {
							beanApi.setHeader(headers.toString());
							cacheDesc = "";
							cacheDesc += "Loaded From: API\n";
							Title = "API";
						}else{
							cacheDesc = "";
							cacheDesc += "Loaded From: Cache\n";
							Title = "Cache";

						}
					} catch (Exception e) {
						exception = e.toString() ;
					}

					if(!BaseHelper.isEmpty(this.getMethod()))
						beanApi.setMethod(this.getMethod() + "\n" + cacheDesc);
				} catch (Exception e) {
					exception = e.toString() ;
				}


				try {
					if(content!=null)
						beanApi.setContent(content);
				} catch (Exception e) {
					exception = e.toString() ;
				}

				try {
					if(!BaseHelper.isEmpty(getParams().toString()))
						beanApi.setParams(getParams().toString());
				} catch (Exception e) {
					exception = e.toString() ;
				}

				try {
					if(!BaseHelper.isEmpty(statuscode))
						beanApi.setStatuscode(statuscode);
				} catch (Exception e) {
					exception = e.toString() ;
				}

				try {
					if(!BaseHelper.isEmpty(getFileParams().toString()))
						beanApi.setFileparam(getFileParams().toString());
				} catch (Exception e) {
					exception = e.toString() ;
				}

				try {
					if(!BaseHelper.isEmpty(getBytesParams().toString()))
						beanApi.setByteparam(getBytesParams().toString());
				} catch (Exception e) {
					exception = e.toString() ;
				}

				try {
					beanApi.setmHeaderParams(Helper.headerList);
				} catch (Exception e) {
					exception = e.toString() ;
				}
				try {
					beanApi.setParameters(this.getParams().toString());
				} catch (Exception e) {
					exception = e.toString() ;
				}
				beanApi.setException(exception);
				Helper.logThisAPI(getContext(), beanApi, Title);

		} catch (Exception e) {
			System.out.println("logThisApi Exception "+e.toString());
			exception = e.toString() ;
		}
	}


}
