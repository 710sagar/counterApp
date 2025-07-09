package com.tsc.controller;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.tsc.model.Entry;
import com.tsc.model.EntryType;
import com.tsc.repository.EntryRepository;

@Controller
public class EntryController {

	@Autowired
	private EntryRepository entryRepository;

	private static final List<String> ALL_GATES = Arrays.asList("Lot A", "Lot B", "Lot C", "Lot D", "SNP", "Family Area", "Uber/Taxi");

	@GetMapping("/")
	public String index(Model model, HttpServletRequest request) {
		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		// Get counts by gate and entry type
		List<Object[]> counts = entryRepository.countByGateAndTypeForDay(startOfDay, endOfDay);

		// Organize counts by gate and entry type
		Map<String, Map<EntryType, Long>> countMap = new HashMap<>();
		for (String gate : ALL_GATES) {
			countMap.put(gate, new HashMap<>());
			for (EntryType type : getAvailableEntryTypes(gate)) {
				countMap.get(gate).put(type, 0L);
			}
		}

		// Populate actual counts
		for (Object[] row : counts) {
			String gate = (String) row[0];
			EntryType entryType = (EntryType) row[1];
			Long count = (Long) row[2];
			if (countMap.containsKey(gate)) {
				countMap.get(gate).put(entryType, count);
			}
		}

		String selectedLot = getSelectedLot(request);
		model.addAttribute("selectedLot", selectedLot);
		model.addAttribute("allGates", ALL_GATES);

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean isAdmin = userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("username", userDetails.getUsername());
		model.addAttribute("role", userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority().replace("ROLE_", ""))
				.findFirst()
				.orElse("USER"));

		model.addAttribute("countMap", countMap);

		if (selectedLot != null) {
			model.addAttribute("availableEntryTypes", getAvailableEntryTypes(selectedLot));
		}

