package com.impzx.java.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlainEchoServer {

	private static final Log LOG = LogFactory.getLog(PlainEchoServer.class);

	public static void main(String[] args) throws IOException {
		PlainEchoServer plainEchoServer = new PlainEchoServer();
		plainEchoServer.server(0);
	}
	
	public void server(int port) throws IOException {
		final ServerSocket socket = new ServerSocket(port);
		try {
			while (true) {
				final Socket clientSocket = socket.accept();
				LOG.info("Accepted connection from " + clientSocket);
				new Thread() {
					public void run() {
						BufferedReader br = null;
						PrintWriter writer = null;
						try {
							br = new BufferedReader(new InputStreamReader(
									clientSocket.getInputStream()));
							writer = new PrintWriter(
									clientSocket.getOutputStream(), true);
							while (true) {
								writer.println(br.readLine());
								writer.flush();
							}
						} catch (Exception e) {
							LOG.error(e.getMessage(), e);
						} finally {
							if (br != null) {
								try {
									br.close();
								} catch (IOException e) {
									LOG.error(e.getMessage(), e);
								}
							}
							if (writer != null) {
								writer.close();
							}
						}
					};
				}.start();
			}
		} catch (Exception e) {
		}
	}
}
