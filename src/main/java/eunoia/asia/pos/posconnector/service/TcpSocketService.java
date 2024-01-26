package eunoia.asia.pos.posconnector.service;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;

@Service
public class TcpSocketService {
	public byte[] sendMessage(String serviceHost, int servicePort, String message) {
		byte[] messageByte = new byte[10];
		try (Socket socket = new Socket(serviceHost, servicePort);
		     DataOutputStream  out = new DataOutputStream(socket.getOutputStream());
			 DataInputStream in = new DataInputStream(socket.getInputStream());
		) {
//			out.println(message);
			out.writeInt(message.length());
			out.write(Hex.decodeHex(message.toCharArray()));

			byte[] prefixBytes = new byte[4];
			in.readFully(prefixBytes);
			StringBuilder prefixStr = new StringBuilder();
			for (int i=0; i < 4; i++) {
				if(i == 2 || i == 3) {
					prefixStr.append(String.format("%02X", prefixBytes[i]));
				}
			}
			int length = Integer.parseInt(prefixStr.toString());
			messageByte = new byte[length];
			in.readFully(messageByte);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DecoderException e) {
			throw new RuntimeException(e);
		}
		return messageByte;
	}
}
