#!/usr/bin/env python3
"""Regenerates the sound name json files from RuneLite's gameval ids.

    python3 scripts/update_sound_names.py

Downloads AmbientSoundID.java and SoundEffectID.java from the RuneLite master branch and rewrites
ambient_sound_names.json and sound_names.json in src/main/resources/com/brooklyn/annoyancemute/.
Each file maps a lowercase constant name to its id. The plugin reads them to let names be used in the muted sound configs,
and the debug overlays use them to label sounds.
The files are only written when their content changes. Pass --check to only report whether they are up to date
(exit code 1 if not), and --ref <branch|tag|sha> to read from something other than master.
"""
import argparse
import re
import sys
import urllib.request
from pathlib import Path

RAW_URL = "https://raw.githubusercontent.com/runelite/runelite/{ref}/runelite-api/src/main/java/net/runelite/api/gameval/{name}.java"
RESOURCE_DIR = Path(__file__).resolve().parent.parent / "src/main/resources/com/brooklyn/annoyancemute"
CONSTANT = re.compile(r"public static final int ([A-Za-z0-9_]+) = (-?\d+);")


def fetch(name, ref):
    with urllib.request.urlopen(RAW_URL.format(ref=ref, name=name), timeout=60) as response:
        return response.read().decode("utf-8")


def parse(source):
    """lowercase name -> id, ordered by id. Constants sharing an id are all kept, in source order."""
    names = {}
    for constant, value in CONSTANT.findall(source):
        names.setdefault(constant.lower(), int(value))
    return dict(sorted(names.items(), key=lambda entry: entry[1]))


def render(names):
    """JSON object of name -> id, one entry per line so diffs stay readable."""
    entries = [f'  "{name}": {value}' for name, value in names.items()]
    return "{\n" + ",\n".join(entries) + "\n}\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--check", action="store_true", help="don't write, exit 1 if the files are out of date")
    parser.add_argument("--ref", default="master", help="RuneLite branch, tag or commit to read (default: master)")
    args = parser.parse_args()

    targets = [
        ("AmbientSoundID", "ambient_sound_names.json", 1),
        ("SoundEffectID", "sound_names.json", 1000),
    ]

    stale = False
    for source_name, file_name, minimum in targets:
        names = parse(fetch(source_name, args.ref))
        if len(names) < minimum:
            # the source format probably changed; don't wipe the maps
            sys.exit(f"{source_name}: only found {len(names)} ids, expected at least {minimum}. Has the format changed?")

        path = RESOURCE_DIR / file_name
        content = render(names)
        current = path.read_text(encoding="utf-8") if path.exists() else None
        if current == content:
            print(f"{file_name}: up to date ({len(names)} names)")
            continue

        stale = True
        if args.check:
            print(f"{file_name}: out of date")
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8", newline="\n")
            print(f"{file_name}: updated ({len(names)} names)")

    if args.check and stale:
        sys.exit(1)


if __name__ == "__main__":
    main()
