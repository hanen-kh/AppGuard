export interface ServerServiceStatsDto {
  totalServers: number;
  activeServers: number;
  maintenanceServers: number;
  unreachableServers: number;

  totalServices: number;
  greenServices: number;
  yellowServices: number;
  redServices: number;
}

export interface CronExecutionStatsDto {
  totalCronJobs: number;
  totalExecutions: number;
  successRate: number;
  averageExecutionTimeSeconds: number;
  failures: number;
  timeouts: number;
  agentUnreachable: number;
  recentFailures: FailedExecutionDto[];
}

export interface FailedExecutionDto {
  cronJobName: string;
  serverName: string;
  startTime: string;
  status: string;
  errorOutput: string;
  exitCode: number;
  durationMillis: number;
  memoryUsedKB: number;
  cpuUsagePercent: number;
}

export interface AlertStatsDto {
  totalAlerts: number;
  unresolvedAlerts: number;
  timeoutAlerts: number;
  failureAlerts: number;
  serverDownAlerts: number;
  latestAlerts: AlertDto[];
}

export interface AlertDto {
  id: number;
  type: string;
  message: string;
  date: string;
  status: string;
}

export interface DashboardData {
  serverData: DashboardData;
  serverServiceStats: ServerServiceStatsDto;
  cronExecutionStats: CronExecutionStatsDto;
  alertStats: AlertStatsDto;
}
