package com.attendance.controller;

import com.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/upload")
    public ResponseEntity<Resource> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("paMinDuration") double paMinDuration,
            @RequestParam("paMaxDuration") double paMaxDuration,
            @RequestParam("ot1MinDuration") double ot1MinDuration,
            @RequestParam("ot1MaxDuration") double ot1MaxDuration,
            @RequestParam("ot2MinDuration") double ot2MinDuration,
            @RequestParam("excludedPaycodes") String excludedPaycodes) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Parse excluded PAYCODEs
        Set<String> excludedPaycodesSet = excludedPaycodes.isEmpty() ? Set.of() :
                Arrays.stream(excludedPaycodes.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());

        byte[] excelData =  attendanceService.processFile(file, paMinDuration, paMaxDuration,
                        ot1MinDuration, ot1MaxDuration, ot2MinDuration, excludedPaycodes);

                //attendanceService.generateExcelFile(
                //attendanceService.processExcelFile(file, paMinDuration, paMaxDuration,
                        //ot1MinDuration, ot1MaxDuration, ot2MinDuration, excludedPaycodesSet));

        ByteArrayResource resource = new ByteArrayResource(excelData);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed_attendance.xlsx");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelData.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}