/**
 *	SimpleMTA SimpleMTAServer.java
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

import java.io.* ;
import java.net.* ;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class SimpleMTAServer {
	public static void main(String argv[]) throws Exception {

		/* Set the port number. */
		int port = 2225;

		/* Establish the listen socket. */
        ServerSocket mailSocket = null;
        
        try{
        	mailSocket = new ServerSocket(port);
        }catch(IOException e){
        	System.out.println(e.toString());
        }
        
        Executor threadPool = Executors.newFixedThreadPool(5);

        System.out.println("Starting server at 2225...\n");
		/* Process SMTP client requests in an infinite loop. */
		while (true) {
			/* Listen for a TCP connection request. */
			Socket SMTPSocket = mailSocket.accept();

			/* Construct an object to process the SMTP request. */
			SMTPConnection connection = new SMTPConnection(SMTPSocket);

			/* Start the process */
			threadPool.execute(connection);
		}
	}
}