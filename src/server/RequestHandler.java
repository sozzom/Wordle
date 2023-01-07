package server;

import common.User;
import common.UsersDB;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;

public class RequestHandler implements Runnable{
    private Socket clientSocket;
    private UsersDB users;

    private User user;
    private boolean logIn_effettuato;

    public RequestHandler(Socket c, UsersDB u) {
        clientSocket = c;
        users = u;
    }

    @Override
    public void run() {
        executeRequest();
    }

    public void executeRequest() {
        String request;
        String reply = null;

        while (true){

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));){

                while ((request = reader.readLine()) != null) {

                    System.out.println("Server receives: " + request);
                    String[] myArgs = request.split(" ");

                    // Qua dentro ci vanno tanti else if con chiamate alle funzioni di questa classe che fanno funzionare il gioco

                    if (myArgs[0].equals("login")){
                        reply = loginHandler(myArgs);           //LOGIN
                    }
                    else if (myArgs[0].equals("logout")){
                        reply = logoutHandler(myArgs);          //LOGOUT
                    }
                    else if (myArgs[0].equals("listUsers")) {
                        reply = listUsersHandler();             //LISTUSERS
                    }
                    // TODO: 06/01/2023 Far partire il gioco
                    else if (myArgs[0].equals("playWORDLE")) {
                        reply = "Giochiamo";
                    }
                    // TODO: 06/01/2023 Implementare funzione che mi dice se la parola è presente o meno nel vocabolario, nel primo caso fornisce gli indizi
                    else if (myArgs[0].equals("sendWord")){
                        reply = "Parola c'è o non c'è, ecco gli indizi";
                    }
                    // TODO: 06/01/2023 Implementare funzione che mostra sulla CLI le notifiche inviate dal server riguardo alle partitedegli altri utenti
                    else if (myArgs[0].equals("showMeSharing")) {
                        reply = "Partite degli altri utenti";
                    }
                    // TODO: 06/01/2023 Viene visualizzata la classifica con le prime tre posizioni della classifica
                    else if (myArgs[0].equals("showMeRanking")) {
                        reply = "Partite degli altri utenti";
                    }
                    else if (myArgs[0].equals("help")) {
                        reply = helpOptionHandler();            //HELP
                    }
                    // TODO: 06/01/2023 Arricchire invalidOptionHandler()
                    else{
                        reply = "Operazione invalida, riprova.\n" + helpOptionHandler();         //INVALID OPTION
                    }

                    writer.write(reply + "\n\r\n");
                    writer.flush();

                }

            } catch (IOException e){
                System.out.println("System: un utente si e' disconnesso dal sistema");

                // Se l'utente si è disconnesso senza aver prima effettuato il logout
                if (user.getStatus().equals("Online")) {
                    synchronized (users) {

                        user.setOffline();
                        logIn_effettuato = false;

                    }
                }
                break;
            }
            catch(Exception e) {
                e.printStackTrace();
            }


        }

    }
    private String loginHandler(String[] myArgs) {

        if (myArgs.length != 3)
            return "Error. Use: login username password";

        if (logIn_effettuato == true)        //se ci si prova a collegare contemporaneamente da due account
            return "Error. User " + user.getUsername() + " currently logged in";

        String name = myArgs[1];
        String pass = myArgs[2];
        String reply = null;

        synchronized (users) {     //gestisco la concorrenza nell'accesso al 'database' degli utenti

            User currentUser = users.getUser(name);

            try {
                currentUser.equals(null);         // se l'utente non esiste
            } catch (Exception e) {
                return "Error. User not found";
            }

            if (!(currentUser.getPassword().equals(pass)))        // se la password non matcha
                reply = "Error. Invalid password.";
            else if (currentUser.getStatus().equals("Online"))        // se l'utente ha gia' effettuato l'accesso
                reply = "Error. " + currentUser.getUsername() + " already logged in";
            else {
                reply = "User " + name + " logged in";
                this.user = currentUser;
                this.user.setOnline();
                logIn_effettuato = true;

                // TODO: 06/01/2023 Gestire questa parte
                /*try {
                    notificationService.update(this.users);            //notifico la modifica
                } catch (RemoteException e) {
                    System.out.println("System_error: cannot do callback");
                }*/
            }
        }

        return reply;
    }

    private String logoutHandler(String[] myArgs) {

        if (myArgs.length != 2)
            return "Error. Use: logout username";

        if (!logIn_effettuato)        //se l'utente vuole eseguire il logout prima di eseguire il login
            return "Error. Login before you can logout";

        String name = myArgs[1];
        String reply = null;

        synchronized (users) {         //gestisco la concorrenza nell'accesso al 'database' degli utenti

            User currentUser = users.getUser(name);

            try {
                currentUser.equals(null);        // se l'utente non esiste
            } catch (Exception e) {
                return "Error. User not found";
            }

            if (!(user.getUsername().equals(name)))        //se un utente vuole disconnettere altri utenti
                reply = "Error. You don't have the permission to logout other users";
            else {
                user.setOffline();
                reply = name + " logged out";
                logIn_effettuato = false;

                // TODO: 06/01/2023 Gestire questa parte
                /*try {
                    notificationService.update(this.users);            //notifico la modifica
                } catch (RemoteException e) {
                    System.out.println("System_error: cannot do callback");
                }*/
            }
        }

        return reply;
    }

    private String listUsersHandler(){
        return users.toString();
    }

    private String helpOptionHandler() {
        String reply = "Select operation:";

        reply = reply + "\n\t\"register username password\" -to register a user";
        reply = reply + "\n\t\"login username password\" -to login";
        reply = reply + "\n\t\"logout username\" -to logout";
        reply = reply + "\n\t\"listUsers\" -to show all registered users";
        reply = reply + "\n\t\"listOnlineUsers\" -to show online users";
        return reply;
    }
}
