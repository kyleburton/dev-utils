#!/bin/sh

# rn-wall-front-end
rm -f ~/projects/rn-wall-front-end/.git/hooks/pre-commit
echo '#!/bin/sh' >> ~/projects/rn-wall-front-end/.git/hooks/pre-commit
cat js-lint-precommit.sh >> ~/projects/rn-wall-front-end/.git/hooks/pre-commit
cat set-pair-precommit.sh >> ~/projects/rn-wall-front-end/.git/hooks/pre-commit
chmod +x ~/projects/rn-wall-front-end/.git/hooks/pre-commit

# rn-boomerang
rm -f ~/projects/rn-boomerang/.git/hooks/pre-commit
echo '#!/bin/sh' >> ~/projects/rn-boomerang/.git/hooks/pre-commit
cat set-pair-precommit.sh >> ~/projects/rn-boomerang/.git/hooks/pre-commit
chmod +x ~/projects/rn-boomerang/.git/hooks/pre-commit

# rn-wall
rm -f ~/projects/rn-wall/.git/hooks/pre-commit
echo '#!/bin/sh' >> ~/projects/rn-wall/.git/hooks/pre-commit
cat set-pair-precommit.sh >> ~/projects/rn-wall/.git/hooks/pre-commit
chmod +x ~/projects/rn-wall/.git/hooks/pre-commit

# rn-client-portal
rm -f ~/projects/rn-client-portal/.git/hooks/pre-commit
echo '#!/bin/sh' >> ~/projects/rn-client-portal/.git/hooks/pre-commit
cat set-pair-precommit.sh >> ~/projects/rn-client-portal/.git/hooks/pre-commit
chmod +x ~/projects/rn-client-portal/.git/hooks/pre-commit
