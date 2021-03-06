/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import applications.Encrypted_P2P_Messenger;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

/**
 * Reporter for the <code>PingApplication</code>. Counts the number of pings
 * and pongs sent and received. Calculates success probabilities.
 *
 * @author teemuk
 */
public class ShAppReporter extends Report implements ApplicationListener {

	private int msgsSent=0, msgsReceived=0;
	private int responsesSent=0, responsesReceived=0;
	private int broadcastSent = 0, broadcastDecrypted = 0, broadcastReceived;

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {
		// Check that the event is sent by correct application type
		if (!(app instanceof Encrypted_P2P_Messenger)) return;

		// Increment the counters based on the event type
		if (event.equalsIgnoreCase("GotMyMessage")) {
			msgsReceived++;
		}
		if (event.equalsIgnoreCase("SentResponse")) {
			responsesSent++;
		}
		if (event.equalsIgnoreCase("ReceivedResponse")) {
			responsesReceived++;
		}
		if (event.equalsIgnoreCase("SentMsg")) {
			msgsSent++;
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

	}


	@Override
	public void done() {
		write("Messages stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));
		double msgProb = 0; // ping probability
		double responseProb = 0; // pong probability
		double broadcastProb = 0;

		if (this.msgsSent > 0) {
			msgProb = (1.0 * this.msgsReceived) / this.msgsSent;
		}
		if (this.responsesSent > 0) {
			responseProb = (1.0 * this.responsesReceived) / this.responsesSent;
		}
		if (this.broadcastSent > 0) {
			broadcastProb = (1.0 * this.broadcastReceived) / (this.broadcastSent * 4);
		}


		String statsText = "messages sent: " + this.msgsSent +
			"\nmessages received: " + this.msgsReceived +
			"\nresponses sent: " + this.responsesSent +
			"\nresponses received: " + this.responsesReceived +
			"\nbroadcasts sent: " + this.broadcastSent +
			"\nbroadcasts received: " + this.broadcastReceived +
			"\nbroadcasts decrypted: " + this.broadcastDecrypted +
			"\nmessage delivery prob: " + format(msgProb) +
			"\nresponse delivery prob: " + format(responseProb) +
			"\nbroadcast delivery prob: " + format(broadcastProb)
			;

		write(statsText);
		super.done();
	}
}
