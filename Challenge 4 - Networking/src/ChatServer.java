import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 25565;
    private static final Set<ClientProcessor> clients = ConcurrentHashMap.newKeySet();
    private static final Set<String> usernames = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        int port = PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server]: listening on port " + port);

            // multi-threads for multi-users
            while (true) {
                Socket socket = serverSocket.accept();
                ClientProcessor clientProcessor = new ClientProcessor(socket);
                clients.add(clientProcessor);
                Thread thread = new Thread(clientProcessor);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    // print on the board
    static void board(String message, ClientProcessor originator) {
        for (ClientProcessor clientProcessor : clients) {
            if (clientProcessor != originator) {
                clientProcessor.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientProcessor client) {
        clients.remove(client);
        usernames.remove(client.getUsername());
    }

    static boolean isUsernameTaken(String username) {
        return usernames.contains(username);
    }

    static void addUsername(String username) {
        usernames.add(username);
    }
}

class ClientProcessor implements Runnable {
    private final Socket socket;
    private BufferedWriter out;
    private String username;

    public ClientProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            // in: get user message into the server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            this.out = out;
            while (true) {
                username = in.readLine();
                if (username == null || username.trim().isEmpty()) {
                    return;
                } else if (ChatServer.isUsernameTaken(username)) {
                    out.write("Username already taken. Please choose another username.");
                    out.newLine();
                    out.flush();
                } else {
                    ChatServer.addUsername(username);
                    out.write("Login successful!");
                    out.newLine();
                    out.flush();
                    break;
                }
            }

            // print login messages on server's display
            System.out.println("[Server]: " + username + " has joined");
            // print login messages to others
            ChatServer.board("[Server]: " + username + " has joined the chat", this);

            // print messages from users
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("[" + username + "]: " + message);
                ChatServer.board("[" + username + "]: " + message, this);
            }

        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } finally {
            ChatServer.removeClient(this);
            ChatServer.board("[Server]: " + username + " has left the chat", this);
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Failed to close socket: " + e.getMessage());
            }
        }
    }

    void sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }

    String getUsername() {
        return username;
    }
}