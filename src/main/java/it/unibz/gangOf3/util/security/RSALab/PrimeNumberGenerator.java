package it.unibz.gangOf3.util.security.RSALab;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrimeNumberGenerator {

   public static int generatePrimeNumber() {
       int prime;
       while (true)
       {
           int count = 0;
           double x  = Math.random();
           double y  = 10000 * x;
           double z  = Math.ceil(y);
           prime     = (int)z;
           for (int i = 1; i <= prime; i++)
           {
               int modfactor = prime % i;
               if (modfactor == 0)
               {
                   count++;
               }
           }
           if (count == 2)
           {
               break;
           }
       }
       return prime;
   }

}
