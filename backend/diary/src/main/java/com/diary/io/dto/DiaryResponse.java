package com.diary.io.dto;

import java.time.LocalDateTime;

import com.diary.io.diary.MyDiary;

/**
 * Output DTO for diary entries. Deliberately excludes the owning {@code User}
 * object so that no user data (and certainly no password hash) is ever exposed
 * through the diary API.
 */
public class DiaryResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DiaryResponse() {}

    public static DiaryResponse from(MyDiary diary) {
        DiaryResponse dto = new DiaryResponse();
        dto.id = diary.getId();
        dto.title = diary.getTitle();
        dto.content = diary.getContent();
        dto.createdAt = diary.getCreatedAt();
        dto.updatedAt = diary.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
