export interface Logger {
  info(message: string): void;
  warn(message: string): void;
  error(message: string): void;
}

const stamp = () => new Date().toISOString();

/** Journal sur la sortie standard : jamais un corps de requête, un mot de passe ni un jeton (aucun appelant n'en reçoit). */
export const consoleLogger: Logger = {
  info: (m) => console.log(`${stamp()} INFO  ${m}`),
  warn: (m) => console.warn(`${stamp()} WARN  ${m}`),
  error: (m) => console.error(`${stamp()} ERROR ${m}`),
};

export const silentLogger: Logger = { info: () => {}, warn: () => {}, error: () => {} };
