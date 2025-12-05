package com.example.appquanlytimtro.utils;

import com.example.appquanlytimtro.BuildConfig;

public final class ImageUtils {

    private ImageUtils() {}

    public static String resolveImageUrl(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        String baseUrl = BuildConfig.BASE_URL != null ? BuildConfig.BASE_URL.trim() : "";
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith("/api")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
        }

        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }

        return baseUrl + trimmed;
    }
}

