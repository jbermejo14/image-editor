package com.svalero.image_editor.history;

import java.util.ArrayList;
import java.util.List;

public class ImageHistory {
    private List<String> entries;

    public ImageHistory() {
        entries = new ArrayList<>();
    }

    public void addEntry(String entry) {
        entries.add(entry);
    }

    public List<String> getEntries() {
        return entries;
    }
}