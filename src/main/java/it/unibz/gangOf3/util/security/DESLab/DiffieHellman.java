package it.unibz.gangOf3.util.security.DESLab;

import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.security.hashing.LinearCongruentialGenerator;

import java.sql.SQLException;

public class DiffieHellman {

    /**
     * A Diffie-Hellman implementation
     * @param user1 The first user
     * @param user2 The second user
     * @return The symmetric key
     * @throws SQLException
     * @throws NotFoundException
     */
    public static String getSharedKey(User user1, User user2) throws SQLException, NotFoundException {
        //Get user keys
        int[] user1Keys = user1.getRSACredentials(); //d, e, n
        int[] user2Keys = user2.getRSACredentials(); //d, e, n
        //Both the users should be agreed upon the public keys G and P
        int G = user1Keys[1];
        int P = user2Keys[1];
        // get input from user for private keys a and b selected by User1 and User2
        int a = user1Keys[0];
        int b = user2Keys[0];

        // generate x and y keys
        long x = calculatePower(G, a, P);
        long y = calculatePower(G, b, P);
        // generate ka and kb secret keys after the exchange of x and y keys
        // calculate secret key for User1
        long ka = calculatePower(y, a, P);
        // calculate secret key for User2
        long kb = calculatePower(x, b, P);

        // compare secret keys of user1 and user2
        if (ka != kb)
            throw new IllegalStateException("Keys do not match");

        return LinearCongruentialGenerator.generateRandom(ka);
    }

    private static long calculatePower(long a, long b, long P) {
        if (b == 1)
            return a;
        else
            return (((long) Math.pow(a, b)) % P);
    }

}
