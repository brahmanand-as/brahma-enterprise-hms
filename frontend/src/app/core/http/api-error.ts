import { ApiProblem } from "../models/api.models";
function record(value: unknown): Record<string, unknown> {
  return value !== null && typeof value === "object"
    ? (value as Record<string, unknown>)
    : {};
}
export function apiErrorMessage(error: unknown): string {
  const outer = record(error),
    problem = record(outer["error"]) as ApiProblem;
  if (problem.detail) return problem.detail;
  if (problem.message) return problem.message;
  if (problem.errors)
    return Object.entries(problem.errors)
      .map(([field, message]) => field + ": " + message)
      .join("; ");
  if (outer["status"] === 401) return "Your session expired. Sign in again.";
  if (outer["status"] === 403)
    return "Your account does not have permission for this action.";
  if (outer["status"] === 0)
    return "The hospital server could not be reached. Check that the backend is running.";
  return typeof outer["message"] === "string"
    ? outer["message"]
    : "The request could not be completed.";
}
export async function readableApiError(error: unknown): Promise<string> {
  const outer = record(error);
  if (outer["error"] instanceof Blob) {
    try {
      return apiErrorMessage({
        ...outer,
        error: JSON.parse(await outer["error"].text()),
      });
    } catch {
      return apiErrorMessage(error);
    }
  }
  return apiErrorMessage(error);
}
