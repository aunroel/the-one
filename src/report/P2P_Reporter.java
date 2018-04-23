/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import applications.*;
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
public class P2P_Reporter extends Report implements ApplicationListener {

	private int msgsSent=0, msgsReceived=0;
	private int responsesSent = 0, responsesReceived = 0;
	private int origins_changed = 0;

	private HashMap<String, Double> masked_hosts = new HashMap<>();
	private HashMap<String, Double> real_hosts = new HashMap<>();

	private boolean locality = false;

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {
		// Check that the event is sent by correct application type
		if (!(app instanceof Encrypted_P2P_Messenger)
				&& !(app instanceof BlackHole_P2P_Messenger) &&  !(app instanceof Locality_P2P_Messenger)) {
			return;
		}

		if (app instanceof Locality_P2P_Messenger) {
			locality = true;
		}

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

		if (event.equalsIgnoreCase("ChangedSender")) {
			origins_changed++;
		}

		if (event.startsWith("SH:")) {
			if (masked_hosts.containsKey(event.substring(2))) {
				masked_hosts.replace(event.substring(2), masked_hosts.get(event.substring(2)) + 1);
			} else {
				masked_hosts.put(event.substring(2), 1d);
			}
		}

		if (event.startsWith("RH:")) {
			if (real_hosts.containsKey(event.substring(2))) {
				real_hosts.replace(event.substring(2), real_hosts.get(event.substring(2)) + 1);
			} else {
				real_hosts.put(event.substring(2), 1d);
			}
		}

	}


	@Override
	public void done() {
		write("Messages stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));
		double msgProb = 0; // ping probability
		double responseProb = 0; // pong probability
		String s1_popular = "", s2_popular = "", s3_popular = "";
		double s1_value = 0, s2_value = 0, s3_value = 0;
		Map.Entry<String, Double> s_maxValue;

		String r1_popular = "", r2_popular = "", r3_popular = "";
		double r1_value = 0, r2_value = 0, r3_value = 0;
		Map.Entry<String, Double> r_maxValue;



		if (this.msgsSent > 0) {
			msgProb = (1.0 * this.msgsReceived) / this.msgsSent;
		}
		if (this.responsesSent > 0) {
			responseProb = (1.0 * this.responsesReceived) / this.responsesSent;
		}

		if (locality) {
			for (int i = 0; i < 3; i++) {
				s_maxValue = Collections.max(masked_hosts.entrySet(), Comparator.comparingDouble(Map.Entry::getValue));
				if (i == 0) {
					s1_popular = s_maxValue.getKey();
					s1_value = s_maxValue.getValue();
					masked_hosts.remove(s_maxValue.getKey());
				}
				if (i == 1) {
					s2_popular = s_maxValue.getKey();
					s2_value = s_maxValue.getValue();
					masked_hosts.remove(s_maxValue.getKey());
				}
				if (i == 2) {
					s3_popular = s_maxValue.getKey();
					s3_value = s_maxValue.getValue();
					masked_hosts.remove(s_maxValue.getKey());
				}
			}

			for (int i = 0; i < 3; i++) {
				r_maxValue = Collections.max(real_hosts.entrySet(), Comparator.comparingDouble(Map.Entry::getValue));
				if (i == 0) {
					r1_popular = r_maxValue.getKey();
					r1_value = r_maxValue.getValue();
					real_hosts.remove(r_maxValue.getKey());
				}
				if (i == 1) {
					r2_popular = r_maxValue.getKey();
					r2_value = r_maxValue.getValue();
					real_hosts.remove(r_maxValue.getKey());
				}
				if (i == 2) {
					r3_popular = r_maxValue.getKey();
					r3_value = r_maxValue.getValue();
					real_hosts.remove(r_maxValue.getKey());
				}
			}
		}



		String statsText = "messages sent: " + this.msgsSent +
			"\nmessages received: " + this.msgsReceived +
			"\nresponses sent: " + this.responsesSent +
			"\nresponses received: " + this.responsesReceived +
			"\nmessage delivery prob: " + format(msgProb) +
			"\nresponse delivery prob: " + format(responseProb) +
			"\n\n1st most active masked node:" + s1_popular +
			"\n1st node masked amount: " + s1_value +
			"\n2nd most active masked node:" + s2_popular +
			"\n2nd node masked amount: " + s2_value +
			"\n3rd most active masked node:" + s3_popular +
			"\n3rd node masked amount: " + s3_value +
				"\n\n1st most active real node:" + r1_popular +
				"\n1st node real amount: " + r1_value +
				"\n2nd most active real node:" + r2_popular +
				"\n2nd node real amount: " + r2_value +
				"\n3rd most active real node:" + r3_popular +
				"\n3rd node real amount: " + r3_value +

				"\norigins changed: " + origins_changed;


		write(statsText);
		super.done();
	}
}
