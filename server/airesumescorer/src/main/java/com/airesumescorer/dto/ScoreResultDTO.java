package com.airesumescorer.dto;

import java.util.List;
import java.util.Map;

public class ScoreResultDTO {
    private int score;
    private List<String> improvements;
    private Map<String, CategoryDTO> sections;

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<String> getImprovements() { return improvements; }
    public void setImprovements(List<String> improvements) { this.improvements = improvements; }

    public Map<String, CategoryDTO> getSections() { return sections; }
    public void setSections(Map<String, CategoryDTO> sections) { this.sections = sections; }
}
