package com.sustech.so_java_stats.repository.projection;

public interface TopicCooccurrenceProjection {
    String getTag1();

    String getTag2();

    Long getFrequency();
}