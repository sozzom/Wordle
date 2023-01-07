package server;

import Gioco.*;
import common.User;
import common.UsersDB;

import java.io.*;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class ServerMain {
    public final static String RECOVERY_FILE_PATH = "C:\\Users\\sozzo\\IdeaProjects\\Wordle\\src\\recovery\\";
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json";
    public static UsersDB users;
    private static int PORT = 5000;


    public static void main(String[] args) {


        users = new UsersDB();

        restoreBackup();

        // Il server stampa la lista iniziale degli utenti registrati. Per semplicità ogni utente ha la stessa password 'myPass'
        System.out.println("Ripristino lo stato iniziale: lista utenti");
        for (User u : users.listUser()) {
            System.out.println(u.getUsername() + " - " + u.getStatus());
        }

        // TODO: 07/01/2023 Implementare la parte RMI

        //TIMER PER PESCARE NUOVA PAROLA
        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                // whatever you need to do every 2 seconds
                System.out.println("prova timer");
                try {
                    Game.pickWord();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        timer.schedule(myTask, 2000, TimeUnit.MINUTES.toMillis(15));

        new MultiThreadedServer(users).start();



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

