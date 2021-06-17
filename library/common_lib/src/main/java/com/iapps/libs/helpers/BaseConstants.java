package com.iapps.libs.helpers;

import java.net.HttpURLConnection;

public class BaseConstants {
	public static final String GET = "get";
	public static final String POST = "post";
	public static int TIMEOUT = 30000;

	public static final int STATUS_SUCCESS = 200;
	public static final int STATUS_BAD_REQUEST = 400;
	public static final int STATUS_NOT_FOUND = 404;
	public static final int STATUS_TIMEOUT = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
	public static final int STATUS_NO_DATA = HttpURLConnection.HTTP_NO_CONTENT;
	public static final int STATUS_NO_CONNECTION = HttpURLConnection.HTTP_NOT_ACCEPTABLE;

	public static final int MAX_IMAGE_SIZE = 720;
	public static final int THUMBNAIL_SIZE = 400;

	public static final int PERMISSION_REQUEST_CAMERA = 91111;
	public static final String KEY_CAMERA_PERMISSION_GRANTED = "KEY_CAMERA_PERMISSION_GRANTED";

	public static final String DEVICE_TYPE = "Android";

	public static final String MIME_JPEG = "image/JPEG";
	public static final String MIME_PNG = "image/PNG";
	public static final String MIME_CSV = "text/csv";
	public static final String TEMP_PHOTO_FILE = "tmp.png";
	public static final String DATE_YMD = "yyyy-MM-dd";
	public static final String DATE_EDMY = "EE, dd MMMM yy";
	public static final String DATE_EDMY_TODAY = "dd MMMM yyyy";
	public static final String DATE_EDMYHMS = "EE, dd MMMM yyyy HH:mm:ss";
	public static final String DATE_HA = "h a";
	public static final String DATE_HMA = "h mm a";
	public static final String DATE_YMDHIS = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_YMDHIS_GMT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_HIS = "HH:mm:ss";
	public static final String DATE_HI = "HH:mm";
	public static final String YES = "Y";
	public static final String NO = "N";
	public static final String NOT_APPLICABLE = "NA";
	public static final int DEFAULT_PAGE = 1;
	public static final int DEFAULT_LIMIT = 10;

	public static final String MALE = "M";
	public static final String FEMALE = "F";

	public static final float PREVIEW_SIZE = 250; // in dp

	public static final String PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=";

//	public static final String PIN_FROM_CERT_PEM_FOR_PINNING = "89a5a8d6e6e7248a22e1db6a8f1a982eeefcd10b"; //uat
//	public static final String PIN_FROM_CERT_PEM_FOR_PINNING = "497c6868e484ccf0ba0601a6c40b7f10072c6a3c"; //uat CA
	public static final String PIN_FROM_CERT_PEM_FOR_PINNING = "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU="; //uat new May 11 2016

//	public static final String PIN_FROM_CERT_PEM_FOR_PINNING_INFO_JSON = "tzsid6cLOVtz0NnqTUDQU/CmN/bSC5vUQRUj6p7JBF0="; //production info json
//	public static final String PIN_FROM_CERT_PEM_FOR_PINNING = "94870f9f6c086af8a3f8481a32b59477c7b60def"; //proxy
	//public static final String PIN_FROM_CERT_PEM_FOR_PINNING = "4f9c7d21799cad0ed8b90c579f1a0299e790f387"; //ca root
	//public static final String PIN_FROM_CERT_PEM_FOR_PINNING = "7b803b669755ef12977daa93fdc18934117a4b1a";

	public static final String PIN_FROM_CERT_PEM_FOR_PINNING_INFO_JSON = "497c6868e484ccf0ba0601a6c40b7f10072c6a3c";
//	public static final String PIN_FROM_CERT_PEM_FOR_PINNING_INFO_JSON = "YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg="; //staging 2019

	public static boolean IS_FOR_UNIT_TESTING = false;
	public static boolean STOP_FOR_AWHILE_SEE_RESULT = false;

}
