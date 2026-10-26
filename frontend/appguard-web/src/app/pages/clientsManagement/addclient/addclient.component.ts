import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ClientService } from '../../../services/clientservice/client.service';
import { Client } from '../../../models/ClientDTO';
import { Supervisor } from '../../../models/supervisor';
@Component({
  selector: 'app-addclient',
  standalone: false,
  templateUrl: './addclient.component.html',
  styleUrl: './addclient.component.css'
})
export class AddclientComponent implements OnInit {
  @Output() clientAdded = new EventEmitter<Client>();
  clientForm: FormGroup;
  supervisorsList: Supervisor[] = [];

  constructor(private clientService: ClientService, private fb: FormBuilder) {
    this.clientForm = this.fb.group({
      name: [''],
      contactEmail: [''],
      description: [''],
      supervisorIds: [[]]
    });
  }

  ngOnInit(): void {
    this.loadSupervisors();
  }

  loadSupervisors(): void {
    this.clientService.getAllSupervisors().subscribe({
      next: (supervisors) => this.supervisorsList = supervisors,
      error: (err) => console.error('Error loading supervisors', err)
    });
  }

  toggleSupervisor(supervisorId: number): void {
    const current = this.clientForm.value.supervisorIds as number[];
    const index = current.indexOf(supervisorId);
    if (index === -1) current.push(supervisorId);
    else current.splice(index, 1);
    this.clientForm.patchValue({ supervisorIds: current });
  }

  addClient(): void {
    const client: Client = this.clientForm.value;
    this.clientService.createClient(client).subscribe({
      next: () => {
        this.clientForm.reset({ supervisorIds: [] });
        this.clientAdded.emit(client); // Notifie le parent
      },
      error: (err) => console.error('Error creating client', err)
    });
  }
}
