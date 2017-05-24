import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * 
 * Client specifies a client in which to be connected
 * to the server. Runs off of a host machine and is
 * paired with a server at a running port number.
 * 
 * @author Derek J. Grove
 *
 */
public class Client implements Runnable {
	private Socket socket;
	private DataOutputStream dout;
	private DataInputStream din;
	//Gets the user directory and appends Downloads folder to path
	private String defDirectory = System.getProperty("user.home")
			+ "\\Downloads";

	/**
	 * Constructor to instantiate a Client
	 * @param host - the host machine
	 * @param port - the port number (should be same as server's)
	 */
	public Client(String host, int port) {
		try {
			socket = new Socket(host, port);
			System.out.println("connected to " + socket);
			din = new DataInputStream(socket.getInputStream());
			dout = new DataOutputStream(socket.getOutputStream());
			new Thread(this).start();
		} catch (IOException ie) {
			System.out.println("Server not found");
		}
	}

	/**
	 * Communication with the thread in which to set-up
	 * a new destination folder if the default does
	 * no suffice
	 */
	public void setupDestination() {
		Scanner in = new Scanner(System.in);
		try {
			System.out.println(din.readUTF());
			System.out.println("Change [y/n]");
			System.out.print("> ");
			String answer = in.nextLine();
			dout.writeUTF(answer);
			System.out.println(din.readUTF());
			System.out.print("> ");
			//Get the new destination
			if (answer.equals("y") || answer.equals("Y")) {
				answer = in.nextLine();
				File attemptDir = new File(answer);
				if(attemptDir.isDirectory()) {
					setDefaultDestination(answer);
				} else {
					System.out.println("Path not found, reverting " +
							"to default...");
				}
			//Use default
			} else {
				answer = in.nextLine();
			}
			dout.writeUTF(answer);
			System.out.println("*****INITIALIZATION COMPLETE*****");
		} catch (IOException e1) {
			System.out.println("Could not find folder");
		}
	}

	/**
	 * Override Runnable run()
	 * Creates a communication with the thread.
	 * Current implementation tests for ls and dl [fileName]
	 */
	public void run() {
		Scanner in = new Scanner(System.in);
		setupDestination();
		String token = " ";
		//while termination is false
		while (!token.equals("exit")) {
			System.out.print("> ");
			token = in.nextLine();
			try {
				dout.writeUTF(defDirectory);
				dout.writeUTF(token);
				System.out.println(din.readUTF());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set the default directory location for a given client
	 * @param destination - the destination to be set
	 */
	public void setDefaultDestination(String destination) {
		this.defDirectory = destination;
	}

	/**
	 * Simple Opening border
	 */
	public static void displayOpening() {
		String border = "*";
		for (int i = 0; i < 60; i++) {
			border += "*";
		}
		System.out.println(border);
		System.out.printf("*%45s%15s\n", "Welcome to the Client initiation",
				"*");
		System.out.println(border);
	}

	/**
	 * Main method to run the application
	 * @param args - none
	 */
	public static void main(String[] args) {

		displayOpening();
		System.out.println("Enter hostname");
		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String hostName = in.next();
		System.out.println("Enter Server port to connect");
		System.out.print("> ");
		int portNum = in.nextInt();
		new Client(hostName, portNum);

	}
}