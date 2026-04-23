package com.springbooters.recruitment.model;

/** Identifies whether a posting is internal or external. */
public enum ChannelType {
    EXTERNAL,
    INTERNAL;

    public static ChannelType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Channel type is required");
        }
        try {
            return ChannelType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid channel type: " + value, ex);
        }
    }
}
