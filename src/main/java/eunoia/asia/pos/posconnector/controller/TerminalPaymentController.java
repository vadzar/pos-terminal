package eunoia.asia.pos.posconnector.controller;

import eunoia.asia.pos.posconnector.model.PaymentRequest;
import eunoia.asia.pos.posconnector.service.PaymentService;
import eunoia.asia.pos.posconnector.service.TcpSocketService;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/terminal-payment")
public class TerminalPaymentController {
	private final TcpSocketService tcpSocketService;
	private final PaymentService paymentService;

	public TerminalPaymentController(TcpSocketService socketService, PaymentService paymentService) {
		this.tcpSocketService = socketService;
		this.paymentService = paymentService;
	}

	@GetMapping("/")
	public ResponseEntity<String> hello() {
		return ResponseEntity.ok("Hello world");
	}

	@PostMapping(value = "/sale", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> sale(@RequestBody PaymentRequest reqBody) {
		Map<String, String> result = paymentService.salePayment(reqBody.getIpAddress(), reqBody.getPort(), reqBody.getAmount(), reqBody.getTrackingId());

		return ResponseEntity.ok(result);
	}

	@PostMapping(value = "/void", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> voidPay(@RequestBody PaymentRequest reqBody) {
		Map<String, String> result = paymentService.voidPayment(reqBody.getIpAddress(), reqBody.getPort(), reqBody.getAmount(), reqBody.getTrackingId(), reqBody.getTraceNumber());

		return ResponseEntity.ok(result);
	}
}
