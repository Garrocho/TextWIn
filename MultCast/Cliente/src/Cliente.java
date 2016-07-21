import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Scanner;


public class Cliente {

	public static void main(String[] args) {
		String op = "comecou";
		Scanner reader = new Scanner(System.in);
		System.out.print("Seu Nome: ");
		String nome = reader.next();
		new RecebeMensagens(nome).start();
		System.out.print("Mensagem: ");
		op = reader.next();
		while (!op.equalsIgnoreCase("SAIR")) {
			new EnviaMensagens(nome + ": " + op).start();
			System.out.print("Mensagem: ");
			op = reader.next();
		}
		System.out.print("\nFinalizando o Programa");
	}
}

class EnviaMensagens extends Thread {

	public String mensagem;
	
	public EnviaMensagens(String mensagem) {
		this.mensagem = mensagem;
	}

	public void run() {
		int cont = 0;

		try {
			InetAddress addr = InetAddress.getByName("224.0.0.3");
			DatagramSocket serverSocket = new DatagramSocket();

			DatagramPacket msgPacket = new DatagramPacket(mensagem.getBytes(),

					mensagem.getBytes().length, addr, 8888);

			serverSocket.send(msgPacket);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class RecebeMensagens extends Thread {
	
	public String nome;
	
	public RecebeMensagens(String nome) {
		this.nome = nome;
	}
	
	public void run() {

		try {
			InetAddress addr = InetAddress.getByName("224.0.0.3");
			byte[] buf = new byte[256];

			MulticastSocket clientSocket = new MulticastSocket(8888);
			clientSocket.joinGroup(addr);

			while (true) {
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				clientSocket.receive(msgPacket);

				String msg = new String(buf, 0, buf.length);
				String[] dados = msg.split(": ");
				if (!dados[0].equalsIgnoreCase(nome))
					System.out.println(msg);

				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}
