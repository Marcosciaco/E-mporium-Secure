package it.unibz.gangOf3.util.security.RSALab;

import java.security.MessageDigest;

public class Hash {

	public String getDigest(String inputString, String hashAlgorithm){
		MessageDigest md;
        try {
            md = MessageDigest.getInstance(hashAlgorithm);
            byte[] hashedPassword = md.digest(inputString.getBytes());
            return convertBytesToString(hashedPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
			
		StringBuilder sb = new StringBuilder();
        for (byte b : inputString.getBytes()) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
	}

    private String convertBytesToString(byte[] hashedPassword) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedPassword) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
