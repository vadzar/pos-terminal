package eunoia.asia.pos.posconnector.service;

public class CommunicationTPE {

	private int montant;
	private int caisse = 1 ;
	private int type = 0;

	private static String port;

	private static int bauRate = 9600;
	private static int dataBits = 7;
	private static int stopBits = 1;
	private static int parity = 0;
	private int current;

	private String ENQ = "5",
			ACK = "6",
			NAK = "15",
			STX = "2",
			ETX = "3",
			EOT = "4";

	public static CommunicationTPE comm;
	private SerialPort serialPort;

	public static void initInstance(String port) {
		comm = new CommunicationTPE();
		comm.openCommunication(port);
	}

	public void openCommunication(String port) {
		CommunicationTPE.port = port;

		//serialPort.writeBytes(SerialPort.);//Write data to port
		if (serialPort != null && serialPort.isOpened()) {
			try {
				serialPort.closePort();
			} catch (SerialPortException ex) {
				Logger.getLogger(CommunicationTPE.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		serialPort = new SerialPort(port.trim());
		try {
			serialPort.openPort();//Open port
			if (!serialPort.isOpened()) {
				JOptionPane.showMessageDialog(null,"Terminal non détecté");
				return;
			}
			// bauRate : data transfer rate, dataBits : number of data bits, stopBits : number of stop bits, parity : parity
			serialPort.setParams(bauRate, dataBits, stopBits, parity);//Set params

			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR + SerialPort.MASK_ERR + SerialPort.MASK_BREAK + SerialPort.MASK_RING + SerialPort.MASK_RLSD + SerialPort.MASK_RXFLAG + SerialPort.MASK_TXEMPTY;//Prepare mask
			serialPort.setEventsMask(mask);//Set mask
			serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
		} catch (SerialPortException ex) {
			Logger.getLogger(CommunicationTPE.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void stopCommunication() {
		try {
			serialPort.closePort();
		} catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}

	public static CommunicationTPE getComm() {
		return comm;
	}


	public void initData(float montant) {
		this.montant = Math.round(montant * 100);
	}

	public void sendData() {
		try {
			if (serialPort == null || !serialPort.isOpened()) {
				openCommunication(Session.PORT_COM);
			}

			if (serialPort.isOpened()) {
				current = 0;
				boolean result = serialPort.writeBytes(ENQ.getBytes());//Write data to port
				if (!result) {
					JOptionPane.showMessageDialog(null,"Envoie de données incomplet");
				}
			} else {
				JOptionPane.showMessageDialog(null,"Terminal non détecté");
			}
		} catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}

	private String sendCommandProtocolE() {
		StringBuilder command = new StringBuilder();

		// Numéro de caisse
		String caisse = this.caisse + "";
		while (caisse.length() < 2) caisse = "0" + caisse;
		command.append(caisse);

		String montant = this.montant + "";
		while (montant.length() < 8) montant = "0" + montant;
		command.append(montant);

		// Mode de transaction, 1 = carte bancaire
		command.append("1");

		// Type de transaction, 0 = achat, 1 = remboursement, 2 = annulation, 4 = pré-autorisation
		command.append(type);

		// Code numérique pour la devise
		command.append("978");

		// Données privées
		String data = "";
		while (data.length() < 10) data = " " + data;
		command.append(data);

		return command.toString();
	}

	public class SerialPortReader implements SerialPortEventListener {

		public void serialEvent(SerialPortEvent event) {
			try {
				//byte buffer[] = serialPort.readBytes(10);
				//String value = new String(buffer, "ASCII");
				String value = serialPort.readString();

				if (value != null) {
					if (value.trim().equals(ACK) && current == 0) {
						serialPort.writeBytes(sendCommandProtocolE().getBytes());//Write data to port
						current ++ ;
					} else if (value.trim().equals(ACK) && current == 1) {
						serialPort.writeBytes(EOT.getBytes());//Write data to port
					}
				} else {

				}
			} catch (SerialPortException ex) {
				Logger.getLogger(CommunicationTPE.class.getName()).log(Level.SEVERE, null, ex);
				//           } catch (UnsupportedEncodingException ex) {
				//               Logger.getLogger(CommunicationTPE.class.getName()).log(Level.SEVERE, null, ex);
			} catch (Exception ex) {
				Logger.getLogger(CommunicationTPE.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

}
