NAME = cypress-tests
BUILD_ARGS = --build-arg 'SORRY_CYPRESS_AUTHOR=$${SORRY_CYPRESS_AUTHOR}' \
             --build-arg 'SORRY_CYPRESS_BRANCH=$${SORRY_CYPRESS_BRANCH}' \
             --build-arg 'SORRY_CYPRESS_EMAIL=$${SORRY_CYPRESS_EMAIL}' \
             --build-arg 'SORRY_CYPRESS_MESSAGE=$${SORRY_CYPRESS_MESSAGE}' \
             --build-arg 'SORRY_CYPRESS_REMOTE=$${SORRY_CYPRESS_REMOTE}' \
             --build-arg 'SORRY_CYPRESS_SHA=$${SORRY_CYPRESS_SHA}' \
             --build-arg 'SORRY_CYPRESS_TIMESTAMP=$${SORRY_CYPRESS_TIMESTAMP}'

CLEAN = rm -rf _user-interface

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
