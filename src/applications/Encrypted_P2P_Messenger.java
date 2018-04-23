package applications;

import core.*;
import sh.RSA_AES_Encryption;
import sh.RandomStringGenerator;


import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Random;

public class Encrypted_P2P_Messenger extends Application{

    /** Message generation interval */
    public static final String MESSAGE_INTERVAL = "interval";
    /** Message interval offset - avoids synchronization of msg sending */
    public static final String MESSAGE_OFFSET = "offset";
    /** Seed for the app's random number generator */
    public static final String MESSAGE_SEED = "seed";
    /** Seed for the app's random number generator */
    public static final String INITIAL_SaW_COPIES = "copies";

    /** Application ID */
    public static final String APP_ID = "cs.standrews.ac.uk.Encrypted_P2P_Messenger";

    // Private vars
    private double	lastMSg = 0;
    private double	interval = 500;
    private int		seed = 0;
    private int		destMin=0;
    private int		destMax=1;
    private int		messageSize=1;
    private int		responseSize=1;
    private Random rng;
    private RSA_AES_Encryption rsa_aes_encryption = new RSA_AES_Encryption();
    private RandomStringGenerator rsg = new RandomStringGenerator();
    public int saw_init_msgs;


    public Encrypted_P2P_Messenger(Settings s) {
        if (s.contains(MESSAGE_INTERVAL)){
            this.interval = s.getDouble(MESSAGE_INTERVAL);
        }
        if (s.contains(MESSAGE_OFFSET)){
            this.lastMSg = s.getDouble(MESSAGE_OFFSET);
        }
        if (s.contains(MESSAGE_SEED)){
            this.seed = s.getInt(MESSAGE_SEED);
        }
        if (s.contains(INITIAL_SaW_COPIES)){
            this.saw_init_msgs = s.getInt(INITIAL_SaW_COPIES);
        }

        rng = new Random(this.seed);
        super.setAppID(APP_ID);
    }

    public Encrypted_P2P_Messenger(Encrypted_P2P_Messenger em) {
        super(em);
        this.lastMSg = em.getLastMSg();
        this.interval = em.getInterval();
        this.destMax = em.getDestMax();
        this.destMin = em.getDestMin();
        this.seed = em.getSeed();
        this.responseSize = em.getResponseSize();
        this.messageSize = em.getMessageSize();
        this.rng = new Random(this.seed);
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        String type = (String) msg.getProperty("type");
        if (type == null) return msg;

        if (host.getName().startsWith("p")) {
            if (msg.getTo() == host && type.equalsIgnoreCase("encryptedMsg")) {
                if (host.getMyMessages().contains(msg.getId())) {
                    return null;
                }
                super.sendEventToListeners("GotMyMessage", null, host);

                String encryptedMsgType = (String) msg.getProperty("contents");
                byte[] utility = (byte[]) msg.getProperty("utility");
                String unencryptedMsgOriginal = (String) msg.getProperty("unencrypted");

                SecretKey receivedKeyDecrypted = rsa_aes_encryption.decryptAESwithRSA(
                        utility,
                        host.getKeyPair().getPrivate()
                );

                boolean found = false;
                for (DTNHost node: host.getSecretKeys().keySet()) {
                    if (Base64.getEncoder().encodeToString(host.getSecretKeys().get(node).getEncoded()).
                            equals(Base64.getEncoder().encodeToString(receivedKeyDecrypted.getEncoded()))) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.err.println("Invalid secret key encountered");
                }

                String unencryptedMessage = rsa_aes_encryption.decryptMessageWithAES(
                        encryptedMsgType,
                        receivedKeyDecrypted
                );
                if (unencryptedMessage.equalsIgnoreCase(unencryptedMsgOriginal)) {
                    host.getMyMessages().add(msg.getId());
                    String id = "encryptedResponse" + SimClock.getIntTime() + "-" + host.getAddress();
                    Message m = new Message(host, msg.getFrom(), id, getResponseSize());
                    byte[] encryptedAES = rsa_aes_encryption.encryptAESwithRSA(
                            host.getSecretKeys().get(msg.getFrom()),
                            rsa_aes_encryption.getPublicKeyFromArray(host.getPublicKeys().get(m.getTo())));
                    m.addProperty("type", "encryptedResponse");
                    m.addProperty("utility", encryptedAES);
                    m.addProperty(
                            "contents",
                            rsa_aes_encryption.encryptMessageWithAES("success" + msg.getId(), host.getSecretKeys().get(msg.getFrom()))
                    );
                    m.setSize(1000000);
                    m.setAppID(APP_ID);
                    host.createNewMessage(m);

                    super.sendEventToListeners("SentResponse", null, host);
                    return null;
                }
            }

            if (msg.getTo() == host && type.equalsIgnoreCase("encryptedResponse")) {
                if (!host.getMyMessages().contains(msg.getId())) {
                    host.getMyMessages().add(msg.getId());
                    super.sendEventToListeners("ReceivedResponse", null, host);
                }
                return null;
            }
        }


        return msg;
    }

    private DTNHost randomHost(DTNHost host) {
        Object[] hosts = host.getSecretKeys().keySet().toArray();
        DTNHost recepient = (DTNHost) hosts[new Random().nextInt(hosts.length)];
        return recepient;
    }

    @Override
    public void update(DTNHost host) {
        double curTime = SimClock.getTime();

        if (host.getName().startsWith("p")) {
            if (curTime - this.lastMSg >= this.interval) {
                DTNHost to = randomHost(host);

                Message m = new Message(host, to, "encryptedMsg" +
                        SimClock.getIntTime() + "-" + host.getAddress(),
                        getMessageSize());
                m.setSize(1000000);

                byte[] encryptedAES = rsa_aes_encryption.encryptAESwithRSA(
                        host.getSecretKeys().get(to),
                        rsa_aes_encryption.getPublicKeyFromArray(host.getPublicKeys().get(to)));
                String unencryptedMsg = rsg.randomBoundedStringGenerator();
                m.addProperty("type", "encryptedMsg");
                m.addProperty("utility", encryptedAES);
                m.addProperty(
                        "contents",
                        rsa_aes_encryption.encryptMessageWithAES(unencryptedMsg, host.getSecretKeys().get(to))
                );
                m.addProperty("unencrypted", unencryptedMsg);
                m.addProperty("time", rsa_aes_encryption.encryptMessageWithAES("" + m.getCreationTime(), host.getSecretKeys().get(to)));


                m.setAppID(APP_ID);
                host.createNewMessage(m);

                super.sendEventToListeners("SentMsg", null, host);

                this.lastMSg = curTime;
            }
        }
    }

    @Override
    public Application replicate() {
        return new Encrypted_P2P_Messenger(this);
    }

    public double getLastMSg() {
        return lastMSg;
    }

    public void setLastMSg(double lastMSg) {
        this.lastMSg = lastMSg;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getDestMin() {
        return destMin;
    }

    public void setDestMin(int destMin) {
        this.destMin = destMin;
    }

    public int getDestMax() {
        return destMax;
    }

    public void setDestMax(int destMax) {
        this.destMax = destMax;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public int getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }

    public Random getRng() {
        return rng;
    }

    public void setRng(Random rng) {
        this.rng = rng;
    }
}
