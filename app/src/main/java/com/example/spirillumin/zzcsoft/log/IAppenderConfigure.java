package com.example.spirillumin.zzcsoft.log;


import ch.qos.logback.core.Appender;

public interface IAppenderConfigure<T extends Appender> {
    Appender configure(T var1);

    String getName();
}
