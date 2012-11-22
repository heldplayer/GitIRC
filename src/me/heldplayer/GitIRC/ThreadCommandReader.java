package me.heldplayer.GitIRC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ThreadCommandReader extends Thread {
	private ConsoleMessageReciever reciever;

	public ThreadCommandReader(ConsoleMessageReciever parent) {
		super("Console command reader");
		reciever = parent;
	}

	public void run() {
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in));
		String s = null;

		try {
			while (reciever.isRunning() && (s = bufferedreader.readLine()) != null) {
				reciever.inputBuffer.put(reciever.index++, s);
			}
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
	}
}
