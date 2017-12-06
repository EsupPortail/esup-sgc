package org.esupportail.sgc.tools;

public class HexStringUtils {

	public static String swapPairs(String s) {
		s = new StringBuilder(s).reverse().toString();
		String even = "";
		String odd = "";
		int length = s.length();

		for (int i = 0; i <= length-2; i+=2) {
			even += s.charAt(i+1) + "" + s.charAt(i);
		}

		if (length % 2 != 0) {
			odd = even + s.charAt(length-1);
			return odd;
		} else {
			return even;
		}
	}

}
