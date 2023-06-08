package it.unibz.gangOf3.util.security.DESLab;

public class DESWrapper {

    public static String encrypt(String message, String key) {
        //Convert message to binary String
        StringBuilder binaryMessage = new StringBuilder();
        for (char c : message.toCharArray()) {
            binaryMessage.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }

        //Convert key to binary String
        StringBuilder binaryKey = new StringBuilder();
        for (char c : key.toCharArray()) {
            binaryKey.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }

        //Pad key to 64 bits
        while (binaryKey.length() < 64) {
            binaryKey.append(binaryKey);
        }

        //Pad last msg block with 0s
        while (binaryMessage.length() % 64 != 0) {
            binaryMessage.append("0");
        }

        //Split binary message into 64-bit blocks
        String[] blocks = Utils.splitEqually(binaryMessage.toString(), 64);

        StringBuilder encryptedMessage = new StringBuilder();

        //Encrypt each block
        for (String block : blocks) {
            //Pad block to 64 bits
            while (block.length() < 64) {
                block += "0";
            }
            encryptedMessage.append(DES.encodeMessage(block, binaryKey.toString()));
        }

        //Convert binary message to String
        StringBuilder stringMessage = new StringBuilder();
        for (int i = 0; i < encryptedMessage.length(); i += 8) {
            String sub = encryptedMessage.substring(i, i + 8);
            int decimal = Integer.parseInt(sub, 2);
            stringMessage.append((char) decimal);
        }

        return stringMessage.toString();
    }

    public static String decrypt(String message, String key) {
        //Convert key to binary String
        StringBuilder binaryKey = new StringBuilder();
        for (char c : key.toCharArray()) {
            binaryKey.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }

        //Pad key to 64 bits
        while (binaryKey.length() < 64) {
            binaryKey.append(binaryKey);
        }

        //Convert message to binary String
        StringBuilder binaryMessage = new StringBuilder();
        for (char c : message.toCharArray()) {
            binaryMessage.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }

        //Split binary message into 64-bit blocks
        String[] blocks = Utils.splitEqually(binaryMessage.toString(), 64);

        StringBuilder decryptedMessage = new StringBuilder();

        //Decrypt each block
        for (String block : blocks) {
            decryptedMessage.append(DES.decodeMessage(block, binaryKey.toString()));
        }

        //Convert binary message to String
        StringBuilder stringMessage = new StringBuilder();
        for (int i = 0; i < decryptedMessage.length(); i += 8) {
            String sub = decryptedMessage.substring(i, i + 8);
            int decimal = Integer.parseInt(sub, 2);
            if (decimal == 0) //End of message
                decimal = 32;
            stringMessage.append((char) decimal);
        }

        return stringMessage.toString().trim();
    }

    public static void main(String[] args) {
        String toEncrypt = "Hello World!";
        String key = "12345678";
        String encrypted = encrypt(toEncrypt, key);
        String decrypted = decrypt(encrypted, key);
    }

}
