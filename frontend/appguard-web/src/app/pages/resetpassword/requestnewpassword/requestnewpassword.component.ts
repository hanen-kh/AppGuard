import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/userservice/user.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
@Component({
  selector: 'app-requestnewpassword',
  standalone: false,
  templateUrl: './requestnewpassword.component.html',
  styleUrl: './requestnewpassword.component.css'
})
export class RequestnewpasswordComponent {

  resetForm: FormGroup;
  submitted = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private userService: UserService, 
   private router: Router
    
  ) {
    this.resetForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  get f() {
    return this.resetForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
  
    if (this.resetForm.invalid) {
      return;
    }
  
    const email = this.resetForm.value.email;
    localStorage.setItem('validatedEmail', email); // Store the email
    this.userService.requestNewPassword(email).subscribe({
      next: () => {
        this.successMessage = "Your code is sent by email"; // afficher le message manuellement
        this.errorMessage = '';
  
        localStorage.setItem('validatedEmail', email); // on garde l'email
        setTimeout(() => {
          this.router.navigate(['/validatecode']); // rediriger après un petit délai
        }, 2000); // 2 secondes pour que l'utilisateur voie le message
      },
      error: (err) => {
        this.errorMessage = err.error.message || 'Une erreur est survenue.';
      }
    });
  }
  
}
