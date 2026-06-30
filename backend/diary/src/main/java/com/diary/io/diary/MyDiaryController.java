package com.diary.io.diary;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.diary.io.dto.CreateDiaryRequest;
import com.diary.io.dto.DiaryResponse;
import com.diary.io.security.UserPrincipal;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/diary")
public class MyDiaryController {

    private final MyDiaryService diaryService;

    public MyDiaryController(MyDiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @PostMapping
    public ResponseEntity<DiaryResponse> createEntry(@Valid @RequestBody CreateDiaryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MyDiary created = diaryService.createEntry(request, currentUser.getUsername());
        return ResponseEntity.ok(DiaryResponse.from(created));
    }

    @GetMapping
    public ResponseEntity<List<DiaryResponse>> getAllForUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<DiaryResponse> entries = diaryService.getAllEntries(currentUser.getId())
                .stream().map(DiaryResponse::from).toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/entry/{id}")
    public ResponseEntity<DiaryResponse> getById(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MyDiary entry = diaryService.getEntryById(id, currentUser.getId());
        return ResponseEntity.ok(DiaryResponse.from(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        diaryService.deleteEntry(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaryResponse> updateEntry(
            @PathVariable Long id,
            @Valid @RequestBody CreateDiaryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MyDiary updatedEntry = diaryService.updateEntry(id, request, currentUser.getId());
        return ResponseEntity.ok(DiaryResponse.from(updatedEntry));
    }

}
