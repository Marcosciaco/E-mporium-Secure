package it.unibz.gangOf3.util.security.hashing;

public class LinearCongruentialGenerator {
    private static final long multiplier = 48271;
    private static final long increment = 12345;
    private static final long modulus = (long) Math.pow(2, 31);

    public static String generateRandom(long seed) {
        //Create a 20 digit salt using the LCG
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            seed = next(seed);
            sb.append((char) (seed % 94 + 33)); //Printable ASCII characters
        }
        return sb.toString();
    }

    private static long next(long seed) {
        return (multiplier * seed + increment) % modulus;
    }

}
