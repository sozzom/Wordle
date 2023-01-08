package server;

import Gioco.*;
import common.User;
import common.UsersDB;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class ServerMain {
    public final static String RECOVERY_FILE_PATH = "src/recovery/";
    public final static String FILENAME_utentiRegistrati = "utentiRegistrati.json";
    public static UsersDB users;
    public static String answer;
    static ArrayList<String> answersList;
    static LinkedHashMap<Character, Integer> letters = new LinkedHashMap<>();
    private static int PORT = 5000;


    public static void main(String[] args) throws FileNotFoundException {

        users = new UsersDB();

        users.addUser(new User("chiara","cacca"));
        users.addUser(new User("matteo","oettam"));
        users.addUser(new User("ciccio","formaggio"));

        users.getUser("chiara").score = 30;
        users.getUser("matteo").score = 20;
        users.getUser("ciccio").score = 10;

        //saveFile(FILENAME_utentiRegistrati,users,UsersDB.class);

        restoreBackup();

            // Il server stampa la lista iniziale degli utenti registrati. Per semplicità ogni utente ha la stessa password 'myPass'
        if (!(users.size()==0)){
            System.out.println("Ripristino lo stato iniziale: lista utenti");
            for (User u : users.listUser()) {
                System.out.println(u.getUsername() + " - " + u.getStatus() + " | " + " score: " + u.score);
            }
        } else System.out.println("Ripristino lo stato iniziale: lista utenti vuota");

        createVocabulary();
        System.out.println("Estraggo la prima parola:");
        theWord();

        ServerNotificationService notificationService = new NotificationClass().start(); // RMI Callback
        new RegistrationClass(users, notificationService).start(); // RMI- creo un riferimento all'oggetto remoto

        new MultiThreadedServer(users,notificationService).start();

    }

    public static void theWord(){

        //TIMER PER PESCARE NUOVA PAROLA
        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                // whatever you need to do every 'tot' time
                try {
                    pickWord();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        timer.schedule(myTask, 2000, TimeUnit.MINUTES.toMillis(15));
    }

    public static void pickWord() throws FileNotFoundException {

        /*//Prendo il file con le parole selezionabili
        File answersTxt = new File("C:\\Users\\sozzo\\Desktop\\Wordle_Java_Console-master\\Wordle_Java_Console-master\\src\\Words\\Words.txt");
        Scanner answersScanner = new Scanner(answersTxt);
        //Inizializzo l'Arraylist che conterrà le parole selezionabili
        answersList = new ArrayList<>();

        //Aggiungo tutte le parole del file nell'Arraylist answerList
        while (answersScanner.hasNextLine()) {
            answersList.add(answersScanner.nextLine().toUpperCase());
        }*/

        //Estraggo casualmente una parola dall'ArrayList answerList
        answer = answersList.get((int) (Math.random() * answersList.size()));

        System.out.println("NUOVA PAROLA ESTRATTA!" + " (" + answer + ")");

        //Crea una LinkedHashMap con coppia lettera contenuta nella parola e numero di occorrenze
        int k;
        for (int i = 0; i < 10; i++) {
            k = 1;
            for (int j = 0; j < 10; j++) {
                if (i != j && answer.charAt(i) == answer.charAt(j))
                    k++;
            }
            letters.put(answer.charAt(i), k);
        }
    }

    public static void createVocabulary() throws FileNotFoundException {
        //Prendo il file con le parole selezionabili
        File answersTxt = new File("C:\\Users\\sozzo\\IdeaProjects\\Wordle\\src\\common\\Words.txt");
        Scanner answersScanner = new Scanner(answersTxt);
        //Inizializzo l'Arraylist che conterrà le parole selezionabili
        answersList = new ArrayList<>();

        //Aggiungo tutte le parole del file nell'Arraylist answerList
        while (answersScanner.hasNextLine()) {
            answersList.add(answersScanner.nextLine().toUpperCase());
        }
    }

    private static void restoreBackup() {

        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
            users.setAllOffline();
        } catch (FileNotFoundException e) {
            System.out.println("System: no users registred");
        } catch (Exception e) {
            e.printStackTrace();
        }

        File recoveryDir = new File(RECOVERY_FILE_PATH);

        // TODO: 06/01/2023 Manca la parte su indirizzi IP ecc...

    }

    public static boolean saveFile(String path, Object obj, Class<?> type) {

        String filePath = RECOVERY_FILE_PATH + path;

        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(filePath));) {

            output.writeObject(type.cast(obj)); // salvo in modo persistente le informazioni del progetto
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("System: cant save changes");
            return false;
        }
        return true;
    }

    // TODO: 07/01/2023 Capire a che mi serve questa funzione
    public static int getPORT() {
        return PORT;
    }

}

