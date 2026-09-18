import { useCallback, useEffect, useState } from 'react';
import { createTask, deleteTask, getTasks, updateTask, type TaskPayload } from '../api/tasks';
import TaskCard from '../components/TaskCard';
import TaskFormModal from '../components/TaskFormModal';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';
import type { Task, TaskStatus } from '../types';
import { STATUS_LABELS } from '../types';

export default function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<TaskStatus | ''>('');
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);

  const { user, logout } = useAuth();
  const { showToast } = useToast();

  const loadTasks = useCallback(async () => {
    setLoading(true);
    try {
      setTasks(await getTasks(statusFilter, search));
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Erreur de chargement');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, search, showToast]);

  // Le filtrage se fait côté API, donc on attend 300ms avant de partir :
  // sans ça c'est une requête par touche tapée dans la recherche.
  useEffect(() => {
    const timer = setTimeout(loadTasks, 300);
    return () => clearTimeout(timer);
  }, [loadTasks]);

  async function handleCreate(payload: TaskPayload) {
    try {
      await createTask(payload);
      showToast('success', 'Tâche créée');
      await loadTasks();
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Erreur lors de la création');
      throw err; // le modal reste ouvert pour que l'utilisateur récupère sa saisie
    }
  }

  async function handleUpdate(payload: TaskPayload) {
    if (!editingTask) return;
    try {
      await updateTask(editingTask.id, payload);
      showToast('success', 'Tâche mise à jour');
      await loadTasks();
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Erreur lors de la modification');
      throw err;
    }
  }

  async function handleDelete(task: Task) {
    // confirm natif, suffisant ici (un vrai produit mériterait une modale maison)
    if (!window.confirm(`Supprimer la tâche « ${task.title} » ?`)) return;

    try {
      await deleteTask(task.id);
      showToast('success', 'Tâche supprimée');
      await loadTasks();
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Erreur lors de la suppression');
    }
  }

  function openCreateModal() {
    setEditingTask(null);
    setModalOpen(true);
  }

  function openEditModal(task: Task) {
    setEditingTask(task);
    setModalOpen(true);
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <div>
            <h1 className="text-lg font-bold text-slate-800">Task Manager</h1>
            <p className="text-xs text-slate-500">Connecté en tant que {user?.name}</p>
          </div>
          <button
            onClick={logout}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Déconnexion
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-6">
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center">
          <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Rechercher une tâche..."
            className="flex-1 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
          />

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as TaskStatus | '')}
            className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
          >
            <option value="">Tous les statuts</option>
            {(Object.keys(STATUS_LABELS) as TaskStatus[]).map((value) => (
              <option key={value} value={value}>
                {STATUS_LABELS[value]}
              </option>
            ))}
          </select>

          <button
            onClick={openCreateModal}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            + Nouvelle tâche
          </button>
        </div>

        {loading ? (
          <p className="py-12 text-center text-sm text-slate-500">Chargement des tâches...</p>
        ) : tasks.length === 0 ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-white py-12 text-center">
            <p className="text-sm text-slate-500">
              {search || statusFilter
                ? 'Aucune tâche ne correspond à vos critères.'
                : 'Aucune tâche pour le moment. Créez votre première tâche !'}
            </p>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {tasks.map((task) => (
              <TaskCard key={task.id} task={task} onEdit={openEditModal} onDelete={handleDelete} />
            ))}
          </div>
        )}
      </main>

      {modalOpen && (
        <TaskFormModal
          task={editingTask}
          onClose={() => setModalOpen(false)}
          onSubmit={editingTask ? handleUpdate : handleCreate}
        />
      )}
    </div>
  );
}
