import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    public static void main(String[] args) {
        // default port
        int port = 25565;

        // modify the port using arguments
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        //ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server]: listening on port " + port);

            // wait for the client
            Socket socket = serverSocket.accept();
            System.out.println("[Server]: Client connected");

            //in: get user message into the server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
                // Optionally, send a response to the client
                // out.println("Message received: " + message);
            }

            in.close();
            socket.close();
        } catch (IOException ex) {
            System.err.println("Exception: " + ex.getMessage());
        }
    }
}