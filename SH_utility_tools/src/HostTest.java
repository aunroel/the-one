package src;


import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HostTest {

    public static void main(String[] args) {
        RSA_AES_Encryption rsa_aes_encryption = new RSA_AES_Encryption();
        List<DTNHost> tempHosts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DTNHost h = new DTNHost();
            tempHosts.add(h);
        }

        for (int j = 0; j < tempHosts.size(); j++) {
//            tempHosts.get(j).setKeysMap(publicKeys);
            for (int k = j + 1; k < tempHosts.size(); k++) {
                SecretKey secretKey = rsa_aes_encryption.generateAESKey();
                tempHosts.get(j).getSecretKeyMap().put(tempHosts.get(k), secretKey);
                tempHosts.get(k).getSecretKeyMap().put(tempHosts.get(j), secretKey);
            }
        }

        System.out.println();

    }

    private static class DTNHost {
        private HashMap<DTNHost, SecretKey> secretKeyMap;


        public DTNHost() {
            this.secretKeyMap = new HashMap<>();
        }

        public HashMap<DTNHost, SecretKey> getSecretKeyMap() {
            return secretKeyMap;
        }
    }
}
