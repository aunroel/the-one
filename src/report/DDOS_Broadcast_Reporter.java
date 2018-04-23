/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import applications.*;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

/**
 * Reporter for the <code>PingApplication</code>. Counts the number of pings
 * and pongs sent and received. Calculates success probabilities.
 *
 * @author teemuk
 */
public class DDOS_Broadcast_Reporter extends Report implements ApplicationListener {

	private int broadcastSent = 0, broadcastDecrypted = 0, broadcastReceived;
	private int ddos_sent = 0;

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {
		if (!(app instanceof DDOSBroadcastMessenger_NoPrevention)
				&& !(app instanceof DDOSBroadcastMessenger_Prevention)) return;

		if (event.equalsIgnoreCase("SentBroadcast")) {
			broadcastSent++;
		}
		if (event.equalsIgnoreCase("decryptedBroadcast")) {
			broadcastDecrypted++;
		}
		if (event.equalsIgnoreCase("GotBroadcast")) {
			broadcastReceived++;
		}
		if (event.equalsIgnoreCase("SentDDOS")) {
			ddos_sent++;
		}

	}


	@Override
	public void done() {
		write("Messages stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));

		double broadcastProb = 0;

		if (this.broadcastSent > 0) {
			broadcastProb = (1.0 * this.broadcastReceived) / (this.broadcastSent * 4);
		}


		String statsText =
			"\nbroadcasts sent: " + this.broadcastSent +
			"\nbroadcasts received: " + this.broadcastReceived +
			"\nbroadcasts decrypted: " + this.broadcastDecrypted +
			"\nddos sent: " + this.ddos_sent +
			"\nbroadcast delivery prob: " + format(broadcastProb)
			;

		write(statsText);
		super.done();
	}
}
