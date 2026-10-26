export class User {
    id!: number;
    firstname!: string;
    lastname!: string;
    email!: string;
    typeRole!: string; 
    roleId?: number;
   
  

   constructor(id: number, firstname: string, lastname: string, email: string, typeRole: string, roleId:number) {
    this.id = id;
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
    this.typeRole = typeRole;
   this.roleId=roleId;
  }
}