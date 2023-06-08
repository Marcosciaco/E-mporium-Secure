package it.unibz.gangOf3.util.security.DESLab;

import static it.unibz.gangOf3.util.security.DESLab.RoundPerformer.performRoundsForDecryption;
import static it.unibz.gangOf3.util.security.DESLab.RoundPerformer.performRoundsForEncryption;

public class DES {

    /**
     * Performs the DES encryption algorithm on a block of 64 bits
     * @param message A block of 64 bits
     * @param key A key of 64 bits
     * @return The encrypted block of 64 bits
     */
    public static String encodeMessage (String message, String key) {
        return performRoundsForEncryption(BlockEncoder.encodeBlock(message), SubKeysGenerator.generateSubKeys(key));
    }

    /**
     * Performs the DES decryption algorithm on a block of 64 bits
     * @param message
     * @param key
     * @return
     */
    public static String decodeMessage (String message, String key) {
        return performRoundsForDecryption(BlockEncoder.encodeBlock(message), SubKeysGenerator.generateSubKeys(key));
    }



    /**
     * Performs the initial permutation on a block of 64 bits
     * @param arrayPerms The array of permutations
     * @param input The block of 64 bits
     * @return The block of 64 bits after the initial permutation
     */
    static String performPermutation(int[] arrayPerms, String input) {
        String output = "";
        for (int i = 0; i < arrayPerms.length; i++) {
            output += input.charAt(arrayPerms[i]-1);
        }

        return output;
    }

}
