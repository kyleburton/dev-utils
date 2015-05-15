echo "NOTE: if you need to bypass this pre-commit hook, pass the --no-verify flag to Git"
for i in $(git diff --cached --name-only | grep -v \\.min\\.js | grep \\.js | grep -v version.js); do 
  if auto-jslint -b $i; then
    echo "ok"
  else
    auto-jslint -b $i
    exit 1
  fi
done
