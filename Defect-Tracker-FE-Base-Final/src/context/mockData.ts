import { Project } from '../types';




export const mockProjects: Project[] = [];


export function getNextDefectId(defects: { id: string; projectId: string }[], projectId: string): string {
  const projectDefects = defects.filter((d: { id: string; projectId: string }) => d.projectId === projectId);
  const ids = projectDefects
    .map((d: { id: string }) => d.id)
    .map((id: string) => parseInt(id.replace("DEF-", "")))
    .filter((n: number) => !isNaN(n));
  const nextNum = ids.length > 0 ? Math.max(...ids) + 1 : 1;
  return `DEF-${nextNum.toString().padStart(4, "0")}`;
}


