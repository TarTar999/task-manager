import { apiFetch } from './client';
import type { Task, TaskStatus } from '../types';

export interface TaskPayload {
  title: string;
  description: string | null;
  status: TaskStatus;
}

export function getTasks(status?: TaskStatus | '', search?: string) {
  // on n'envoie que les paramètres réellement remplis, le back gère les absents
  const params = new URLSearchParams();
  if (status) params.set('status', status);
  if (search) params.set('search', search);

  const query = params.toString();
  return apiFetch<Task[]>(`/api/tasks${query ? `?${query}` : ''}`);
}

export function createTask(payload: TaskPayload) {
  return apiFetch<Task>('/api/tasks', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateTask(id: number, payload: TaskPayload) {
  return apiFetch<Task>(`/api/tasks/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function deleteTask(id: number) {
  return apiFetch<void>(`/api/tasks/${id}`, { method: 'DELETE' });
}
