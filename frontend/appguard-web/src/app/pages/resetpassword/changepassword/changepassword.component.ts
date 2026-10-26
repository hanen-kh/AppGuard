import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../services/userservice/user.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-changepassword',
  standalone: false,
  templateUrl: './changepassword.component.html',
  styleUrl: './changepassword.component.css'
})
export class ChangepasswordComponent implements OnInit{

  changePasswordForm!: FormGroup;
  isSubmitting = false;
  message = '';
  email = '';
  isError = false;
  
  constructor(private fb: FormBuilder, private userService: UserService, private router: Router) {}
  
  ngOnInit(): void {
    this.email = localStorage.getItem('validatedEmail') || '';
    this.changePasswordForm = this.fb.group({
      newPassword: ['', [Validators.required]],
      confirmPassword: ['', [Validators.required]]
    });
  }
  
  onSubmit(): void {
    if (this.changePasswordForm.invalid) return;
  
    const newPassword = this.changePasswordForm.get('newPassword')?.value;
    const confirmPassword = this.changePasswordForm.get('confirmPassword')?.value;
  
    if (newPassword !== confirmPassword) {
      this.message = "The passwords do not match.";
      this.isError = true;
      return;
    }
  
    this.isSubmitting = true;
    this.isError = false;
  
    this.userService.changePassword(newPassword, this.email).subscribe({
      next: (response: string) => {
        this.message = response;
      }
    });
  }
  
}