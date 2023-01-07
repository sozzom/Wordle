package client;

import common.*;
import server.*;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static server.ServerMain.*;


public class ClientMain {
    private static final int TCP_Port = 4572;
    private static UsersDB localUsersDB; // stuttura dati degli utenti aggiornata tramite callbacks
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json";
    public static void main(String[] args) {

        localUsersDB = new UsersDB();

        System.out.println("Welcome in WORDLE");
        System.out.println("Please login or register to proceed. If you need help send \"help\"");

        String message = null;
        String result = null;

        // TODO: 06/01/2023 Implementare verifica guessed word nello stesso vocabolario dal quale si pesca la secretWord
        // TODO: 06/01/2023 Calcolare punteggio Utente: Numero medio di tentativi impiegati per raggiugimento soluzione * numero partite vinte
        // TODO: 06/01/2023 Fornire traduzione della secretWord a fine gioco, vinto o perso che sia
        // TODO: 06/01/2023 Struttura dati nel client che tiene traccia delle prime 3 posizioni aggiornata con callback 
        // TODO: 06/01/2023 Dopo Login il client invia comandi sulla connessione TCP 
        // TODO: 06/01/2023 Dopo Login l'utente si registra a un gruppo multicast punto 5 specifiche traccia 
        // TODO: 06/01/2023 Server ThreadPool o NIO 
        // TODO: 06/01/2023 Il server memorizza le informazioni su un file Json 
        // TODO: 06/01/2023 Stabilire intervallo tra le estrazioni di una parola, l'utente potrà nella stessa sesseione partecipare a più giochi purchè aspetti l'estrazione successiva
        // TODO: 06/01/2023 Creare file di configurazione con parametri di input dell'applicazione: numeri porta, indirizzi, volori timeout ecc...


        BufferedReader reader; // stream dal server TCP al client
        BufferedWriter writer; // stream dal client TCP al server

        try (Socket socket = new Socket();
             BufferedReader cmd_line = new BufferedReader(new InputStreamReader(System.in));){

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
                        // TODO: 06/01/2023 Ricorda di chiamare saveFile per aggiornare il file json
                        continue;
                    } else if (message.startsWith("login")) {
                        // TODO: 06/01/2023 Implementare funzione per registrarsi a un servizio di notifica dal quale riceve aggiornamenti sulla classifica degli utenti subito dopo il login (tramite RMI callback)
                        //server.registramiAlServizioDiNotifica(stub);
                    } else if (message.startsWith("logout")) {
                        // TODO: 06/01/2023 Effettua il logout dell'utente dal servizio
                        System.out.println("Effettua il logout dell'utente dal servizio");

                    } else if (message.startsWith("playWORDLE")) {
                        // TODO: 06/01/2023 Richiesta di gioco con l'ultima parola estratta, se l'utente ha già partecipato con quella parola il server risponde con errore altrimenti invia messaggio che può iniziare con guessed Word 
                        System.out.println("Richiesta d'iniziare il gioco indovinando l'ultima parola estratta");
                    } else if (message.startsWith("sendWord")) {
                        // TODO: 06/01/2023 Invio da parte del client della parola con relativa risposta e 12 tentativi
                        System.out.println("Invio da parte del client di una Guessed Word al server");
                    } else if (message.startsWith("sendMeStatistics")) {
                        // TODO: 06/01/2023 Richiesta delle statistiche dell'utente aggiornate dopo l'ultimo gioco
                        System.out.println("Richiesta delle statistiche dell'utente aggiornata dopo l'ultimo gioco");
                    } else if (message.startsWith("share")) {
                        // TODO: 06/01/2023 Richiesta di condividere i risultati del gioco su un gruppo sociale impementato come gruppo multicast
                        System.out.println("Richiesta di condividere i risultati del gioco su un gruppo sociale (multicast)");
                    } else if (message.startsWith("showMeSharing")) {
                        // TODO: 06/01/2023 Mostra sulla CLI le notifiche inviate dal server riguardo alle partite degli altri utenti
                        System.out.println("Mostra sulla CLI le notifiche inviate dal server riguardo alle partite degli altri utenti");
                    } else if (message.startsWith("showMeRanking")) {
                        // TODO: 06/01/2023 Viene visualizzata on demand la classifica contenente le tre prime posizioni
                    } else if (message.startsWith("help")) {
                        // TODO: 07/01/2023 Invocare metodo invalidOptionHandler() del server
                        //result = invalidOptionHandler();
                        System.out.println(result);
                    }
                    // TODO: 07/01/2023 Eliminare
                    /*else {
                        System.out.println("Operazione invalida, riprova. Possibili operazioni:");
                        result = invalidOptionHandler();
                        System.out.println(result);
                    }*/


                    writer.write(message + "\r\n");                     // invio la richiesta al server
                    writer.flush();

                    //Leggo la risposta del server
                    while (!(result = reader.readLine()).equals("")){


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

    public static void registerFunction(String[] myArgs) throws RemoteException, NotBoundException {

        // TODO: 06/01/2023 Implementata tramite RMI quindi sul server
        /*String result;

        if(logIn_effettuato == true)
            result = "Error. Log out before new registration";
        else
            result = registration.register(myArgs);     //RMI- invoco il metodo remoto

        System.out.println("< "+result);*/
    }



}


