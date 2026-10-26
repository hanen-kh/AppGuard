// report-data.model.ts
export interface FailedExecutionDto {
  cronJobName: string;
  serverName: string;
  startTime: string; // ISO date string
  status: 'FAILURE' | 'TIMEOUT' | 'AGENT_UNREACHABLE' | string;
  errorOutput: string;
  exitCode: number;
  durationMillis: number;
  memoryUsedKB: number;
  cpuUsagePercent: number;
}

export interface ReportDataDto {
  type: string; // ex: "GLOBAL_STATS"
  createdAt: string;
  createdBy: string;

  totalClients: number;
  totalServers: number;
  activeServers: number;
  inactiveServers: number;

  totalApplications: number;
  totalServices: number;
  greenServices: number;
  yellowServices: number;
  redServices: number;

  totalExecutions: number;
  successRate: number;
  failures: number;
  timeouts: number;
  agentUnreachable: number;
  averageExecutionTimeSeconds: number;
  recentFailures: FailedExecutionDto[];

  totalAlerts: number;
  timeoutAlerts: number;
  failureAlerts: number;
  serverDownAlerts: number;

  
}
