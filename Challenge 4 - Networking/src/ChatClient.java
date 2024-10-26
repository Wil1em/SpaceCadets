import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 25565;

        // modify the port using arguments
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        //Socket
        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("[Client]: Connected to the server");

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter outStream = new PrintWriter(socket.getOutputStream(), true);
//            BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String username, message;
            System.out.print("Enter your username: ");
            username = userInput.readLine();
            if (username.isEmpty()){
                System.out.println("[Client]: Username is null!");
            }
            while (true) {
                System.out.print("[Type]: ");
                message = userInput.readLine();
                if (message.equalsIgnoreCase("/exit")) {
                    break;
                }
                outStream.println("[" + username + "]: " + message);
//                String serverResponse = inStream.readLine();
//                System.out.println("[Server]: " + serverResponse);
            }

            userInput.close();
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println("Server not found:" + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}