package com.sustech.so_java_stats.controller;

import com.sustech.so_java_stats.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "TagController", description = "Controller for tag-related operations.")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Check Tag Existence", description = "Checks if a specific tag exists in the collected database.")
    @GetMapping("/tags/check")
    public ResponseEntity<Boolean> checkTagExists(
            @Parameter(description = "The tag name to check", example = "spring-boot")
            @RequestParam String tagName
    ) {
        boolean exists = tagService.checkTagExists(tagName);
        return ResponseEntity.ok(exists);
    }
}
