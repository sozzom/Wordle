package server;

import Gioco.*;
import common.User;
import common.UsersDB;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.*;


import static server.ServerMain.*;

public class RequestHandler implements Runnable {
    private static ServerNotificationService notificationService;

    private Socket clientSocket;
    private UsersDB users;
    private User user;
    private boolean logIn_effettuato;
    public String logged;
    public static Game currentGame;

    public RequestHandler(Socket c, UsersDB u,ServerNotificationService ns) throws FileNotFoundException {
        clientSocket = c;
        users = u;
        logIn_effettuato = false;
        notificationService = ns;
    }

    @Override
    public void run() {
        executeRequest();
    }

    public void executeRequest() {
        String request;
        String reply = null;

        while (true) {

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));) {

                while ((request = reader.readLine()) != null) {

                    System.out.println("Server receives: " + request);
                    String[] myArgs = request.split(" ");

                    // Qua dentro ci vanno tanti else if con chiamate alle funzioni di questa classe che fanno funzionare il gioco

                    if (myArgs[0].equals("login")) {
                        reply = loginHandler(myArgs);           //LOGIN
                    } else if (myArgs[0].equals("playWORDLE")) {
                        reply = playWORDLEHandler(myArgs);      //PLAY GAME
                    }
                    else if (myArgs[0].equals("sendWord")) {
                        reply = sendWordHandler(myArgs);
                    }
                    else if (myArgs[0].equals("myData")) {
                        reply = myData(myArgs);
                    }
                    // TODO: 06/01/2023 Implementare funzione che mostra sulla CLI le notifiche inviate dal server riguardo alle partite degli altri utenti
                    else if (myArgs[0].equals("showMeSharing")) {
                        reply = "Partite degli altri utenti";
                    } else if (myArgs[0].equals("listUsers")) {
                        reply = listUsersHandler();             //LIST USERS
                    } else if (myArgs[0].equals("logout")) {
                        reply = logoutHandler(myArgs);          //LOGOUT
                    } else if (myArgs[0].equals("help")) {
                        reply = helpOptionHandler();            //HELP
                    }
                    else {
                        reply = "Operazione invalida, riprova.\n" + helpOptionHandler();         //INVALID OPTION
                    }

                    writer.write(reply + "\n\r\n");
                    writer.flush();

                }

            } catch (IOException e) {
                System.out.println("System: un utente si e' disconnesso dal sistema");

                // Se l'utente si è disconnesso senza aver prima effettuato il logout
                if(!logIn_effettuato) break;
                if (user.getStatus().equals("Online")) {
                    synchronized (users) {
                        user.setOffline();
                        logIn_effettuato = false;
                    }
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String playWORDLEHandler(String[] myArgs) throws FileNotFoundException {

        if (myArgs.length != 1) {
            return "Errore. Usa: playWORDLE";
        }

        if (!logIn_effettuato) {
            return "Errore. Effettua prima il login";
        }

        User currentUser = users.getUser(logged);

        synchronized (users) {

            //Verifica che il giocatore non abbia già giocato con la parola corrente
            if (currentUser.playedWords.contains(answer)){
                return "Hai gia' giocato con questa parola, aspetta la prossima estrazione";
            }
            else {
                currentUser.playedWords.add(answer);

                currentGame = new Game();
                currentUser.played++;
                currentGame.secret = answer;

                ServerMain.saveFile(ServerMain.RECOVERY_FILE_PATH + ServerMain.FILENAME_utentiRegistrati,users,UsersDB.class);

                return "                START GAME\n\t" + disegna(currentGame);
            }
        }
    }

    private String sendWordHandler(String[] myArgs) {

        String result = null;

        if (myArgs.length != 2) {
            return "Errore. Usa: sendWord 'word'";
        }

        try {
            currentGame.getClass();
        } catch (Exception e) {
            return "Devi prima far partire un nuovo WORDLE game";
        }

        String guessed = myArgs[1];
        guessed = guessed.toUpperCase();

        User currentUser = users.getUser(logged);

        synchronized (users){
            if (guessed.length() != 10)
                return "Parola non valida! Deve essere di 10 lettere.";

            if(!answersList.contains(guessed))
                return "Parola non valida! Inserisci una parola contenuta nel vocabolario.";

            writeSlots(currentGame,guessed);
            findColors(currentGame);

            currentGame.row++;
            if (guessed.equals(currentGame.secret) && currentGame.row <= 12) {

                currentUser.score += currentGame.row;

                reorderList(users);

                result = "Congratulazioni, hai indovinato in ";
                result += currentGame.row + "/12\n\t";
                result += disegna(currentGame) + "\n\t";
                result += "Il tuo punteggio e' -- " + currentUser.score  + " --" ;

                //Il garbace collector in questo modo elimina il currentGame terminato
                currentGame = null;
                ServerMain.saveFile(ServerMain.RECOVERY_FILE_PATH + ServerMain.FILENAME_utentiRegistrati,users,UsersDB.class);

                return result;
            } else if (currentGame.row >= 12) {

                result = disegna(currentGame) + "\n\t";
                result += "Hai esaurito i tentativi disponibili, riprova alla prossima estrazione. (ogni 15 min.)";

                currentGame = null;
                ServerMain.saveFile(ServerMain.RECOVERY_FILE_PATH + ServerMain.FILENAME_utentiRegistrati,users,UsersDB.class);

                return result;
            } else return disegna(currentGame);
        }
    }

    public static void reorderList(UsersDB users) {
        if(users.size()<2) return;

        Map<String, User> map = users.users;

        List<Map.Entry<String, User>> list = new LinkedList<Map.Entry<String, User>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, User>>() {
            public int compare(Map.Entry<String, User> o1, Map.Entry<String, User> o2) {
                return  o2.getValue().getScore() - o1.getValue().getScore();
            }
        });

        HashMap<String, User> sortedMap = new LinkedHashMap<String, User>();
        for (Map.Entry<String, User> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        users.users = sortedMap;

        try {
            notificationService.update(users);            //notifico la modifica
        } catch (RemoteException e) {
            System.out.println("System_error: cannot do callback");
        }

    }

    public void printMap(Map<String, User> map) {
        System.out.println("Company\t Price ");
        for (Map.Entry<String, User> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        System.out.println("\n");
    }

    private void writeSlots(Game currentGame, String guessed) {
        for (int i = 0; i < 10; i++) {
            currentGame.slots[currentGame.row][i].Char = guessed.charAt(i);
        }
    }

    static void findColors(Game game) {

        for (int i = 0; i < 10; i++) {
            char Char = currentGame.slots[currentGame.row][i].Char;

            if (currentGame.secret.contains(String.valueOf(Char))) {
                currentGame.slots[currentGame.row][i].color = Colors.YELLOW;
            }
        }
        for (int i = 0; i < 10; i++) {
            char Char = currentGame.slots[currentGame.row][i].Char;

            if (Char == currentGame.secret.charAt(i)) {
                currentGame.slots[currentGame.row][i].color = Colors.GREEN;
            }
        }
    }

    static String disegna(Game game) {

        String result = currentGame.row + "/12 " + "\t\n\t\n\t";

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 10; j++) {

                //In ogni corpo stampo un quadratino, eventualmente con una lettera all'interno. + spazio alla fine
                if (game.slots[i][j].color == Colors.GREEN) {
                    result += "\u001b[42;1m" + " " + game.slots[i][j].Char + " " + "\033[0m ";

                } else if (game.slots[i][j].color == Colors.YELLOW) {
                    result += "\u001b[43;1m" + " " + game.slots[i][j].Char + " " + "\033[0m ";

                } else result += "\u001b[40;1m" + " " + game.slots[i][j].Char + " " + "\033[0m ";
            }
            result += "\n\t\n\t";
        }
        return result;
    }

    private String listUsersHandler() {

        String relpy = "UsersDB users:";

        for (User u : users.listUser())
            relpy += "\n\t"+u.getUsername() + " - "+ u.getStatus();

        return relpy;

        //Versione semplificata
        //return users.toString();
    }

    private String myData(String[] myArgs) {

        if (!logIn_effettuato)        //se l'utente vuole eseguire l'operazione prima di eseguire il login
            return "Errore. Effettuare prima il login";

        String reply = "Your data: ";
        reply += "\n\t\"username: " + users.getUser(logged).getUsername();
        reply += "\n\t\"partite vinte: " + users.getUser(logged).wins;
        reply += "\n\t\"partite giocate: " + users.getUser(logged).played;
        reply += "\n\t\"score: " + users.getUser(logged).score;

        return  reply;
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
                logged = name;

                // TODO: 08/01/2023 callback per aggiornare il localDB di avvenuta login
                try {
                    reorderList(users);
                    notificationService.update(users);            //notifico la modifica
                } catch (RemoteException e) {
                    System.out.println("System_error: cannot do callback");
                }
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
                logged = null;

                // TODO: 06/01/2023 callback per aggiornare il localDB di avvenuta logout
                /*try {
                    notificationService.update(this.users);            //notifico la modifica
                } catch (RemoteException e) {
                    System.out.println("System_error: cannot do callback");
                }*/
            }
        }

        return reply;
    }

    private String helpOptionHandler() {
        String reply = "Select operation:";

        reply = reply + "\n\t\"register username password\" -per registrare un utente";
        reply = reply + "\n\t\"login username password\" -per fare login di un tente gia' registrato";
        reply = reply + "\n\t\"logout username\" -per fare il logout di un utente in sessione";
        reply = reply + "\n\t\"listUsers\" -per mostrare tutti gli utenti registrati e il loro stato";
        reply = reply + "\n\t\"playWORDLE\" -per permettere all'utente di inziare il gioco";
        reply = reply + "\n\t\"share\" -per richiedere le statistiche dell'utente aggiornata dopo l'ultimo gioco";
        reply = reply + "\n\t\"myData\" -per richiedere i propri dati su username, partite vinte, partite giocate e score";
        return reply;
    }

}
