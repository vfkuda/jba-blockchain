package blockchain.blockchain;

import java.io.*;
import java.security.*;

public class CryptoHelper {

    public static String exncodeHex(byte[] data) {
        StringBuilder hexString = new StringBuilder();
        for (byte elem : data) {
            String hex = Integer.toHexString(0xff & elem);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /* Applies Sha256 to a string and returns a hash. */
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            return exncodeHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //The method that signs the data using the private key that is stored in keyFile path
    public static byte[] sign(String data, PrivateKey key) throws InvalidKeyException, Exception {
        Signature rsa = Signature.getInstance("SHA1withRSA");
        rsa.initSign(key);
        rsa.update(data.getBytes());
        return rsa.sign();
    }

    public static boolean verifySignature(String data, byte[] signature, PublicKey key) throws Exception {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(key);
        sig.update(data.getBytes());

        return sig.verify(signature);
    }


    public static void serialize(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    /**
     * Deserialize to an object from the file
     */
    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException, FileNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    public static boolean isHashComplexEnough(String hash, int leadingZeros) {
        for (int i = 0; i < leadingZeros; i++) {
            if (hash.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

}