package it.unibz.gangOf3.util.security.RSALab;

import java.security.SecureRandom;

public class PrimeNumberGenerator {

   public static int generatePrimeNumber() {
       int[] primesList = {2, 3, 5, 7, 11, 13, 17, 19}; // list of known primes
       int num = 0;
       int i = 0;
       int counter = 1;
       SecureRandom rand = new SecureRandom(); // generate a random number

       while (i != counter) {
           num = rand.nextInt(1000) + 1;

           if (num % primesList[i] == 0) { // check if num is evenly divisible by a prime from the list
               i++;
           } else { // if it is prime exit loop
               i = 0;
               counter = 0;
           }
       }
       return num;
   }

}
