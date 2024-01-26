package eunoia.asia.pos.posconnector.service;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
	final static String SALE = "0";
	final static String OFFLINE_SALE = "2";
	final static String VOID = "4";
	final static String REFUND = "5";

	final static String MSG_VERSION = "V18";
	final static String SENDER_INDICATOR = "P";
	final static String STX = "02";
	final static String ETX = "03";

	private final TcpSocketService tcpSocketService;

	public PaymentService(TcpSocketService tcpSocketService) {
		this.tcpSocketService = tcpSocketService;
	}

	/**
	 *
	 * @param amount For example:  000000001002 represents $10.02
	 * @param trackingId   ECR payment references, Padded with SPACES, left aligned, if less than 20 digits
	 */
	public Map<String, String> salePayment(String host, int port, BigDecimal amount, String trackingId) {
		String mainMsg = SENDER_INDICATOR + SALE + MSG_VERSION + convertTrackingId(trackingId) + convertAmount(amount) + "000000";
		String lod = "00 " + String.valueOf(mainMsg.length()) + " ";

		StringBuilder finalMsg = new StringBuilder();
		finalMsg.append(STX + " ");
		finalMsg.append(lod);
		for(String s: mainMsg.split("")) {
			finalMsg.append(String.format("%x ", new BigInteger(1, s.getBytes(StandardCharsets.UTF_8))));
		}
		finalMsg.append(ETX + " ");
		String LRC = calculateLrc(finalMsg.toString().substring(3));
		finalMsg.append(LRC);

		byte[] terminalResponse = tcpSocketService.sendMessage(host, port, finalMsg.toString().replace(" ", ""));
		String resStr = new String(terminalResponse, StandardCharsets.UTF_8);
		// parse string response to map
		Map<String, String> resMap = new HashMap<>();
		resMap.put("sender_indicator", resStr.substring(0,1).replaceAll("\\p{C}", ""));
		resMap.put("type", parseType(resStr.substring(1,2).replaceAll("\\p{C}", "")));
		resMap.put("message_version", resStr.substring(2,5).replaceAll("\\p{C}", ""));
		resMap.put("tracking_id", resStr.substring(5,25).replaceAll("\\p{C}", ""));
		resMap.put("amount", String.valueOf(
				new BigDecimal(resStr.substring(25,37).replaceAll("\\p{C}", ""))
						.divide(BigDecimal.valueOf(100))
						.setScale(2)
		));
		resMap.put("resp_code", resStr.substring(37,39).replaceAll("\\p{C}", ""));
		resMap.put("merchant_id", resStr.substring(39,54).replaceAll("\\p{C}", ""));
		resMap.put("terminal_id", resStr.substring(54,62).replaceAll("\\p{C}", ""));
		resMap.put("card_number", resStr.substring(62,82).replaceAll("\\p{C}", ""));
		resMap.put("card_expr", resStr.substring(82,86).replaceAll("\\p{C}", ""));
		resMap.put("approval_code", resStr.substring(86,92).replaceAll("\\p{C}", ""));
		resMap.put("card_label", resStr.substring(92,102).replaceAll("\\p{C}", ""));
		resMap.put("rrn", resStr.substring(102,114).replaceAll("\\p{C}", ""));
		resMap.put("paid_time", resStr.substring(114,126).replaceAll("\\p{C}", ""));
		resMap.put("batch_number", resStr.substring(126,132).replaceAll("\\p{C}", ""));
		resMap.put("card_type", resStr.substring(132,134).replaceAll("\\p{C}", ""));
		resMap.put("card_holder", resStr.substring(134,161).replaceAll("\\p{C}", ""));
		resMap.put("trace_number", resStr.substring(164,170).replaceAll("\\p{C}", ""));
		resMap.put("redemption_amount", String.valueOf(
				new BigDecimal(resStr.substring(170,182).replaceAll("\\p{C}", ""))
						.divide(BigDecimal.valueOf(100))
						.setScale(2)
		));
		resMap.put("net_amount", String.valueOf(
				new BigDecimal(resStr.substring(182,194).replaceAll("\\p{C}", ""))
						.divide(BigDecimal.valueOf(100))
						.setScale(2)
		));


		return resMap;
	}

	private String parseType(String typeNo) {
		switch (typeNo) {
			case "0":
				return "SALE";
			case "2":
				return "OFFLINE_SALE";
			case "4":
				return "VOID";
			case "5":
				return "REFUND";
			default:
				return "UNKNOWN";
		}
	}

	private String convertAmount(BigDecimal amount) {
		final int max_length = 12;
		BigDecimal priceHundred = amount.multiply(BigDecimal.valueOf(100));
		String amountStr = String.valueOf(priceHundred);
		int idx = amountStr.indexOf('.');
		amountStr = amountStr.substring(0, idx);

		StringBuilder finalStrAmount = new StringBuilder();
		int prefix = max_length - amountStr.length();
		for (int i = 0; prefix > i; i++) {
			finalStrAmount.append('0');
		}
		finalStrAmount.append(amountStr);

		return finalStrAmount.toString();
	}

	private String convertTrackingId(String trackingId) {
		final int max_length = 20;
		int suffix = max_length - trackingId.length();

		StringBuilder finalTrackingId = new StringBuilder();
		finalTrackingId.append(trackingId);
		for (int i = 0; suffix > i; i++) {
			finalTrackingId.append(" ");
		}

		return finalTrackingId.toString();
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}
	private String calculateLrc(String msg) {
		byte lrc = 0x00;
		byte [] bytes = hexStringToByteArray(msg.replace(" ", ""));

		for (byte b : bytes) {
			lrc ^= b;
		}

		// lrc msut be between 48 to 95
//		lrc %= 48;
//		lrc += 48;

		System.out.println("LRC: " + (char) lrc);

		return String.format("%02X", lrc);
	}
}
