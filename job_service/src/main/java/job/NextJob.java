package job;

public record NextJob(
    UUID nextJobId,
    TimeStamp nextRunTime,
    UUID jobId
) {}