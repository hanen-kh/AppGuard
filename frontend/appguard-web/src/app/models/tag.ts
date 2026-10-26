// src/app/models/tag.model.ts
export enum TagType {
  ENVIRONMENT = 'ENVIRONMENT',
  CLIENT = 'CLIENT',
  TYPE = 'TYPE',
  PRIORITY = 'PRIORITY',
  CUSTOM = 'CUSTOM'
}

export interface Tag {
  id?: number;   // optionnel car il est généré par le backend
  name: string;
  type: TagType;
}
