package org.esupportail.sgc.tools;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.util.StopWatch;

import java.time.Duration;

public class PrettyStopWatch extends StopWatch {

    @Override
    public String shortSummary() {
        String timeInMMSS = getTimeInMMSS();
        return "StopWatch '" + getId() + "': running time = " + timeInMMSS + "";
    }

    public String getTimeInMMSS() {
        return DurationFormatUtils.formatDuration(getTotalTimeMillis(), "mm 'min' ss 'sec' SS 'ms'", false);
    }

    @Override
    public void start(String taskName) throws IllegalStateException {
        if (this.isRunning()) {
            super.stop();
        }
        super.start(taskName);
    }

}
