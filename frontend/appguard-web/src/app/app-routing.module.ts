import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { authGuard } from './guards/auth.guard';
import { AdduserComponent } from './pages/usersManagement/adduser/adduser.component';
import { ViewusersComponent } from './pages/usersManagement/viewusers/viewusers.component';
import { LoginComponent } from './pages/login/login.component';
import { HomeComponent } from './pages/home/home.component';
import { RequestnewpasswordComponent } from './pages/resetpassword/requestnewpassword/requestnewpassword.component';
import { ValidatecodeComponent } from './pages/resetpassword/validatecode/validatecode.component';
import { ChangepasswordComponent } from './pages/resetpassword/changepassword/changepassword.component';
import { LogsComponent } from './pages/logs/logs.component';
import { ApplicationDetailsComponent } from './pages/application/application-details/application-details.component';
import { ApplicationsListComponent } from './pages/application/applications-list/applications-list.component';
import { MetricsComponent } from './pages/metrics/metrics.component';
import { ServersComponent } from './pages/servers/servers.component';
import { AddclientComponent } from './pages/clientsManagement/addclient/addclient.component';
import { client } from 'stompjs';
import { ClientslistComponent } from './pages/clientsManagement/clientslist/clientslist.component';
import { CronjobslistComponent } from './pages/cronjobs/cronjobslist/cronjobslist.component';
import { CronhistoryComponent } from './pages/cronjobs/cronhistory/cronhistory.component';
import { ServeroverwiewComponent } from './pages/serveroverwiew/serveroverwiew.component';
import { ReportComponent } from './pages/report/report.component';
const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' }, // Redirige vers 'home' par défaut

  // Home route avec login
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'requestnewpassword', component: RequestnewpasswordComponent },
   {path: 'validatecode', component: ValidatecodeComponent},
   {path: 'changepassword', component: ChangepasswordComponent},
  // Routes principales
  { path: 'dashboard', component: DashboardComponent },
  {path: 'servers', component: ServersComponent},
  { path: 'adduser', component: AdduserComponent },
  { path: 'users', component: ViewusersComponent },
    { path: 'metrics', component: MetricsComponent },

  { path: 'logs', component: LogsComponent },
  { path: 'cronjobs', component: CronjobslistComponent },
  {path: 'cronhistory', component: CronhistoryComponent},
  { path: 'applications', component: ApplicationsListComponent },
  { path: 'applicationDetails/:appName', component: ApplicationDetailsComponent },
{ path: 'addclient', component: AddclientComponent },
{path: 'clientslist', component: ClientslistComponent},
{path: 'serveroverview', component: ServeroverwiewComponent},
{path: 'report', component: ReportComponent}
];



@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
