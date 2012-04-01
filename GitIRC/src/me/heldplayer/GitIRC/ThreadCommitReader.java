package me.heldplayer.GitIRC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ThreadCommitReader extends Thread {
	private ConsoleMessageReciever reciever;
	private byte pos = 0;
	private static boolean launched = false;
	private String chan;

	public ThreadCommitReader(ConsoleMessageReciever parent, String channel) {
		super("Commit reader");
		reciever = parent;
		chan = channel;
		
		launched = !launched;
	}

	public void run() {
		if(!launched){
			launched = true;
			return;
		}
		
		try {
			while (reciever.isRunning()) {
				pos++;
				
				if(pos >= 30){
					pos = 0;
					
					try {
						URL oracle = new URL("http://dsiwars.co.cc/Git/retrieve.php");
				        BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

				        String inputLine;
				        while ((inputLine = in.readLine()) != null) {
				        	if(inputLine.equalsIgnoreCase("0")){
				        		break;
				        	}
				        	reciever.inputBuffer.add("/say " + chan + " " + inputLine);
				        }
				        in.close();
					} catch (Exception ex) {
					}
				}
				
				Thread.sleep(1000L);
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
