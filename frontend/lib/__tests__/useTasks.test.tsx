import { act, renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { useTaskTracker } from "../useTasks";
import { api } from "../api";
import { Task } from "../types";

vi.mock("../api", () => ({
  api: {
    getTask: vi.fn(),
    listTasks: vi.fn(),
    submit: vi.fn(),
    upload: vi.fn(),
    listTemplates: vi.fn(),
    listCategories: vi.fn(),
    getTemplate: vi.fn()
  },
  getUserId: vi.fn(() => "u-test"),
  assetUrl: vi.fn((p: string | null | undefined) => p ?? "")
}));

function makeTask(overrides: Partial<Task>): Task {
  return {
    id: "t1",
    userId: "u-test",
    templateId: "tpl",
    templateTitle: "模板",
    requiredImageCount: 1,
    status: "PENDING",
    stage: "NONE",
    stageDetail: null,
    resultUrl: null,
    failReason: null,
    createdAt: "2026-01-01T00:00:00",
    updatedAt: "2026-01-01T00:00:00",
    ...overrides
  };
}

beforeEach(() => {
  vi.mocked(api.getTask).mockReset();
});

describe("useTaskTracker（前端异步状态管理，R-07/R-08/R-12）", () => {
  it("多个任务互不串扰：各自的状态、失败原因与结果独立", () => {
    const success = makeTask({
      id: "a",
      status: "SUCCESS",
      stage: "DONE",
      stageDetail: "完成",
      resultUrl: "/files/results/a.svg"
    });
    const failed = makeTask({
      id: "b",
      status: "FAILED",
      stage: "GENERATE",
      stageDetail: "失败：模拟生成失败（演示用）",
      failReason: "模拟生成失败（演示用）"
    });

    const { result } = renderHook(() => useTaskTracker([success, failed]));
    const byId = (id: string) => result.current.tasks.find((t) => t.id === id);

    expect(result.current.tasks).toHaveLength(2);
    expect(byId("a")?.status).toBe("SUCCESS");
    expect(byId("a")?.resultUrl).toBe("/files/results/a.svg");
    expect(byId("a")?.failReason).toBeNull();
    expect(byId("b")?.status).toBe("FAILED");
    expect(byId("b")?.failReason).toBe("模拟生成失败（演示用）");
    expect(byId("b")?.resultUrl).toBeNull();
  });

  it("只轮询未完成任务：已结束任务不再被查询", async () => {
    const done = makeTask({ id: "a", status: "SUCCESS", stage: "DONE", stageDetail: "完成" });
    const processing = makeTask({
      id: "b",
      status: "PROCESSING",
      stage: "GENERATE",
      stageDetail: "模拟生成中"
    });
    vi.mocked(api.getTask).mockResolvedValue(
      makeTask({ id: "b", status: "SUCCESS", stage: "DONE", stageDetail: "完成", resultUrl: "/files/results/b.svg" })
    );

    const { result } = renderHook(() => useTaskTracker([done, processing], 20));

    await waitFor(() => {
      expect(result.current.tasks.find((t) => t.id === "b")?.status).toBe("SUCCESS");
    });

    const calledIds = vi.mocked(api.getTask).mock.calls.map((c) => c[0]);
    expect(calledIds).toContain("b");
    expect(calledIds).not.toContain("a");
  });

  it("查询接口异常时保留已有任务信息，不会把任务误标为失败", async () => {
    const processing = makeTask({
      id: "p",
      status: "PROCESSING",
      stage: "GENERATE",
      stageDetail: "模拟生成中"
    });
    vi.mocked(api.getTask).mockRejectedValue(new Error("db unavailable"));

    const { result } = renderHook(() => useTaskTracker([processing], 20));

    await waitFor(() => expect(api.getTask).toHaveBeenCalled());
    await act(async () => {
      await new Promise((r) => setTimeout(r, 60));
    });

    const p = result.current.tasks.find((t) => t.id === "p");
    expect(p?.status).toBe("PROCESSING");
    expect(p?.stageDetail).toBe("模拟生成中");
    expect(p?.failReason).toBeNull();
  });

  it("任务失败被如实呈现（与查询异常区分开）", async () => {
    const processing = makeTask({
      id: "f",
      status: "PROCESSING",
      stage: "GENERATE",
      stageDetail: "模拟生成中"
    });
    vi.mocked(api.getTask).mockResolvedValue(
      makeTask({
        id: "f",
        status: "FAILED",
        stage: "GENERATE",
        stageDetail: "失败：模拟生成失败（演示用）",
        failReason: "模拟生成失败（演示用）"
      })
    );

    const { result } = renderHook(() => useTaskTracker([processing], 20));

    await waitFor(() => {
      expect(result.current.tasks.find((t) => t.id === "f")?.status).toBe("FAILED");
    });
    expect(result.current.tasks.find((t) => t.id === "f")?.failReason).toBe(
      "模拟生成失败（演示用）"
    );
  });

  it("没有待处理任务时不轮询；新任务受理后恢复轮询", async () => {
    const done = makeTask({ id: "a", status: "SUCCESS", stage: "DONE", stageDetail: "完成" });
    vi.mocked(api.getTask).mockResolvedValue(
      makeTask({ id: "n", status: "SUCCESS", stage: "DONE", stageDetail: "完成" })
    );
    const { result } = renderHook(() => useTaskTracker([done], 20));

    await act(async () => {
      await new Promise((r) => setTimeout(r, 80));
    });
    expect(api.getTask).not.toHaveBeenCalled();

    act(() => {
      result.current.track(
        makeTask({ id: "n", status: "PROCESSING", stage: "CHECK", stageDetail: "素材检查中" })
      );
    });
    await waitFor(() => expect(api.getTask).toHaveBeenCalled());
  });
});
