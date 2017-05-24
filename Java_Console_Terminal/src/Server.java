import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import javax.imageio.ImageIO;

/**
 * 
 * Server specifies a host and port in which
 * to have clients connect to.
 * 
 * @author Derek J. Grove
 *
 */
public class Server {
	private ServerSocket ss;
	private Hashtable<Socket, DataOutputStream> outputStreams = 
			new Hashtable<Socket, DataOutputStream>();
	private File[] files;
	private String defDirectory = System.getProperty("user.home")
			+ "\\Downloads";
	private String storingDirectory = "Resources/";

	/**
	 * Constructor to instantiate a Server
	 * @param port - the port number to open
	 * @throws IOException - Input/Output exception thrown
	 */
	public Server(int port) throws IOException {
		File file = new File(storingDirectory);
		System.out.println("Default Directory: " + file.toPath());
		System.out.println("Change [y/n]");
		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String answer = in.next();
		if (answer.equals("y") || answer.equals("Y")) {
			System.out.println("New Directory location:");
			System.out.print("> ");
			String nStoringDirectory = in.next() + "/";
			File targetDir = new File(nStoringDirectory);
			if(targetDir.isDirectory()) {
				storingDirectory = nStoringDirectory ;
			} else {
				System.out.println("Directory not found, reverting" +
						" to default.");
			}
		}
		files = new File(storingDirectory).listFiles();
		listen(port);
	}

	/**
	 * Simple header
	 */
	public static void displayOpening() {
		String border = "*";
		for (int i = 0; i < 60; i++) {
			border += "*";
		}
		System.out.println(border);
		System.out.printf("*%45s%15s\n", "Welcome to the Server initiation",
				"*");
		System.out.println(border);
	}

	/**
	 * Set the Default destination directory based upon
	 * the client executing the command.
	 * @param destination - destination folder the download
	 * will go to.
	 */
	public void setDefDirectory(String destination) {
		this.defDirectory = destination;
	}

	/**
	 * Get the default destination directory currently being
	 * pointed to
	 * @return the destination directory
	 */
	public String getDefaultDestination() {
		return defDirectory;
	}

	/**
	 * Display the files that are in the
	 * files array
	 * @return - the resulting strin of names
	 */
	public String displayFiles() {
		String fileNames = "";
		for (File file : files) {
			if(file.isFile()) {
				fileNames += "\n" + file.getName();
			}
		}
		return fileNames;
	}

	/**
	 * Read in a file from the server import directory
	 * @param fileName - the name of the file
	 * @param destination - destination of the copied file
	 * @param typeOfFile - determines the type of file
	 * so that the right measures are taken when reading
	 * @throws IOException - Input/Output exception thrown
	 */
	public void readFile(Path fileName, File destination, 
			String typeOfFile) throws IOException {
		//Text processing
		if(typeOfFile.equals("txt")) {
			String[] fileBuilder = new String[2000];
			Charset charset = Charset.forName("US-ASCII");
			try (BufferedReader reader = Files.newBufferedReader(fileName, charset)) {
				String line = null;
				int i = 0;
				while ((line = reader.readLine()) != null) {
					fileBuilder[i++] = line;
				}
				writeFile(destination.toPath(), fileBuilder, typeOfFile, null);
			} catch (Exception e) {
				
			}
		//Image processing
		} else if(typeOfFile.equals("image")) {
			BufferedImage image = null;
			File imgPath = new File(fileName.toString());
			image = ImageIO.read(imgPath);
			writeFile(destination.toPath(), null, typeOfFile, image);
		//Binary processing
		} else if(typeOfFile.equals("dat")) {
			try {
		       DataInputStream input = new DataInputStream(new FileInputStream(
		                fileName.toString()));
		       String[] fileBuilder = new String[2000];
		       int i = 0;
		       while (input.available() != 0) {
		        	String line = input.readLine();
		            fileBuilder[i++] = line;
		        }
		        input.close();
		        writeFile(destination.toPath(), fileBuilder, typeOfFile, null);
			} catch (Exception e) {
				
			}
		}
	}

