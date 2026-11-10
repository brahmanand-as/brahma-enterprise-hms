import { CurrentUser } from "../models/api.models";
import { WorkspaceDefinition } from "../../shared/workspace/workspace.models";
export const PATIENT_WORKSPACES = [
  "myappointments",
  "myinvoices",
  "myreports",
  "myprescriptions",
  "mydocuments",
  "myprofile",
  "helpdesk",
  "notifications",
];
export function hasPermission(
  user: CurrentUser | null,
  permission?: string,
): boolean {
  return !!user && (!permission || user.permissions.includes(permission));
}
export function visibleWorkspaces(
  user: CurrentUser | null,
  workspaces: WorkspaceDefinition[],
): WorkspaceDefinition[] {
  if (!user) return [];
  if (user.role === "PATIENT")
    return PATIENT_WORKSPACES.map((id) =>
      workspaces.find((workspace) => workspace.id === id),
    ).filter((workspace): workspace is WorkspaceDefinition => !!workspace);
  return workspaces.filter(
    (workspace) =>
      !workspace.patient &&
      hasPermission(user, workspace.permission) &&
      (!["myhr", "swaps"].includes(workspace.id) ||
        !!user.employeeId ||
        user.role === "ADMIN"),
  );
}
export function portalUrl(user: CurrentUser): string {
  return "/" + user.role.toLowerCase();
}
