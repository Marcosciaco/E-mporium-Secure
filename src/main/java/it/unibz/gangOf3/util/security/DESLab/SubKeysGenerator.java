package it.unibz.gangOf3.util.security.DESLab;

import java.util.Arrays;

public class SubKeysGenerator {

	private static int[] PC1 = {
			57, 49, 41, 33, 25, 17, 9,
			1, 58, 50, 42, 34, 26, 18,
			10, 2, 59, 51, 43, 35, 27,
			19, 11, 3, 60, 52, 44, 36,
			63, 55, 47, 39, 31, 23, 15,
			7, 62, 54, 46, 38, 30, 22,
			14, 6, 61, 53, 45, 37, 29,
			21, 13, 5, 28, 20, 12, 4
	};

	private static int[] PC2 = {
			14, 17, 11, 24, 1, 5,
			3, 28, 15, 6, 21, 10,
			23, 19, 12, 4, 26, 8,
			16, 7, 27, 20, 13, 2,
			41, 52, 31, 37, 47, 55,
			30, 40, 51, 45, 33, 48,
			44, 49, 39, 56, 34, 53,
			46, 42, 50, 36, 29, 32
	};

	private static int[] KEY_SHIFTS = {
			1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
	};

	public static String[] generateSubKeys(String binkey) {
		String[] keys = new String[16];
		// Reduce the input key to a 56-bit permuted key
		String binKey_PC1 = "";

		// Apply Permuted Choice 1 (64 -> 56 bit)
		for (int i = 0; i < PC1.length; i++) {
			binKey_PC1 = binKey_PC1 + binkey.charAt(PC1[i] - 1);
		}

		String Cn, Dn;

		// Split permuted string in half | 56/2 = 28
		Cn = binKey_PC1.substring(0, 28);
		Dn = binKey_PC1.substring(28);

		for (int i = 0; i < KEY_SHIFTS.length; i++) {
			Cn = Utils.cyclicLeftShift(Cn, KEY_SHIFTS[i]);
			Dn = Utils.cyclicLeftShift(Dn, KEY_SHIFTS[i]);
			// Merge the two halves into 56-bit merged
			String m = Cn + Dn;
			// Reduce the 56-bit merged
			String binKey_PC2 = "";
			// Apply Permuted Choice 2 (56 -> 48 bit)
			for (int j = 0; j < PC2.length; j++) {
				binKey_PC2 = binKey_PC2 + m.charAt(PC2[j] - 1);
			}
			// Set the 48-bit key keys[i]
			keys[i] = binKey_PC2;
		}
		return keys;
	}

	public static void main(String[] args) {
		System.out.println(
				Arrays.toString(generateSubKeys("0001001100110100010101110111100110011011101111001101111111110001")));
	}
}
