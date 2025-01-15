package com.svalero.image_editor.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HistoryManager {

    private static final ObservableList<String> history = FXCollections.observableArrayList();


    public static ObservableList<String> getHistory() {
        return history;
    }

    public static void addToHistory(String imageName, String filtersApplied) {
        String entry = "Imagen: " + imageName + " | Filtros: " + filtersApplied;
        history.add(entry);
    }

    public static void clearHistory() {
        history.clear();
    }
}
