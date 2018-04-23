package src;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class RSA_AES_Encryption {

    public PublicKey getPublicKeyFromArray(byte[] stringKey) {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(stringKey);
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

    public byte[] encryptAESwithRSA(SecretKey aesKey, PublicKey rsaKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaKey);

            return cipher.doFinal(aesKey.getEncoded());
        } catch (Exception e) {
            System.out.println("RSA AES encryption gone wrong: " + e.toString());
        }
        return null;
    }

    public SecretKey decryptAESwithRSA(byte[] encryptedAESkey, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] keyb = cipher.doFinal(encryptedAESkey);
            return new SecretKeySpec(keyb, "AES");
        } catch (Exception e) {
            System.out.println("RSA AES decryption gone wrong: " + e.toString() + "\n" + Base64.getEncoder().encodeToString(encryptedAESkey) + " = " + privateKey);
        }
        return null;
    }

   public byte[] signAESwithRSA(SecretKey aesKey, PrivateKey rsaKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaKey);

            return cipher.doFinal(aesKey.getEncoded());
        } catch (Exception e) {
            System.out.println("RSA AES ecnryption gone wrong: " + e.toString());
        }
        return null;
    }

    public SecretKey verifyAESwithRSA(byte[] encryptedAESkey, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] keyb = cipher.doFinal(encryptedAESkey);
            return new SecretKeySpec(keyb, "AES");
        } catch (Exception e) {
            System.out.println("RSA AES decryption gone wrong: " + e.toString());
        }
        return null;
    }

    public byte[] encryptOwnerWithRSA(PublicKey pkey, String owner) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pkey);

            byte[] cipherText = cipher.doFinal(owner.getBytes());

            return cipherText;
        } catch (Exception e) {
            System.out.println("RSA owner encryption gone wrong: " + e.toString());
        }
        return null;
    }

    public byte[] decryptOwnerWithRSA(byte[] owner, PrivateKey privateKey) {
        try {
            Cipher decriptCipher = Cipher.getInstance("RSA");
            decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

            return decriptCipher.doFinal(owner);
        } catch (Exception e) {
            System.out.println("RSA owner decryption gone wrong: " + e.toString());
        }
        return null;
    }



}
