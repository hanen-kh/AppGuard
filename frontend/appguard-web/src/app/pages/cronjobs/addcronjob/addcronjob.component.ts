import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CronJob } from '../../../models/cronjob';
import { CronjobService } from '../../../services/cronjobservice/cronjob.service';
import { CreateCronRequest } from '../../../models/cronjob';
import { Server } from '../../../models/application';
import { ServerDto } from '../../../models/Server';
import { ServerService } from '../../../services/serverservice/server.service';
@Component({
  selector: 'app-addcronjob',
  standalone: false,
  templateUrl: './addcronjob.component.html',
  styleUrl: './addcronjob.component.css'
})

export class AddcronjobComponent implements OnInit {
  @Output() cronJobAdded = new EventEmitter<CronJob>();
  cronForm: FormGroup;
  servers: { id: number; name: string }[] = [];

 constructor(
    private fb: FormBuilder,
    private cronService: CronjobService,
    private serverService: ServerService
  ) {
    this.cronForm = this.fb.group({
      name: [''], // ✅ Retirez Validators.required
      command: [''], // ✅ Retirez Validators.required
      cronExpression: [''], // ✅ Retirez Validators.required
      description: [''],
      serverId: [null], // ✅ Retirez Validators.required
      tagIds: [[]],
      timeoutMinutes: [60],
      retryCount: [0],
      status: ['ACTIVE']
    });
  }

  ngOnInit(): void {
    this.loadServers();
  }

  private loadServers(): void {
  this.serverService.getAllServers().subscribe({
    next: (servers: ServerDto[]) => {
      this.servers = servers.map(s => ({ id: s.id, name: s.name }));
      
      // Affichez les serveurs dans la console pour debug
      console.log('=== SERVERS LOADED ===');
      console.log('Total servers:', this.servers.length);
      
      if (this.servers.length === 0) {
        console.warn('No servers available!');
      } else {
        this.servers.forEach(server => {
          console.log(`Server ID: ${server.id}, Name: ${server.name}`);
        });
      }
      
      console.log('======================');
    },
    error: (err) => {
      console.error('Error loading servers:', err);
      alert('Cannot load servers list. Check console for details.');
    }
  });
}

 
  createCronJob(): void {
  const raw = this.cronForm.value;
  const serverId = Number(raw.serverId);
  
  const request = {
    name: raw.name,
    command: raw.command,
    cronExpression: raw.cronExpression,
    description: raw.description || '',
    serverId: serverId,
    tagIds: raw.tagIds || [],
    timeoutMinutes: raw.timeoutMinutes || 60,
    retryCount: raw.retryCount || 0,
    status: raw.status || 'ACTIVE'
  };

  console.log('Creating cron job with:', request);

 this.cronService.createCronJob(serverId, request).subscribe({
    next: () => {
      console.log('Cron job created successfully');
      this.cronJobAdded.emit(); // ✅ Émettez sans paramètre ou avec les données que vous voulez
      
      this.cronForm.reset({
        timeoutMinutes: 60,
        retryCount: 0,
        status: 'ACTIVE'
      });
      
    
    },
    error: (err) => {
      console.error('Error creating cron job:', err);
      alert('Error: ' + (err.error?.message || 'Unknown error'));
    }
  });
}
}