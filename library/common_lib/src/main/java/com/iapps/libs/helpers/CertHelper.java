package com.iapps.libs.helpers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

/**
 * Helper class to manage Certificate in Android App
 * 
 * @author melvin
 * 
 */
public class CertHelper {

	private static final X500Principal DEBUG_DN = new X500Principal(
			"CN=Android Debug,O=Android,C=US");

	/**
	 * Get the SHA1 fingerprint for the cetificate
	 * 
	 * @param pm
	 *            {@link PackageManager} package Manager
	 * @param pn
	 *            {@link String} package name
	 * @return
	 */
	public static String getSHA1Fingerprint(PackageManager pm, String pn) {
		String fingerprintSHA1 = null;
		byte[] cert = getCert(pm, pn);

		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");

			byte[] sha1digest = new byte[0];
			if (md != null) {
				sha1digest = md.digest(cert);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < sha1digest.length; ++i) {
					sb.append((Integer
							.toHexString((sha1digest[i] & 0xFF) | 0x100))
							.substring(1, 3));
				}
				fingerprintSHA1 = sb.toString();
			}

		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		return fingerprintSHA1;
	}

	/**
	 * Check if the app is signed using debug certificate
	 * 
	 * @param pm
	 *            {@link PackageManager} Package Manager
	 * @param pn
	 *            {@link String} Package Name
	 * @return true if is signed using debug certificate, false otherwise.
	 */
	public static boolean isDebuggable(PackageManager pm, String pn) {
		boolean debuggable = false;
		byte[] cert = getCert(pm, pn);

		InputStream input = new ByteArrayInputStream(cert);

		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X509");

		} catch (CertificateException e) {
			e.printStackTrace();
		}
		X509Certificate c = null;

		try {
			c = (X509Certificate) cf.generateCertificate(input);
			debuggable = c.getSubjectX500Principal().equals(DEBUG_DN);

		} catch (CertificateException e) {
			e.printStackTrace();
		}

		return debuggable;
	}

	/**
	 * Get certificate in bytes
	 * 
	 * @param pm
	 * @param pn
	 * @return
	 */
	private static byte[] getCert(PackageManager pm, String pn) {

		int flags = PackageManager.GET_SIGNATURES;

		PackageInfo packageInfo = null;
		byte[] cert = null;
		try {
			packageInfo = pm.getPackageInfo(pn, flags);
			Signature[] signatures = packageInfo.signatures;
			cert = signatures[0].toByteArray();

//			String x = Arrays.toString(cert);
//			x.toString();


//			byte[] cert2 = new byte[] {48, -126, 2, 79, 48, -126, 1, -72, -96, 3, 2, 1, 2, 2, 4, 82, -4, 32, -101, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0, 48, 108, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 54, 53, 49, 18, 48, 16, 6, 3, 85, 4, 8, 19, 9, 83, 105, 110, 103, 97, 112, 111, 114, 101, 49, 18, 48, 16, 6, 3, 85, 4, 7, 19, 9, 83, 105, 110, 103, 97, 112, 111, 114, 101, 49, 14, 48, 12, 6, 3, 85, 4, 10, 19, 5, 105, 65, 80, 80, 83, 49, 12, 48, 10, 6, 3, 85, 4, 11, 12, 3, 82, 38, 68, 49, 23, 48, 21, 6, 3, 85, 4, 3, 19, 14, 77, 97, 108, 118, 105, 110, 32, 83, 117, 116, 97, 110, 116, 111, 48, 30, 23, 13, 49, 52, 48, 50, 49, 51, 48, 49, 51, 50, 49, 49, 90, 23, 13, 52, 52, 48, 50, 48, 54, 48, 49, 51, 50, 49, 49, 90, 48, 108, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 54, 53, 49, 18, 48, 16, 6, 3, 85, 4, 8, 19, 9, 83, 105, 110, 103, 97, 112, 111, 114, 101, 49, 18, 48, 16, 6, 3, 85, 4, 7, 19, 9, 83, 105, 110, 103, 97, 112, 111, 114, 101, 49, 14, 48, 12, 6, 3, 85, 4, 10, 19, 5, 105, 65, 80, 80, 83, 49, 12, 48, 10, 6, 3, 85, 4, 11, 12, 3, 82, 38, 68, 49, 23, 48, 21, 6, 3, 85, 4, 3, 19, 14, 77, 97, 108, 118, 105, 110, 32, 83, 117, 116, 97, 110, 116, 111, 48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0, -125, 85, -111, 109, -29, -82, 71, 81, 121, -107, -108, -58, -33, 88, -46, 22, -41, -78, 75, -9, -38, -14, -37, -78, -70, -100, 44, -122, -49, 46, 105, 23, 54, -122, 92, -69, -106, -39, 35, -52, -37, 12, 84, 1, -23, 93, -12, 119, -60, -81, -83, -81, -19, 13, 41, -41, 110, -124, -68, -56, 38, -22, 51, 72, -76, -49, 109, -91, 66, 43, 93, -81, -40, 2, 45, -118, 11, -79, 67, -10, -118, -68, -28, 100, 110, -99, 111, -47, -94, 116, 68, 12, 16, 83, 79, 35, 57, -29, -48, 78, 65, 16, 126, 101, 24, 69, 8, -51, 16, -18, -56, -100, -3, 53, -36, -114, 24, 81, -115, -13, 11, -105, -110, -85, 47, -78, -15, 21, 2, 3, 1, 0, 1, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0, 3, -127, -127, 0, 108, 109, -102, 20, 55, 69, 21, -36, -56, 68, -42, 97, -122, 94, -20, -76, 10, 101, -65, 107, -60, 12, -97, 14, 96, 18, -14, -93, 127, -20, -61, -57, 102, -18, -16, 1, 97, -56, 84, 117, -28, -3, -86, 100, 94, 86, 59, -74, 125, -99, -103, -8, -24, 51, -41, 105, -107, 81, 70, -9, 16, -57, 92, 38, 85, -13, -111, 98, -80, 112, 83, 106, -76, -21, 60, -35, -4, 51, 1, -69, -9, 11, -27, -41, 57, -108, 97, 125, 89, 82, 121, 23, 86, 10, 39, -98, -17, 4, 59, 58, 44, -35, -18, -83, 111, 104, -9, -94, 93, -38, -88, 61, 1, -44, 94, -54, -103, 28, 10, 16, 36, 121, -114, 29, -43, 47, 92, 16};

//			if(Arrays.equals(cert, cert2)){
//				x.toString();
//			}else{
//				x.toString();
//			}


		} catch (Exception e) {

			if(BaseConstants.IS_FOR_UNIT_TESTING)
			cert = new byte[] {48, -126, 2, 79, 48, -126, 1, -72, -96, 3, 2, 1, 2, 2, 4, 82, -4, 32, -101, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0, 48, 108, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 54, 53, 49, 18, 48, 16, 6, 3, 85, 4, 8, 19, 9, 83, 105, 110, 103, 97, 112, 111, 114, 101, 49, 18, 48, 16, 6, 3, 85, 4, 7, 19, 9, 83, 105, 110, 103, 97, 112, 111, 114, 101, 49, 14, 48, 12, 6, 3, 85, 4, 10, 19, 5, 105, 65, 80, 80, 83, 49, 12, 48, 10, 6, 3, 85, 4, 11, 12, 3, 82, 38, 68, 49, 23, 48, 21, 6, 3, 85, 4, 3, 19, 14, 77, 97, 108, 118, 105, 110, 32, 83, 117, 116, 97, 110, 116, 111, 48, 30, 23, 13, 49, 52, 48, 50, 49, 51, 48, 49, 51, 50, 49, 49, 90, 23, 13, 52, 52, 48, 50, 48, 54, 48, 49, 51, 50, 49, 49, 90, 48, 108, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 54, 53, 49, 18, 48, 16, 6, 3, 85, 4, 8, 19, 9, 83, 105, 110, 103, 97, 112, 111, 114, 101, 49, 18, 48, 16, 6, 3, 85, 4, 7, 19, 9, 83, 105, 110, 103, 97, 112, 111, 114, 101, 49, 14, 48, 12, 6, 3, 85, 4, 10, 19, 5, 105, 65, 80, 80, 83, 49, 12, 48, 10, 6, 3, 85, 4, 11, 12, 3, 82, 38, 68, 49, 23, 48, 21, 6, 3, 85, 4, 3, 19, 14, 77, 97, 108, 118, 105, 110, 32, 83, 117, 116, 97, 110, 116, 111, 48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0, -125, 85, -111, 109, -29, -82, 71, 81, 121, -107, -108, -58, -33, 88, -46, 22, -41, -78, 75, -9, -38, -14, -37, -78, -70, -100, 44, -122, -49, 46, 105, 23, 54, -122, 92, -69, -106, -39, 35, -52, -37, 12, 84, 1, -23, 93, -12, 119, -60, -81, -83, -81, -19, 13, 41, -41, 110, -124, -68, -56, 38, -22, 51, 72, -76, -49, 109, -91, 66, 43, 93, -81, -40, 2, 45, -118, 11, -79, 67, -10, -118, -68, -28, 100, 110, -99, 111, -47, -94, 116, 68, 12, 16, 83, 79, 35, 57, -29, -48, 78, 65, 16, 126, 101, 24, 69, 8, -51, 16, -18, -56, -100, -3, 53, -36, -114, 24, 81, -115, -13, 11, -105, -110, -85, 47, -78, -15, 21, 2, 3, 1, 0, 1, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0, 3, -127, -127, 0, 108, 109, -102, 20, 55, 69, 21, -36, -56, 68, -42, 97, -122, 94, -20, -76, 10, 101, -65, 107, -60, 12, -97, 14, 96, 18, -14, -93, 127, -20, -61, -57, 102, -18, -16, 1, 97, -56, 84, 117, -28, -3, -86, 100, 94, 86, 59, -74, 125, -99, -103, -8, -24, 51, -41, 105, -107, 81, 70, -9, 16, -57, 92, 38, 85, -13, -111, 98, -80, 112, 83, 106, -76, -21, 60, -35, -4, 51, 1, -69, -9, 11, -27, -41, 57, -108, 97, 125, 89, 82, 121, 23, 86, 10, 39, -98, -17, 4, 59, 58, 44, -35, -18, -83, 111, 104, -9, -94, 93, -38, -88, 61, 1, -44, 94, -54, -103, 28, 10, 16, 36, 121, -114, 29, -43, 47, 92, 16};

		}

		return cert;
	}

}
