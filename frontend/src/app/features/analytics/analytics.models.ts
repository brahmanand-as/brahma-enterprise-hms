export interface AnalyticsMetrics {
  [metric: string]: number;
  patients: number;
  visits: number;
  netCollections: number;
  averageWaitMinutes: number;
  collections: number;
  refunds: number;
  qualityEvents: number;
  admissions: number;
}
export interface MetricChange {
  direction: string;
  absolute: number;
  percent: number | null;
}
export interface AnalyticsReport {
  current: AnalyticsMetrics;
  changes: Record<string, MetricChange | undefined>;
  departments: { department: string; visits: number }[];
}
