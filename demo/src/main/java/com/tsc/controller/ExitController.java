package com.tsc.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

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

		// Get selected lot from cookie
		String selectedLot = getSelectedLot(request);
		logger.info("Selected lot from cookie: {}", selectedLot);
		model.addAttribute("selectedLot", selectedLot);
		model.addAttribute("allGates", ALL_GATES);

		// Get exit counts using optimized queries
		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		if (isAdmin) {
			// Admin sees all lot counts using optimized query
			Map<String, Long> allLotCounts = new HashMap<>();
			// Initialize with zeros
			allLotCounts.put("A", 0L);
			allLotCounts.put("B", 0L);
			allLotCounts.put("C", 0L);
			allLotCounts.put("D", 0L);
			allLotCounts.put("SNP", 0L);
			allLotCounts.put("Family", 0L);
			allLotCounts.put("Uber/Taxi", 0L);

			// Get actual counts
			List<Object[]> counts = repository.countAllByLotForDay(startOfDay, endOfDay);
			for (Object[] row : counts) {
				String lot = (String) row[0];
				Long count = (Long) row[1];
				allLotCounts.put(lot, count);
			}
			model.addAttribute("allLotCounts", allLotCounts);
		} else if (selectedLot != null) {
			// Regular user sees only their selected lot count
			String exitLotName = convertLotNameForExit(selectedLot);
			Long selectedLotCount = repository.countByLotForDay(exitLotName, startOfDay, endOfDay);
			model.addAttribute("selectedLotCount", selectedLotCount != null ? selectedLotCount : 0L);
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

		// Use the selected lot from cookie
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

		// Use selected lot from cookie
		String selectedLot = getSelectedLot(request);
		if (selectedLot == null) {
			logger.warn("No lot selected from cookie for exit logging");
			return "redirect:/exitIndex";
		}

		logger.info("Logging {} exits for lot: {}", count, selectedLot);

		// Convert lot name to match the exit record format
		String exitLotName = convertLotNameForExit(selectedLot);

		// Save exits
		for (int i = 0; i < count; i++) {
			repository.save(new ExitRecord(exitLotName));
		}

		model.addAttribute("lot", selectedLot);
		model.addAttribute("selectedLot", selectedLot);

		// Get updated counts using optimized query
		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		Map<String, Long> lotCounts = new HashMap<>();
		// Initialize with zeros
		lotCounts.put("A", 0L);
		lotCounts.put("B", 0L);
		lotCounts.put("C", 0L);
		lotCounts.put("D", 0L);
		lotCounts.put("SNP", 0L);
		lotCounts.put("Family", 0L);
		lotCounts.put("Uber/Taxi", 0L);

		// Get actual counts
		List<Object[]> counts = repository.countAllByLotForDay(startOfDay, endOfDay);
		for (Object[] row : counts) {
			String lotName = (String) row[0];
			Long lotCount = (Long) row[1];
			lotCounts.put(lotName, lotCount);
		}

		model.addAttribute("lotCounts", lotCounts);

		return "logExit";
	}

	@PostMapping("/api/logExit")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> logExitApi(@RequestParam(defaultValue = "1") int count,
														  HttpServletRequest request) {

		// Get user info
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		// Use selected lot from cookie
		String selectedLot = getSelectedLot(request);
		if (selectedLot == null) {
			logger.warn("No lot selected from cookie for exit logging");
			return ResponseEntity.badRequest().body(Map.of("error", "No lot selected"));
		}

		if (count <= 0 || count > 50) {
			logger.error("Invalid count: {}", count);
			return ResponseEntity.badRequest().body(Map.of("error", "Invalid count"));
		}

		logger.info("Logging {} exits for lot: {} by user: {}", count, selectedLot, username);

		try {
			// Convert lot name to match the exit record format
			String exitLotName = convertLotNameForExit(selectedLot);

			// Save all exits in batch for better performance
			List<ExitRecord> exitRecords = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				exitRecords.add(new ExitRecord(exitLotName));
			}
			repository.saveAll(exitRecords);

			// Get updated count for this lot
			LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
			LocalDateTime endOfDay = startOfDay.plusDays(1);
			Long updatedCount = repository.countByLotForDay(exitLotName, startOfDay, endOfDay);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Exits logged successfully");
			response.put("lot", selectedLot);
			response.put("count", count);
			response.put("totalCount", updatedCount != null ? updatedCount : 0);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("Failed to log exits: {}", e.getMessage());
			return ResponseEntity.status(500).body(Map.of("error", "Failed to log exits: " + e.getMessage()));
		}
	}

	@GetMapping("/api/exitCounts")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getExitCounts(HttpServletRequest request) {
		try {
			LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
			LocalDateTime endOfDay = startOfDay.plusDays(1);

			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			boolean isAdmin = userDetails.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

			Map<String, Object> response = new HashMap<>();

			if (isAdmin) {
				// Admin gets all lot counts
				Map<String, Long> allLotCounts = new HashMap<>();
				// Initialize with zeros
				allLotCounts.put("A", 0L);
				allLotCounts.put("B", 0L);
				allLotCounts.put("C", 0L);
				allLotCounts.put("D", 0L);
				allLotCounts.put("SNP", 0L);
				allLotCounts.put("Family", 0L);
				allLotCounts.put("Uber/Taxi", 0L);

				// Get actual counts
				List<Object[]> counts = repository.countAllByLotForDay(startOfDay, endOfDay);
				for (Object[] row : counts) {
					String lot = (String) row[0];
					Long count = (Long) row[1];
					allLotCounts.put(lot, count);
				}
				response.put("allLotCounts", allLotCounts);
				response.put("isAdmin", true);
			} else {
				// Regular user gets only their selected lot count
				String selectedLot = getSelectedLot(request);
				if (selectedLot != null) {
					String exitLotName = convertLotNameForExit(selectedLot);
					Long count = repository.countByLotForDay(exitLotName, startOfDay, endOfDay);
					response.put("selectedLotCount", count != null ? count : 0);
					response.put("selectedLot", selectedLot);
				}
				response.put("isAdmin", false);
			}

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("Failed to get exit counts: {}", e.getMessage());
			return ResponseEntity.status(500).body(Map.of("error", "Failed to get exit counts"));
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

		// Get only today's records using optimized query
		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		model.addAttribute("records", repository.findExitsForDay(startOfDay, endOfDay));

		return "success";
	}

	/**
	 * Get selected lot from cookie
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
	 * Convert lot name from entry format to exit format
	 */
	private String convertLotNameForExit(String lotName) {
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