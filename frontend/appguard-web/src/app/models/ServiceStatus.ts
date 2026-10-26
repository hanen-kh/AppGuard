export interface ServiceStatus {
  id: number | null;
  serviceName: string;
 
  port: number;
  isActive: boolean;
  cpuUsage: number;
  memoryUsage: number;
  diskUsage: number;
  lastChecked: string; 
}
