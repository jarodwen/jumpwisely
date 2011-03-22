/**
 *	SimpleMTA SMTPConnection.java
 *
 *  Copyright (C) 2010 JArod Wen
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 **/
package edu.ucr.cs.jumpwisely.simpleMTA;

import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.*;
import javax.naming.directory.*;

/**
 * Spawn a new SMTP connection for the connected client.
 * 
 */
public class SMTPConnection implements Runnable {

	/* The socket to the client */
	private Socket socket;

	/* We need the name of the local machine and remote machine. */
	private String localHost, remoteHost, localAddress;

	private MailMessage message;

	private boolean isLocalDelivery;

	private ArrayList<MailinQueue> mailQueue;
	
	private boolean isRetry;

	private SimpleLog simpleLog;

	private String threadID;

	/* Socket input and output Stream */
	DataOutputStream toClient;

	BufferedReader fromClient;

	private static final String CRLF = "\r\n";

	private static final boolean SENDER = false;

	private static final boolean RECEIVER = true;

	public String spoolFilePath;
	public int retryDuration;

	/* Constructor */
	public SMTPConnection(Socket socket) throws Exception {
		this.socket = socket;
		isLocalDelivery = true;
		mailQueue = new ArrayList<MailinQueue>();
		threadID = UUID.randomUUID().toString().substring(0, 8);
		simpleLog = new SimpleLog(threadID);
		isRetry = true;

		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			localHost = addr.getHostName();
			localAddress = addr.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			localHost = "";
			localAddress = "";
			e.printStackTrace();
		}

		remoteHost = "";
		message = new MailMessage(UUID.randomUUID().toString().substring(0, 6));
		message.From = "";
		message.To = new ArrayList<String>();
		message.Data = new ArrayList<String>();

