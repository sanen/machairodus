package org.machairodus.topology;

import org.nanoframework.server.JettyCustomServer;

public class Startup {

	public static void main(String[] args) {
		final JettyCustomServer server = new JettyCustomServer("/context.properties");
		new Thread(new Runnable() {
			@Override
			public void run() {
				server.startServer();
			}
		}).start();
	}

}
