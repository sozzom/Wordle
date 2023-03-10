package server;

import common.UsersDB;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MultiThreadedServer {

    private UsersDB users;
    private final int TCP_Port = 4572;
    private ServerNotificationService notificationService;


    public MultiThreadedServer(UsersDB u, ServerNotificationService ns) {
        users = u;
        notificationService = ns;

    }

    public void start() {

        try (ServerSocket listeningSocket = new ServerSocket()){
            listeningSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), TCP_Port));      //il server resta in ascolto sulla porta 4569

            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();   //creo un threadpool
            while(true){
                Socket socket = listeningSocket.accept();       //accetto le richieste di connessione da parte degli utenti
                System.out.println("System: un utente si e' connesso al sistema");

                // TODO: 08/01/2023 controllare questa saveFile
                threadPool.execute(new RequestHandler(socket, users,notificationService));   //gestisco le loro richieste
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
