import { describe, expect, it } from 'vitest';
import { ConfigError, DEV_DB_PASSWORD, loadConfig } from '../src/config';

const production = {
  NODE_ENV: 'production',
  JWT_SECRET: 'x'.repeat(40),
  DB_USER: 'miniprojet',
  DB_PASSWORD: 'un-vrai-mot-de-passe',
};

describe('configuration', () => {
  it('démarre en développement sans rien configurer, en inventant le secret des jetons', () => {
    const config = loadConfig({});
    expect(config.jwt.secret.length).toBeGreaterThanOrEqual(32);
    expect(config.generated).toEqual(['JWT_SECRET']);
    expect(config.db.password).toBe(DEV_DB_PASSWORD);
  });

  it('accepte une configuration de production complète', () => {
    expect(loadConfig(production).generated).toEqual([]);
  });

  it.each([
    ['sans JWT_SECRET', { JWT_SECRET: undefined }, 'JWT_SECRET est requis'],
    ['avec un JWT_SECRET trop court', { JWT_SECRET: 'court' }, 'au moins 32'],
    ['sans DB_PASSWORD', { DB_PASSWORD: undefined }, 'DB_PASSWORD est requis'],
    ['avec un DB_PASSWORD vide', { DB_PASSWORD: '' }, 'ni vide'],
    ['avec le mot de passe du développement', { DB_PASSWORD: DEV_DB_PASSWORD }, 'ni vide'],
    ['avec le compte root', { DB_USER: 'root' }, 'root'],
  ])('refuse la production %s', (_name, change, message) => {
    const env = { ...production, ...change } as NodeJS.ProcessEnv;
    expect(() => loadConfig(env)).toThrowError(ConfigError);
    expect(() => loadConfig(env)).toThrowError(message);
  });

  it('refuse un port invalide', () => {
    expect(() => loadConfig({ PORT: 'abc' })).toThrowError(ConfigError);
  });
});
