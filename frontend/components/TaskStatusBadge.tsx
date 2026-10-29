import { TaskStatus } from "@/lib/types";

const LABEL: Record<TaskStatus, string> = {
  PENDING: "等待处理",
  PROCESSING: "处理中",
  SUCCESS: "成功",
  FAILED: "失败"
};

export default function TaskStatusBadge({ status }: { status: TaskStatus }) {
  return (
    <span className={`badge ${status}`}>
      <span className="dot" />
      {LABEL[status]}
    </span>
  );
}
