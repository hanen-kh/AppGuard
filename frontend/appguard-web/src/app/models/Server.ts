export interface ServerDto {
  id: number;
  name: string;
  os: string;
  client: string;
  cronJobs: number;
  applications: number;
  servicesNb: number;
  status: 'ACTIVE' | 'MAINTENANCE' | 'UNREACHABLE'| 'INACTIVE'; 

 applicationNames: string[]; // noms des applications
    serviceNames: string[];     // noms des services
}