export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface Task {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  createdAt: string;
  // null tant que la tâche n'a jamais été modifiée
  updatedAt: string | null;
}

export interface User {
  id: number;
  name: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

// format renvoyé par GlobalExceptionHandler côté Spring
export interface ApiError {
  timestamp: string;
  status: number;
  message: string;
}

// l'ordre des clés sert aussi pour l'ordre du select de filtre
export const STATUS_LABELS: Record<TaskStatus, string> = {
  TODO: 'À faire',
  IN_PROGRESS: 'En cours',
  DONE: 'Terminée',
};

export const STATUS_COLORS: Record<TaskStatus, string> = {
  TODO: 'bg-slate-100 text-slate-700 border-slate-200',
  IN_PROGRESS: 'bg-amber-100 text-amber-700 border-amber-200',
  DONE: 'bg-emerald-100 text-emerald-700 border-emerald-200',
};
