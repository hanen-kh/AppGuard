export class Client {
  id?: number;
  name: string;
  contactEmail: string;
  description: string;
  supervisorIds: number[];
  supervisorCount: number;

  constructor(data: Partial<Client>) {
    // Assurez-vous que les tableaux sont initialisés
    this.id = data.id;
    this.name = data.name ?? '';
    this.contactEmail = data.contactEmail ?? '';
    this.description = data.description ?? '';
    this.supervisorIds = data.supervisorIds ?? [];
    this.supervisorCount = this.supervisorIds.length;
  }
}
