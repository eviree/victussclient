// Victus launcher: auto-install the right Victus Client mod jar (+ Fabric API) for a profile.
// Works in Node 18+ / Electron main process. Call before launching a Fabric profile:
//
//   await ensureVictusMod(path.join(profileDir, 'mods'), '1.21.4');
//
// Set REPO to your GitHub "owner/repo".

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const REPO = 'YOUR_GITHUB_USERNAME/victusclient';
const MANIFEST_URL = `https://github.com/${REPO}/releases/latest/download/versions.json`;
const UA = 'VictusLauncher/1.0 (victusclient)';

const sha256 = (buf) => crypto.createHash('sha256').update(buf).digest('hex');

async function getBuffer(url) {
  const res = await fetch(url, { headers: { 'User-Agent': UA } });
  if (!res.ok) throw new Error(`Download failed (${res.status}): ${url}`);
  return Buffer.from(await res.arrayBuffer());
}

/** Installs/updates the Victus mod jar matching mcVersion. Returns a status object. */
async function ensureVictusMod(modsDir, mcVersion) {
  fs.mkdirSync(modsDir, { recursive: true });

  const manifest = JSON.parse((await getBuffer(MANIFEST_URL)).toString('utf8'));
  const build = manifest.builds.find((b) => b.minecraft.includes(mcVersion));
  if (!build) return { installed: false, reason: `Victus mod does not support Minecraft ${mcVersion} yet` };

  const target = path.join(modsDir, build.file);
  if (fs.existsSync(target) && sha256(fs.readFileSync(target)) === build.sha256) {
    return { installed: true, updated: false, file: build.file };
  }

  const jar = await getBuffer(build.url);
  if (sha256(jar) !== build.sha256) throw new Error('Victus mod download is corrupted (hash mismatch)');

  // remove any older Victus jars (other versions / old releases)
  for (const f of fs.readdirSync(modsDir)) {
    if (/^victusclient-.*\.jar$/i.test(f)) fs.unlinkSync(path.join(modsDir, f));
  }
  fs.writeFileSync(target, jar);
  return { installed: true, updated: true, file: build.file, version: manifest.version };
}

/** Fabric API is required by the Victus mod. Downloads the latest release for mcVersion from Modrinth. */
async function ensureFabricApi(modsDir, mcVersion) {
  fs.mkdirSync(modsDir, { recursive: true });
  const url = 'https://api.modrinth.com/v2/project/fabric-api/version'
    + `?game_versions=${encodeURIComponent(JSON.stringify([mcVersion]))}`
    + `&loaders=${encodeURIComponent(JSON.stringify(['fabric']))}`;
  const versions = JSON.parse((await getBuffer(url)).toString('utf8'));
  const latest = versions.find((v) => v.version_type === 'release') || versions[0];
  if (!latest) return { installed: false, reason: `No Fabric API for ${mcVersion}` };

  const file = latest.files.find((f) => f.primary) || latest.files[0];
  const target = path.join(modsDir, file.filename);
  if (fs.existsSync(target)) return { installed: true, updated: false, file: file.filename };

  const jar = await getBuffer(file.url);
  if (file.hashes && file.hashes.sha512) {
    const h = crypto.createHash('sha512').update(jar).digest('hex');
    if (h !== file.hashes.sha512) throw new Error('Fabric API hash mismatch');
  }
  for (const f of fs.readdirSync(modsDir)) {
    if (/^fabric-api-.*\.jar$/i.test(f)) fs.unlinkSync(path.join(modsDir, f));
  }
  fs.writeFileSync(target, jar);
  return { installed: true, updated: true, file: file.filename };
}

module.exports = { ensureVictusMod, ensureFabricApi };