		this.spoolFilePath = "spool";
		this.retryDuration = 2000;
	}

	/* Implement the run() method of the Runnable interface. */
	public void run() {
		try {
			simpleLog.println("Reading configuration...\n");
			getConfiguration();
			simpleLog.println("Spool processing...\n");
			int intMailinQueue = processQueue();
			if (intMailinQueue == 0) {
				simpleLog.println("No mail in queue.\n");
			} else {
				simpleLog.println(String.valueOf(intMailinQueue)
						+ " mails are processed.\n");
			}
			processRequest();
		} catch (Exception e) {
			simpleLog.println(e);
		}
	}

	private void getConfiguration() throws FileNotFoundException {
		BufferedReader bufFileNew = new BufferedReader(new FileReader(
				"SimpleMTA.conf"));
		String strTempNew;
		try {
			while ((strTempNew = bufFileNew.readLine()) != null) {
				if (strTempNew.startsWith("%"))
					continue;
				else {
					if (strTempNew.trim().startsWith("SPOOL_FILE_PATH"))
						this.spoolFilePath = strTempNew.split("\t")[1];
					if (strTempNew.trim().startsWith("RETRY_TIMES"))
						this.retryDuration = Integer.valueOf(strTempNew
								.split("\t")[1]);
					if (strTempNew.trim().startsWith("LOCALADDR"))
						this.localAddress = strTempNew.split("\t")[1];
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processRequest() {

		/* Local Variables */
		/* flags to indicate client quit the connection */
		boolean quit = false;
		/* flags to indicate the connection is reset */
		boolean HELOagain = false;
		/* flags for delivery */
		boolean canDelivery = false;
		/* String variable to store the client command */
		String requestCommand;

		try {

			/* Get a reference to the socket's input and output streams. */
			InputStream is = socket.getInputStream();
			toClient = new DataOutputStream(socket.getOutputStream());

			/* Set up input stream filters. */
			InputStreamReader sr = new InputStreamReader(is);
			fromClient = new BufferedReader(sr);

		} catch (IOException e) {
			simpleLog.println("Initialization error: " + e);
			System.exit(1);
		}

		/* SMTP handshake and negotiation. */
		/* We need the name of the local machine and remote machine. */
		try {
			InetAddress addr = socket.getLocalAddress();
			localHost = addr.getHostName();
			localAddress = addr.getHostAddress().toString();
			message.localHost = localHost;
		} catch (Exception e) {
			simpleLog.println("Get local hostname error: " + e
					+ ", using default value.");
			localHost = "Unknown Host";
			localAddress = "0.0.0.0";
			message.localHost = localHost;
		}
		try {
			InetAddress addr = socket.getInetAddress();
			remoteHost = addr.getHostName();
		} catch (Exception e) {
			simpleLog.println("Get remote hostname error: " + e
					+ ", using default value.");
			remoteHost = "Unknown Host";
		}

		/* Get the domain name */
		String localDomain = localHost.substring(localHost.indexOf(".") + 1);

		/* Send the appropriate SMTP Welcome command. */
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		reply("220 " + localHost + " SimpleMTA "
				+ dfm.format(new Date(System.currentTimeMillis())));

		/* Wait the client to send the HELO command, no EHLO support */
		while (!quit) {
			if ((requestCommand = fetch()).length() == 0)
				quit = true;
			else if (requestCommand.trim().substring(0, 4).equalsIgnoreCase(
					"helo")
					|| requestCommand.trim().substring(0, 4).equalsIgnoreCase(
							"ehlo")) {
				if (!parseHELO(requestCommand.trim()))
					continue;
				else {
					/* String variable to store the sender information */
					message.From = "";
					/* String variable to store the receiver information */
					message.To = new ArrayList<String>();
				}
			}

			/* If the client want to quit this session */
			else if (requestCommand.trim().substring(0, 4).equalsIgnoreCase(
					"quit"))
				quit = true;

			/* If the client want to reset the session */
			else if (requestCommand.trim().substring(0, 4).equalsIgnoreCase(
					"rset"))
				continue;

			/*
			 * If the client send the command that is not expected to see now,
			 * output an error
			 */
			else if (requestCommand.trim().substring(0, 4).equalsIgnoreCase(
					"expn")
					|| requestCommand.trim().substring(0, 4).equalsIgnoreCase(
							"vrfy")
					|| requestCommand.trim().substring(0, 4).equalsIgnoreCase(
							"help")
					|| requestCommand.trim().substring(0, 4).equalsIgnoreCase(
							"etrn")
					|| requestCommand.trim().substring(0, 4).equalsIgnoreCase(
							"noop")
					|| requestCommand.trim().substring(0, 4).equalsIgnoreCase(
							"verb")) {
				reply("502 Sorry this command has not been implemented");
			} else {
				/* If unrecognized command is received, output an error */
				reply("500 Unrecognizable command");
			}

			HELOagain = false;

			/* Wait for Mail session */
			while (!quit && !HELOagain) {
				if ((requestCommand = fetch()).length() == 0)
					quit = true;

				/* If the client send the appropriate command */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("mail")) {

					if (validate(requestCommand, SENDER)) {

						/* get the sender address */
						/*
						 * get rid of the head command "MAIL FROM:", which is
						 * 10-char long
						 */
						message.From = requestCommand.trim().substring(10);

						/*
						 * if receiver is not in the form of "<address>",
						 * standarize it
						 */
						if (!message.From.startsWith("<"))
							message.From = "<" + message.From + ">";

						/*
						 * if the sender part is empty, use the user itself as
						 * the default sender
						 */
						if (message.From.trim() == "") {
							message.From = "<"
									+ System.getProperty("user.name") + "@"
									+ localDomain + ">";
						}

						/* tell the client the sender is ok */
						/* 251 for remote forward will be handled in validate */
						if (isLocalDelivery)
							reply("250 OK");
						break;
					}
				}

				/* If the client says HELLO again */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("helo")) {
					if (parseHELO(requestCommand))
						HELOagain = true;
				}

				/* If the client say RSET */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("rset")) {
					HELOagain = true;
				}

				/* If the client want to quit this session */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("quit"))
					quit = true;

				/*
				 * If the client send the command that is not expected to see
				 * now, output an error
				 */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("rcpt")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("data")) {
					reply("503 Bad sequence of commands");
				}
				/* If unrecognized command is received, output an error */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("expn")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("vrfy")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("help")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("etrn")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("noop")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("verb")) {
					reply("502 Sorry this command has not been implemented");
				} else {
					/* If unrecognized command is received, output an error */
					reply("500 Unrecognizable command");
				}
			}

			/* Wait for Receipent and Data session */
			while (!quit && !HELOagain) {
				if ((requestCommand = fetch()).length() == 0)
					quit = true;

				/* If the client send the appropriate command */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("rcpt")) {
					if (validate(requestCommand, RECEIVER)) {

						/* get the receiver address */
						String strTo = requestCommand.trim().substring(8);

						/*
						 * if receiver is not in the form of "<address>",
						 * standarize it
						 */
						if (!strTo.startsWith("<"))
							strTo = "<" + strTo + ">";

						/*
						 * if the receiver part is empty, use the user itself as
						 * the default sender
						 */
						if (strTo.trim() == "") {
							strTo = "<" + System.getProperty("user.name") + "@"
									+ localHost + ">";
						}

						message.To.add(strTo);

						/* tell the client the receiver is ok */
						reply("250 Accepted");
						continue;
					}
				}

				/* If the client send the appropriate command */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("data")) {
					reply("354 Enter message, ending with \".\" on a line by itself");

					/* Call another method to handle this session */
					receiveMessage();

					/* tell the client the message is saved */
					reply("250 OK id=" + message.Message_Id);
					canDelivery = true;
					HELOagain = true;
				}

				/* If the client says HELLO again */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("helo")) {
					if (parseHELO(requestCommand))
						HELOagain = true;
				}

				/* If the client want to quit this session */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("quit"))
					quit = true;

				/*
				 * If the client send the command that is not expected to see
				 * now, output an error
				 */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("mail")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("rcpt")) {
					reply("503 Bad sequence of commands");
				}

				/* If unrecognized command is received, output an error */
				else if (requestCommand.trim().substring(0, 4)
						.equalsIgnoreCase("expn")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("vrfy")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("help")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("etrn")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("noop")
						|| requestCommand.trim().substring(0, 4)
								.equalsIgnoreCase("verb")) {
					reply("502 Sorry this command has not been implemented");
				} else {
					/* If unrecognized command is received, output an error */
					reply("500 Unrecognizable command");
				}
			}

			/* Delivery the mail */
			if (canDelivery) {
				if (delivery(message)) {
					simpleLog.println("Mail delivaried successfully!");
					canDelivery = false;
					continue;
				} else {
					simpleLog.println("Mail delivary Failed!");
					reply("550 Delivery failed: User or host unreachable.");
					canDelivery = false;
					continue;
				}
			}

			/* Reset the session by re-enter the loop */
			if (HELOagain) {
				continue;
			}
		}

		/* tell the client that the server is closing the channel */
		reply("221 " + localHost + " SimpleMTA closing connection");
		reply("Connection to " + localHost + " closed by foreign host.");
		try {
			socket.close();
		} catch (IOException e) {
			simpleLog.println("Close connection error: " + e);
			System.exit(1);
		}
	}

	/* This method fetch every line from the client */
	private String fetch() {
		String strMessage = "";
		try {
			do {
				strMessage = fromClient.readLine();
				simpleLog.println(strMessage);
				if (strMessage == null)
					continue;

				/* If the message length is less than 5 characters */
				if (strMessage.length() < 4) {

					/* tell the client the command is unrecognized */
					reply("500 Unrecognizable command");
				} else {
					break;
				}
			} while (strMessage.length() < 512); /*
			 * The limitation of length
			 * for the command line is
			 * 512
			 */
		} catch (IOException e) {
			simpleLog.println("Read socket error: " + e);
		}
		try {
			if (strMessage == null) {
				socket.close();
				simpleLog.println("Null input");
				return "";
			}
		} catch (IOException e) {
			simpleLog.println("Close connection error: " + e);
		}
		return strMessage;
	}

	/* This method is to validate the HELLO command */
	private boolean parseHELO(String command) {
		/* flags to indicate the mode of this connection */
		boolean isEHLO;

		/* Check whether it is an EHLO or just an HELO */
		if (command.trim().substring(0, 4).equalsIgnoreCase("ehlo"))
			isEHLO = true;
		else
			isEHLO = false;
		if (command.substring(4).length() > 0) {

			// /* If it is an EHLO, display the greetings and server
			// compatibility */
			// if (/* Fill in */) {
			// reply(/* Fill in */);
			// /* Fill in */;
			// }
			//
			// /* If only HELO is received, just greets the client */
			// /* reply: 250 localHost Hello remoteHost [remoteIP] */
			// else {
			InetAddress addr = socket.getLocalAddress();
			String localHost = addr.getHostName();
			addr = socket.getInetAddress();
			String remoteHost = addr.getHostName();
			reply("250 " + localHost + " Hello " + remoteHost + " ["
					+ addr.getHostAddress().toString() + "]");
			// }
			return true;
		}

		/* If the HELLO command is not valid */
		else {
			reply("501 Syntax error in parameters or arguments");
		}
		return false;
	}

	/* This method validate the email address */
	private boolean validate(String user, boolean isReceiver) {
		boolean ok = false;
		InetAddress addr = socket.getLocalAddress();
		String localHost = addr.getHostName();
		String localIP = addr.getHostAddress().toString();

		/* get the domain name of the host */
		String localDomain = localHost.substring(localHost.indexOf(".") + 1);

		/* Ignore the case */
		user = user.toLowerCase();

		/* pre-filter for empty or no "@" part */
		if (user.length() == 0) {
			user = System.getProperty("user.name") + "@" + localHost;
		}
		if (user.indexOf("@") < 0) {
			user += "@" + localHost;
		}

		if (isReceiver) {

			/* The receiver email address is valid */
			if (user.matches(".+@.+")) {

				/*
				 * Check whether the message will be deliveried locally.
				 */
				if (user.indexOf(localHost) >= 0 || user.indexOf(localIP) >= 0) {
					isLocalDelivery = true;
				}
				/*
				 * If the receiver email address is valid but not in local
				 * domain
				 */
				else {
					isLocalDelivery = false;
					String strForwardRcpt = "";
					if (user.trim().charAt(0) == '<') {
						strForwardRcpt = user.trim().substring(1,
								user.trim().length() - 2);
					}
					// reply("251 User not local; will forward to <" + localHost
					// + ":" + strForwardRcpt + ">");
				}

				ok = true;
			}

			/* If the host name is [IP_Address] */
			else if (user.matches(".+@\\[.+\\]")) {

				String two_five_five = "(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))";
				Pattern IPPattern = Pattern.compile("^(?:.*?" + two_five_five
						+ "\\.){3}" + two_five_five + ".*?$");
				Matcher m = IPPattern.matcher(user);

				if (m.matches())
					ok = true;
			}

			/*
			 * If receiver the email address is not valid, output the error to
			 * client
			 */
			else {
				reply("501 Syntax error in parameters or arguments: Wrong receiver email address");
			}
		}

		/* If the sender email address is invalid */
		else if (user.matches(".+@.+")) {
			ok = true;
		}

		/* validation fail */
		else {
			reply("501 Syntax error in parameters or arguments: Wrong sender email address");
		}
		return ok;
	}

	private void reply(String command) {
		try {
			if (!socket.isClosed())
				toClient.writeBytes(command + CRLF);
		} catch (IOException e) {
			simpleLog.println("Write socket error: " + e);
		}
		simpleLog.println(command);
		return;
	}

	/* This method process the message body */
	private void receiveMessage() {
		String body = "";
		String line = "";
		message.Data = new ArrayList<String>();

		/* get the domain name of the host */
		String localDomain = localHost.substring(localHost.indexOf(".") + 1);
		message.Message_Id = message.Message_Id + "@" + localHost;

		try {
			do {
				/* Read each line from client */
				line = fromClient.readLine();

				if (line == null)
					break;

				/* Add the line into the strData for delivery */
				message.Data.add(line);

				/*
				 * If two dots appear at the beginning of a line, it means the
				 * transparency for the dot.
				 */
				if (line.trim() == "..")
					body += ".";
				else
					body += line;

				/* Do it again until the ending delimiter is hit */
			} while (!line.equals("."));
		} catch (IOException e) {
			simpleLog.println("Read socket error: " + e);
		}
		try {
			if (line == null)
				socket.close();
		} catch (IOException e) {
			simpleLog.println("Close connection error: " + e);
		}

		return;
	}

	private boolean delivery(MailMessage mailMessage) {
		ArrayList<String> strRemoteReceiver = new ArrayList<String>();
		ArrayList<String> strLocalReceiver = new ArrayList<String>();
		ArrayList<MailinQueue> mailFailed = new ArrayList<MailinQueue>();
		boolean blnRtn = true;
		try {
			mailMessage.fillHeader();
		} catch (Exception ex) {
			simpleLog.print(ex.toString());
			return false;
		}
		for (int i = 0; i < mailMessage.To.size(); i++) {
			/* Check whether delivery locally */
			if (mailMessage.To.get(i).trim().indexOf(localHost) >= 0
					|| mailMessage.To.get(i).trim().indexOf(localAddress) >= 0
					|| mailMessage.To.get(i).trim().indexOf("@") <= 0) {
				strLocalReceiver.add(mailMessage.To.get(i).trim());
			} else {
				strRemoteReceiver.add(mailMessage.To.get(i).trim());
			}
		}
		if (strLocalReceiver.size() != 0) {
			for (int i = 0; i < strLocalReceiver.size(); i++) {
				try {
					if (!deliveryLocal(strLocalReceiver.get(i), mailMessage)) {
						simpleLog.println("Local delivery to "+ strLocalReceiver.get(i) +" failed;");
						mailFailed.add(new MailinQueue(strLocalReceiver.get(i), "localhost", mailMessage));
						blnRtn = false;
					} else {
						simpleLog.println("Local delivery succeeded!");
					}
				} catch (Exception e) {
					simpleLog.println(e);
				}
			}
		}
		if (strRemoteReceiver.size() != 0) {
			for (int j = 0; j < strRemoteReceiver.size(); j++) {
				/* get the host information from the address */
				String strHostAdd = strRemoteReceiver.get(j).substring(
						strRemoteReceiver.get(j).indexOf("@") + 1).trim();
				if (strHostAdd.endsWith(">")) {
					strHostAdd = strHostAdd.substring(0,
							strHostAdd.length() - 1);
				}
				if (!deliveryRemote(strRemoteReceiver.get(j), strHostAdd,
						mailMessage)) {
					simpleLog
							.println("Remote delivery failed! Mail is enqueued for retry.");
					mailQueue.add(new MailinQueue(strRemoteReceiver.get(j),
							strHostAdd, mailMessage));
					blnRtn = false;
				} else {
					simpleLog.println("Remote delivery succeeded!");
				}
			}
		}
		if (mailQueue.size() > 0) {
			try {
				Thread.sleep(this.retryDuration);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				simpleLog.println("Thread is interrupted.");
				e.printStackTrace();
			}
			simpleLog.println("Retry for mails failed before...");
			while (mailQueue.size() != 0) {
				MailinQueue tempMail = mailQueue.get(0);
				if (!deliveryRemote(tempMail.receiver, tempMail.remoteMX,
						tempMail.mail)) {
					simpleLog.println("Retry for Remote delivery failed.");
					mailFailed.add(new MailinQueue(tempMail.receiver, tempMail.remoteMX, tempMail.mail));
					blnRtn = false;
				} else {
					simpleLog.println("Retry for Remote delivery succeeded!");
				}
				mailQueue.remove(0);
			}
		}
		
		// Failure notification
		if(mailFailed.size()>0 && isRetry){
			isRetry = false;
			for(int i=0; i<mailFailed.size(); i++){
				failNotification(mailFailed.get(i));
			}
			return false;
		}else{
			isRetry = true;
			return true;
		}
	}

	private boolean deliveryRemote(String strReceiver, String remoteMXName,
			MailMessage mailMessage) {

		simpleLog.println("Remote delivery");

		int remoteMXPort = 25;

		String two_five_five = "(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))";
		Pattern IPPattern = Pattern.compile("^(?:.*?" + two_five_five
				+ "\\.){3}" + two_five_five + ".*?$");
		Matcher m = IPPattern.matcher(remoteMXName);

		if (m.matches()) {
			/* set port to be 2225 */
			remoteMXPort = 2225;
			/* get rid of the bracket */
			if (remoteMXName.startsWith("["))
				try {
					remoteMXName = InetAddress.getByName(
							remoteMXName
									.substring(1, remoteMXName.length() - 1))
							.getHostName();
				} catch (UnknownHostException e) {
					simpleLog.println(e);
					return false;
				}
			else
				try {
					remoteMXName = InetAddress.getByName(remoteMXName.trim())
							.getHostName();
				} catch (UnknownHostException e) {
					simpleLog.println(e);
					return false;
				}
		} else if (remoteMXName.indexOf("ccs.neu.edu") >= 0) {
			remoteMXName = "smtp.ccs.neu.edu";
		} else {
			simpleLog.println("Cannot forward this message to:" + remoteMXName);
			return false;
		}
		// remoteMXName = "localhost";

		String strTempRtn = "";

		Socket socketDelivery = null;
		DataOutputStream osDelivery = null;
		BufferedReader isDelivery = null;

		try {
			socketDelivery = new Socket(remoteMXName, remoteMXPort);
			osDelivery = new DataOutputStream(socketDelivery.getOutputStream());
			isDelivery = new BufferedReader(new InputStreamReader(
					socketDelivery.getInputStream()));
		} catch (UnknownHostException e) {
			simpleLog.print("Cannot resolve the remote host of " + remoteMXName
					+ ".\n");
			return false;
		} catch (IOException e) {
			simpleLog.print("Cannot establish the socket.\n");
			return false;
		}

		if (socketDelivery == null || isDelivery == null || osDelivery == null) {
			System.err.println("Fail to establish a valid socket.");
			return false;
		}

		try {
			strTempRtn = isDelivery.readLine().trim();
			simpleLog.println(strTempRtn.substring(0, 3));
			if (!strTempRtn.substring(0, 3).equalsIgnoreCase("220")) {
				System.err.println("Fail to contact with SMTP server.");
				return false;
			}
		} catch (IOException e1) {
			System.err.println("Error: " + e1.toString());
		}

		/* Dealing with the sender and receiver */

		try {

			/* Send HELO */
			osDelivery.writeBytes("HELO "
					+ localHost.substring(localHost.indexOf(".") + 1) + "\r\n");
			strTempRtn = isDelivery.readLine().trim();
			if (!strTempRtn.substring(0, 3).equalsIgnoreCase("250")) {
				System.err.println("Error from remote server for HELO: "
						+ strTempRtn);
				return false;
			}

			/* MAIL FROM */
			osDelivery.writeBytes("MAIL FROM: " + mailMessage.From + "\r\n");
			strTempRtn = isDelivery.readLine().trim();
			if (!strTempRtn.substring(0, 3).equalsIgnoreCase("250")) {
				System.err.println("Error from remote server for MAIL FROM: "
						+ strTempRtn);
				return false;
			}

			/* RCPT TO */
			/* Pre-process on strReceiver */
			if (strReceiver.endsWith(","))
				strReceiver = strReceiver.substring(0,
						strReceiver.indexOf(',') - 1);
			osDelivery.writeBytes("RCPT TO: " + strReceiver + "\r\n");
			strTempRtn = isDelivery.readLine().trim();
			if (!strTempRtn.substring(0, 3).equalsIgnoreCase("250")) {
				System.err.println("Error from remote server for RCPT TO: "
						+ strTempRtn);
				return false;
			}

			/* DATA */
			osDelivery.writeBytes("DATA\r\n");
			strTempRtn = isDelivery.readLine().trim();
			if (!strTempRtn.substring(0, 3).equalsIgnoreCase("354")) {
				System.err.println("Error from remote server for DATA: "
						+ strTempRtn);
				return false;
			}

			/* Write message */
			for (int i = 0; i < mailMessage.Data.size(); i++) {
				osDelivery.writeBytes(mailMessage.Data.get(i) + "\r\n");
			}
			strTempRtn = isDelivery.readLine().trim();
			if (!strTempRtn.substring(0, 3).equalsIgnoreCase("250")) {
				System.err.println("Error from remote server for input: "
						+ strTempRtn);
				return false;
			}

			/* QUIT */
			osDelivery.writeBytes("QUIT\r\n");
			strTempRtn = isDelivery.readLine().trim();
			if (!strTempRtn.substring(0, 3).equalsIgnoreCase("221")) {
				System.err.println("Error from remote server for QUIT: "
						+ strTempRtn);
				return false;
			}

		} catch (UnknownHostException e) {
			System.err.print("Cannot resolve the remote host of "
					+ remoteMXName + ".\n");
			return false;
		} catch (IOException e) {
			System.err.print("Cannot establish the socket.\n");
			return false;
		}

		return true;
	}

	/* Delivery the message locally */
	private boolean deliveryLocal(String strReceiver, MailMessage mailMessage)
			throws Exception {
		simpleLog.println("Local delivery");
		String strUserList = "";
		String strTemp = "";
		/* Get the user information from /etc/passwd */
		BufferedReader bufFile = new BufferedReader(new FileReader(
				"/etc/passwd"));
		while ((strTemp = bufFile.readLine()) != null) {
			strUserList += strTemp.trim().split(":")[0] + ",";
		}
		String strUserName = strReceiver;

		/* Pre-filter on the address */
		/* Get rid of the "@Domain" part */
		if (strUserName.indexOf("@") > 0)
			strUserName = strUserName.split("@")[0];
		/* Remove the "<" and ">" */
		if (strUserName.startsWith("<")) {
			strUserName = strUserName.substring(1);
		}
		if (strUserList.toLowerCase().indexOf(strUserName.trim().toLowerCase()) >= 0) {
			try {
				String strFileName = "";
				String strFileNameList[] = strUserList.split(",");
				for (int j = 0; j < strFileNameList.length; j++) {
					if (strFileNameList[j].equalsIgnoreCase(strUserName.trim()))
						strFileName = strFileNameList[j];
				}
				if (strFileName == "") {
					simpleLog.println("The user has no mailbox");
					return false;
				}
				simpleLog.println(strFileName);
				File fileMailbox = new File("/var/spool/mail/" + strFileName);
				if (!fileMailbox.exists()) {
					fileMailbox.createNewFile();
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(
						"/var/spool/mail/" + strFileName, true));
				out.write(mailMessage.assembleMail());
				out.close();
				simpleLog.println("Local delivery success to " + strUserName);
			} catch (IOException e) {
				simpleLog.println(e);
				return false;
			}
		} else {
			simpleLog.println("No such user or the user has no mailbox.");
			return false;
		}
		return true;
	}

	/* Failure notification sent to the sender */
	private void failNotification(MailinQueue failedMail) {

		simpleLog.println("Failure notification delivery.");
		
		MailMessage mailMessage = failedMail.mail;

		/* Change the receptant of the mail to the original receiver */
		mailMessage.To = new ArrayList<String>();
		String strFailNotificationTo = mailMessage.From;
		mailMessage.To.add(strFailNotificationTo);

		/* Change the sender of the mail to be the simpleMTA system */
		mailMessage.From = "jarod@129.10.112.181";

		mailMessage.Subject = "Undeliverable mail to: "+failedMail.receiver+" at: "+failedMail.remoteMX;

		String originalMail = mailMessage.assembleMail();
		
		mailMessage.Data = new ArrayList<String>();
		mailMessage.Data.add("----------Original Mail----------");
		mailMessage.Data.add(">"+originalMail);
		mailMessage.Data.add(".");
		
		/* delivery the fail notification */
		if (delivery(mailMessage)) {
			simpleLog.println("Failure notification has been sent to:"
					+ mailMessage.To.get(0));
		} else {
			simpleLog.println("Fail to send the failure notification to:"
					+ mailMessage.To.get(0));
		}
	}

	/* Destructor. Closes the connection if something bad happens. */
	protected void finalize() throws Throwable {
		socket.close();
		super.finalize();
	}

	/**
	 * Get MX information Refer to:
	 * http://www.rgagnon.com/javadetails/java-0452.html
	 * 
	 * @param hostName
	 * @return
	 * @throws NamingException
	 */
	private static ArrayList getMX(String hostName) throws NamingException {
		// Perform a DNS lookup for MX records in the domain
		Hashtable env = new Hashtable();
		env.put("java.naming.factory.initial",
				"com.sun.jndi.dns.DnsContextFactory");
		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(hostName, new String[] { "MX" });
		Attribute attr = attrs.get("MX");

		// if we don't have an MX record, try the machine itself
		if ((attr == null) || (attr.size() == 0)) {
			attrs = ictx.getAttributes(hostName, new String[] { "A" });
			attr = attrs.get("A");
			if (attr == null)
				throw new NamingException("No match for name '" + hostName
						+ "'");
		}

		// Huzzah! we have machines to try. Return them as an array list
		// NOTE: We SHOULD take the preference into account to be absolutely
		// correct. This is left as an exercise for anyone who cares.
		ArrayList res = new ArrayList();
		NamingEnumeration en = attr.getAll();

		while (en.hasMore()) {
			String x = (String) en.next();
			String f[] = x.split(" ");
			if (f[1].endsWith("."))
				f[1] = f[1].substring(0, (f[1].length() - 1));
			res.add(f[1]);
		}
		return res;
	}

	private int processQueue() throws FileNotFoundException {
		int intRtnMail = 0;
		boolean exists = (new File(this.spoolFilePath)).exists();
	    if (!exists) {
	        return 0;
	    }
		BufferedReader bufFileNew = new BufferedReader(new FileReader(
				this.spoolFilePath));
		String strTempNew;
		ArrayList<MailMessage> altMailMessage = new ArrayList<MailMessage>();
		try {
			strTempNew = bufFileNew.readLine();
			while (strTempNew != null) {
				ArrayList<String> altMailContent = new ArrayList<String>();
				while ((strTempNew = bufFileNew.readLine()) != null) {
					if (strTempNew.startsWith("From "))
						break;
					altMailContent.add(strTempNew);
				}
				altMailContent.add(".");
				if (this.delivery(MailMessage.getMailFromText(altMailContent)))
					intRtnMail++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return intRtnMail;
	}
}

class MailinQueue {
	int retryTimes;
	String receiver;
	String remoteMX;
	MailMessage mail;

	public MailinQueue(String Receiver, String RemoteMX, MailMessage Message) {
		this.retryTimes = 0;
		this.receiver = Receiver;
		this.remoteMX = RemoteMX;
		this.mail = Message;
	}
}