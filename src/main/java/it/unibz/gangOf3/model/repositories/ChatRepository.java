package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.security.DESLab.DHKeyExchange;
import it.unibz.gangOf3.util.security.hashing.LinearCongruentialGenerator;

import javax.crypto.KeyAgreement;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class ChatRepository {

    public static String generateSymmetricKey(User user1, User user2) throws SQLException, NotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        //Phase 1 - Generate the random code

        String user1Username = user1.getUsername();
        String user2Username = user2.getUsername();
        if (user1Username.equals(user2Username))
            throw new IllegalArgumentException("Cannot create chat with yourself");

        //Create a seed for the LCG based on both usernames
        long sum = 0;
        for (char c : user1Username.toCharArray()) {
            sum += c;
        }
        for (char c : user2Username.toCharArray()) {
            sum += c;
        }
        long seed = sum % 1000000000;

        String random = LinearCongruentialGenerator.generateRandom(seed);


        //Phase 2 - Key exchange algorithm
        //FIXME

//        DHKeyExchange dhKeyExchange = new DHKeyExchange();
//
//        KeyAgreement user1KeyAgreement = dhKeyExchange.generateKeyAgreement();
//        KeyAgreement user2KeyAgreement = dhKeyExchange.generateKeyAgreement(user2.getPrivateKey());
//
//        Key user1SymmetricKey = dhKeyExchange.generateSymmetricKey(user1KeyAgreement, user2.getPublicKey());
//        Key user2SymmetricKey = dhKeyExchange.generateSymmetricKey(user2KeyAgreement, user1.getPublicKey());
//
//        if (!user1SymmetricKey.equals(user2SymmetricKey))
//            throw new IllegalStateException("The keys are not the same");
//
//        StringBuilder desKey = new StringBuilder();
//        byte[] keyBytes = user1SymmetricKey.getEncoded();
//        for (byte keyByte : keyBytes) {
//            String partialKey = String.format("%8s", Integer.toBinaryString(keyByte & 0xFF)).replace(' ', '0');
//            desKey.append(partialKey);
//        }
//
//        //Return the key
//        return desKey.toString();
        return random;
    }

}
