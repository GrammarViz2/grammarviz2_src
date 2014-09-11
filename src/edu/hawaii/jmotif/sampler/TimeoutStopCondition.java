/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import java.util.Date;

/**
 * Simple stop condition checking process maximum duration.
 *
 * @author ytoh
 */
public class TimeoutStopCondition implements StopCondition {
    private long startDate;
    private long duration;
    private Date lastCheck;

    public TimeoutStopCondition(long duration) {
        this.duration  = duration;
        this.startDate = Long.MAX_VALUE;
    }

    public void start() {
        startDate = new Date().getTime();
    }

    public long getDuration() {
        return duration;
    }

    public Date getLastCheck() {
        return new Date(lastCheck.getTime());
    }

    public Date getStartDate() {
        return new Date(startDate);
    }

    public boolean isConditionMet() {
        lastCheck = new Date();
        return lastCheck.getTime() - startDate > duration;
    }
}
