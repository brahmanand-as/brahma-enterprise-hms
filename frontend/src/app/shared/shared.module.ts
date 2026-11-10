import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ReactiveFormsModule, FormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { NoticeComponent } from "./components/notice/notice.component";
import { RecordTableComponent } from "./components/record-table/record-table.component";
import { RecordDetailsComponent } from "./components/record-details/record-details.component";
import { FormFieldComponent } from "./components/form-field/form-field.component";
import { TaskDialogComponent } from "./components/task-dialog/task-dialog.component";
const components = [
  NoticeComponent,
  RecordTableComponent,
  RecordDetailsComponent,
  FormFieldComponent,
  TaskDialogComponent,
];
@NgModule({
  declarations: components,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule],
  exports: [
    ...components,
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
  ],
})
export class SharedModule {}
