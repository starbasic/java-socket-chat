package chatserver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;


public class ChatServer {

    private static final int PORT = 8107;
    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket server = new ServerSocket(PORT);
        try {
            //Цикл прослуховування порту з наданням підключення
            while (true) {
                Socket sock = server.accept();
                System.out.println( sock.getInetAddress().getHostName()
                        + " connected");
                new SocketThread(sock).start();
            }
        } finally {
            server.close();
        }
    }
    //клас сокет-потоку для з'єднання з клієнтом
    private static class SocketThread extends Thread {
        private String name;
        private final Socket socket;
        private BufferedReader inStream;
        private PrintWriter outStream;

    public SocketThread(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            try {
                inStream = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                outStream = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    outStream.println("SUBMITNAME");
                    name = inStream.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }
               outStream.println("NAMEACCEPTED");
                writers.add(outStream);
                while (true) {
                    String input = inStream.readLine();
                    if (input == null) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    names.remove(name);
                }
                if (outStream != null) {
                    writers.remove(outStream);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}

