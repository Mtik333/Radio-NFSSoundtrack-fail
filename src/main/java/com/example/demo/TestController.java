package com.example.demo;

import com.example.demo.music.Song;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class TestController {

    private static final Logger logger = Logger.getLogger(TestController.class.getName());

    @RequestMapping("/")
    public String url() {
        return "aaa";
    }

    @ResponseBody
    @RequestMapping(value = "/newSong", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Song getNewSongSrcId() throws IOException {
        Song song = DemoApplication.getRandomSong();
        logger.log(Level.INFO, song.toString());
        return song;
    }

    @ResponseBody
    @RequestMapping(value = "/queueSong", method = RequestMethod.GET)
    public Song queueSong(@RequestParam("band") String band, @RequestParam("songTitle") String songTitle) {
        Song song = DemoApplication.findSongByTitleBand(songTitle, band);
        logger.log(Level.INFO, song.toString());
        return song;
    }
}
