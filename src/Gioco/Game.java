package Gioco;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.function.ToDoubleBiFunction;

public class Game {
    static LinkedHashMap<Character, Integer> letters = new LinkedHashMap<>();
    static ArrayList<String> answersList;
    public Slot[][] slots = new Slot[12][10];
    public String secret;
    public int row;

    public Game() throws FileNotFoundException {
        row = 0;
        createSlots();
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
}