package com.shareyourproxy.api.jobs;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

/**
 * Created by Evan on 5/13/15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class UploadDataService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
