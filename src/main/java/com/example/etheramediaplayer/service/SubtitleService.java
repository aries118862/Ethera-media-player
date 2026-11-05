package com.example.etheramediaplayer.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SubtitleService {
    private static final Pattern TIMING_PATTERN = Pattern.compile(
            "(\\d{1,2}):(\\d{2}):(\\d{2})[,.](\\d{3})\\s*-->\\s*(\\d{1,2}):(\\d{2}):(\\d{2})[,.](\\d{3})");

    public boolean isSupported(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".srt") || name.endsWith(".vtt");
    }

    public List<SubtitleCue> parse(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<SubtitleCue> cues = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            Matcher matcher = TIMING_PATTERN.matcher(lines.get(index));
            if (!matcher.find()) {
                continue;
            }
            double start = timestamp(matcher, 1);
            double end = timestamp(matcher, 5);
            StringBuilder text = new StringBuilder();
            for (int textIndex = index + 1; textIndex < lines.size() && !lines.get(textIndex).isBlank(); textIndex++) {
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(lines.get(textIndex).replaceAll("<[^>]+>", "").trim());
                index = textIndex;
            }
            if (text.length() > 0 && end > start) {
                cues.add(new SubtitleCue(start, end, text.toString()));
            }
        }
        return cues;
    }

    private double timestamp(Matcher matcher, int offset) {
        long hours = Long.parseLong(matcher.group(offset));
        long minutes = Long.parseLong(matcher.group(offset + 1));
        long seconds = Long.parseLong(matcher.group(offset + 2));
        long millis = Long.parseLong(matcher.group(offset + 3));
        return hours * 3600 + minutes * 60 + seconds + millis / 1000.0;
    }
}
