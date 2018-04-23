//package src;
//
//import sh.RSA_AES_Encryption;
//import sh.RandomStringGenerator;
//
//import javax.crypto.SecretKey;
//import java.security.KeyPair;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.util.ArrayList;
//import java.util.Base64;
//
//public class EncryptionTest {
//
//    public static void main(String[] args) {
//        RandomStringGenerator rsg = new RandomStringGenerator();
//        RSA_AES_Encryption rae = new RSA_AES_Encryption();
//        User alice = new User();
//        User bob = new User();
//
//
//        KeyPair aliceKp = rae.generateRSAKeyPair();
//        KeyPair bobKp = rae.generateRSAKeyPair();
//
//        alice.setPublicRSA(aliceKp.getPublic());
//        alice.setPrivateRSA(aliceKp.getPrivate());
//        alice.setSecretAES(rae.generateAESKey());
//        alice.setMessage(rsg.randomBoundedStringGenerator());
//
//        bob.setPublicRSA(bobKp.getPublic());
//        bob.setPrivateRSA(bobKp.getPrivate());
//        bob.setSecretAES(rae.generateAESKey());
//        bob.setMessage(rsg.randomBoundedStringGenerator());
//
//        alice.addKeyToList(bob.publicRSA);
//        bob.addKeyToList(alice.publicRSA);
//
////        System.out.println(alice.toString());
////        System.out.println();
////        System.out.println();
////        System.out.println(bob.toString());
//
//        MessageDummy aTob = new MessageDummy();
//        MessageDummy bToa = new MessageDummy();
//
//        aTob.owner = alice;
//        aTob.recepient = bob;
//        bToa.owner = bob;
//        bToa.recepient = alice;
//
//        String host = "p13";
//        String encrHost = rae.encryptOwnerWithRSA(alice.privateRSA, host);
//        String decrHost = rae.decryptOwnerWithRSA(encrHost, rae.getKeyFromString(Base64.getMimeEncoder().encodeToString(alice.publicRSA.getEncoded())));
//
//        System.out.println("\n\n\n############################################");
//
//        aTob.encryptedAES = rae.encryptAESwithRSA(alice.secretAES, alice.privateRSA);
//        System.out.println("Original alice's aes: " + getAsString(alice.getSecretAES().getEncoded()));
//        System.out.println("\nEncrypted alice's aes: " + aTob.encryptedAES);
//        aTob.encryptedMessage = rae.encryptMessageWithAES(alice.message, alice.secretAES);
//        System.out.println("\nOriginal alice's msg: " + alice.getMessage());
//        System.out.println("\nEncrypted alice's msg: " + aTob.encryptedMessage);
//
//        bToa.encryptedAES = rae.encryptAESwithRSA(bob.secretAES, bob.privateRSA);
//        System.out.println("\nOriginal bob's aes: " + getAsString(bob.getSecretAES().getEncoded()));
//        System.out.println("\nEncrypted bob's aes: " + bToa.encryptedAES);
//        bToa.encryptedMessage = rae.encryptMessageWithAES(bob.message, bob.secretAES);
//        System.out.println("\nOriginal bob's msg: " + bob.getMessage());
//        System.out.println("\nEncrypted bob's msg: " + bToa.encryptedMessage);
//
//        System.out.println("====================================");
//        aTob.decryptedAES = getAsString(rae.decryptAESwithRSA(aTob.encryptedAES, bob.publicKeys.get(0)).getEncoded());
//        System.out.println("AES aTOb comparison:" + "'" + aTob.decryptedAES.equals(getAsString(alice.getSecretAES().getEncoded())) + "'");
//        aTob.decryptedMessage = rae.decryptMessageWithAES(aTob.encryptedMessage, rae.decryptAESwithRSA(aTob.encryptedAES, bob.publicKeys.get(0)));
//        System.out.println("aTOb msg comparison: " + "'" + aTob.decryptedMessage.equals(alice.getMessage()));
//
//        bToa.decryptedAES = getAsString(rae.decryptAESwithRSA(bToa.encryptedAES, alice.publicKeys.get(0)).getEncoded());
//        System.out.println("AES bTOa comparison:" + "'" + bToa.decryptedAES.equals(getAsString(bob.getSecretAES().getEncoded())) + "'");
//        bToa.decryptedMessage = rae.decryptMessageWithAES(bToa.encryptedMessage, rae.decryptAESwithRSA(bToa.encryptedAES, alice.publicKeys.get(0)));
//        System.out.println("bTOa msg comparison: " + "'" + bToa.decryptedMessage.equals(bob.getMessage()));
//
//        System.out.println();
//        System.out.println();
//        System.out.println(encrHost);
//        System.out.println(decrHost);
//
//    }
//
//    public static String getAsString(byte[] array) {
//        return Base64.getMimeEncoder().encodeToString(array);
//    }
//
//
//    private static class User {
//        private PublicKey publicRSA;
//        private PrivateKey privateRSA;
//        private SecretKey secretAES;
//        private String message;
//
//        private ArrayList<PublicKey> publicKeys = new ArrayList<>();
//
//        public User() {
//        }
//
//        public String getMessage() {
//            return message;
//        }
//
//        public void setMessage(String message) {
//            this.message = message;
//        }
//
//        public PublicKey getPublicRSA() {
//            return publicRSA;
//        }
//
//        public void setPublicRSA(PublicKey publicRSA) {
//            this.publicRSA = publicRSA;
//        }
//
//        public PrivateKey getPrivateRSA() {
//            return privateRSA;
//        }
//
//        public void setPrivateRSA(PrivateKey privateRSA) {
//            this.privateRSA = privateRSA;
//        }
//
//        public SecretKey getSecretAES() {
//            return secretAES;
//        }
//
//        public void setSecretAES(SecretKey secretAES) {
//            this.secretAES = secretAES;
//        }
//
//        public ArrayList<PublicKey> getPublicKeys() {
//            return publicKeys;
//        }
//
//        public void addKeyToList(PublicKey toAdd) {
//            publicKeys.add(toAdd);
//        }
//
//        public String getAESasString() {
//            return Base64.getMimeEncoder().encodeToString(secretAES.getEncoded());
//        }
//
//        public String getPublicAsString() {
//            return Base64.getMimeEncoder().encodeToString(publicRSA.getEncoded());
//        }
//
//        public String getPrivateAsString() {
//            return Base64.getMimeEncoder().encodeToString(privateRSA.getEncoded());
//        }
//
//        @Override
//        public String toString() {
//            return "public: " + Base64.getMimeEncoder().encodeToString(publicRSA.getEncoded())
//                    + "\n\nprivate: " + Base64.getMimeEncoder().encodeToString(privateRSA.getEncoded())
//                    + "\n\nsecretAES: " + Base64.getMimeEncoder().encodeToString(secretAES.getEncoded())
//                    + "\n\nmessage: " + message
//                    + "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
//        }
//    }
//
//    private static class MessageDummy {
//        private User owner;
//        private User recepient;
//        private String encryptedAES;
//        private String decryptedAES;
//        private String encryptedMessage;
//        private String decryptedMessage;
//
//        public MessageDummy() {}
//
//        public MessageDummy(User owner, String encryptedAES, String encryptedMessage) {
//            this.owner = owner;
//            this.encryptedAES = encryptedAES;
//            this.encryptedMessage = encryptedMessage;
//        }
//
//        public User getOwner() {
//            return owner;
//        }
//
//        public void setOwner(User owner) {
//            this.owner = owner;
//        }
//
//        public User getRecepient() {
//            return recepient;
//        }
//
//        public void setRecepient(User recepient) {
//            this.recepient = recepient;
//        }
//
//        public String getEncryptedAES() {
//            return encryptedAES;
//        }
//
//        public void setEncryptedAES(String encryptedAES) {
//            this.encryptedAES = encryptedAES;
//        }
//
//        public String getEncryptedMessage() {
//            return encryptedMessage;
//        }
//
//        public void setEncryptedMessage(String encryptedMessage) {
//            this.encryptedMessage = encryptedMessage;
//        }
//
//        public String getDecryptedMessage() {
//            return decryptedMessage;
//        }
//
//        public void setDecryptedMessage(String decryptedMessage) {
//            this.decryptedMessage = decryptedMessage;
//        }
//
//        public String getDecryptedAES() {
//            return decryptedAES;
//        }
//
//        public void setDecryptedAES(String decryptedAES) {
//            this.decryptedAES = decryptedAES;
//        }
//    }
//}
