import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthenticationService } from './authentication.service';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const excludedUrls = ['/api/users/login', '/api/users/requestNewPassword', '/api/users/validate-code', '/api/users/change-password'];

  // Vérifie si la requête fait partie des URLs exclues
  const shouldExclude = excludedUrls.some(url => req.url.includes(url));

  if (shouldExclude) {
    // Ne pas ajouter d'en-tête Authorization
    return next(req);
  }

  const accessToken = localStorage.getItem('accessToken');

  if (accessToken) {
    
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    });
  }

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        const authService = inject(AuthenticationService);
        authService.logout();
        window.location.href = '/login';
      }
      return throwError(() => error);
    })
  );
};
