package com.KidsWord.KidsWord;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class GameController {

    private final List<String> correctWords = new ArrayList<>();

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "הכנס מילים שמתחילות בחיריק!");
        model.addAttribute("count", correctWords.size());
        return "game";
    }
}