package applications;

import core.*;
import sh.RSA_AES_Encryption;
import sh.RandomStringGenerator;

import javax.crypto.SecretKey;
import java.util.Random;

public class DDOSBroadcastMessenger_Prevention extends Application{

    /** Message generation interval */
    public static final String MESSAGE_INTERVAL = "interval";
    /** Message interval offset - avoids synchronization of msg sending */
    public static final String MESSAGE_OFFSET = "offset";
    /** Seed for the app's random number generator */
    public static final String MESSAGE_SEED = "seed";
    /** Seed for the app's random number generator */
    public static final String INITIAL_SaW_COPIES = "copies";

    public static final int MAX_MSGS_PER_15M = 2;

    public static final int PENALTY_PERIOD = 5400;

    public static final int DDOS_INTERVAL = 120;


    /** Application ID */
    public static final String APP_ID = "cs.standrews.ac.uk.DDOSBroadcastMessenger_Prevention";

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


    public DDOSBroadcastMessenger_Prevention(Settings s) {
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

    public DDOSBroadcastMessenger_Prevention(DDOSBroadcastMessenger_Prevention em) {
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

        if (host.getName().startsWith("o")) {
            return null;
        }


        String purpose = (String) msg.getProperty("purpose");

        if (purpose != null && purpose.equalsIgnoreCase("broadcast")) {
            super.sendEventToListeners("GotBroadcast", null, host);

            String encryptedBroadcast = (String) msg.getProperty("contents");
            byte[] utilityBroadcast = (byte[]) msg.getProperty("utility");
            String unencryptedBroadcastOriginal = (String) msg.getProperty("unencrypted");


            SecretKey broadcastKeyVerified = rsa_aes_encryption.verifyAESwithRSA(
                    utilityBroadcast,
                    rsa_aes_encryption.getPublicKeyFromArray(host.getPublicKeys().get(msg.getFrom()))
            );
            String unencryptedBroadcast = rsa_aes_encryption.decryptMessageWithAES(
                    encryptedBroadcast,
                    broadcastKeyVerified
            );
            if (unencryptedBroadcast.equalsIgnoreCase(unencryptedBroadcastOriginal)) {
                super.sendEventToListeners("decryptedBroadcast", null, host);
            }
            if (msg.getTo() == host) {
                return null;
            } else
                return msg;
        }

        return msg;
    }

    private DTNHost randomHost(DTNHost host) {
        if (host.getName().startsWith("p")) {
            Object[] hosts = host.getSecretKeys().keySet().toArray();
            return  (DTNHost) hosts[new Random().nextInt(hosts.length)];
        } else
            return host.getHosts().get((new Random()).nextInt(host.getHosts().size()));
    }

    @Override
    public void update(DTNHost host) {
        double curTime = SimClock.getTime();

        double tempInterval = this.interval;

        if (host.getName().startsWith("o")) {
            this.interval = DDOS_INTERVAL;
        }

        if (curTime - host.getProbationStarted() >= 900) {
            host.setProbationStarted(curTime);
            host.setMsgsPerTimePeriod(0);
        }

        if (host.getMsgsPerTimePeriod() > MAX_MSGS_PER_15M) {
            host.setPenaltyTimeStarted(curTime);
            return;
        }

        if (host.getName().startsWith("o")) {
            if (curTime - this.lastMSg >= this.interval && curTime - host.getPenaltyTimeStarted() >= PENALTY_PERIOD) {
                DTNHost to = randomHost(host);
                Message m = new Message(host, to, "ddos" +
                        SimClock.getIntTime() + "-" + host.getAddress(),
                        getMessageSize());
                m.setSize(1000000);
                m.setAppID(APP_ID);
                host.createNewMessage(m);

                if (curTime - host.getProbationStarted() >= 900) {
                    host.setProbationStarted(curTime);
                    host.setMsgsPerTimePeriod(0);
                }

                host.setMsgsPerTimePeriod(host.getMsgsPerTimePeriod() + 1);

                super.sendEventToListeners("SentDDOS", null, host);

                this.interval = tempInterval;

                this.lastMSg = curTime;

                return;
            }
        }


        if (curTime - this.lastMSg >= this.interval) {
            DTNHost to = randomHost(host);
            Message m = new Message(host, to, "encryptedMsg" +
                    SimClock.getIntTime() + "-" + host.getAddress(),
                    getMessageSize());
            m.setSize(1000000);
            byte[] encryptedAES = rsa_aes_encryption.signAESwithRSA(
                    host.getSecretKeys().get(to),
                    host.getKeyPair().getPrivate()
                    );
            String unencryptesMsg = rsg.randomBoundedStringGenerator();
            m.addProperty("type", "encryptedMsg");
            m.addProperty("utility", encryptedAES);
            m.addProperty(
                    "contents",
                    rsa_aes_encryption.encryptMessageWithAES(unencryptesMsg, host.getSecretKeys().get(to))
            );
            m.addProperty("unencrypted", unencryptesMsg);
            m.addProperty("purpose", "broadcast");


            m.setAppID(APP_ID);
            host.createNewMessage(m);

            super.sendEventToListeners("SentBroadcast", null, host);

            this.lastMSg = curTime;
        }
        this.interval = tempInterval;

    }

    @Override
    public Application replicate() {
        return new DDOSBroadcastMessenger_Prevention(this);
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
