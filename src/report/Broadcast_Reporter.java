/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import applications.BlackHoleBroadcastMessenger;
import applications.EncryptedBroadcastMessenger;
import applications.Encrypted_P2P_Messenger;
import applications.LocalityBroadcastMessenger;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

import java.util.*;

/**
 * Reporter for the <code>PingApplication</code>. Counts the number of pings
 * and pongs sent and received. Calculates success probabilities.
 *
 * @author teemuk
 */
public class Broadcast_Reporter extends Report implements ApplicationListener {

	private int broadcastSent = 0, broadcastDecrypted = 0, broadcastReceived = 0;
	private int copies = 6; private double origins_changed = 0;
	boolean locality = false;

	private HashMap<String, Double> hosts = new HashMap<>();

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {
		if (!(app instanceof EncryptedBroadcastMessenger)
				&& !(app instanceof BlackHoleBroadcastMessenger) && !(app instanceof LocalityBroadcastMessenger)) {
			return;
		}
		if (app instanceof LocalityBroadcastMessenger) {
			locality = true;
		}


		if (event.equalsIgnoreCase("SentBroadcast")) {
			broadcastSent++;
		}
		if (event.equalsIgnoreCase("decryptedBroadcast")) {
			broadcastDecrypted++;
		}
		if (event.equalsIgnoreCase("GotBroadcast")) {
			broadcastReceived++;
		}

		if (event.equalsIgnoreCase("ChangedSender")) {
			origins_changed++;
		}

		if (event.startsWith("H:")) {
			if (hosts.containsKey(event.substring(2))) {
				hosts.replace(event.substring(2), hosts.get(event.substring(2)) + 1d);
			} else {
				hosts.put(event.substring(2), 1d);
			}
		}

	}


	@Override
	public void done() {

		write("Messages stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));

		double broadcastProb = 0.0;
		String n1_popular = "", n2_popular = "", n3_popular = "";
		double n1_value = 0, n2_value = 0, n3_value = 0;
		Map.Entry<String, Double> maxValue;

		if (this.broadcastSent > 0) {
			broadcastProb = (1.0 * this.broadcastReceived) / (this.broadcastSent * copies);
		}

		if (locality) {
			for (int i = 0; i < 3; i++) {
				maxValue = Collections.max(hosts.entrySet(), Comparator.comparingDouble(Map.Entry::getValue));
				if (i == 0) {
					n1_popular = maxValue.getKey();
					n1_value = maxValue.getValue();
					hosts.remove(maxValue.getKey());
				}
				if (i == 1) {
					n2_popular = maxValue.getKey();
					n2_value = maxValue.getValue();
					hosts.remove(maxValue.getKey());
				}
				if (i == 2) {
					n3_popular = maxValue.getKey();
					n3_value = maxValue.getValue();
					hosts.remove(maxValue.getKey());
				}
			}
		}


		String statsText =
			"\nbroadcasts sent: " + this.broadcastSent +
			"\nbroadcasts received: " + this.broadcastReceived +
			"\nbroadcasts decrypted: " + this.broadcastDecrypted +
			"\nbroadcast delivery prob: " + format(broadcastProb) +
			"\n\n1st most active node: " + n1_popular +
			"\n1st node amount: " + n1_value +
			"\n2nd most active node: " + n2_popular +
			"\n2nd node amount: " + n2_value +
			"\n3rd most active node: " + n3_popular +
			"\n3rd node amount: " + n3_value +
			"\norigins changed: " + origins_changed;


		write(statsText);
		super.done();
	}
}
