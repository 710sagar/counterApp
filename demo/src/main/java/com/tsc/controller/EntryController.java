package com.tsc.controller;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tsc.model.Entry;
import com.tsc.repository.EntryRepository;

@Controller
public class EntryController {

	@Autowired
	private EntryRepository entryRepository;

	@GetMapping("/")
	public String index(Model model, HttpServletRequest request) {
		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		List<Object[]> counts = entryRepository.countByGateForDay(startOfDay, endOfDay);
		Map<String, Long> countMap = new HashMap<>();
		for (Object[] row : counts) {
			countMap.put((String) row[0], (Long) row[1]);
		}
		List<String> allGates = Arrays.asList("Lot A", "Lot B", "Lot C", "Lot D", "SNP", "Family Area");
		model.addAttribute("allGates", allGates);

		String selectedLot = getSelectedLot(request);
		model.addAttribute("selectedLot", selectedLot);

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


		if (!isAdmin && selectedLot != null) {
			Long selectedCount = countMap.getOrDefault(selectedLot, 0L);
			model.addAttribute("selectedCount", selectedCount);
		}

		return "index";
	}

	@GetMapping("/api/user")
	@ResponseBody
	public ResponseEntity<Map<String, String>> getUserInfo() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String role = userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority().replace("ROLE_", ""))
				.findFirst()
				.orElse("USER");
		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("username", userDetails.getUsername());
		userInfo.put("role", role);
		return ResponseEntity.ok(userInfo);
	}

	@PostMapping("/api/setLot")
	@ResponseBody
	public ResponseEntity<String> setLot(@RequestBody Map<String, String> payload, HttpServletResponse response) {
		String lot = payload.get("lot");
		if (lot == null || !Arrays.asList("Lot A", "Lot B", "Lot C", "Lot D", "SNP", "Family Area").contains(lot)) {
			return ResponseEntity.badRequest().body("Invalid lot");
		}
		String encodedLot = URLEncoder.encode(lot, StandardCharsets.UTF_8);
		Cookie cookie = new Cookie("selectedLot", encodedLot);
		cookie.setPath("/");
		cookie.setMaxAge(24 * 60 * 60);
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
		Integer count = (Integer) payload.get("count");
		if (count == null || count <= 0) {
			return ResponseEntity.badRequest().body("Invalid count");
		}
		for (int i = 0; i < count; i++) {
			Entry entry = new Entry(lot, LocalDateTime.now());
			entryRepository.save(entry);
		}
		return ResponseEntity.ok("Entries added successfully");
	}

	@GetMapping("/api/entries")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getEntries(@RequestParam String lot) {
		if (!Arrays.asList("Lot A", "Lot B", "Lot C", "Lot D", "SNP", "Family Area").contains(lot)) {
			return ResponseEntity.badRequest().body(null);
		}
		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		Long count = entryRepository.countByGateForDay(lot, startOfDay, endOfDay);
		Map<String, Object> response = new HashMap<>();
		response.put("lot", lot);
		response.put("count", count != null ? count : 0L);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/entries/all")
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getAllEntries() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean isAdmin = userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
		if (!isAdmin) {
			return ResponseEntity.status(403).body(null);
		}
		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		List<Object[]> counts = entryRepository.countByGateForDay(startOfDay, endOfDay);
		List<Map<String, Object>> response = Arrays.asList("Lot A", "Lot B", "Lot C", "Lot D", "SNP", "Family Area")
				.stream()
				.map(lot -> {
					Map<String, Object> entry = new HashMap<>();
					entry.put("lot", lot);
					entry.put("count", counts.stream()
							.filter(row -> row[0].equals(lot))
							.map(row -> (Long) row[1])
							.findFirst()
							.orElse(0L));
					return entry;
				})
				.toList();
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
		entryRepository.deleteAll(entryRepository.findAll().stream()
				.filter(e -> e.getTimestamp().isAfter(startOfDay) && e.getTimestamp().isBefore(endOfDay))
				.toList());
		return ResponseEntity.ok("Today's entries reset");
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