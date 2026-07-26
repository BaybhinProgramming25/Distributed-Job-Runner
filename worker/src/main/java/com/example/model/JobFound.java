package com.example.model;

public record JobFound(
    String company,
    String ats,
    String jobId,
    String title,
    String location,
    String department,
    String url,
    String posted
) {
    public static JobFound of(String company, String ats, Object jobId, String title, String location, String department, String url, Object posted) {

        return new JobFound(
            company,
            ats,
            jobId == null ? "" : String.valueOf(jobId),
            title == null ? "" : title.strip(),
            location == null ? "" : location.strip(),
            department == null ? "" : department.strip(),
            url == null ? "" : url,
            posted == null ? "" : String.valueOf(posted)
        );
    }
}