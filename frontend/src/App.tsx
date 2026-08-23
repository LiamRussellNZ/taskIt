import { FormEvent, useCallback, useEffect, useState } from 'react'
import { api } from './api'
import type { BoardView, Task, TaskDraft, TaskPage, User, UserNotification, UserProfile, UserRole } from './types'

const emptyTask: TaskDraft = { title: '', description: '', category: '', location: '', remote: false }
const viewLabels: Record<BoardView, string> = {
  OPEN: 'Open tasks',
  MINE_AS_ASKER: 'My requests',
  MINE_AS_DOER: 'My work',
}
const pageSizes = [25, 50, 75]
const emptyTaskPage: TaskPage = { content: [], page: 0, size: 25, totalElements: 0, totalPages: 0 }

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Something went wrong. Please try again.'
}

export default function App() {
  const [users, setUsers] = useState<User[]>([])
  const [activeUserId, setActiveUserId] = useState<number | undefined>(() => {
    const stored = localStorage.getItem('taskit.activeUser')
    return stored ? Number(stored) : undefined
  })
  const [view, setView] = useState<BoardView>('OPEN')
  const [category, setCategory] = useState('')
  const [taskPage, setTaskPage] = useState<TaskPage>(emptyTaskPage)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(() => {
    const stored = Number(localStorage.getItem('taskit.taskPageSize'))
    return pageSizes.includes(stored) ? stored : 25
  })
  const [selected, setSelected] = useState<Task | null>(null)
  const [draft, setDraft] = useState<TaskDraft | null>(null)
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [profileLoading, setProfileLoading] = useState(false)
  const [notifications, setNotifications] = useState<UserNotification[]>([])
  const [showNotifications, setShowNotifications] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const activeUser = users.find((user) => user.id === activeUserId)
  const loadUsers = useCallback(async () => {
    try {
      setUsers(await api.listUsers())
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }, [])

  const loadTasks = useCallback(async () => {
    setLoading(true)
    setError('')
    if (view !== 'OPEN' && !activeUserId) {
      setTaskPage({ ...emptyTaskPage, size: pageSize })
      setLoading(false)
      return
    }
    try {
      const result = await api.listTaskPage(view, category, page, pageSize, activeUserId)
      const lastPage = Math.max(result.totalPages - 1, 0)
      if (page > lastPage) {
        setTaskPage({ ...emptyTaskPage, size: pageSize })
        setPage(lastPage)
        return
      }
      setTaskPage(result)
    } catch (requestError) {
      setTaskPage({ ...emptyTaskPage, size: pageSize })
      setError(errorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }, [view, category, activeUserId, page, pageSize])

  useEffect(() => { void loadUsers() }, [loadUsers])
  useEffect(() => { void loadTasks() }, [loadTasks])

  const loadNotifications = useCallback(async () => {
    if (!activeUserId) {
      setNotifications([])
      return
    }
    try {
      setNotifications(await api.listNotifications(activeUserId, activeUserId))
    } catch (requestError) {
      setNotifications([])
      setError(errorMessage(requestError))
    }
  }, [activeUserId])

  useEffect(() => { void loadNotifications() }, [loadNotifications])

  function selectUser(id: number | undefined) {
    setActiveUserId(id)
    setPage(0)
    setProfile(null)
    setShowNotifications(false)
    if (id) localStorage.setItem('taskit.activeUser', String(id))
    else localStorage.removeItem('taskit.activeUser')
  }

  async function selectTask(id: number) {
    setError('')
    try {
      setSelected(await api.getTask(id))
      setDraft(null)
      setProfile(null)
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  async function openProfile(userId: number) {
    setProfileLoading(true)
    setError('')
    try {
      setProfile(await api.getUserProfile(userId))
      setDraft(null)
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setProfileLoading(false)
    }
  }

  async function createUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const roles = ['ASKER', 'DOER'].filter((role) => form.get(role) === 'on') as UserRole[]
    setSaving(true)
    setError('')
    try {
      const user = await api.createUser(String(form.get('displayName')), String(form.get('email')), roles)
      await loadUsers()
      selectUser(user.id)
      event.currentTarget.reset()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  async function saveTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeUser || !draft) return
    setSaving(true)
    setError('')
    try {
      const task = selected
        ? await api.updateTask(selected.id, draft, activeUser.id)
        : await api.createTask(draft, activeUser.id)
      setSelected(task)
      setDraft(null)
      await loadTasks()
      await loadNotifications()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  async function taskAction(action: (id: number, userId: number) => Promise<Task>) {
    if (!activeUser || !selected) return
    setSaving(true)
    setError('')
    try {
      setSelected(await action(selected.id, activeUser.id))
      await loadTasks()
      await loadNotifications()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  async function respondToStatusUpdate(statusUpdateId: number, response: string) {
    if (!activeUser || !selected) return
    setSaving(true)
    setError('')
    try {
      setSelected(await api.respondToStatusUpdate(selected.id, statusUpdateId, response, activeUser.id))
      await loadTasks()
      await loadNotifications()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  async function reviewTaskDrop(dropId: number, rating: number, review: string) {
    if (!activeUser || !selected) return
    setSaving(true)
    setError('')
    try {
      setSelected(await api.reviewTaskDrop(selected.id, dropId, rating, review, activeUser.id))
      await loadTasks()
      await loadNotifications()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  async function reviewTaskCompletion(rating: number, review: string) {
    if (!activeUser || !selected) return
    setSaving(true)
    setError('')
    try {
      setSelected(await api.reviewTaskCompletion(selected.id, rating, review, activeUser.id))
      await loadTasks()
      await loadNotifications()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  async function askQuestion(question: string) {
    if (!activeUser || !selected) return
    setSaving(true)
    setError('')
    try {
      setSelected(await api.askQuestion(selected.id, question, activeUser.id))
      await loadTasks()
      await loadNotifications()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  async function answerQuestion(questionId: number, answer: string) {
    if (!activeUser || !selected) return
    setSaving(true)
    setError('')
    try {
      setSelected(await api.answerQuestion(selected.id, questionId, answer, activeUser.id))
      await loadTasks()
      await loadNotifications()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  const canAsk = activeUser?.roles.includes('ASKER') ?? false
  const tasks = taskPage.content
  const firstTask = taskPage.totalElements === 0 ? 0 : page * pageSize + 1
  const lastTask = Math.min((page + 1) * pageSize, taskPage.totalElements)

  return (
    <main>
      <header>
        <div>
          <p className="eyebrow">COMMUNITY TASK BOARD</p>
          <h1>TaskIt</h1>
        </div>
        <div className="persona">
          <label htmlFor="active-user">Working as</label>
          <select id="active-user" value={activeUserId ?? ''} onChange={(event) => selectUser(event.target.value ? Number(event.target.value) : undefined)}>
            <option value="">Choose a user</option>
            {users.map((user) => <option key={user.id} value={user.id}>{user.displayName} · {user.roles.join(' + ')}</option>)}
          </select>
          {activeUser && <div className="persona-actions">
            <button className="link" onClick={() => void openProfile(activeUser.id)}>View profile</button>
            <button className="link" onClick={() => setShowNotifications((shown) => !shown)}>
              Notifications ({notifications.length})
            </button>
          </div>}
        </div>
      </header>

      {error && <p className="notice error" role="alert">{error}</p>}
      {showNotifications && activeUser && <section className="panel notifications">
        <div className="section-heading">
          <h2>Notifications</h2>
          <button className="link" onClick={() => setShowNotifications(false)}>Hide</button>
        </div>
        {notifications.length === 0 ? <p className="empty">No notifications yet.</p> :
          <ul className="notification-list">
            {notifications.map((notification) => <li key={notification.id}>
              <button className="link" onClick={() => { void selectTask(notification.taskId); setShowNotifications(false) }}>
                {notification.type === 'ASSISTANCE_REQUEST'
                  ? `${notification.actor.displayName} requested help with “${notification.taskTitle}”.`
                  : notification.type === 'TASK_QUESTION'
                    ? `${notification.actor.displayName} asked a question about “${notification.taskTitle}”.`
                    : `${notification.actor.displayName} completed “${notification.taskTitle}”. Leave a review.`}
              </button>
              <small>{new Date(notification.createdAt).toLocaleString()}</small>
            </li>)}
          </ul>}
      </section>}

      <section className="identity panel">
        <details>
          <summary>Create a user</summary>
          <form className="inline-form" onSubmit={createUser}>
            <input name="displayName" aria-label="Display name" placeholder="Display name" required maxLength={100} />
            <input name="email" aria-label="Email" type="email" placeholder="name@example.com" required maxLength={254} />
            <label><input name="ASKER" type="checkbox" defaultChecked /> Ask tasks</label>
            <label><input name="DOER" type="checkbox" defaultChecked /> Do tasks</label>
            <button disabled={saving}>Create user</button>
          </form>
        </details>
      </section>

      <section className="toolbar">
        <div className="tabs" aria-label="Task board views">
          {(Object.keys(viewLabels) as BoardView[]).map((option) =>
            <button className={view === option ? 'active' : ''} key={option} onClick={() => { setView(option); setPage(0) }}>
              {viewLabels[option]}
            </button>
          )}
        </div>
        <input value={category} onChange={(event) => { setCategory(event.target.value); setPage(0) }} placeholder="Filter by category" aria-label="Category filter" />
        <button className="primary" disabled={!canAsk} onClick={() => { setSelected(null); setDraft(emptyTask) }}>
          + Post a task
        </button>
      </section>

      {!activeUser && <p className="notice">Select or create a user to post tasks and use personal board views.</p>}

      <section className="content">
        <div className="board panel">
          <div className="section-heading">
            <h2>{viewLabels[view]}</h2>
            <label className="page-size">Tasks per page
              <select value={pageSize} onChange={(event) => {
                const size = Number(event.target.value)
                setPageSize(size)
                localStorage.setItem('taskit.taskPageSize', String(size))
                setPage(0)
              }}>
                {pageSizes.map((size) => <option key={size} value={size}>{size}</option>)}
              </select>
            </label>
          </div>
          {loading && <p>Loading tasks…</p>}
          {!loading && tasks.length === 0 && <p className="empty">No tasks here yet.</p>}
          <div className="task-grid">
            {tasks.map((task) => {
              const unansweredQuestionCount = task.questions.filter((question) => question.answer === null).length
              const answeredQuestionCount = task.questions.length - unansweredQuestionCount
              return (
                <button className={`task-card ${selected?.id === task.id ? 'selected' : ''}`} key={task.id} onClick={() => void selectTask(task.id)}>
                  <span className={`status ${task.status.toLowerCase()}`}>{task.status}</span>
                  {task.assistanceRequest && !task.assistanceRequest.helper && <span className="assistance">Needs assistance</span>}
                  {view === 'OPEN' && unansweredQuestionCount > 0 && <span className="question-indicator unanswered">
                    {unansweredQuestionCount} question{unansweredQuestionCount === 1 ? '' : 's'} awaiting an answer
                  </span>}
                  {view === 'OPEN' && answeredQuestionCount > 0 && <span className="question-indicator answered">
                    {answeredQuestionCount} answered question{answeredQuestionCount === 1 ? '' : 's'}
                  </span>}
                  <strong>{task.title}</strong>
                  <span>{task.category} · {task.remote ? 'Remote' : task.location}</span>
                  <small>Asked by {task.asker.displayName}</small>
                </button>
              )
            })}
          </div>
          {!loading && taskPage.totalElements > 0 && <div className="pagination">
            <span>Showing {firstTask}-{lastTask} of {taskPage.totalElements}</span>
            <div className="actions">
              <button disabled={page === 0} onClick={() => setPage((current) => Math.max(0, current - 1))}>Previous</button>
              <span>Page {page + 1} of {taskPage.totalPages}</span>
              <button disabled={page + 1 >= taskPage.totalPages} onClick={() => setPage((current) => Math.min(taskPage.totalPages - 1, current + 1))}>Next</button>
            </div>
          </div>}
        </div>
        <aside className="panel details">
          {profileLoading ? <p>Loading profile…</p> :
            profile ? <ProfileDetails profile={profile} onClose={() => setProfile(null)} onSelectTask={(id) => void selectTask(id)} /> :
            draft ? <TaskForm draft={draft} setDraft={setDraft} saving={saving} onSubmit={saveTask} editing={Boolean(selected)} /> :
            selected ? <TaskDetails task={selected} activeUser={activeUser} saving={saving}
              onEdit={() => setDraft({ title: selected.title, description: selected.description, category: selected.category, location: selected.location, remote: selected.remote })}
              onClaim={() => void taskAction(api.claimTask)}
              onComplete={() => void taskAction(api.completeTask)}
              onCancel={() => void taskAction(api.cancelTask)}
              onDrop={() => void taskAction(api.dropTask)}
              onRequestStatusUpdate={() => void taskAction(api.requestStatusUpdate)}
              onRequestAssistance={() => void taskAction(api.requestAssistance)}
              onOfferAssistance={() => void taskAction(api.offerAssistance)}
              onRespondToStatusUpdate={(statusUpdateId, response) => void respondToStatusUpdate(statusUpdateId, response)}
              onReviewDrop={(dropId, rating, review) => void reviewTaskDrop(dropId, rating, review)}
              onReviewCompletion={(rating, review) => void reviewTaskCompletion(rating, review)}
              onAskQuestion={(question) => void askQuestion(question)}
              onAnswerQuestion={(questionId, answer) => void answerQuestion(questionId, answer)}
              onViewProfile={(userId) => void openProfile(userId)} /> :
              <p className="empty">Select a task to see its details, or post one when acting as an asker.</p>}
        </aside>
      </section>
    </main>
  )
}

function TaskForm({ draft, setDraft, saving, onSubmit, editing }: {
  draft: TaskDraft
  setDraft: (draft: TaskDraft | null) => void
  saving: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => Promise<void>
  editing: boolean
}) {
  function update<K extends keyof TaskDraft>(key: K, value: TaskDraft[K]) {
    setDraft({ ...draft, [key]: value })
  }
  return (
    <>
      <h2>{editing ? 'Edit task' : 'Post a task'}</h2>
      <form className="task-form" onSubmit={(event) => void onSubmit(event)}>
        <label>Title<input value={draft.title} onChange={(event) => update('title', event.target.value)} required maxLength={120} /></label>
        <label>Description<textarea value={draft.description} onChange={(event) => update('description', event.target.value)} required maxLength={4000} rows={5} /></label>
        <label>Category<input value={draft.category} onChange={(event) => update('category', event.target.value)} required maxLength={80} placeholder="e.g. Moving" /></label>
        <label>Location<input value={draft.location} onChange={(event) => update('location', event.target.value)} required maxLength={160} placeholder="e.g. Wellington" /></label>
        <label className="checkbox"><input type="checkbox" checked={draft.remote} onChange={(event) => update('remote', event.target.checked)} /> This task can be done remotely</label>
        <div className="actions"><button className="primary" disabled={saving}>{saving ? 'Saving…' : 'Save task'}</button><button type="button" onClick={() => setDraft(null)}>Cancel</button></div>
      </form>
    </>
  )
}

function TaskDetails({
  task,
  activeUser,
  saving,
  onEdit,
  onClaim,
  onComplete,
  onCancel,
  onDrop,
  onRequestStatusUpdate,
  onRequestAssistance,
  onOfferAssistance,
  onRespondToStatusUpdate,
  onReviewDrop,
  onReviewCompletion,
  onAskQuestion,
  onAnswerQuestion,
  onViewProfile,
}: {
  task: Task
  activeUser?: User
  saving: boolean
  onEdit: () => void
  onClaim: () => void
  onComplete: () => void
  onCancel: () => void
  onDrop: () => void
  onRequestStatusUpdate: () => void
  onRequestAssistance: () => void
  onOfferAssistance: () => void
  onRespondToStatusUpdate: (statusUpdateId: number, response: string) => void
  onReviewDrop: (dropId: number, rating: number, review: string) => void
  onReviewCompletion: (rating: number, review: string) => void
  onAskQuestion: (question: string) => void
  onAnswerQuestion: (questionId: number, answer: string) => void
  onViewProfile: (userId: number) => void
}) {
  const isAsker = activeUser?.id === task.asker.id
  const isDoer = activeUser?.id === task.assignedDoer?.id
  const canClaim = task.status === 'OPEN' && activeUser?.roles.includes('DOER') && !isAsker
  const canComplete = task.status === 'CLAIMED' && (isAsker || isDoer)
  const canCancel = (task.status === 'OPEN' || task.status === 'CLAIMED') && isAsker
  const canDrop = task.status === 'CLAIMED' && isDoer
  const canRequestStatusUpdate = task.status === 'CLAIMED' && isAsker
  const canRequestAssistance = task.status === 'CLAIMED' && isDoer && !task.assistanceRequest
  const canOfferAssistance = task.status === 'CLAIMED'
    && Boolean(task.assistanceRequest)
    && !task.assistanceRequest?.helper
    && activeUser?.roles.includes('DOER')
    && !isAsker
    && !isDoer
  const canAskQuestion = task.status === 'OPEN' && activeUser?.roles.includes('DOER')
  return (
    <>
      <span className={`status ${task.status.toLowerCase()}`}>{task.status}</span>
      <h2>{task.title}</h2>
      <p className="description">{task.description}</p>
      <dl>
        <dt>Category</dt><dd>{task.category}</dd>
        <dt>Where</dt><dd>{task.remote ? 'Remote' : task.location}</dd>
        <dt>Asker</dt><dd><ProfileLink user={task.asker} onViewProfile={onViewProfile} /></dd>
        {task.assignedDoer && <><dt>Doer</dt><dd><ProfileLink user={task.assignedDoer} onViewProfile={onViewProfile} /></dd></>}
      </dl>
      {task.assistanceRequest && <section className="status-updates">
        <h3>Assistance</h3>
        <div className="status-update">
          <p><ProfileLink user={task.assistanceRequest.requestingDoer} onViewProfile={onViewProfile} /> requested help.</p>
          {task.assistanceRequest.helper
            ? <p><ProfileLink user={task.assistanceRequest.helper} onViewProfile={onViewProfile} /> offered to assist. The assigned doer remains in control.</p>
            : <p>This claimed task needs another doer to offer assistance.</p>}
        </div>
      </section>}
      <div className="actions">
        {task.status === 'OPEN' && isAsker && <button onClick={onEdit}>Edit</button>}
        {canClaim && <button className="primary" disabled={saving} onClick={onClaim}>Claim task</button>}
        {canComplete && <button className="primary" disabled={saving} onClick={onComplete}>Mark complete</button>}
        {canDrop && <button className="danger" disabled={saving} onClick={onDrop}>Drop task</button>}
        {canCancel && <button className="danger" disabled={saving} onClick={onCancel}>Cancel task</button>}
        {canRequestStatusUpdate && <button disabled={saving} onClick={onRequestStatusUpdate}>Request status update</button>}
        {canRequestAssistance && <button disabled={saving} onClick={onRequestAssistance}>Request assistance</button>}
        {canOfferAssistance && <button className="primary" disabled={saving} onClick={onOfferAssistance}>Offer assistance</button>}
      </div>
      {(task.questions.length > 0 || canAskQuestion) && <section className="status-updates">
        <h3>Questions</h3>
        {task.questions.map((question) => (
          <div key={question.id} className="status-update">
            <p><ProfileLink user={question.askingDoer} onViewProfile={onViewProfile} /> asks: {question.question}</p>
            {question.answer ? <p><strong>Answer:</strong> {question.answer}</p> :
              isAsker ? <QuestionForm saving={saving} label="Answer" submitLabel="Send answer" onSubmit={(answer) => onAnswerQuestion(question.id, answer)} /> :
                <p>Awaiting an answer from the asker.</p>}
          </div>
        ))}
        {canAskQuestion && <QuestionForm saving={saving} label="Ask a question" submitLabel="Post question" onSubmit={onAskQuestion} />}
      </section>}
      {task.statusUpdates.length > 0 && <section className="status-updates">
        <h3>Status updates</h3>
        {task.statusUpdates.map((statusUpdate) => (
          <div key={statusUpdate.id} className="status-update">
            {statusUpdate.response ? <p>{statusUpdate.response}</p> :
              isDoer ? <StatusUpdateForm saving={saving} onSubmit={(response) => onRespondToStatusUpdate(statusUpdate.id, response)} /> :
                <p>Awaiting an update from the doer.</p>}
            <small>Requested {new Date(statusUpdate.requestedAt).toLocaleString()}</small>
          </div>
        ))}
      </section>}
      {task.status === 'COMPLETED' && task.assignedDoer && <section className="status-updates">
        <h3>Completion review</h3>
        {task.completionReview ? <div className="status-update">
          <p>{task.completionReview.rating}/5 — {task.completionReview.review || 'No written review.'}</p>
        </div> :
          isAsker ? <ReviewForm saving={saving} onSubmit={onReviewCompletion} /> :
            <p>Awaiting a review from the asker.</p>}
      </section>}
      {task.drops.length > 0 && <section className="status-updates">
        <h3>Dropped assignments</h3>
        {task.drops.map((drop) => (
          <div key={drop.id} className="status-update">
            <p><ProfileLink user={drop.doer} onViewProfile={onViewProfile} /> dropped this task on {new Date(drop.droppedAt).toLocaleString()}.</p>
            {drop.rating !== null ? <p>{drop.rating}/5 — {drop.review || 'No written review.'}</p> :
              isAsker ? <ReviewForm saving={saving} onSubmit={(rating, review) => onReviewDrop(drop.id, rating, review)} /> :
                <p>Waiting for the asker to review this drop.</p>}
          </div>
        ))}
      </section>}
    </>
  )
}

function ReviewForm({ saving, onSubmit }: { saving: boolean; onSubmit: (rating: number, review: string) => void }) {
  const [rating, setRating] = useState(1)
  const [review, setReview] = useState('')
  return (
    <form className="task-form" onSubmit={(event) => { event.preventDefault(); onSubmit(rating, review) }}>
      <p>Would you like to rate this doer?</p>
      <label>Rating
        <select value={rating} onChange={(event) => setRating(Number(event.target.value))}>
          {[1, 2, 3, 4, 5].map((value) => <option key={value} value={value}>{value} / 5</option>)}
        </select>
      </label>
      <label>Review (optional)<textarea value={review} onChange={(event) => setReview(event.target.value)} maxLength={2000} rows={3} /></label>
      <button className="primary" disabled={saving}>{saving ? 'Sending…' : 'Submit review'}</button>
    </form>
  )
}

function StatusUpdateForm({ saving, onSubmit }: { saving: boolean; onSubmit: (response: string) => void }) {
  const [response, setResponse] = useState('')
  return (
    <form className="task-form" onSubmit={(event) => { event.preventDefault(); onSubmit(response) }}>
      <label>Status update<textarea value={response} onChange={(event) => setResponse(event.target.value)} required maxLength={2000} rows={3} /></label>
      <button className="primary" disabled={saving}>{saving ? 'Sending…' : 'Send update'}</button>
    </form>
  )
}

function QuestionForm({ saving, label, submitLabel, onSubmit }: {
  saving: boolean
  label: string
  submitLabel: string
  onSubmit: (question: string) => void
}) {
  const [value, setValue] = useState('')
  return (
    <form className="task-form" onSubmit={(event) => {
      event.preventDefault()
      onSubmit(value)
      setValue('')
    }}>
      <label>{label}<textarea value={value} onChange={(event) => setValue(event.target.value)} required maxLength={2000} rows={3} /></label>
      <button className="primary" disabled={saving}>{saving ? 'Sending…' : submitLabel}</button>
    </form>
  )
}

function ProfileLink({ user, onViewProfile }: { user: User; onViewProfile: (userId: number) => void }) {
  return <button className="link" type="button" onClick={() => onViewProfile(user.id)}>{user.displayName}</button>
}

function ProfileDetails({ profile, onClose, onSelectTask }: {
  profile: UserProfile
  onClose: () => void
  onSelectTask: (taskId: number) => void
}) {
  return (
    <>
      <div className="section-heading">
        <h2>{profile.displayName}</h2>
        <button className="link" onClick={onClose}>Close profile</button>
      </div>
      <p>{profile.email} · {profile.roles.join(' + ')}</p>
      <p><strong>Average received rating:</strong> {profile.averageReceivedRating === null ? 'No ratings yet' : `${profile.averageReceivedRating.toFixed(1)} / 5`}</p>
      <ProfileTaskSection title="Requested tasks" tasks={profile.requestedTasks} onSelectTask={onSelectTask} />
      <ProfileTaskSection title="Current assignments" tasks={profile.currentAssignments} onSelectTask={onSelectTask} />
      <section className="status-updates">
        <h3>Reviews received</h3>
        {profile.reviews.length === 0 ? <p className="empty">No written reviews yet.</p> :
          profile.reviews.map((review) => <div className="status-update" key={`${review.task.id}-${review.reviewedAt}`}>
            <p><button className="link" onClick={() => onSelectTask(review.task.id)}>{review.task.title}</button> — {review.rating}/5</p>
            <p>{review.review}</p>
          </div>)}
      </section>
      <section className="status-updates">
        <h3>Prior dropped assignments</h3>
        {profile.priorAssignments.length === 0 ? <p className="empty">No dropped assignments.</p> :
          profile.priorAssignments.map((assignment) => <div className="status-update" key={`${assignment.task.id}-${assignment.droppedAt}`}>
            <p><button className="link" onClick={() => onSelectTask(assignment.task.id)}>{assignment.task.title}</button> — dropped {new Date(assignment.droppedAt).toLocaleDateString()}</p>
            {assignment.rating !== null && <p>{assignment.rating}/5 — {assignment.review || 'No written review.'}</p>}
          </div>)}
      </section>
    </>
  )
}

function ProfileTaskSection({ title, tasks, onSelectTask }: {
  title: string
  tasks: UserProfile['requestedTasks']
  onSelectTask: (taskId: number) => void
}) {
  return (
    <section className="status-updates">
      <h3>{title}</h3>
      {tasks.length === 0 ? <p className="empty">None.</p> :
        tasks.map((task) => <div className="status-update" key={task.id}>
          <button className="link" onClick={() => onSelectTask(task.id)}>{task.title}</button>
          <span className={`status ${task.status.toLowerCase()}`}>{task.status}</span>
        </div>)}
    </section>
  )
}