	/**
	 * Write to the new file created
	 * @param fileName - the file to be written to
	 * @param content - the content in the file
	 * @param typeOfFile - determines the type of file
	 * so that the right measures are taken when writing
	 * @param img - the image that was read in
	 * @throws IOException - Input/Output exception thrown
	 */
	public void writeFile(Path fileName, String[] content, String typeOfFile, 
			BufferedImage img) throws IOException {
		//Process text
		if(typeOfFile.equals("txt")) {
			Charset charset = Charset.forName("US-ASCII");
			try (BufferedWriter writer = Files.newBufferedWriter(fileName, charset)) {
				for (int i = 0; i < content.length; i++) {
					writer.write(content[i], 0, content[i].length());
					writer.newLine();
				}
			} catch (Exception e) {

			}
		//Process image
		} else if(typeOfFile.equals("image")) {
			File imgPath = new File(fileName.toString());
			ImageIO.write(img, "jpg", imgPath);
		//Process binary
		} else if(typeOfFile.equals("dat")) {
			try {
				DataOutputStream output = new DataOutputStream(new FileOutputStream(
						fileName.toString()));
				for (int i = 0; i < content.length; i++) {
					output.writeUTF(content[i]);
				}
				output.close();
			} catch (Exception e) {
			
			}
		}
	}

	/**
	 * Create a temporary file in the specified destination directory
	 * Then read in the file from the server and write to the temp
	 * file
	 * @param target - target path of the temporary file
	 * @return if the file was downloaded or not
	 * @throws IOException - Input/Output exception thrown
	 */
	public String downloadFile(String target) throws IOException {
		for (File file : files) {
			String typeOfFile = "";
			if (file.getName().compareTo(target) == 0) {
				File dlFile = new File(defDirectory + "\\" + target);
				dlFile.createNewFile();
				//Process image
				if(target.contains(".jpg") || target.contains(".gif") ||
						target.contains(".png")) {
					typeOfFile = "image";
				//Process text
				} else if (target.contains(".txt") || target.contains(".TXT")){
					typeOfFile = "txt";
				//Process dat
				} else if (target.contains(".dat") || target.contains(".DAT")) {
					typeOfFile = "dat";
				}
				readFile(file.toPath(), dlFile, typeOfFile);
				return "Size: " + file.length() + "\n" +
					"Bytes: " + file.getTotalSpace() + "\n" +
					target + " Successfully Downloaded";
			}
		}
		return target + "not found";

	}

	/**
	 * Listen to new threads opened within the server
	 * @param port - the port of the new client
	 * @throws IOException - Input/Output exception thrown
	 */
	private void listen(int port) throws IOException {
		ss = new ServerSocket(port);
		System.out.println("Listening on " + ss);
		System.out.println("*****INITIALIZATION COMPLETE*****");
		while (true) {
			Socket s = ss.accept();
			System.out.println("Connection from " + s);
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			outputStreams.put(s, dout);
			new ServerThread(this, s);
		}
	}

	/**
	 * Build DataStream
	 * @return the line submitted
	 */
	Enumeration<DataOutputStream> getOutputStreams() {
		return outputStreams.elements();
	}

	/**
	 * Log the client portNo and statement they executed on the
	 * server log
	 * @param s - the socket listening to
	 * @param command - the command inputted from client
	 */
	void log(Socket s, String command) {
		System.out.println(s + " Executed " + command);
	}

	/**
	 * Remove the client, but keep server open to serve
	 * other clients
	 * @param s - the socket(client) to be removed
	 */
	void removeConnection(Socket s) {
		synchronized (outputStreams) {
			System.out.println("Removing connection to " + s);
			outputStreams.remove(s);
			try {
				s.close();
			} catch (IOException ie) {
				System.out.println("Error closing " + s);
				ie.printStackTrace();
			}
		}
	}
	
	/**
	 * Main method to execute the Server application
	 * @param args - none
	 * @throws IOException - Input/Output exception thrown
	 */
	public static void main(String[] args) throws IOException {
		// Get the port # from the command line
		displayOpening();
		System.out.println("Enter Port Number ");
		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		int port = in.nextInt();
		new Server(port);
	}
}