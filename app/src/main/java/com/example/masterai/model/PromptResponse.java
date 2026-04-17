package com.example.masterai.model;

public class PromptResponse {
    public String status;
    public String original_prompt;
    public String enhanced_prompt;

    public String error;

    public PromptResponse(String status, String original_prompt, String enhanced_prompt, String error) {
        this.status = status;
        this.original_prompt = original_prompt;
        this.enhanced_prompt = enhanced_prompt;
        this.error = error;
    }


    public PromptResponse(String status, String enhanced_prompt, String error) {
        this.status = status;
        this.enhanced_prompt = enhanced_prompt;
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnhanced_prompt() {
        return enhanced_prompt;
    }

    public void setEnhanced_prompt(String enhanced_prompt) {
        this.enhanced_prompt = enhanced_prompt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}