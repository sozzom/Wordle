package client;

import common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class ClientMain {
    private static final int TCP_Port = 4572;
    private static UsersDB localUsersDB; // stuttura dati degli utenti aggiornata tramite callbacks
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json";

    private static boolean logIn_effettuato; // flag di controllo per verificare se l'utente e' loggato
    private static final int RMI_Port = 4567;
    private static final int RMI_CALLBACK_Port = 4568;

    private static Registry registration_registry;
    private static RegistrationInterface registration;
    public static void main(String[] args) {

        localUsersDB = new UsersDB();

        System.out.println("Welcome in WORDLE");
        System.out.println("Per favore effettua login o register per procedere. Se hai bisogno di aiuto scrivi \"help\"");

        String message = null;
        String result = null;

        // TODO: 06/01/2023 Fornire traduzione della secretWord a fine gioco, vinto o perso che sia
        // TODO: 06/01/2023 Struttura dati nel client che tiene traccia delle prime 3 posizioni aggiornata con callback 
        // TODO: 06/01/2023 Dopo Login l'utente si registra a un gruppo multicast punto 5 specifiche traccia
        // TODO: 06/01/2023 Creare file di configurazione con parametri di input dell'applicazione: numeri porta, indirizzi, volori timeout ecc...


        BufferedReader reader; // stream dal server TCP al client
        BufferedWriter writer; // stream dal client TCP al server

        try (Socket socket = new Socket();
             BufferedReader cmd_line = new BufferedReader(new InputStreamReader(System.in));) {

            // RMI- ottengo un riferimento all'oggetto remoto in modo da utilizzare i suoi metodi
            registration_registry = LocateRegistry.getRegistry(RMI_Port); // recupero la registry sulla porta RMI
            registration = (RegistrationInterface) registration_registry.lookup("RegisterUser"); // richiedo l'oggetto dal nome pubblico

            // Callbacks
            NotificationSystemClientInterface callbackObj = new ClientNotificationService(localUsersDB);
            Registry registry = LocateRegistry.getRegistry(RMI_CALLBACK_Port);
            NotificationSystemServerInterface server = (NotificationSystemServerInterface) registry.lookup("NotificationService");
            NotificationSystemClientInterface stub = (NotificationSystemClientInterface) UnicastRemoteObject.exportObject(callbackObj, 0);


            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), TCP_Port));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            do {
                System.out.println();
                System.out.printf("> ");
                try {
                    message = cmd_line.readLine();
                    String[] myArgs = message.split(" ");
                    if (message.startsWith("close")) {
                        break;
                    } else if (message.startsWith("register")) {
                        registerFunction(myArgs);
                        continue;
                    } else if (message.startsWith("login")) {
                        // TODO: 06/01/2023 Implementare funzione per registrarsi a un servizio di notifica dal quale riceve aggiornamenti sulla classifica degli utenti subito dopo il login (tramite RMI callback)
                        server.registerForCallback(stub);
                    } else if (message.startsWith("showMeRanking")) { // LISTUSERS- visualizza lista utenti

                        showMeRanking();
                        continue;
                    }

                    writer.write(message + "\r\n");                     // invio la richiesta al server
                    writer.flush();

                    //Leggo la risposta del server
                    while (!(result = reader.readLine()).equals("")) {

                        if (message.startsWith("login") && !result.startsWith("Error")) { // gestisco lo stato a seguito del login
                            logIn_effettuato = true;
                        } else if (message.startsWith("logout") && !result.startsWith("Error")) { // gestisco lo stato a seguito del logout
                            server.unregisterForCallback(stub); // mi disiscrivo dal servizio di notifica
                            logIn_effettuato = false;
                            localUsersDB.clear();
                        }

                        System.out.println("< " + result);
                    }


                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } while (!message.equals("close"));

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ue giovane, fai partire il server!");
        }
    }

    private static void showMeRanking() {

        if(logIn_effettuato == false) {
            System.out.println("Errore. Log in se vuoi sapere la classifica.");
            return;
        };

        // TODO: 08/01/2023 primi tre in classifica
        System.out.println("Primi tre in classifica:");

        int i = 0;

        for (User u : localUsersDB.listUser()) {
            System.out.println("\n\t" + u.getUsername() + " - " + u.getStatus() + " - " + u.getScore());
            i++;
            if(i == 3) return ;
        }
    }

    public static void registerFunction(String[] myArgs) throws RemoteException, NotBoundException {

        String result;

        if(logIn_effettuato == true)
            result = "Errore. Log out prima di una nuova registrazione";
        else
            result = registration.register(myArgs);     //RMI- invoco il metodo remoto

        System.out.println("< "+result);
    }


}