		return "index";
	}

	@GetMapping("/api/user")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getUserInfo() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String role = userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority().replace("ROLE_", ""))
				.findFirst()
				.orElse("USER");
		boolean isAdmin = userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("username", userDetails.getUsername());
		userInfo.put("role", role);
		userInfo.put("isAdmin", isAdmin);
		return ResponseEntity.ok(userInfo);
	}

	@PostMapping("/api/setLot")
	@ResponseBody
	public ResponseEntity<String> setLot(@RequestBody Map<String, String> payload, HttpServletResponse response) {
		String lot = payload.get("lot");
		if (lot == null || !ALL_GATES.contains(lot)) {
			return ResponseEntity.badRequest().body("Invalid lot");
		}
		String encodedLot = URLEncoder.encode(lot, StandardCharsets.UTF_8);
		Cookie cookie = new Cookie("selectedLot", encodedLot);
		cookie.setPath("/");
		cookie.setMaxAge(24 * 60 * 60); // 24 hours
		response.addCookie(cookie);
		return ResponseEntity.ok("Lot set successfully");
	}

	@PostMapping("/api/addEntry")
	@ResponseBody
	public ResponseEntity<String> addEntry(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
		String lot = getSelectedLot(request);
		if (lot == null) {
			return ResponseEntity.badRequest().body("No lot selected");
		}

		String entryTypeStr = (String) payload.get("entryType");
		if (entryTypeStr == null) {
			return ResponseEntity.badRequest().body("Entry type is required");
		}

		EntryType entryType;
		try {
			entryType = EntryType.valueOf(entryTypeStr);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body("Invalid entry type");
		}

		// Validate entry type is available for the selected lot
		if (!getAvailableEntryTypes(lot).contains(entryType)) {
			return ResponseEntity.badRequest().body("Entry type not available for this lot");
		}

		Integer count = (Integer) payload.get("count");
		if (count == null || count <= 0) {
			return ResponseEntity.badRequest().body("Invalid count");
		}

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		for (int i = 0; i < count; i++) {
			Entry entry = new Entry(lot, entryType, LocalDateTime.now(), username);
			entryRepository.save(entry);
		}

		return ResponseEntity.ok("Entries added successfully");
	}

	@GetMapping("/api/entries")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getEntries(@RequestParam String lot) {
		if (!ALL_GATES.contains(lot)) {
			return ResponseEntity.badRequest().body(null);
		}

		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		Map<String, Object> response = new HashMap<>();
		response.put("lot", lot);

		// Get counts by entry type for this lot
		Map<EntryType, Long> typeCounts = new HashMap<>();
		for (EntryType type : getAvailableEntryTypes(lot)) {
			Long count = entryRepository.countByGateAndTypeForDay(lot, type, startOfDay, endOfDay);
			typeCounts.put(type, count != null ? count : 0L);
		}

		response.put("typeCounts", typeCounts);
		response.put("totalCount", typeCounts.values().stream().mapToLong(Long::longValue).sum());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/entries/all")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getAllEntries() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean isAdmin = userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

		if (!isAdmin) {
			return ResponseEntity.status(403).body(null);
		}

		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		List<Object[]> counts = entryRepository.countByGateAndTypeForDay(startOfDay, endOfDay);

		Map<String, Map<EntryType, Long>> allCounts = new HashMap<>();
		for (String gate : ALL_GATES) {
			allCounts.put(gate, new HashMap<>());
			for (EntryType type : getAvailableEntryTypes(gate)) {
				allCounts.get(gate).put(type, 0L);
			}
		}

		for (Object[] row : counts) {
			String gate = (String) row[0];
			EntryType entryType = (EntryType) row[1];
			Long count = (Long) row[2];
			if (allCounts.containsKey(gate)) {
				allCounts.get(gate).put(entryType, count);
			}
		}

		Map<String, Object> response = new HashMap<>();
		response.put("allCounts", allCounts);
		response.put("gates", ALL_GATES);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/reset")
	@ResponseBody
	public ResponseEntity<String> resetAllEntries() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean isAdmin = userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

		if (!isAdmin) {
			return ResponseEntity.status(403).body("Unauthorized");
		}

		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		// Soft delete instead of hard delete
		int deletedCount = entryRepository.softDeleteEntriesForDay(startOfDay, endOfDay, userDetails.getUsername(), LocalDateTime.now());

		return ResponseEntity.ok("Successfully reset " + deletedCount + " entries for today");
	}

	@PostMapping("/api/undoLastEntry")
	@ResponseBody
	public ResponseEntity<String> undoLastEntry() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		List<Entry> lastEntries = entryRepository.findLastEntryByUserForDay(username, startOfDay, endOfDay);

		if (lastEntries.isEmpty()) {
			return ResponseEntity.badRequest().body("No entries found to undo");
		}

		Entry lastEntry = lastEntries.get(0);
		lastEntry.setDeleted(true);
		lastEntry.setDeletedBy(username);
		lastEntry.setDeletedAt(LocalDateTime.now());
		entryRepository.save(lastEntry);

		return ResponseEntity.ok("Last entry undone successfully");
	}

	@GetMapping("/api/availableEntryTypes")
	@ResponseBody
	public ResponseEntity<List<EntryType>> getAvailableEntryTypesForLot(@RequestParam String lot) {
		if (!ALL_GATES.contains(lot)) {
			return ResponseEntity.badRequest().body(null);
		}

		return ResponseEntity.ok(getAvailableEntryTypes(lot));
	}

	private List<EntryType> getAvailableEntryTypes(String lot) {
		switch (lot) {
			case "Lot A":
			case "Lot B":
			case "Lot C":
			case "Lot D":
				return Arrays.asList(EntryType.STANDARD, EntryType.OUT_OF_PROVINCE, EntryType.USA,
						EntryType.NON_REGISTERED, EntryType.FAMILY_AREA, EntryType.SNP);
			case "SNP":
				return Arrays.asList(EntryType.STANDARD, EntryType.OUT_OF_PROVINCE, EntryType.USA,
						EntryType.NON_REGISTERED, EntryType.FAMILY_AREA, EntryType.NORMAL_LOT);
			case "Family Area":
				return Arrays.asList(EntryType.STANDARD, EntryType.OUT_OF_PROVINCE, EntryType.USA,
						EntryType.NON_REGISTERED, EntryType.SNP, EntryType.NORMAL_LOT);
			case "Uber/Taxi":
				return Arrays.asList(EntryType.STANDARD, EntryType.NORMAL_LOT);
			default:
				return new ArrayList<>();
		}
	}

	private String getSelectedLot(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("selectedLot".equals(cookie.getName())) {
					return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
				}
			}
		}
		return null;
	}
}