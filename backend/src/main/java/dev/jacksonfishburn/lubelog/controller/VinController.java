package dev.jacksonfishburn.lubelog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jacksonfishburn.lubelog.client.VinClient;
import dev.jacksonfishburn.lubelog.dto.VinDecodeResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vin")
@RequiredArgsConstructor
public class VinController {

    private final VinClient vinClient;

    @GetMapping("/{vin}")
    public ResponseEntity<VinDecodeResponse> decodeVin(@PathVariable String vin) {
        return ResponseEntity.ok(vinClient.decodeVin(vin));
    }
}
