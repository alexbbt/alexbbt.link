package com.hna.webserver.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateShortLinkRequest {

    @NotBlank(message = "URL is required")
    private String url;

    private String slug;

    public CreateShortLinkRequest() {
    }

    public CreateShortLinkRequest(String url, String slug) {
        this.url = url;
        this.slug = slug;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
