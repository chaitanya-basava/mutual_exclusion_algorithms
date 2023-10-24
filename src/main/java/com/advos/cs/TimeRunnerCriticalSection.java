package com.advos.cs;

import com.advos.MutualExclusionTesting;
import org.apache.commons.math3.distribution.ExponentialDistribution;

public class TimeRunnerCriticalSection implements CriticalSection {
    private final ExponentialDistribution expDist;

    public TimeRunnerCriticalSection(double mean) {
        this.expDist = new ExponentialDistribution(mean);
    }

    @Override
    public void execute() {
        MutualExclusionTesting.sleep((int) this.expDist.sample());
    }
}
