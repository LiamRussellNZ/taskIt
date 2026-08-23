import type { ApiError, BoardView, Task, TaskDraft, TaskPage, User, UserNotification, UserProfile, UserRole } from './types'

async function request<T>(path: string, options: RequestInit = {}, userId?: number): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')
  if (options.body) headers.set('Content-Type', 'application/json')
  if (userId) headers.set('X-User-Id', String(userId))

  const response = await fetch(path, { ...options, headers })
  if (!response.ok) {
    const error = await response.json().catch(() => ({} as ApiError)) as ApiError
    throw new Error(error.message || error.error || `Request failed (${response.status})`)
  }
  return response.json() as Promise<T>
}

export const api = {
  listUsers: () => request<User[]>('/api/users'),
  getUserProfile: (id: number) => request<UserProfile>(`/api/users/${id}/profile`),
  listNotifications: (id: number, userId: number) =>
    request<UserNotification[]>(`/api/users/${id}/notifications`, {}, userId),
  createUser: (displayName: string, email: string, roles: UserRole[]) =>
    request<User>('/api/users', { method: 'POST', body: JSON.stringify({ displayName, email, roles }) }),
  listTasks: (view: BoardView, category: string, userId?: number) => {
    const query = new URLSearchParams({ view })
    if (category.trim()) query.set('category', category.trim())
    return request<Task[]>(`/api/tasks?${query}`, {}, userId)
  },
  listTaskPage: (view: BoardView, category: string, page: number, size: number, userId?: number) => {
    const query = new URLSearchParams({ view, page: String(page), size: String(size) })
    if (category.trim()) query.set('category', category.trim())
    return request<TaskPage>(`/api/v2/tasks?${query}`, {}, userId)
  },
  getTask: (id: number) => request<Task>(`/api/tasks/${id}`),
  createTask: (draft: TaskDraft, userId: number) =>
    request<Task>('/api/tasks', { method: 'POST', body: JSON.stringify(draft) }, userId),
  updateTask: (id: number, draft: TaskDraft, userId: number) =>
    request<Task>(`/api/tasks/${id}`, { method: 'PATCH', body: JSON.stringify(draft) }, userId),
  claimTask: (id: number, userId: number) =>
    request<Task>(`/api/tasks/${id}/claim`, { method: 'POST' }, userId),
  completeTask: (id: number, userId: number) =>
    request<Task>(`/api/tasks/${id}/complete`, { method: 'POST' }, userId),
  cancelTask: (id: number, userId: number) =>
    request<Task>(`/api/tasks/${id}/cancel`, { method: 'POST' }, userId),
  dropTask: (id: number, userId: number) =>
    request<Task>(`/api/tasks/${id}/drop`, { method: 'POST' }, userId),
  reviewTaskDrop: (taskId: number, dropId: number, rating: number, review: string, userId: number) =>
    request<Task>(`/api/tasks/${taskId}/drops/${dropId}/review`, {
      method: 'POST',
      body: JSON.stringify({ rating, review }),
    }, userId),
  reviewTaskCompletion: (taskId: number, rating: number, review: string, userId: number) =>
    request<Task>(`/api/tasks/${taskId}/completion-review`, {
      method: 'POST',
      body: JSON.stringify({ rating, review }),
    }, userId),
  requestStatusUpdate: (id: number, userId: number) =>
    request<Task>(`/api/tasks/${id}/status-updates`, { method: 'POST' }, userId),
  respondToStatusUpdate: (taskId: number, statusUpdateId: number, response: string, userId: number) =>
    request<Task>(`/api/tasks/${taskId}/status-updates/${statusUpdateId}/respond`, {
      method: 'POST',
      body: JSON.stringify({ response }),
    }, userId),
  requestAssistance: (id: number, userId: number) =>
    request<Task>(`/api/tasks/${id}/assistance-requests`, { method: 'POST' }, userId),
  offerAssistance: (id: number, userId: number) =>
    request<Task>(`/api/tasks/${id}/assistance-requests/offer`, { method: 'POST' }, userId),
  askQuestion: (id: number, question: string, userId: number) =>
    request<Task>(`/api/tasks/${id}/questions`, {
      method: 'POST',
      body: JSON.stringify({ question }),
    }, userId),
  answerQuestion: (taskId: number, questionId: number, answer: string, userId: number) =>
    request<Task>(`/api/tasks/${taskId}/questions/${questionId}/answer`, {
      method: 'POST',
      body: JSON.stringify({ answer }),
    }, userId),
}
