#!/usr/bin/env bash
#
# Checks commit subjects against the convention written down in CONTRIBUTING.md.
#
# A convention that is only described is a suggestion. This is the thing that
# makes it a rule, which is also why it lives in a file you can run rather than
# only in a workflow: before pushing, run it.
#
#   .github/scripts/check-commit-subjects.sh                 # origin/main..HEAD
#   .github/scripts/check-commit-subjects.sh <base> <head>   # an explicit range
#
set -euo pipefail

if [ -n "${2:-}" ]; then
  range="$1..$2"
elif [ -n "${1:-}" ]; then
  range="$1"
else
  range="origin/main..HEAD"
fi

# type(scope): description
#
# The description may start with a digit, because a release commit is named
# after the version it declares and `chore(release): 1.2.1` is the clearest
# thing that commit can say.
pattern='^(build|chore|ci|docs|feat|fix|perf|refactor|revert|style|test)(\([a-z0-9:._/-]+\))?!?: [a-z0-9].*[^.]$'

# Long enough for a scope and a real sentence, short enough to read in a log.
limit=80

failed=0
checked=0

while IFS= read -r subject; do
  [ -z "$subject" ] && continue

  # A merge commit's subject is written by Git, not by a person. History here is
  # kept linear so these should not appear, but a check that fails on something
  # nobody typed teaches people to ignore it.
  case "$subject" in
    "Merge "*) continue ;;
  esac

  checked=$((checked + 1))

  if ! printf '%s' "$subject" | grep -Eq "$pattern"; then
    printf 'not in the convention:\n  %s\n\n' "$subject"
    failed=1
  elif [ "${#subject}" -gt "$limit" ]; then
    printf '%s characters, over the limit of %s:\n  %s\n\n' \
      "${#subject}" "$limit" "$subject"
    failed=1
  fi
done < <(git log --format=%s "$range")

if [ "$failed" -ne 0 ]; then
  cat <<'EOF'
Subjects follow Conventional Commits:

  type(scope): summary in the imperative, lowercase, no full stop

type is one of build, chore, ci, docs, feat, fix, perf, refactor, revert, style
or test. scope is the module or feature the change lives in and may be left out
when a change is genuinely project-wide.

CONTRIBUTING.md has the reasoning and an example of a good message body.
EOF
  exit 1
fi

printf '%s commit subject(s) checked, all in the convention.\n' "$checked"
