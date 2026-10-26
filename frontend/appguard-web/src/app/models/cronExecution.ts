
export interface CronExecution {
  id: number;
  startTime: string;           // ISO string
  status: string;
  exitCode?: number;
  output?: string;
  errorOutput?: string;
  durationMillis?: number;
  createdAt: string;           // ISO string
  cronJobId?: number;
  memoryUsedKB?: number;
  cpuUsagePercent?: number;
   cronJobName?: string; 
}
