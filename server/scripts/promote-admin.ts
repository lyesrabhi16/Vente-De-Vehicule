import { loadConfig } from '../src/config';
import { createDb, run } from '../src/db';

/**
 * Donne le rôle d'administrateur à un compte existant : `npm run promote-admin -- adresse@exemple.fr`.
 * Le compte se crée d'abord par l'inscription de l'application ; aucun mot de passe ne passe par ici.
 */
async function main(): Promise<void> {
  const email = process.argv[2]?.trim().toLowerCase();
  if (!email) throw new Error('Usage : npm run promote-admin -- adresse@exemple.fr');
  const db = createDb(loadConfig());
  try {
    const result = await run(db, "UPDATE client SET role = 'admin' WHERE email = ?", [email]);
    if (result.affectedRows === 0) throw new Error(`Aucun compte avec l'adresse ${email} (inscrivez-vous d'abord dans l'application)`);
    console.log(`${email} est maintenant administrateur`);
  } finally {
    await db.end();
  }
}

main().catch((error: Error) => {
  console.error(error.message);
  process.exit(1);
});
