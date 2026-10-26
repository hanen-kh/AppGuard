export interface Server {
  id: number;
  name: string;
  os: string;
  ip: string;
  services: string[];
}

export interface Application {
  id: number;
  name: string;
  description?: string;
  indexElasticsearch?: string;
  createdAt: Date;
  clientName: string;
  server: Server;
}