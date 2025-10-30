package com.KidsWord.KidsWord;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "שלום! המשחק של ניקוד מוכן להתחלה 😄";
    }
}
