package it.unibz.gangOf3.util.security;

public class LinearCongruentialGenerator {

    private static long seed = 110;
    private static final long multiplier = 48271;
    private static final long increment = 12345;
    private static final long modulus = (long) Math.pow(2, 31);

    public static String generateSalt() {
        //Create a 20 digit salt using the LCG
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append((char) (next() % 94 + 33)); //Printable ASCII characters
        }
        return sb.toString();
    }

    private static long next() {
        seed = (multiplier * seed + increment) % modulus;
        return seed;
    }

}
