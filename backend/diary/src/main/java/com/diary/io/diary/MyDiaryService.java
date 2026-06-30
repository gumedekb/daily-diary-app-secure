package com.diary.io.diary;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.diary.io.user.User;
import com.diary.io.user.UserRepository;
import com.diary.io.dto.CreateDiaryRequest;
import com.diary.io.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MyDiaryService {

    private final MyDiaryRepository diaryRepository;
    private final UserRepository userRepository;

    public MyDiaryService(MyDiaryRepository diaryRepository, UserRepository userRep) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRep;
    }

    public MyDiary createEntry(CreateDiaryRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MyDiary diary = new MyDiary();
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setUser(user);

        return diaryRepository.save(diary);
    }

    public List<MyDiary> getAllEntries(Long userId) {
        return diaryRepository.findByUser_Id(userId);
    }

    public MyDiary updateEntry(Long id, CreateDiaryRequest request, Long userId) {
        MyDiary existing = getOwnedEntry(id, userId);

        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setUpdatedAt(LocalDateTime.now());

        return diaryRepository.save(existing);
    }

    /** Returns an entry only if it belongs to the given user (fixes IDOR on read). */
    public MyDiary getEntryById(Long id, Long userId) {
        return getOwnedEntry(id, userId);
    }

    /** Deletes an entry only if it belongs to the given user (fixes IDOR on delete). */
    public void deleteEntry(Long id, Long userId) {
        MyDiary existing = getOwnedEntry(id, userId);
        diaryRepository.delete(existing);
    }

    /**
     * Loads an entry and enforces ownership. Throws 404 if it does not exist and
     * 403 if it belongs to a different user.
     */
    private MyDiary getOwnedEntry(Long id, Long userId) {
        MyDiary existing = diaryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Diary entry not found"));

        if (!existing.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this diary entry");
        }
        return existing;
    }
}
