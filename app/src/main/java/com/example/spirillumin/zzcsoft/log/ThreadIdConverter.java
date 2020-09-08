package com.example.spirillumin.zzcsoft.log;

import android.os.Process;


import com.example.spirillumin.zzcsoft.utils.StringUtil;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;


public class ThreadIdConverter extends ClassicConverter {
    public ThreadIdConverter() {
    }

    public String convert(ILoggingEvent event) {
        return StringUtil.nullStrToDefault(event.getMDCPropertyMap().get("tid"), String.valueOf(Process.myTid()));
    }
}
