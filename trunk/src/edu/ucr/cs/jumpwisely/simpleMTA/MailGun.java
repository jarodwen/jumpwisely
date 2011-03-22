/**
 *	SimpleMTA MailGun.java
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class MailGun {
	
	/**
	 * MailGun Mail Generator
	 * MailGun is a mail generator which can generate mails to specified users on the 
	 * server running SimpleMTA.
	 * @param intMailNum
	 * @param strMailTitle
	 * @param spoolFilePath
	 * @throws IOException
	 */
	public static void main(String argv[])throws IOException{
		if(argv.length != 3){
			System.out.println("Usage: MailGun Num_Mail MailTitle SpoolFilePath\n");
			return;
		}
		int intMailNum = Integer.valueOf(argv[0]); 
		String strMailTitle = argv[1]; 
		String spoolFilePath = argv [2]; 
		if(intMailNum <= 0) return;
		BufferedWriter out = new BufferedWriter(new FileWriter(spoolFilePath));
		System.out.println("Mail Bullets Loading... \n");
		for(int i = 0; i<intMailNum; i++){
			MailMessage message = new MailMessage(UUID.randomUUID().toString().substring(0, 6));
			message.Subject = strMailTitle + "_" + String.valueOf(i) + "_" +String.valueOf(System.currentTimeMillis());
			message.From = "alice@129.10.112.181";
			// Randomly generate recipients
			switch(i%4){
			case 1:
				message.To.add("alice@129.10.112.181");
				break;
			case 2:
				message.To.add("bob@129.10.112.181");
				break;
			case 3:
				message.To.add("dummyman@129.10.112.181");
			default:
				message.To.add("jarodwen@ccs.neu.edu");
			}
			message.Data.add("Test From MailGun!");
			message.Data.add("Yoho! I shot you!");
			message.Data.add(".");
			try {
				message.fillHeader();
				out.write(message.assembleMail());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(String.valueOf(i) + " mail loaded.\n");
		}
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Mail loaded in: "+ spoolFilePath +".\n");
	}
}
