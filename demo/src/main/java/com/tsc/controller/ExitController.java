package com.tsc.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tsc.model.ExitRecord;
import com.tsc.repository.ExitRecordInterface;

@Controller
public class ExitController {
	 private final ExitRecordInterface repository;

	    public ExitController(ExitRecordInterface repository) {
	        this.repository = repository;
	    }

	    @GetMapping("/exitIndex")
	    public String index(Model model) {
	        List<ExitRecord> allRecords = repository.findAll();
	        model.addAttribute("records", allRecords);
	        return "indexExit";
	    }
	    
	    @PostMapping("/confirmLot")
	    public String confirmLot(@RequestParam String lot, Model model) {
	        model.addAttribute("lot", lot);
	        return "logExit";
	    }

	    @PostMapping("/exit")
	    public String logExit(@RequestParam String lot, @RequestParam(defaultValue = "1") int count, Model model) {
	        for (int i = 0; i < count; i++) {
	            repository.save(new ExitRecord(lot));
	        }
	        model.addAttribute("lot", lot);
	        // manually update the counts after new logs
	        Map<String, Long> lotCounts = repository.findAll()
	            .stream()
	            .collect(Collectors.groupingBy(ExitRecord::getLot, Collectors.counting()));
	        model.addAttribute("lotCounts", lotCounts);
	        return "logExit";
	    }

	    @GetMapping("/exitLogs")
	    public String exitLogs(Model model) {
	        model.addAttribute("records", repository.findAll());
	        return "success";
	    }
	    
	    @ModelAttribute("lotCounts")
	    public Map<String, Long> getLotCounts() {
	        return repository.findAll()
	                .stream()
	                .collect(Collectors.groupingBy(ExitRecord::getLot, Collectors.counting()));
	    }
}
