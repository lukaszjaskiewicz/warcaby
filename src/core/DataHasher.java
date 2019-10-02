package core;

import java.math.BigInteger;
import java.security.MessageDigest;

public class DataHasher {
    public static String encryption(String input){
        String answer = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] msgDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, msgDigest);
            answer = no.toString(16);
            while(answer.length()<32){
                answer = "0" + answer;
            }
        }catch (Exception exc){
            System.out.println(exc);
        }
        return answer;
    }
}
