package io.stalk.mod.shortener.persister;

public class BijectiveUtil {

	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int    BASE     = 62;

	public static String encode(int num) {
		StringBuilder sb = new StringBuilder();

		while ( num > 0 ) {
			sb.append( ALPHABET.charAt( num % BASE ) );
			num /= BASE;
		}

		return sb.reverse().toString();
	}

	public static int decode(String str)  {
		int num = 0;

		for ( int i = 0, len = str.length(); i < len; i++ ) {
			num = num * BASE + ALPHABET.indexOf( str.charAt(i) );
		}

		return num;
	}
}
