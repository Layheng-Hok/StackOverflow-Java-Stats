package com.sustech.so_java_stats.repository.projection;

public interface TopicTrendProjection {

    String getTopic();

    String getDatePoint();

    Long getCountVal();
}