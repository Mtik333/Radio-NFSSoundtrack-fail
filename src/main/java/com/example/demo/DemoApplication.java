package com.example.demo;

import com.example.demo.music.Song;
import com.example.demo.music.SongComparator;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.AccessPolicy;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetailsRegionRestriction;
import com.google.api.services.youtube.model.VideoListResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SpringBootApplication
public class DemoApplication {

    private static final Logger logger = Logger.getLogger(DemoApplication.class.getName());
    public final static YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
            request -> {
            }).setApplicationName("").build();
    public final static Locale currentLocale = Locale.getDefault();
    private static List<Song> songsFromFile;
    private static Queue<Song> songQueue;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private static void loadFile() {
        logger.log(Level.INFO, "Started method loadFile");
        Path path;
        List<String> lines = new ArrayList<>();
        try {
            path = Paths.get(Objects.requireNonNull(DemoApplication.class.getClassLoader()
                    .getResource("songs_14_12_2019.sql")).toURI());
            lines = Files.readAllLines(path);
            logger.log(Level.INFO, "Path to input file: " + path.toString());
        } catch (Exception exp) {
            logger.log(Level.INFO, "Jar mode");
            try {
                InputStream in = DemoApplication.class.getResourceAsStream("/songs_14_12_2019.sql");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                lines = reader.lines().collect(Collectors.toList());
            } catch (Exception ex2p) {
                logger.log(Level.SEVERE, "Can't do it.");
            }
        }
        logger.log(Level.INFO, "Number of lines: " + lines.size());
        for (String line : lines) {
            logger.log(Level.INFO, "Line text: " + line);
            String skipStartEndChars = line.substring(1, line.length() - 2);
            String[] splitByComma = skipStartEndChars.split(",");
            for (int i = 0; i < splitByComma.length; i++) {
                String value = splitByComma[i].trim().replaceAll("'$", "").replaceFirst("^'", "");
                logger.log(Level.INFO, "Value from line is: " + value);
                splitByComma[i] = value;
            }
            Song song = createSong(splitByComma);
            //filter by game maybe?
//            if (song.getPrefix().toUpperCase().contains("NEED FOR SPEED")
//            || song.getGametitle().toUpperCase().contains("NEED FOR SPEED")
//            || song.getGametitle().toUpperCase().contains("SHIFT 2")){
//                logger.log(Level.INFO, "NFS Filter: Song loaded is: " + song);
//                songsFromFile.add(song);
//            }
            logger.log(Level.INFO, "Song loaded is: " + song);
            songsFromFile.add(song);
        }
        songsFromFile.sort(new SongComparator());
    }

    private static Song createSong(String[] values) {
        logger.log(Level.INFO, "Started method createSong");
        Integer id = Integer.valueOf(values[0]);
        String band = values[1].replace("\\", "");
        String title = values[2].replace("\\", "");
        String prefix = values[3];
        String gameTitle = values[4];
        String src_id = values[5];
        String description = values[6].replace("\\", "");
        return new Song(id, band, title, prefix, gameTitle, src_id, description);
    }

    private static void getYoutubeDetails(Song song) throws IOException {
        logger.log(Level.INFO, "Started method testYoutubeDetails");
        YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics,contentDetails");
        videoRequest.setId(song.getSrc_id());
        videoRequest.setKey("");
        logger.log(Level.INFO, "Request input for YT: " + videoRequest.toString());
        VideoListResponse listResponse = videoRequest.execute();
        logger.log(Level.INFO, "Response from YT: " + listResponse.toPrettyString());
        logger.log(Level.INFO, "Started method setSongStatus");
        List<Video> videos = listResponse.getItems();
        if (videos.isEmpty()) {
            logger.log(Level.INFO, "No video");
            song.setStatus(Song.Status.DELETED);
        } else {
            Video video = videos.get(0);
            logger.log(Level.INFO, "Video from response: " + video.toPrettyString());
            VideoContentDetailsRegionRestriction regionRestriction = video.getContentDetails().getRegionRestriction();
            AccessPolicy policy = video.getContentDetails().getCountryRestriction();
            if (policy == null && regionRestriction == null) {
                //means there are no restrictions
                logger.log(Level.INFO, "Video is available everywhere, so it is WORKING");
                if (videoTooLong(video)) {
                    song.setStatus(Song.Status.TOO_LONG);
                } else {
                    song.setStatus(Song.Status.WORKING);
                }
            } else {
                //video with restrictions gives regionRestriction field
                song.setStatus(Song.Status.RESTRICTED);
                song.setRegionRestriction(regionRestriction);
                logger.log(Level.INFO, "Video has restrictions, so it is RESTRICTED");
            }
        }
    }

    public static Song getRandomSong() throws IOException {
        if (!songQueue.isEmpty()) {
            return songQueue.remove();
        } else {
            int randomValue = ThreadLocalRandom.current().nextInt(1, songsFromFile.size());
            logger.log(Level.INFO, "Integer: " + randomValue);
            Song song = songsFromFile.get(randomValue);
            logger.log(Level.INFO, "Song: " + song.toString());
            if (song.getStatus() == null) {
                getYoutubeDetails(song);
            }
            while (song.getStatus().equals(Song.Status.DELETED) || isCountryRestricted(song.getRegionRestriction()) || song.getStatus().equals(Song.Status.TOO_LONG)) {
                logger.log(Level.INFO, "Need to find different song: is deleted? " + song.getStatus() + "; country restriction? " + song.getRegionRestriction());
                song = getRandomSong();
            }
            return song;
        }
    }

    private static boolean isCountryRestricted(VideoContentDetailsRegionRestriction policy) {
        logger.log(Level.INFO, "Restrictions: " + policy);
        if (policy == null) {
            return false;
        }
        try {
            logger.log(Level.INFO, policy.toPrettyString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> getBlocked = policy.getBlocked();
        if (getBlocked == null || getBlocked.isEmpty()) {
            return false;
        }
        if (getBlocked.contains(currentLocale.getCountry())) {
            return true;
        }
        List<String> getAllowed = policy.getAllowed();
        return getAllowed == null || getAllowed.isEmpty();
    }

    public static boolean videoTooLong(Video video) {
        String duration = video.getContentDetails().getDuration();
        logger.log(Level.INFO, "Duration of video: " + duration);
        duration = duration.replace("PT", "");
        if (duration.indexOf('H') > -1) {
            logger.log(Level.INFO, "Duration is over hour, TOO_LONG");
            return true;
        }
        if (duration.indexOf('M') > -1) {
            String[] minutesSplit = duration.split("M");
            int minutes = Integer.parseInt(minutesSplit[0]);
            if (minutes > 15) {
                logger.log(Level.INFO, "Duration over 15 minutes, TOO_LONG");
                return true;
            }
        }
        return false;
    }

    public static Song findSongByTitleBand(String title, String band) {
        List<Song> filteredSongs = songsFromFile.stream().filter(song -> (title.toLowerCase().trim().contentEquals(song.getTitle().toLowerCase().trim())
                && band.toLowerCase().trim().contentEquals(song.getBand().toLowerCase().trim()))).collect(Collectors.toList());
        if (!filteredSongs.isEmpty()) {
            songQueue.add(filteredSongs.get(0));
        }
        return songQueue.element();
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "Start of tool");
        logger.log(Level.INFO, currentLocale.getCountry());
        songsFromFile = new ArrayList<>();
        songQueue = new LinkedList<>();
        loadFile();
    }
}
