package it.unibz.gangOf3.util.security.DESLab;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;

import javax.crypto.spec.DESKeySpec;

public class DHKeyExchange {


	public KeyPair generateKeys() {
	// generates g, p, a and A
		KeyPairGenerator kpg = null;
		KeyPair kp = null;
		try {
			kpg = KeyPairGenerator.getInstance("DH");
			kp = kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return kp;
	}

	public KeyAgreement generateKeyAgreement(Key privateKey) {
		KeyAgreement keyAgreement = null;
		try {
			keyAgreement = KeyAgreement.getInstance("DH");
			keyAgreement.init(privateKey);

		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
		}
		return keyAgreement;
	}

	public Key generateSymmetricKey(KeyAgreement keyAgreement, PublicKey publickey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		byte[] sharedsecret = null;
		try {
			keyAgreement.doPhase(publickey, true);
			sharedsecret = keyAgreement.generateSecret();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		DESKeySpec desSpec = new DESKeySpec(sharedsecret);
		SecretKey key = skf.generateSecret(desSpec);
		return key;	}
}
