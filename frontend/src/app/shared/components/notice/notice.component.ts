import { Component, EventEmitter, Input, Output } from "@angular/core";
@Component({
  selector: "app-notice",
  template: `<div
    class="notice"
    [class.error]="kind === 'error'"
    [attr.role]="kind === 'error' ? 'alert' : 'status'"
    *ngIf="message"
  >
    {{ kind === "success" ? "✓ " : "" }}{{ message
    }}<button
      type="button"
      aria-label="Dismiss message"
      (click)="dismiss.emit()"
    >
      ×
    </button>
  </div>`,
})
export class NoticeComponent {
  @Input() message = "";
  @Input() kind: "success" | "error" = "success";
  @Output() readonly dismiss = new EventEmitter<void>();
}
