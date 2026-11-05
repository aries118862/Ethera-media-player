package com.example.etheramediaplayer.service;

public record SubtitleCue(double startSeconds, double endSeconds, String text) {
    public boolean contains(double seconds) {
        return seconds >= startSeconds && seconds <= endSeconds;
    }
}
