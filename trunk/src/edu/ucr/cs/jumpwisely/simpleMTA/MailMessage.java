/**
 *	SimpleMTA MailMessage.java
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MailMessage {

	String localHost;

	String Message_Id;
	String From;
	ArrayList<String> To;
	String Content_Type;
	String Content_Transfer_Encoding;
	String X_smtp_Server;
	String Mime_Version;
	String Subject;
	String Date;
	String Cc;
	String BCc;
	ArrayList<String> Data;

	public MailMessage(String messageID) {
		Message_Id = messageID;
		Content_Type = "text/plain\ncharset=US-ASCII\nformat=flowed\ndelsp=yes";
		Content_Transfer_Encoding = "7 bit";
		Mime_Version = "1.0 (SimpleMTA)";
		To = new ArrayList<String>();
		Data = new ArrayList<String>();
		
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			localHost = addr.getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			localHost = "";
			e.printStackTrace();
		}
	}
	
	public MailMessage(){
		Message_Id = "";
		Content_Type = "text/plain\ncharset=US-ASCII\nformat=flowed\ndelsp=yes";
		Content_Transfer_Encoding = "7 bit";
		Mime_Version = "1.0 (SimpleMTA)";
		To = new ArrayList<String>();
		Data = new ArrayList<String>();
		
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			localHost = addr.getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			localHost = "";
			e.printStackTrace();
		}
	}

	public void setFrom(String strFrom) {
		From = strFrom;
	}

	public void setTo(ArrayList<String> strTo) {
		To = strTo;
	}

	public void setSubject(String strSubject) {
		Subject = strSubject;
	}

	public void setDate(String strDate) {
		Date = strDate;
	}

	public void setCc(String strCc) {
		Cc = strCc;
	}

	public void setBCc(String strBCc) {
		BCc = strBCc;
	}

	public void setData(ArrayList<String> strData) {
		Data = strData;
	}

	public String getMessage() {
		return Message_Id + "\n" + From + "\n" + To + "\n" + Content_Type
				+ "\n" + Content_Transfer_Encoding + "\n" + X_smtp_Server
				+ "\n" + Mime_Version + "\n" + Subject + "\n" + Date + "\n"
				+ Cc + "\n" + BCc + "\n" + Data;
	}

	/* Fill in the header in the DATA part if it is missing or invalid */
	/* Only the From, To, Date, Message-ID and Subject are necessary. */
	public void fillHeader() {

		/* get the domain name of the host */
		String localDomain = localHost.substring(localHost.indexOf(".") + 1);

		boolean isFrom = false, isTo = false, isSubject = false, isDate = false, isMessageID = false;

		/* Check the DATA for the header */
		for (int i = 0; i < Data.size(); i++) {
			if (Data.get(i).trim().toLowerCase().startsWith("from:")) {
				isFrom = true;
			}
			if (Data.get(i).trim().toLowerCase().startsWith("to:")) {
				isTo = true;
			}
			if (Data.get(i).trim().toLowerCase().startsWith("subject:")) {
				isSubject = true;
			}
			if (Data.get(i).trim().toLowerCase().startsWith("date:")) {
				isDate = true;
			}
			if (Data.get(i).trim().toLowerCase().startsWith("message-id:")) {
				isMessageID = true;
			}
		}

		/* Insert missing header */
		if (!isMessageID) {
			Data.add(0, "Message-ID: <" + Message_Id + "@" + localDomain + ">");
		}
		if (!isSubject) {
			if(Subject == null)
				Data.add(0, "Subject: (No Subject)");
			else
				Data.add(0, "Subject:"+Subject);
		}
		if (!isDate) {
			Date dDate = new Date();
			DateFormat dFormat = new SimpleDateFormat(
					"E, dd MMM yyyy HH:mm:ss Z");
			Data.add(0, "Date: " + dFormat.format(dDate));
		}
		if (!isTo) {
			for (int i = 0; i < To.size(); i++) {
				Data.add(0, "To: " + To.get(i) + ",");
			}
			if(Data.get(0).endsWith(",")) Data.set(0, Data.get(0).substring(0, Data.get(0).length() - 1));
		}
		if (!isFrom) {
			Data.add(0, "From: " + From);
		}
	}

	/* Assemble the mail for local delivery */
	public String assembleMail() {

		Date dDate = new Date();
		DateFormat dFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");

		String strDate = dFormat.format(dDate);
		String mailMessage = "From " + From + " " + strDate + "\n";
		/* Assemble the content of the message, and remove the final . */
		for (int i = 0; i < Data.size() - 1; i++) {
			mailMessage += Data.get(i) + "\r\n";
		}
		mailMessage += "\n";
		return mailMessage;
	}
	
	public static MailMessage getMailFromText(ArrayList<String> altStr){
		
		MailMessage mailMessage = new MailMessage();
		for(int i = 0; i < altStr.size(); i++){
			String strTemp = altStr.get(i);
			if(strTemp.trim().startsWith("From") && !(strTemp.trim().startsWith("From:")))
				continue;
			if(strTemp.trim().startsWith("From:")){
				mailMessage.From = strTemp.substring(strTemp.indexOf(":")+1);
			}else if(strTemp.trim().startsWith("To:")){
				mailMessage.To.add(strTemp.substring(strTemp.indexOf(":")+1));
			}else if(strTemp.trim().startsWith("Message-ID:")){
				mailMessage.Message_Id = strTemp.substring(strTemp.indexOf('<')+1, strTemp.indexOf('<')+7);
			}
			mailMessage.Data.add(strTemp);
		}
		
		return mailMessage;
	}
}
