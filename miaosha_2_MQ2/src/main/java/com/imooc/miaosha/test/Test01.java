package com.imooc.miaosha.test;

import org.apache.commons.codec.digest.DigestUtils;

import com.imooc.miaosha.util.MD5Util;

public class Test01 {

	public static void main(String[] args) {
		String result = MD5Util.inputPassToFormPass("xy742742");
		System.out.println("第一次："+result);
		String formPassToDBPass = MD5Util.formPassToDBPass(result,"1a2b3c4d");
		System.out.println("第二次："+formPassToDBPass);
//		System.out.println(DigestUtils.md5Hex("8cfb91396dd8ae5b3963692fce2470af"));
	}

}
