import { mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';

export const EXTENSIONS = ['jpeg', 'jpg', 'png', 'webp'] as const;
export type Extension = (typeof EXTENSIONS)[number];

export type ImageRef = { kind: 'avatar'; id: number; ext: Extension } | { kind: 'annonce'; id: number; index: number; ext: Extension };

const NAME = /^image(?:Client-\[(\d{1,10})\]|Annonce-\[(\d{1,10})\]-\[(\d{1,2})\])\.(jpeg|jpg|png|webp)$/;

/** Le nom qu'une image porte (celui que l'application demande), ou null : on ne lit jamais autre chose que ces noms-là. */
export function parseImageName(name: string): ImageRef | null {
  const match = NAME.exec(name);
  if (!match) return null;
  const ext = match[4] as Extension;
  if (match[1] !== undefined) return { kind: 'avatar', id: Number(match[1]), ext };
  return { kind: 'annonce', id: Number(match[2]), index: Number(match[3]), ext };
}

export function imageName(ref: ImageRef): string {
  return ref.kind === 'avatar' ? `imageClient-[${ref.id}].${ref.ext}` : `imageAnnonce-[${ref.id}]-[${ref.index}].${ref.ext}`;
}

/** Le vrai format d'après les premiers octets : ce que l'expéditeur dit être n'est pas ce qu'on enregistre. */
export function detectFormat(buffer: Buffer): 'jpeg' | 'png' | 'webp' | null {
  if (buffer.length >= 3 && buffer[0] === 0xff && buffer[1] === 0xd8 && buffer[2] === 0xff) return 'jpeg';
  if (buffer.length >= 8 && buffer.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]))) return 'png';
  if (buffer.length >= 12 && buffer.subarray(0, 4).toString('latin1') === 'RIFF' && buffer.subarray(8, 12).toString('latin1') === 'WEBP') return 'webp';
  return null;
}

export const extensionMatches = (ext: Extension, detected: 'jpeg' | 'png' | 'webp'): boolean =>
  ext === detected || (ext === 'jpg' && detected === 'jpeg');

/**
 * Les images sur le disque : `<dir>/client/imageClient-[id].ext` et `<dir>/annonce/imageAnnonce-[id]-[index].ext`.
 * Aucun chemin n'est construit à partir de ce que le client écrit : seulement d'entiers validés et d'une liste fermée d'extensions.
 */
export class Images {
  constructor(
    private readonly dir: string,
    readonly maxBytes: number,
  ) {}

  private folder(ref: ImageRef): string {
    return path.join(this.dir, ref.kind === 'avatar' ? 'client' : 'annonce');
  }

  private file(ref: ImageRef): string {
    return path.join(this.folder(ref), imageName(ref));
  }

  async save(ref: ImageRef, data: Buffer): Promise<void> {
    await mkdir(this.folder(ref), { recursive: true });
    // une seule image par emplacement : l'ancienne, d'une autre extension, ne reste pas
    await this.removeSlot(ref);
    await writeFile(this.file(ref), data);
  }

  async read(ref: ImageRef): Promise<Buffer | null> {
    try {
      return await readFile(this.file(ref));
    } catch {
      return null;
    }
  }

  private async removeSlot(ref: ImageRef): Promise<void> {
    for (const ext of EXTENSIONS) await rm(this.file({ ...ref, ext }), { force: true });
  }

  async removeAvatar(idClient: number): Promise<void> {
    await this.removeSlot({ kind: 'avatar', id: idClient, ext: 'jpeg' });
  }

  async removeAnnonceImages(idAnnonce: number): Promise<void> {
    const folder = path.join(this.dir, 'annonce');
    let names: string[] = [];
    try {
      names = await readdir(folder);
    } catch {
      return;
    }
    for (const name of names) {
      const ref = parseImageName(name);
      if (ref?.kind === 'annonce' && ref.id === idAnnonce) await rm(path.join(folder, name), { force: true });
    }
  }
}
