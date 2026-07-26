package com.example.helpers;

import com.example.model.JobFound;
import java.util.List;
 

public final class CsJobFilter {
 
    private static final List<String> DEPARTMENT_HINTS = List.of(
            "engineering", "software", "technology", "technical", "data",
            "infrastructure", "platform", "security", "machine learning", "ml",
            "ai", "research", "developer", "devops", "sre",
            "product engineering", "it");
 
    private static final List<String> TITLE_KEYWORDS = List.of(
            "software engineer", "software developer", "swe", "backend",
            "back-end", "back end", "frontend", "front-end", "front end",
            "full stack", "fullstack", "full-stack", "web developer",
            "developer", "programmer",
            "devops", "sre", "site reliability", "platform engineer",
            "infrastructure", "cloud engineer", "systems engineer",
            "distributed systems",
            "data engineer", "data scientist", "analytics engineer", "database",
            "machine learning", "ml engineer", "mlops", "ai engineer",
            "deep learning", "computer vision", "nlp", "natural language",
            "research engineer", "research scientist", "applied scientist",
            "security engineer", "appsec", "application security",
            "cryptography",
            "mobile engineer", "ios engineer", "android engineer",
            "ios developer", "android developer", "embedded", "firmware",
            "compiler", "kernel",
            "qa engineer", "test engineer", "sdet", "automation engineer",
            "solutions architect", "software architect", "api engineer",
            "game engineer", "graphics engineer", "robotics software");
 
    /** Checked first; a hit here rejects the job no matter what else matches. */
    private static final List<String> TITLE_BLOCKLIST = List.of(
            "sales engineer", "solutions engineer", "customer engineer",
            "support engineer", "field engineer", "implementation engineer",
            "mechanical engineer", "electrical engineer", "civil engineer",
            "chemical engineer", "process engineer", "industrial engineer",
            "hardware engineer", "manufacturing", "facilities",
            "recruiter", "sourcer", "talent", "account executive",
            "account manager", "sales", "marketing", "customer success",
            "technical writer", "program manager", "project manager",
            "product manager", "engineering manager",
            "director of engineering", "head of engineering",
            "vp of engineering", "chief");
 
    private static final List<String> SOFT_TECH_WORDS = List.of(
            "engineer", "developer", "scientist", "architect", "programmer",
            "technical", "software", "data", "security");
 
    // Optional knobs — empty list means "don't filter on this".
    private static final List<String> SENIORITY_REQUIRED = List.of();
    private static final List<String> SENIORITY_EXCLUDED = List.of();
    private static final List<String> LOCATION_REQUIRED  = List.of();
 
    private CsJobFilter() {}
 
    public static boolean isCsJob(JobFound j) {
        String title = j.title().toLowerCase();
        String dept  = j.department().toLowerCase();
        String loc   = j.location().toLowerCase();
 
        for (String bad : TITLE_BLOCKLIST) {
            if (title.contains(bad)) return false;
        }
 
        boolean deptHit  = !dept.isEmpty() && containsAny(dept, DEPARTMENT_HINTS);
        boolean titleHit = containsAny(title, TITLE_KEYWORDS);
 
        if (!deptHit && !titleHit) return false;
 
        if (deptHit && !titleHit && !containsAny(title, SOFT_TECH_WORDS)) {
            return false;
        }
 
        if (!SENIORITY_REQUIRED.isEmpty() && !containsAny(title, SENIORITY_REQUIRED)) return false;
        if (!SENIORITY_EXCLUDED.isEmpty() &&  containsAny(title, SENIORITY_EXCLUDED)) return false;
        if (!LOCATION_REQUIRED.isEmpty()  && !containsAny(loc, LOCATION_REQUIRED))    return false;
 
        return true;
    }
 
    private static boolean containsAny(String haystack, List<String> needles) {
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }
}
