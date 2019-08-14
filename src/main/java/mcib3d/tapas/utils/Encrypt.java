package mcib3d.tapas.utils;


import ij.IJ;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

public class Encrypt {
    private static final String ALGO = "AES";
    private byte[] keyValue;
    //private String keyS = null;

    public Encrypt() {
        keyValue = loadKey();
    }

    //public  void setKey(String key) {
    //    keyS = key;
    //}


    /**
     * Encrypt a string with AES algorithm.
     *
     * @param data is a string
     * @return the encrypted string
     */
    public String encrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encVal);
    }

    public String encrypt(byte[] bytes) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(bytes);
        return new String(Base64.getEncoder().encodeToString(encVal));
    }

    public String encrypt(byte[] bytes, byte[] key) throws Exception {
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGO));
        byte[] encVal = c.doFinal(bytes);
        return Base64.getEncoder().encodeToString(encVal);
    }


    /**
     * Decrypt a string with AES algorithm.
     *
     * @param encryptedData is a string
     * @return the decrypted string
     */
    public String decrypt(String encryptedData) throws Exception {
        //IJ.log("Decrypting "+encryptedData);
        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = cipher.doFinal(decodedValue);
        //System.out.println("decrypt bytes "+new String(decValue));
        //for (int c = 0; c < decodedValue.length; c++) System.out.print(" "+decodedValue[c]);
        //System.out.println("decrypt bytes");
        //for (int c = 0; c < decValue.length; c++) System.out.print(" "+decValue[c]);
        //System.out.println("decrypt " + encryptedData + " / " + new String(decValue));
        return new String(decValue);

        /*
        byte[] encryptedData = Base64.getDecoder().decode(data);
        Cipher c = null;
        c = Cipher.getInstance(algorithm);
        SecretKeySpec k = new SecretKeySpec(key, algorithm);
        c.init(Cipher.DECRYPT_MODE, k);
        decrypted = c.doFinal(encryptedData);
        */
    }

    /**
     * Decrypt a string with AES algorithm.
     *
     * @param encryptedData is a string
     * @return the decrypted string
     */
    public String decrypt(String encryptedData, byte[] bkey) throws Exception {
        Key key = new SecretKeySpec(bkey, ALGO);
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue);
    }


    /**
     * Generate a new encryption key.
     */
    private SecretKeySpec generateKey() throws Exception {
        //System.out.println("generate key");
        //for (int c = 0; c < keyValue.length; c++) System.out.print(keyValue[c] + " ");
        return new SecretKeySpec(keyValue, ALGO);
    }

    /*
    public void readKey(String file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
            keyS = reader.readLine();
            reader.close();
        } catch (FileNotFoundException e) {
            IJ.log("file key not found " + file);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    public String generateRandomString() {
        String aToZ = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789";
        SecureRandom rand = new SecureRandom();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int randIndex = rand.nextInt(aToZ.length());
            res.append(aToZ.charAt(randIndex));
        }
        return res.toString();
    }

    public char[] generateRandomChars() {
        String aToZ = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789";
        SecureRandom rand = new SecureRandom();
        char[] key = new char[16];
        for (int i = 0; i < 16; i++) {
            int randIndex = rand.nextInt(aToZ.length());
            key[i] = aToZ.charAt(randIndex);
        }
        return key;
    }

    private byte[] loadKey() {
        /// TEST SAVE KEY
        RandomAccessFile accessFile = null;
        char aChar = '.';
        byte[] key = new byte[0];
        try {
            File file = new File(System.getProperty("user.home") + File.separator + "OMEROKey");
            accessFile = new RandomAccessFile(file, "rw");
            accessFile.seek(0);
            key = new byte[(int) (accessFile.length() / 2)];
            for (int c = 0; c < accessFile.length() / 2; c++) {
                int idx = key.length - 1 - c;
                aChar = accessFile.readChar();
                aChar--;
                key[idx] = (byte) aChar;
            }
            //System.out.println("LOAD ENCRYPT");
            //for (int c = 0; c < key.length; c++) System.out.print(" " + key[c]);
            //System.out.println();
            accessFile.close();
        } catch (FileNotFoundException e) {
            IJ.log("load pb filenotfound : " + e.getMessage());
        } catch (IOException e) {
            IJ.log("load pb ioexception : " + e.getMessage());
        }


        return key;
    }


}
