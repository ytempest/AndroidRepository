package com.ytempest.test2.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.util.List;

/**
 * @author ytempest
 *         Description:
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobWakeUpService extends JobService {

    private int JobWakeUpId = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(JobWakeUpId, new ComponentName(this, JobWakeUpService.class));

        jobInfoBuilder.setPeriodic(1000);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.schedule(jobInfoBuilder.build());
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        // 开启定时任务，定时轮寻，看 MessageService 有没有被杀死
        // 如果 MessageService 被杀死则启动

        Log.e("TAG", "onStartJob:111111111111111111111 ");
        boolean isMessageServiceAlive = serviceAlive(MessageService.class.getName());
        if (!isMessageServiceAlive) {
            startService(new Intent(JobWakeUpService.this, MessageService.class));
        }

        return false;
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private boolean serviceAlive(String serviceName) {

        boolean isWork = false;

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> infos = manager.getRunningServices(100);

        if (infos.size() <= 0) {
            return false;
        }

        for (ActivityManager.RunningServiceInfo serviceInfo : infos) {
            String name = serviceInfo.service.getClassName().toString();
            if (name.equals(serviceName)) {
                isWork = true;
            }
        }

        return isWork;
    }
}
