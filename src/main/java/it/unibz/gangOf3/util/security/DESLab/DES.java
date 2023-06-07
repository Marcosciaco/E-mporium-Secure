package it.unibz.gangOf3.util.security.DESLab;

public class DES {

    //TODO: implement the following methods

	public String encryptECB (String plaintext, String key) {
		//convert plaintext to binary

		//generate sub keys
		//pad the plaintext to obtain blocks of 64 bits
		//split the plaintext into blocks of 64 bits
		String[] blocks = null;
		for(int i = 0; i<blocks.length; i++) {
			//encrypt each block
			//concatenate the encrypted blocks into a single ciphertext 
		}
		//return the ciphertext;
		return null;
	}


	public String encryptCBC (String plaintext, String key, String iv) {
		//convert plaintext to binary
		//generate sub keys
		//pad the plaintext to obtain blocks of 64 bits
		//split the plaintext into blocks of 64 bits
		String[] blocks = null;
		for(int i = 0; i<blocks.length; i++) {
			//xor iv with the plaintext block
			//perform the encryption			
			//the current ciphertext block becomes the new iv 
			//concatenate the encrypted blocks into a single ciphertext 
		}
		//return the ciphertext;
		return null;
	}

	public String decryptECB (String ciphertext, String key) {
		//generate sub keys
		//pad the ciphertext to obtain blocks of 64 bits
		//split the ciphertext into blocks of 64 bits
		String[] blocks = null;
		for(int i = 0; i<blocks.length; i++) {
			//decrypt each block
			//concatenate the encrypted blocks into a single plaintext 
		}
		//convert the plaintext to text and return it;
		return null;
	}

	public String decryptCBC (String ciphertext, String key, String iv) {
		//generate sub keys
		//pad the ciphertext to obtain blocks of 64 bits
		//split the ciphertext into blocks of 64 bits
		String[] blocks = null;
		for(int i = 0; i<blocks.length; i++) {
			//decrypt each block
			//xor iv with the plaintext block
			//the current ciphertext block becomes the new iv 
			//concatenate the encrypted blocks into a single plaintext 
		}
		//convert the plaintext to text and return it;
		return null;
	}
}
