package com.KidsWord.KidsWord;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class NikudController {
    @GetMapping("/check-nikud")
    public String checkNikud(@RequestParam String word, @RequestParam String nikudType) {
        String apiUrl = "https://nakdan-u1-0.loadbalancer.dicta.org.il/api";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("data", word);
        request.put("genre", "modern");
        request.put("task", "nakdan");
        request.put("addmorph", true);
        request.put("keepmetagim", true);
        request.put("useTokenization", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.getBody().get("data");
            if (dataList == null || dataList.isEmpty()) {
                return "❌ לא התקבלו נתונים";
            }

            Map<String, Object> nakdan = (Map<String, Object>) dataList.get(0).get("nakdan");
            List<Map<String, Object>> options = (List<Map<String, Object>>) nakdan.get("options");

            if (options == null || options.isEmpty()) {
                return "❌ לא נמצאו אפשרויות ניקוד";
            }

            boolean found = options.stream()
                    .map(opt -> (String) opt.get("w"))
                    .filter(w -> w != null && !w.isEmpty())
                    .map(this::cleanUnnecessaryNikudWord)
                    .map(this::findFirstNikudWord)
                    .findFirst()
                    .map(firstNikud -> switch (nikudType) {
                        case "חיריק" -> firstNikud.contains("ִ");
                        case "פתח" -> firstNikud.contains("ַ");
                        case "קמץ" -> firstNikud.contains("ָ");
                        case "צירה" -> firstNikud.contains("ֵ");
                        case "סגול" -> firstNikud.contains("ֶ");
                        case "שורוק" -> firstNikud.equals("וּ");
                        case "קובוץ" -> firstNikud.contains("ֻ");
                        case "חולם" -> firstNikud.equals("וֹ") || firstNikud.contains("ֹ");
                        default -> false;
                    })
                    .orElse(false);

            return found
                    ? "✅ נכון! המילה \"" + word + "\" *מתחילה* ב" + nikudType
                    : "❌ לא, המילה \"" + word + "\" לא מתחילה ב" + nikudType;

        } catch (Exception e) {
            return "⚠️ שגיאה: " + e.getMessage();
        }
    }

    private String cleanUnnecessaryNikudWord(String word) {
        if (word == null) return "";
        word = word.replaceAll("וֹ", "##HOLAM##").replaceAll("וּ", "##SHURUK##");
        word = word.replaceAll("[ּׁׂ\u05C1\u05C2]", "");
        word = word.replace("##HOLAM##", "וֹ").replace("##SHURUK##", "וּ");
        return word;
    }

    private String findFirstNikudWord(String word) {
        if (word == null || word.isEmpty()) return "";

        if (word.length() > 1 && word.charAt(1) == 'ו') {
            if (word.startsWith("וֹ", 1)) return "וֹ";
            if (word.startsWith("וּ", 1)) return "וּ";
        }

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c >= '\u05B0' && c <= '\u05C7') {
                return String.valueOf(c);
            }
        }

        return "";
    }
}