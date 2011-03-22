/**
 *	SimpleMTA rec2822Validater.java
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

/**
 * Based on the existing code of Les Hazlewood.
 * Original Code is Licensed under the Apache License, Version 2.0 (the "License");
 * @author jianwen
 *
 */

public class rfc2822Validater {
	/**
	 * This constant states that domain literals are allowed in the email address, e.g.:
	 *
	 * <p><tt>someone@[192.168.1.100]</tt> or <br/>
	 * <tt>john.doe@[23:33:A2:22:16:1F]</tt> or <br/>
	 * <tt>me@[my computer]</tt></p>
	 *
	 * <p>The RFC says these are valid email addresses, but most people don't like allowing them.
	 * If you don't want to allow them, and only want to allow valid domain names
	 * (<a href="http://www.ietf.org/rfc/rfc1035.txt">RFC 1035</a>, x.y.z.com, etc),
	 * change this constant to <tt>false</tt>.
	 *
	 * <p>Its default value is <tt>true</tt> to remain RFC 2822 compliant, but
	 * you should set it depending on what you need for your application.
	 */
	private static final boolean ALLOW_DOMAIN_LITERALS = true;

	/**
	 * This contstant states that quoted identifiers are allowed
	 * (using quotes and angle brackets around the raw address) are allowed, e.g.:
	 *
	 * <p><tt>"John Smith" &lt;john.smith@somewhere.com&gt;</tt>
	 *
	 * <p>The RFC says this is a valid mailbox.  If you don't want to
	 * allow this, because for example, you only want users to enter in
	 * a raw address (<tt>john.smith@somewhere.com</tt> - no quotes or angle
	 * brackets), then change this constant to <tt>false</tt>.
	 *
	 * <p>Its default value is <tt>true</tt> to remain RFC 2822 compliant, but
	 * you should set it depending on what you need for your application.
	 */
	private static final boolean ALLOW_QUOTED_IDENTIFIERS = true;
	
	private static final String CRLF = "\r\n";

	// RFC 2822 2.2.2 Structured Header Field Bodies
	private static final String wsp = "[ \\t]"; //space or tab
	private static final String fwsp = wsp + "*";

	//RFC 2822 3.2.1 Primitive tokens
	private static final String dquote = "\\\"";
	//ASCII Control characters excluding white space:
	private static final String noWsCtl = "\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F";
	//all ASCII characters except CR and LF:
	private static final String asciiText = "[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]";

	// RFC 2822 3.2.2 Quoted characters:
	//single backslash followed by a text char
	private static final String quotedPair = "(\\\\" + asciiText + ")";

	//RFC 2822 3.2.4 Atom:
	private static final String atext = "[a-zA-Z0-9\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~]";
	private static final String atom = fwsp + atext + "+" + fwsp;
	private static final String dotAtomText = atext + "+" + "(" + "\\." + atext
			+ "+)*";
	private static final String dotAtom = fwsp + "(" + dotAtomText + ")" + fwsp;

	//RFC 2822 3.2.5 Quoted strings:
	//noWsCtl and the rest of ASCII except the doublequote and backslash characters:
	private static final String qtext = "[" + noWsCtl
			+ "\\x21\\x23-\\x5B\\x5D-\\x7E]";
	private static final String qcontent = "(" + qtext + "|" + quotedPair + ")";
	private static final String quotedString = dquote + "(" + fwsp + qcontent
			+ ")*" + fwsp + dquote;

	//RFC 2822 3.2.6 Miscellaneous tokens
	private static final String word = "((" + atom + ")|(" + quotedString
			+ "))";
	private static final String phrase = word + "+"; //one or more words.

	//RFC 1035 tokens for domain names:
	private static final String letter = "[a-zA-Z]";
	private static final String letDig = "[a-zA-Z0-9]";
	private static final String letDigHyp = "[a-zA-Z0-9-]";
	private static final String rfcLabel = letDig + "(" + letDigHyp + "{0,61}"
			+ letDig + ")?";
	private static final String rfc1035DomainName = rfcLabel + "(\\."
			+ rfcLabel + ")*\\." + letter + "{2,6}";

	//RFC 2822 3.4 Address specification
	//domain text - non white space controls and the rest of ASCII chars not including [, ], or \:
	private static final String dtext = "[" + noWsCtl
			+ "\\x21-\\x5A\\x5E-\\x7E]";
	private static final String dcontent = dtext + "|" + quotedPair;
	private static final String domainLiteral = "\\[" + "(" + fwsp + dcontent
			+ "+)*" + fwsp + "\\]";
	private static final String rfc2822Domain = "(" + dotAtom + "|"
			+ domainLiteral + ")";

	private static final String domain = ALLOW_DOMAIN_LITERALS ? rfc2822Domain
			: rfc1035DomainName;

	private static final String localPart = "((" + dotAtom + ")|("
			+ quotedString + "))";
	private static final String addrSpec = localPart + "@" + domain;
	private static final String angleAddr = "<" + addrSpec + ">";
	private static final String nameAddr = "(" + phrase + ")?" + fwsp
			+ angleAddr;
	private static final String mailbox = nameAddr + "|" + addrSpec;
	
	/* .+@[^,]+(,.+@.+)* */
	private static final String mailbox_list = mailbox + ",(" + mailbox +")*";
	private static final String display_name = "\\w+";
	private static final String group = display_name + ":" + mailbox_list +";" + fwsp;
	private static final String address = mailbox + "|" + group;
	private static final String address_list = address + ",(" + address + ")*";
	
	/* RFC 2822 Date specification */
	private static final String zone = "[\\+|\\-][0-9]{4}";
	private static final String second = "[0-9]{2}";
	private static final String minute = "[0-9]{2}";
	private static final String hour = "[0-9]{2}";
	private static final String time_of_day = hour + ":" + minute + "(:"+second+")";
	private static final String time = time_of_day + fwsp + zone;
	
	/* \s*[0-9]{1,2} */
	private static final String day = fwsp + "[0-9]{1,2}";
	private static final String month_name = "[Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec]";
	private static final String month = fwsp + month_name;
	private static final String year = "[0-9]{4}";
	private static final String date = day + month + year;
	private static final String day_name = "[Mon|Tue|Wed|Thu|Fri|Sat|Sun]";
	private static final String day_of_week = fwsp + day_name;
	private static final String date_time = "("+day_of_week+",)"+date+fwsp+time+fwsp;
	
	/* RFC 2822 3.6 header specification */
	private static final String orig_date = "Date:"+date_time+CRLF;
	
	private static final String from = "From:"+mailbox_list+CRLF;
	private static final String sender = "Sender:"+mailbox+CRLF;
	private static final String reply_to = "Reply-to:"+address_list+CRLF;
	
	private static final String to = "To:"+address_list+CRLF;
	private static final String cc = "Cc:"+address_list+CRLF;
	private static final String bcc = "Bcc:"+address_list+CRLF;
}
