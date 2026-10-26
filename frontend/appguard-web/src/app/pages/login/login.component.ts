import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { OnInit } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../services/userservice/user.service';
import { AppRoutingModule } from '../../app-routing.module';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../services/authservice/authentication.service';
import { Authentification } from '../../models/Authentification';
@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit{

  loginForm: FormGroup;
    loginError: string | null = null; // ← message d'erreur

  constructor(private fb: FormBuilder, private authService: AuthenticationService, private router: Router) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],  // Champ 'username' requis
      password: ['', Validators.required]   // Champ 'password' requis
    });
  }

  ngOnInit(): void {
    // Initialisation du FormGroup avec les contrôles et leurs validations
    this.loginForm = this.fb.group({
      username: ['', Validators.required],  // Champ 'username' requis
      password: ['', Validators.required]   // Champ 'password' requis
    });
  }

  // Méthode appelée lors de la soumission du formulaire
 onLogin() {
    if (this.loginForm.invalid) {
      return;
    }

    const auth: Authentification = {
      username: this.loginForm.value.username,
      password: this.loginForm.value.password
    };

    this.authService.login(auth).subscribe(
      response => {
        this.loginError = null; // reset si ok
        this.router.navigate(['/dashboard']);
      },
      error => {
        console.error('Erreur de connexion:', error);
        this.loginError = 'Bad credentials !'; // message affiché
      }
    );
  }
  
}