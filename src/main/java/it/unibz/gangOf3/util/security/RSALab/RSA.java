package it.unibz.gangOf3.util.security.RSALab;

import java.math.BigInteger;
import java.util.Arrays;

public class RSA {

	public RSAKeys generateKeys(){

        // generate two random prime numbers p and q. Tip: https://stackoverflow.com/questions/24006143/generating-a-random-prime-number-in-java
        int p = PrimeNumberGenerator.generatePrimeNumber();
        int q = PrimeNumberGenerator.generatePrimeNumber();

		// calculate n = p*q
        int n = p*q;

		// calculate phi = (p-1)*(q-1)
        int phi = (p-1)*(q-1);

		// compute e: the minimum number that is coprime with phi greater than 1 and lower than phi
        int e = 0;
        for (int i = 2; i < phi; i++) {
            if (euclideanAlgorithm(i, phi) == 1) {
                e = i;
                break;
            }
        }
		
		// compute d with the Extended Euclidean algorithm
        int p2[]={0,1}, quotient=0, dividend= phi, divisor= e, remainder=0, pi=0;
        while (divisor != 0) {
            quotient = dividend / divisor;
            remainder = dividend % divisor;
            dividend = divisor;
            divisor = remainder;
            pi = p2[0] - p2[1] * quotient;
            p2[0] = p2[1];
            p2[1] = pi;
        }
        int d = p2[0];
        if (d < 0) {
            d = phi + d;
        }

        // return the keys
        return new RSAKeys(e, d, n);
	}

    /**
     * Using euclidean algorithm to calculate the greatest common divisor
     * @param num1
     * @param num2
     * @return the greatest common divisor
     */
    private int euclideanAlgorithm(int num1, int num2) {
        if (num2 == 0) {
            return num1;
        }
        return euclideanAlgorithm(num2, num1 % num2);
    }

    public static String encrypt(String plaintext, int e, int n){
        // plaintext -> each character is converted into asci in this way: int ascii = (int) character;
        int[] plaintextNumbers = new int[plaintext.length()];
        for (int i = 0; i < plaintext.length(); i++) {
            plaintextNumbers[i] = (int) plaintext.charAt(i);
        }

        //for each number from the plaintext compute  ( pow(number, e) ) mod n :  Use for this, method modpow of BigInteger. Documentation: https://learn.microsoft.com/it-it/dotnet/api/system.numerics.biginteger.modpow?view=net-6.0
        // return the resulting numbers
        int[] ciphertext = new int[plaintextNumbers.length];
        for (int i = 0; i < plaintextNumbers.length; i++) {
            //Create a BigInteger and use its modpow method
            BigInteger bigInteger = new BigInteger(String.valueOf(plaintextNumbers[i]));
            bigInteger = bigInteger.modPow(BigInteger.valueOf(e), BigInteger.valueOf(n));
            ciphertext[i] = bigInteger.intValue();
        }

        //Convert the resulting numbers into a string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ciphertext.length; i++) {
            sb.append(ciphertext[i]);
            if (i != ciphertext.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static String decrypt(String ciphertextStr, int d, int n){
        int[] ciphertext = Arrays.stream(ciphertextStr.split(","))
            .mapToInt(Integer::parseInt)
            .toArray();

        // for each number in the ciphertext compute ( pow(number, d) ) mod n:  Use for this method modpow of BigInteger. Documentation: https://learn.microsoft.com/it-it/dotnet/api/system.numerics.biginteger.modpow?view=net-6.0
        ciphertext = Arrays.stream(ciphertext)
            .map(i -> BigInteger.valueOf(i).modPow(BigInteger.valueOf(d), BigInteger.valueOf(n)).intValue())
            .toArray();

        //each resulting number is converted into a character assuming that this number is ascii code of the character in this way: Character.toString ((char) ascii);
        StringBuilder plaintext = new StringBuilder();
        for (int j : ciphertext) {
            plaintext.append(Character.toString((char) j));
        }
        // return the resulting string
        return plaintext.toString();
    }

    public static void main(String[] args) {
        //Test euclidean algorithm

        RSA rsa1 = new RSA();
        System.out.println("Greatest common divisor of 3 and 352: " + rsa1.euclideanAlgorithm(3, 352));

        //Test the RSA key generation

        RSA rsa = new RSA();
       // RSAKeys keys = new RSAKeys(3, 4137747, 6211693);
        RSAKeys keys = rsa.generateKeys();

        System.out.println("RSA keys generated:");
        System.out.println("e: " + keys.getE());
        System.out.println("d: " + keys.getD());
        System.out.println("n: " + keys.getN());

        //Test the encryption and decryption

        String plaintext = "eliasbinder#1#2";
        System.out.println("Plaintext: " + plaintext);
        String encrypted = encrypt(plaintext, keys.getE(), keys.getN());
        System.out.println("Encrypted: " + encrypted);
        String decrypted = decrypt(encrypted, keys.getD(), keys.getN());
        System.out.println("Decrypted: " + decrypted);
    }


}
