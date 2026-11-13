#!/usr/bin/env python3
"""Builds dist/versions.json from versions/*.properties and the jars in dist/."""
import glob, hashlib, json, os, sys

tag, repo = sys.argv[1], sys.argv[2]
builds = []
for pf in sorted(glob.glob("versions/*.properties")):
    target = os.path.basename(pf)[:-len(".properties")]
    props = {}
    for line in open(pf, encoding="utf-8"):
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            props[k.strip()] = v.strip()
    jar = f"victusclient-{target}.jar"
    path = os.path.join("dist", jar)
    if not os.path.exists(path):
        print("skip (not built):", jar)
        continue
    builds.append({
        "target": target,
        "minecraft": [v for v in props["mc_supported"].split(",") if v],
        "file": jar,
        "url": f"https://github.com/{repo}/releases/download/{tag}/{jar}",
        "sha256": hashlib.sha256(open(path, "rb").read()).hexdigest(),
    })

manifest = {"mod": "victusclient", "version": tag.lstrip("v"), "builds": builds}
json.dump(manifest, open("dist/versions.json", "w"), indent=2)
print(json.dumps(manifest, indent=2))
