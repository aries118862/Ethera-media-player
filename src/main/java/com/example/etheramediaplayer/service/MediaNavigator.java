package com.example.etheramediaplayer.service;

import java.util.List;

public final class MediaNavigator {
    public int nextIndex(int currentIndex, int size) {
        if (size <= 0) {
            return -1;
        }
        return Math.floorMod(currentIndex + 1, size);
    }

    public int previousIndex(int currentIndex, int size) {
        if (size <= 0) {
            return -1;
        }
        return Math.floorMod(currentIndex - 1, size);
    }

    public <T> T itemAt(List<T> items, int index) {
        if (items == null || index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }
}
