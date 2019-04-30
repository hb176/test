package com.example.Util;

/*
 * MD5.java
 *
 * Created on 2013-09-25 by sealink
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.security.MessageDigest;

/**
 * 
 * @author Administrator
 */
public class MD5 {

	/**
	 * 
	 * @param doc
	 *            type: Document, DOM;
	 * @param param
	 *            type: BizContext;
	 * @return: int
	 * @throws Exception
	 *             <p>
	 *             ** bizlet ???????? **
	 * @bizlet_displayName BL_getMD5str
	 * @bizlet_param passing="in" type="variable" value="/" name="" desc=""
	 * @bizlet_param passing="in_out" type="field" value="/" name="" desc=""
	 */
	public final static String getMD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String KL(String inStr){
		char[] a = inStr.toCharArray();
		for(int i=0; i<a.length; i++){
			a[i] = (char)(a[i] ^ 't');
		}
		String s = new String(a);
		return s;
	}
	
	public static String JM(String inStr){
		char[] a = inStr.toCharArray();
		for(int i=0; i<a.length; i++){
			a[i] = (char)(a[i] ^ 't');
		}
		String k =new String(a);
		return k;
	}
}
