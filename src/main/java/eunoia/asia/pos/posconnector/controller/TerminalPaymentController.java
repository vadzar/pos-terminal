package eunoia.asia.pos.posconnector.controller;

import eunoia.asia.pos.posconnector.service.TcpSocketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(value = "api/v1/terminal-payment")
public class TerminalPaymentController {
	private final TcpSocketService tcpSocketService;

	public TerminalPaymentController(TcpSocketService socketService) {
		this.tcpSocketService = socketService;
	}

	@GetMapping("/")
	public ResponseEntity<String> hello() {
		return ResponseEntity.ok("Hello world");
	}

	@GetMapping("/run")
	public ResponseEntity<String> run() {
		final String STX = "02";
		String myStr = "02 00 43 50 30 56 31 38 31 32 33 34 35 36 37 38" +
				"39 30 41 42 43 44 45 46 47 20 20 20 30 30 30 30"+
				"30 30 30 31 30 30 30 32 30 30 30 30 30 30 03 1D";
		System.out.println(myStr.replace(" ", ""));
		String testStr = "P0V181234567890ABCDEFG   000000010002000000";
		for(String s: testStr.split("")) {
			System.out.print(String.format("%x", new BigInteger(1, s.getBytes(StandardCharsets.UTF_8))) + " ");
		}
//		System.out.println(String.format("%x", new BigInteger(1, "P0V181234567890ABCDEFG   000000010002000000".getBytes(StandardCharsets.UTF_8))));
//		System.out.println(String.format("%x", new BigInteger(1, "P".getBytes(StandardCharsets.UTF_8))));
//		System.out.println(String.format("%03x", new BigInteger(1, "V18".getBytes(StandardCharsets.UTF_8))));
//		System.out.println(String.format("%x", new BigInteger(1, "1234567890ABCDEFG".getBytes(StandardCharsets.UTF_8))));
//		System.out.println(String.format("%12x", new BigInteger(1, "000000010002".getBytes(StandardCharsets.UTF_8))));

		tcpSocketService.sendMessage("192.168.0.105", 8888, myStr.replace(" ", ""));
		return ResponseEntity.ok("finished !!");
	}
}
