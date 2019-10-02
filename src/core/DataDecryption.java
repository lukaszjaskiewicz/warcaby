package core;

import java.util.Base64;

public class DataDecryption {

    public static String dataDecryption(String encrypted) {
        String ret=null;
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            ret = new String(decoded);
        } catch (Exception exc) {
            System.out.println(exc);
        }
        return ret;
    }
    
}
