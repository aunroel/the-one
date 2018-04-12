package sh;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class RSA_AES_Encryption {

    public PublicKey getKeyFromString(String stringKey) {
        byte[] data = Base64.getMimeDecoder().decode(stringKey.getBytes());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            System.out.println("RSA key from string gone wrong: " + e.toString());
        }
        return null;
    }

    public KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            return kp;
        } catch (Exception e) {
            System.out.println("RSA generation gone wrong: " + e.toString());
        }
       return null;
    }

    public SecretKey generateAESKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);

            return kgen.generateKey();
        } catch (Exception e) {
            System.out.println("AES generation gone wrong: " + e.toString());
        }
        return null;
    }

    public String encryptMessageWithAES(String message, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return new String(Base64.getMimeEncoder().encodeToString(cipher.doFinal(message.getBytes("UTF-8"))));
        } catch (Exception e) {
            System.out.println("AES encryption gone wrong: " + e.toString());
        }
        return null;
    }

    public String decryptMessageWithAES(String toDecrypt, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");

            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(cipher.doFinal(Base64.getMimeDecoder().decode(toDecrypt)));
        } catch (Exception e) {
            System.out.println("AES decryption gone wrong: " + e.toString());
        }
        return null;
    }

    public String encryptAESwithRSA(SecretKey aesKey, PrivateKey rsaKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaKey);

            return Base64.getMimeEncoder().encodeToString(cipher.doFinal(aesKey.getEncoded()));
        } catch (Exception e) {
            System.out.println("RSA AES ecnryption gone wrong: " + e.toString());
        }
        return null;
    }

    public SecretKey decryptAESwithRSA(String encryptedAESkey, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] keyb = cipher.doFinal(Base64.getMimeDecoder().decode(encryptedAESkey));
            return new SecretKeySpec(keyb, "AES");
        } catch (Exception e) {
            System.out.println("RSA AES decryption gone wrong: " + e.toString());
        }
        return null;
    }



}
