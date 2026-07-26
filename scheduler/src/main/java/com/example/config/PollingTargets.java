package com.example.config;
 
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
 
import java.util.ArrayList;
import java.util.List;
 
@Component
@ConfigurationProperties(prefix = "ats")
public class PollingTargets {
 
    private List<Target> targets = new ArrayList<>();
 
    public List<Target> getTargets() { return targets; }
    public void setTargets(List<Target> targets) { this.targets = targets; }
 
    public static class Target {
        private String ats;
        private String slug = "";
 
        public String getAts() { return ats; }
        public void setAts(String ats) { this.ats = ats; }
 
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
 
        @Override
        public String toString() { return ats + "/" + slug; }
    }
}
