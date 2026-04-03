package com.sangeng.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTools {

    @Tool(description = "获取当前的北京时间")
    public String getCurrentTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return zonedDateTime.format(formatter);
    }

    @Tool(description = "通过时区id获取当前时间")
    public String getTimeByZoneId(
            @ToolParam(description = "时区id, 比如 Asia/Shanghai") String zoneId
    ) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(zoneId));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return zonedDateTime.format(formatter);
    }

    public static void main(String[] args) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(zonedDateTime.format(formatter));
        ;
    }


}