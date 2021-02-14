package org.sterl.jmsui.bl.common.helper;

import java.util.concurrent.TimeUnit;

public class StopWatch {

    private long start;
    private long end;
    
    public StopWatch start() {
        start = System.nanoTime();
        return this;
    }
    public StopWatch stop() {
        end = System.nanoTime();
        return this;
    }
    
    public long getTimeInMs() {
        return TimeUnit.NANOSECONDS.toMillis(end - start);
    }
}
