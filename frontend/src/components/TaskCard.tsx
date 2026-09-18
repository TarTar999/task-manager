import type { Task } from '../types';
import { STATUS_COLORS, STATUS_LABELS } from '../types';

interface TaskCardProps {
  task: Task;
  onEdit: (task: Task) => void;
  onDelete: (task: Task) => void;
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function TaskCard({ task, onEdit, onDelete }: TaskCardProps) {
  return (
    <article className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:shadow-md">
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-semibold text-slate-800">{task.title}</h3>
        <span
          className={`shrink-0 rounded-full border px-2.5 py-0.5 text-xs font-medium ${STATUS_COLORS[task.status]}`}
        >
          {STATUS_LABELS[task.status]}
        </span>
      </div>

      {task.description && (
        <p className="text-sm text-slate-600 whitespace-pre-wrap">{task.description}</p>
      )}

      {/* mt-auto pour que la ligne du bas reste alignée entre cartes de hauteurs différentes */}
      <div className="mt-auto flex items-center justify-between border-t border-slate-100 pt-3">
        <time className="text-xs text-slate-400">Créée le {formatDate(task.createdAt)}</time>
        <div className="flex gap-1">
          <button
            onClick={() => onEdit(task)}
            className="rounded-lg px-2.5 py-1.5 text-xs font-medium text-indigo-600 hover:bg-indigo-50"
            aria-label={`Modifier ${task.title}`}
          >
            Modifier
          </button>
          <button
            onClick={() => onDelete(task)}
            className="rounded-lg px-2.5 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50"
            aria-label={`Supprimer ${task.title}`}
          >
            Supprimer
          </button>
        </div>
      </div>
    </article>
  );
}
