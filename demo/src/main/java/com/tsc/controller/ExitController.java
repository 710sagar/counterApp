package com.tsc.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

	private static final Logger logger = LoggerFactory.getLogger(ExitController.class);
	private final ExitRecordInterface repository;
	private static final List<String> ALL_GATES = Arrays.asList("Lot A", "Lot B", "Lot C", "Lot D", "SNP", "Family Area", "Uber/Taxi");

	public ExitController(ExitRecordInterface repository) {
		this.repository = repository;
	}

	@GetMapping("/exitIndex")
	public String index(Model model, HttpServletRequest request) {
		logger.info("Accessing exit index page");

		// Get user info
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean isAdmin = userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("username", userDetails.getUsername());
		model.addAttribute("role", userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority().replace("ROLE_", ""))
				.findFirst()
				.orElse("USER"));

		// Get selected lot from cookie (same as entry module)
		String selectedLot = getSelectedLot(request);
		logger.info("Selected lot from cookie: {}", selectedLot);
		model.addAttribute("selectedLot", selectedLot);
		model.addAttribute("allGates", ALL_GATES);

		// Get exit counts - all for admin, selected lot only for regular users
		if (isAdmin) {
			// Admin sees all lot counts
			Map<String, Long> allLotCounts = repository.findAll()
					.stream()
					.collect(Collectors.groupingBy(ExitRecord::getLot, Collectors.counting()));
			model.addAttribute("allLotCounts", allLotCounts);
		} else if (selectedLot != null) {
			// Regular user sees only their selected lot count
			String exitLotName = convertLotNameForExit(selectedLot);
			Long selectedLotCount = repository.findAll()
					.stream()
					.filter(record -> exitLotName.equals(record.getLot()))
					.count();
			model.addAttribute("selectedLotCount", selectedLotCount);
		}

		return "indexExit";
	}

	@PostMapping("/confirmLot")
	public String confirmLot(@RequestParam String lot, Model model, HttpServletRequest request) {
		logger.info("Confirming lot selection: {}", lot);

		// Get user info
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("username", userDetails.getUsername());
		model.addAttribute("role", userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority().replace("ROLE_", ""))
				.findFirst()
				.orElse("USER"));

		// Use the selected lot from cookie, not from form parameter
		String selectedLot = getSelectedLot(request);
		if (selectedLot == null) {
			logger.warn("No lot selected from cookie, redirecting to exit index");
			return "redirect:/exitIndex";
		}

		model.addAttribute("lot", selectedLot);
		model.addAttribute("selectedLot", selectedLot);

		return "logExit";
	}

	@PostMapping("/exit")
	public String logExit(@RequestParam(required = false) String lot,
						  @RequestParam(defaultValue = "1") int count,
						  Model model, HttpServletRequest request) {

		// Get user info
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("username", userDetails.getUsername());
		model.addAttribute("role", userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority().replace("ROLE_", ""))
				.findFirst()
				.orElse("USER"));

		// Use selected lot from cookie instead of form parameter
		String selectedLot = getSelectedLot(request);
		if (selectedLot == null) {
			logger.warn("No lot selected from cookie for exit logging");
			return "redirect:/exitIndex";
		}

		logger.info("Logging {} exits for lot: {}", count, selectedLot);

		// Convert lot name to match the exit record format if needed
		String exitLotName = convertLotNameForExit(selectedLot);

		for (int i = 0; i < count; i++) {
			repository.save(new ExitRecord(exitLotName));
		}

		model.addAttribute("lot", selectedLot);
		model.addAttribute("selectedLot", selectedLot);

		// Manually update the counts after new logs
		Map<String, Long> lotCounts = repository.findAll()
				.stream()
				.collect(Collectors.groupingBy(ExitRecord::getLot, Collectors.counting()));
		model.addAttribute("lotCounts", lotCounts);

		return "logExit";
	}

	@PostMapping("/api/logExit")
	@ResponseBody
	public ResponseEntity<String> logExitApi(@RequestParam(defaultValue = "1") int count,
											 HttpServletRequest request) {

		// Get user info
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		// Use selected lot from cookie
		String selectedLot = getSelectedLot(request);
		if (selectedLot == null) {
			logger.warn("No lot selected from cookie for exit logging");
			return ResponseEntity.badRequest().body("No lot selected");
		}

		if (count <= 0) {
			logger.error("Invalid count: {}", count);
			return ResponseEntity.badRequest().body("Invalid count");
		}

		logger.info("Logging {} exits for lot: {} by user: {}", count, selectedLot, username);

		try {
			// Convert lot name to match the exit record format if needed
			String exitLotName = convertLotNameForExit(selectedLot);

			for (int i = 0; i < count; i++) {
				repository.save(new ExitRecord(exitLotName));
			}

			return ResponseEntity.ok("Exits logged successfully");
		} catch (Exception e) {
			logger.error("Failed to log exits: {}", e.getMessage());
			return ResponseEntity.status(500).body("Failed to log exits: " + e.getMessage());
		}
	}

	@GetMapping("/exitLogs")
	public String exitLogs(Model model, HttpServletRequest request) {
		logger.info("Accessing exit logs page");

		// Get user info
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("username", userDetails.getUsername());
		model.addAttribute("role", userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority().replace("ROLE_", ""))
				.findFirst()
				.orElse("USER"));

		// Get selected lot from cookie
		String selectedLot = getSelectedLot(request);
		model.addAttribute("selectedLot", selectedLot);

		model.addAttribute("records", repository.findAll());
		return "success";
	}

	@ModelAttribute("lotCounts")
	public Map<String, Long> getLotCounts() {
		return repository.findAll()
				.stream()
				.collect(Collectors.groupingBy(ExitRecord::getLot, Collectors.counting()));
	}

	/**
	 * Get selected lot from cookie (same method as EntryController)
	 */
	private String getSelectedLot(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("selectedLot".equals(cookie.getName())) {
					String lot = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
					logger.info("Selected lot from cookie: {}", lot);
					return lot;
				}
			}
		}
		logger.warn("No selectedLot cookie found");
		return null;
	}

	/**
	 * Convert lot name from entry format to exit format if needed
	 */
	private String convertLotNameForExit(String lotName) {
		// Map the lot names to match your existing exit record format
		switch (lotName) {
			case "Lot A": return "A";
			case "Lot B": return "B";
			case "Lot C": return "C";
			case "Lot D": return "D";
			case "SNP": return "SNP";
			case "Family Area": return "Family";
			case "Uber/Taxi": return "Uber/Taxi";
			default: return lotName;
		}
	}
}