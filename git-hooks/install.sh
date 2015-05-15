#!/bin/sh

# rn-wall-front-end
rm -f ~/projects/rn-wall-front-end/.git/hooks/pre-commit
echo '#!/bin/sh' >> ~/projects/rn-wall-front-end/.git/hooks/pre-commit
cat js-lint-precommit.sh >> ~/projects/rn-wall-front-end/.git/hooks/pre-commit
cat set-pair-precommit.sh >> ~/projects/rn-wall-front-end/.git/hooks/pre-commit
chmod +x ~/projects/rn-wall-front-end/.git/hooks/pre-commit
