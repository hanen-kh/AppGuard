import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/userservice/user.service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms'; 
@Component({
  selector: 'app-validatecode',
  standalone: false,
  templateUrl: './validatecode.component.html',
  styleUrl: './validatecode.component.css'
})
export class ValidatecodeComponent implements OnInit{

  validateCodeForm!: FormGroup;  // Declare the FormGroup
  errorMessage: string = ''; // To store the error message
  successMessage: string = ''; // To store the success message
  email: string = ''; // To store the email

  constructor(
    private fb: FormBuilder,  // FormBuilder to create the form group
    private userService: UserService,  // Inject your UserService
    private router: Router  // Inject router for navigation
  ) {}

  ngOnInit(): void {
    // Retrieve email from localStorage and set it to the email variable
    this.email = localStorage.getItem('validatedEmail') || '';

    // Initialize the form group with validators
    this.validateCodeForm = this.fb.group({
      email: [{ value: this.email, disabled: true }, [Validators.required, Validators.email]], // email is read-only
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]  // Example: Code must be 6 digits
    });
  }

  // Getter for the form controls to simplify access
  get f() {
    return this.validateCodeForm.controls;
  }

  // Method to call the validateCode API when form is submitted
  onSubmit(): void {
    if (this.validateCodeForm.invalid) {
      return;
    }

    const { code, email } = this.validateCodeForm.value;

    // Call the validateCode method from UserService
    this.userService.validateCode(code, email).subscribe({
      next: (response) => {
        this.successMessage = 'Code validated successfully!';
        this.errorMessage = ''; // Clear error message if successful

        // Redirect to another page after successful validation, e.g., login page
        setTimeout(() => {
          this.router.navigate(['/changepassword']);
        }, 2000);
      },
      error: (err) => {
        // Handle error
        if (err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'An unexpected error occurred.';
        }
      }
    });
  }
}