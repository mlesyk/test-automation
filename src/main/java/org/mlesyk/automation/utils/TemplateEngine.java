package org.mlesyk.automation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEngine {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([A-Z_]+)\\}\\}");

    public static String loadTemplate(String templatePath) {
        try (InputStream inputStream = TemplateEngine.class.getClassLoader().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Template not found: " + templatePath);
            }

            byte[] bytes = inputStream.readAllBytes();
            String template = new String(bytes, StandardCharsets.UTF_8);

            LoggerUtil.debug("Loaded template: {}", templatePath);
            return template;

        } catch (IOException e) {
            LoggerUtil.error("Failed to load template: {}", templatePath, e);
            throw new RuntimeException("Template loading failed", e);
        }
    }

    public static String processTemplate(String template, Map<String, String> variables) {
        String result = template;

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            String placeholder = matcher.group(0); // {{VARIABLE_NAME}}
            String variableName = matcher.group(1); // VARIABLE_NAME

            String value = variables.getOrDefault(variableName, "");
            result = result.replace(placeholder, value);
        }

        LoggerUtil.debug("Processed template with {} variables", variables.size());
        return result;
    }

    public static String loadAndProcessTemplate(String templatePath, Map<String, String> variables) {
        String template = loadTemplate(templatePath);
        return processTemplate(template, variables);
    }

    // Helper method to safely format numbers
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }

    public static String formatDecimal(double number, int decimalPlaces) {
        return String.format("%." + decimalPlaces + "f", number);
    }

    // Helper method to escape HTML content
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}