export interface Template {
  id: string;
  title: string;
  category: string;
  coverUrl: string;
  description: string;
  requiredImageCount: number;
  acceptedMimeTypes: string; // JSON 数组字符串，如 ["image/jpeg","image/png"]
  maxFileSizeBytes: number;
}

export type TaskStatus = "PENDING" | "PROCESSING" | "SUCCESS" | "FAILED";
export type TaskStage = "NONE" | "CHECK" | "GENERATE" | "FINALIZE" | "DONE";

export interface Task {
  id: string;
  userId: string;
  templateId: string;
  templateTitle: string;
  requiredImageCount: number;
  status: TaskStatus;
  stage: TaskStage;
  stageDetail: string | null;
  resultUrl: string | null;
  failReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UploadResult {
  url: string;
  name: string;
  size: number;
  contentType: string;
}
