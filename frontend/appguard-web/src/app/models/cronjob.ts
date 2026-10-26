// src/app/models/cron-job.model.ts

export interface CronJob {
  id: number;
  name: string;
  command: string;
  cronExpression: string;
  description: string;
  serverName: string;      // seulement le nom du serveur
  tags: string[];          // liste de noms de tags
  timeoutMinutes: number;
  retryCount: number;
  status: CronStatus;
}

export type CronStatus = 'ACTIVE' | 'INACTIVE';

export interface CreateCronRequest {
  name: string;                // obligatoire
  command: string;             // obligatoire
  cronExpression: string;      // obligatoire
  description?: string;        // optionnel
  serverId: number;            // obligatoire
  tagIds?: number[];           // optionnel
  timeoutMinutes?: number;     // optionnel
  retryCount?: number;         // optionnel
    status: CronStatus;

}