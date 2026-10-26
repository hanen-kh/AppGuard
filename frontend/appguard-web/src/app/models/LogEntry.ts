export interface LogEntry {
  timestamp: string;  
  level: 'INFO' | 'WARN' | 'ERROR' | 'DEBUG';
  message: string;
}