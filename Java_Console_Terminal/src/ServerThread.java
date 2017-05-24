import java.io.*;
import java.net.*;

/**
 * 
 * ServerThread creates a connection between a server and client.
 * This class allows concurrent connections.
 * 
 * @author Derek J. Grove
 *
 */
public class ServerThread extends Thread {
	private Server server;
	private Socket socket;

	/**
	 * Constructor to instantiate a ServerThread
	 * @param server - the server to be connected to
	 * @param socket - the socket that is connecting
	 */
	public ServerThread(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		start();
	}

	/**
	 * Creates an initial transaction sequence with the client
	 * determining if there is to be a change in the destination
	 * folder of where the downloads are to go.
	 */
	public void setupDestination() {
		try {
			DataInputStream din = new DataInputStream(socket.getInputStream());
			DataOutputStream dout = new DataOutputStream(
					socket.getOutputStream());

			dout.writeUTF("Default Destination: "
					+ server.getDefaultDestination());
			String answer = din.readUTF();
			//Set destination for this client
			if (answer.equals("y") || answer.equals("Y")) {
				dout.writeUTF("New Default Location:");
				server.setDefDirectory(din.readUTF());
			//Leave as default, hold true to communication scheme
			} else {
				dout.writeUTF("[Enter to Continue]");
				din.readUTF();
			}
		} catch (Exception e) {
			//Robust enough to not print out exception
		}
	}

	/**
	 * Overrides Runnable run()
	 * Creates a communication with the client.
	 * Current implementation tests for ls and dl [fileName]
	 */
	public void run() {
		try {
			DataInputStream din = new DataInputStream(socket.getInputStream());
			DataOutputStream dout = new DataOutputStream(
					socket.getOutputStream());
			setupDestination();
			//While there is a connection
			while (true) {
				String path = din.readUTF().toString();
				server.setDefDirectory(path);
				String message = din.readUTF().toString();
				String[] fields = message.split(" ");
				if (message.equals("ls")) {
					dout.writeUTF(server.displayFiles());
				} else if (fields[0].equals("dl")) {
					dout.writeUTF(server.downloadFile(message.substring(fields[0].length() + 1)));
				} else if(message.equals("exit")) {
					dout.writeUTF("Good-bye");
					break;
				} else {
					dout.writeUTF("Error: " + message + " not valid.");
				}
				server.log(socket, message);
			}

		} catch (IOException e) {
			//Robust enough to not print out exception
		} finally {
			//Remove client from server
			server.removeConnection(socket);
		}
	}
}