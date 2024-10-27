import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 25565;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("[Client]: Try to log into the server...");

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            // weird auto-flush of PrintWriter class, sometimes has problems
            // PrintWriter outStream = new PrintWriter(socket.getOutputStream(), true);
            BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String username;
            while (true) {
                System.out.print("Enter your username: ");
                username = userInput.readLine();
                username = username.trim();
                if (username.isEmpty()) {
                    System.out.println("[Server]: Invalid username!");
                } else {
                    outStream.write(username);
                    outStream.newLine();
                    outStream.flush();
                    String serverResponse = inStream.readLine();
                    if (serverResponse.equals("Username already taken. Please choose another username.")) {
                        System.out.println(serverResponse);
                    } else {
                        System.out.println(serverResponse);
                        break;
                    }
                }
            }

            // Start a thread to listen from the server
            Thread listenThread = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = inStream.readLine()) != null) {
                        System.out.println();
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Connection closed: " + e.getMessage());
                }
            });
            listenThread.start();

            String message;
            while (true) {
                System.out.print("Type: ");
                message = userInput.readLine();
                if (message.isEmpty()) continue;
                if (message.equalsIgnoreCase("/exit")) {
                    break;
                }
                outStream.write(message);
                outStream.newLine();
                outStream.flush();
            }

        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}