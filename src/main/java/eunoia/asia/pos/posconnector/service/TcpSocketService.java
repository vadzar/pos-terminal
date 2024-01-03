package eunoia.asia.pos.posconnector.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Service
public class TcpSocketService {
	public void sendMessage(String serviceHost, int servicePort, String message) {
		try (Socket socket = new Socket(serviceHost, servicePort);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//			out.println(message);
			out.write(message.toCharArray());

			String response = in.readLine();
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
