package com.tsc.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tsc.model.Entry;
import com.tsc.repository.EntryRepository;

@Controller
public class EntryController {

	 @Autowired
	    private EntryRepository entryRepository;

	    @GetMapping("/")
	    public String index(Model model) {
	        List<Object[]> counts = entryRepository.countByGate();
	        Map<String, Long> countMap = new HashMap<>();
	        for (Object[] row : counts) {
	            countMap.put((String) row[0], (Long) row[1]);
	        }
	        model.addAttribute("countMap", countMap);
	        return "index";
	    }

	    @PostMapping("/enter-single")
	    public String enterSingle(@RequestParam String gate) {
	        Entry entry = new Entry(gate, LocalDateTime.now());
	        entryRepository.save(entry);
	        return "redirect:/";
	    }

	    @PostMapping("/enter-multiple")
	    public String enterMultiple(@RequestParam String gate, @RequestParam int count) {
	        for (int i = 0; i < count; i++) {
	            Entry entry = new Entry(gate, LocalDateTime.now());
	            entryRepository.save(entry);
	        }
	        return "redirect:/";
	    }
	    @PostMapping("/reset")
	    public String resetAllEntries() {
	        entryRepository.deleteAll();
	        return "redirect:/";
	    }

	    
}
