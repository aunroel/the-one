//package applications;
//
//import core.*;
//import sh.RSA_AES_Encryption;
//import sh.RandomStringGenerator;
//
//import javax.crypto.SecretKey;
//import java.security.PublicKey;
//import java.util.Map;
//import java.util.Random;
//
//public class LocalityEncrMessanger extends Application{
//
//    /** Message generation interval */
//    public static final String MESSAGE_INTERVAL = "interval";
//    /** Message interval offset - avoids synchronization of msg sending */
//    public static final String MESSAGE_OFFSET = "offset";
//    /** Destination address range - inclusive lower, exclusive upper */
//    public static final String MESSAGE_DEST_RANGE = "destinationRange";
//    /** Seed for the app's random number generator */
//    public static final String MESSAGE_SEED = "seed";
//    /** Size of the message */
//    public static final String MESSAGE_INITIAL_SIZE = "messageSize";
//    /** Size of the pong message */
//    public static final String MESSAGE_RESPONSE_SIZE = "responseSize";
//
//    /** Application ID */
//    public static final String APP_ID = "cs.standrews.ac.uk.DTNEncryptionApplication";
//
//    // Private vars
//    private double	lastMSg = 0;
//    private double	interval = 500;
//    private int		seed = 0;
//    private int		destMin=0;
//    private int		destMax=1;
//    private int		messageSize=1;
//    private int		responseSize=1;
//    private int curHostBroadcastsAmount = 0;
//    private Random rng;
//    private RSA_AES_Encryption rsa_aes_encryption = new RSA_AES_Encryption();
//    private RandomStringGenerator rsg = new RandomStringGenerator();
//
//    public LocalityEncrMessanger(Settings s) {
//        if (s.contains(MESSAGE_INTERVAL)){
//            this.interval = s.getDouble(MESSAGE_INTERVAL);
//        }
//        if (s.contains(MESSAGE_OFFSET)){
//            this.lastMSg = s.getDouble(MESSAGE_OFFSET);
//        }
//        if (s.contains(MESSAGE_SEED)){
//            this.seed = s.getInt(MESSAGE_SEED);
//        }
//        if (s.contains(MESSAGE_INITIAL_SIZE)) {
//            this.messageSize = s.getInt(MESSAGE_INITIAL_SIZE);
//        }
//        if (s.contains(MESSAGE_RESPONSE_SIZE)) {
//            this.responseSize = s.getInt(MESSAGE_RESPONSE_SIZE);
//        }
//        if (s.contains(MESSAGE_DEST_RANGE)){
//            int[] destination = s.getCsvInts(MESSAGE_DEST_RANGE,2);
//            this.destMin = destination[0];
//            this.destMax = destination[1];
//        }
//
//        rng = new Random(this.seed);
//        super.setAppID(APP_ID);
//    }
//
//    public LocalityEncrMessanger(LocalityEncrMessanger em) {
//        super(em);
//        this.lastMSg = em.getLastMSg();
//        this.interval = em.getInterval();
//        this.destMax = em.getDestMax();
//        this.destMin = em.getDestMin();
//        this.seed = em.getSeed();
//        this.responseSize = em.getResponseSize();
//        this.messageSize = em.getMessageSize();
//        this.rng = new Random(this.seed);
//    }
//
//    @Override
//    public Message handle(Message msg, DTNHost host) {
//        String type = (String) msg.getProperty("type");
//        if (type == null) return msg;
//
//        if (msg.getTo() == host && type.equalsIgnoreCase("encryptedMsg")) {
//            super.sendEventToListeners("GotMyMessage", null, host);
//
//            String encryptedMsgType = (String) msg.getProperty("contents");
//            byte[] utility = (byte[]) msg.getProperty("utility");
//            String unencryptedMsgOriginal = (String) msg.getProperty("unencrypted");
//            byte[] ownerProperty = (byte[]) msg.getProperty("owner");
//
//            byte[] owner = null;
//            boolean found = false;
//            owner:for (byte[] key: host.getPublicKeys().values()) {
//                owner = rsa_aes_encryption.decryptOwnerWithRSA(ownerProperty, host.getKeyPair().getPrivate());
//                for (Map.Entry<DTNHost, byte[]> entry : host.getPublicKeys().entrySet()) {
//                    if (entry.getKey().getName().equals(new String(owner))) {
//                        found = true;
//                        break owner;
//                    }
//                }
//            }
//
//            if (!found)
//                System.err.println("DOESN'T WORK");
//
//
//            SecretKey receivedKeyDecrypted = rsa_aes_encryption.decryptAESwithRSA(
//                    utility,
//                    host.getKeyPair().getPrivate()
//            );
//            String unencryptedMessage = rsa_aes_encryption.decryptMessageWithAES(
//                    encryptedMsgType,
//                    receivedKeyDecrypted
//            );
//            if (unencryptedMessage.equalsIgnoreCase(unencryptedMsgOriginal)) {
//                String id = "encryptedResponse" + SimClock.getIntTime() + "-" + host.getAddress();
//                Message m = new Message(host, msg.getFrom(), id, getResponseSize());
//                SecretKey secretKey = rsa_aes_encryption.generateAESKey();
//                byte[] encryptedAES = rsa_aes_encryption.encryptAESwithRSA(
//                        secretKey,
//                        rsa_aes_encryption.getPublicKeyFromArray(host.getPublicKeys().get(m.getTo())));
//                m.addProperty("type", "encryptedResponse");
//                m.addProperty("utility", encryptedAES);
//                m.addProperty(
//                        "contents",
//                        rsa_aes_encryption.encryptMessageWithAES("success" + msg.getId(), secretKey)
//                );
//                if (host.getName().startsWith("p")) {
//                    m.addProperty("owner", rsa_aes_encryption.encryptMessageWithAES(host.getName(), secretKey));
//                }
//                m.setAppID(APP_ID);
//                host.createNewMessage(m);
//
//
//                super.sendEventToListeners("SentResponse", null, host);
//                return null;
//            }
//
//        }
//
//        if (msg.getTo() == host && type.equalsIgnoreCase("encryptedResponse")) {
//            super.sendEventToListeners("ReceivedResponse", null, host);
//            return null;
//        }
//
//        if (msg.getTo() != host) {
//            msg.setFrom(host);
//            super.sendEventToListeners("ChangedSender", null, host);
//            return msg;
//        }
//
//        return msg;
//    }
//
//    private DTNHost randomHost() {
//        int destaddr = 0;
//        if (destMax == destMin) {
//            destaddr = destMin;
//        }
//        destaddr = destMin + rng.nextInt(destMax - destMin);
//        World w = SimScenario.getInstance().getWorld();
////        Object[] hosts = host.getPublicKeys().keySet().toArray();
////        DTNHost recepient = (DTNHost) hosts[new Random().nextInt(hosts.length)];
//        return w.getNodeByAddress(destaddr);
//    }
//
//    @Override
//    public void update(DTNHost host) {
//        double curTime = SimClock.getTime();
//        if (curTime - this.lastMSg >= this.interval) {
//
//            DTNHost to = randomHost();
//            while (!host.getPublicKeys().containsKey(to)) {
//                to = randomHost();
//            }
//            Message m = new Message(host, to, "encryptedMsg" +
//                    SimClock.getIntTime() + "-" + host.getAddress(),
//                    getMessageSize());
//            SecretKey secretKey = rsa_aes_encryption.generateAESKey();
//            byte[] encryptedAES = rsa_aes_encryption.encryptAESwithRSA(
//                    secretKey,
//                    rsa_aes_encryption.getPublicKeyFromArray(host.getPublicKeys().get(to)));
//            String unencryptesMsg = rsg.randomBoundedStringGenerator();
//            m.addProperty("type", "encryptedMsg");
//            m.addProperty("utility", encryptedAES);
//            m.addProperty(
//                    "contents",
//                    rsa_aes_encryption.encryptMessageWithAES(unencryptesMsg, secretKey)
//            );
//            m.addProperty("unencrypted", unencryptesMsg);
//            m.addProperty("time", rsa_aes_encryption.encryptMessageWithAES("" + m.getCreationTime(), secretKey));
//            m.setTimeCreated(0);
//            if (host.getName().startsWith("p")) {
//                m.addProperty("owner", rsa_aes_encryption.encryptOwnerWithRSA(rsa_aes_encryption.getPublicKeyFromArray(host.getPublicKeys().get(to)), host.getName()));
//            }
//            m.setAppID(APP_ID);
//            host.createNewMessage(m);
//
//
//            super.sendEventToListeners("SentMsg", null, host);
//
//            this.lastMSg = curTime;
//        }
//    }
//
//    @Override
//    public Application replicate() {
//        return new LocalityEncrMessanger(this);
//    }
//
//    public double getLastMSg() {
//        return lastMSg;
//    }
//
//    public void setLastMSg(double lastMSg) {
//        this.lastMSg = lastMSg;
//    }
//
//    public double getInterval() {
//        return interval;
//    }
//
//    public void setInterval(double interval) {
//        this.interval = interval;
//    }
//
//    public int getSeed() {
//        return seed;
//    }
//
//    public void setSeed(int seed) {
//        this.seed = seed;
//    }
//
//    public int getDestMin() {
//        return destMin;
//    }
//
//    public void setDestMin(int destMin) {
//        this.destMin = destMin;
//    }
//
//    public int getDestMax() {
//        return destMax;
//    }
//
//    public void setDestMax(int destMax) {
//        this.destMax = destMax;
//    }
//
//    public int getMessageSize() {
//        return messageSize;
//    }
//
//    public void setMessageSize(int messageSize) {
//        this.messageSize = messageSize;
//    }
//
//    public int getResponseSize() {
//        return responseSize;
//    }
//
//    public void setResponseSize(int responseSize) {
//        this.responseSize = responseSize;
//    }
//
//    public Random getRng() {
//        return rng;
//    }
//
//    public void setRng(Random rng) {
//        this.rng = rng;
//    }
//}
