import { defineStore } from 'pinia'
import { getTodoCount, getTaskTodo, completeTask, claimTask } from '@/api/workflow'

/**
 * 流程状态管理 - 待办数量、任务列表
 */
export const useWorkflowStore = defineStore('workflow', {
  state: () => ({
    todoCount: 0,
    claimableCount: 0,
    overdueCount: 0,
    todoList: [] as any[],
  }),

  actions: {
    /** 获取待办数量（用于侧边栏角标） */
    async fetchTodoCount() {
      try {
        const res = await getTodoCount()
        this.todoCount = res.data?.todo || 0
        this.claimableCount = res.data?.claimable || 0
        this.overdueCount = res.data?.overdue || 0
      } catch (_) {
        // 服务不可用时静默处理
        this.todoCount = 0
        this.claimableCount = 0
        this.overdueCount = 0
      }
    },

    /** 获取待办列表 */
    async fetchTodoList(pageNum = 1, pageSize = 10) {
      const res = await getTaskTodo({ pageNum, pageSize })
      this.todoList = res.data.records || []
      return res.data
    },
  },
})
