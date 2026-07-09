package dev.jacksonfishburn.lubelog.controller;

import dev.jacksonfishburn.lubelog.service.VinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jacksonfishburn.lubelog.dto.vin.VinDecodeResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vin")
@RequiredArgsConstructor
public class VinController {

    private final VinService vinService;

    @GetMapping("/{vin}")
    public ResponseEntity<VinDecodeResponse> decodeVin(@PathVariable String vin) {
        return ResponseEntity.ok(vinService.decodeVin(vin));
    }
}
