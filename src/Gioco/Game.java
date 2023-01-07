package Gioco;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.function.ToDoubleBiFunction;

public class
Game {
    static String answer;
    static LinkedHashMap<Character, Integer> letters = new LinkedHashMap<>();
    static ArrayList<String> answersList;
    Slot[][] slots = new Slot[12][10];

    public Game() throws FileNotFoundException {
        createSlots();
        pickWord();
    }

    private void createSlots() {
        //Creo la griglia 5x5
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 10; j++) {
                Slot slot = new Slot();
                slots[i][j] = slot;
            }
        }
    }

    // TODO: 07/01/2023 Da spezzare in una sorta di "createDatabase" e "pickWord"
    public static void pickWord() throws FileNotFoundException {

        //Prendo il file con le parole selezionabili
        File answersTxt = new File("C:\\Users\\sozzo\\Desktop\\Wordle_Java_Console-master\\Wordle_Java_Console-master\\src\\Words\\Words.txt");
        Scanner answersScanner = new Scanner(answersTxt);
        //Inizializzo l'Arraylist che conterrà le parole selezionabili
        answersList = new ArrayList<>();

        //Aggiungo tutte le parole del file nell'Arraylist answerList
        while (answersScanner.hasNextLine()) {
            answersList.add(answersScanner.nextLine().toUpperCase());
        }

        //Estraggo casualmente una parola dall'ArrayList answerList
        answer = answersList.get((int) (Math.random() * answersList.size()));
        System.out.println(answer);

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
}