import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, NgModel } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { Router, RouterModule } from '@angular/router';
import { AuthenticationService } from './services/authservice/authentication.service';
import { Routes } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { provideHttpClient } from '@angular/common/http';
import { SidebarComponent } from './dashboardComponents/sidebar/sidebar.component';
import { HeaderComponent } from './dashboardComponents/header/header.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { LoginComponent } from './pages/login/login.component';
import { AdduserComponent } from './pages/usersManagement/adduser/adduser.component';
import { ViewusersComponent } from './pages/usersManagement/viewusers/viewusers.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HomeComponent } from './pages/home/home.component';
import { RequestnewpasswordComponent } from './pages/resetpassword/requestnewpassword/requestnewpassword.component';
import { ValidatecodeComponent } from './pages/resetpassword/validatecode/validatecode.component';
import { ChangepasswordComponent } from './pages/resetpassword/changepassword/changepassword.component';
import { tokenInterceptor } from './services/authservice/token.interceptor';
import { withInterceptors } from '@angular/common/http';
import { LogsComponent } from './pages/logs/logs.component';
import { MetricsComponent } from './pages/metrics/metrics.component';
import { ApplicationsListComponent } from './pages/application/applications-list/applications-list.component';
import { ApplicationDetailsComponent } from './pages/application/application-details/application-details.component';
import { SocketIoModule, SocketIoConfig } from 'ngx-socket-io';
import { ApplicationsComponent } from './dashboardComponents/applications/applications.component';
import { ServersComponent } from './pages/servers/servers.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { AddclientComponent } from './pages/clientsManagement/addclient/addclient.component';
import { ClientslistComponent } from './pages/clientsManagement/clientslist/clientslist.component';
import { AddcronjobComponent } from './pages/cronjobs/addcronjob/addcronjob.component';
import { CronjobslistComponent } from './pages/cronjobs/cronjobslist/cronjobslist.component';
import { CronhistoryComponent } from './pages/cronjobs/cronhistory/cronhistory.component';
import { ServeroverwiewComponent } from './pages/serveroverwiew/serveroverwiew.component';
import { ReportComponent } from './pages/report/report.component';
import { TagComponent } from './pages/cronjobs/tags/tag/tag.component';
import { TaglistComponent } from './pages/cronjobs/tags/taglist/taglist.component';

@NgModule({
  declarations: [
    AppComponent,
    SidebarComponent,
    HeaderComponent,
  
    DashboardComponent,
    LoginComponent,
    AdduserComponent,
  
    
    HomeComponent,
   
    RequestnewpasswordComponent,
        ValidatecodeComponent,
        ChangepasswordComponent,
        LogsComponent,
        MetricsComponent,
        ApplicationsListComponent,
        ApplicationDetailsComponent,
        ApplicationsComponent,
        ServersComponent,
        AddclientComponent,
        ClientslistComponent,
        ViewusersComponent,
        AddcronjobComponent,
        CronjobslistComponent,
        CronhistoryComponent,
        ServeroverwiewComponent,
        ReportComponent,
        TagComponent,
        TaglistComponent
  
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule
   
  ],
  providers: [
    AuthenticationService,
    provideHttpClient(
      withInterceptors([
        tokenInterceptor
      ])
    )
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
