export type UserRole = 'ASKER' | 'DOER'
export type TaskStatus = 'OPEN' | 'CLAIMED' | 'COMPLETED' | 'CANCELLED'
export type BoardView = 'OPEN' | 'MINE_AS_ASKER' | 'MINE_AS_DOER'
export type NotificationType = 'ASSISTANCE_REQUEST' | 'TASK_QUESTION' | 'TASK_COMPLETED' | 'CHAT_MESSAGE'

export interface User {
  id: number
  displayName: string
  email: string
  roles: UserRole[]
}

export interface Task {
  id: number
  title: string
  description: string
  category: string
  location: string
  remote: boolean
  status: TaskStatus
  asker: User
  assignedDoer: User | null
  assistanceRequest: TaskAssistanceRequest | null
  questions: TaskQuestion[]
  statusUpdates: StatusUpdate[]
  completionReview: TaskCompletionReview | null
  drops: TaskDrop[]
  createdAt: string
  updatedAt: string
}

export interface TaskAssistanceRequest {
  id: number
  requestingDoer: User
  helper: User | null
  requestedAt: string
  offeredAt: string | null
}

export interface TaskQuestion {
  id: number
  askingDoer: User
  question: string
  askedAt: string
  answer: string | null
  answeredAt: string | null
}

export interface TaskDrop {
  id: number
  doer: User
  droppedAt: string
  rating: number | null
  review: string | null
  reviewedAt: string | null
}

export interface TaskCompletionReview {
  id: number
  doer: User
  rating: number
  review: string | null
  reviewedAt: string
}

export interface StatusUpdate {
  id: number
  requestedAt: string
  response: string | null
  respondedAt: string | null
}

export interface TaskDraft {
  title: string
  description: string
  category: string
  location: string
  remote: boolean
}

export interface UserNotification {
  id: number
  type: NotificationType
  taskId: number
  taskTitle: string
  actor: User
  createdAt: string
}

export interface TaskChatMessage {
  id: number
  sender: User
  message: string
  sentAt: string
}

export interface ProfileTask {
  id: number
  title: string
  category: string
  location: string
  remote: boolean
  status: TaskStatus
  createdAt: string
  updatedAt: string
}

export interface ProfileReview {
  task: ProfileTask
  rating: number
  review: string
  reviewedAt: string
}

export interface ProfileDroppedAssignment {
  task: ProfileTask
  droppedAt: string
  rating: number | null
  review: string | null
  reviewedAt: string | null
}

export interface UserProfile {
  id: number
  displayName: string
  email: string
  roles: UserRole[]
  averageReceivedRating: number | null
  reviews: ProfileReview[]
  requestedTasks: ProfileTask[]
  currentAssignments: ProfileTask[]
  priorAssignments: ProfileDroppedAssignment[]
}

export interface ApiError {
  message?: string
  error?: string
}
