set -x

FRONTEND_ROOT=./frontendNG/funemployed
FRONTEND_BUILD=${FRONTEND_ROOT}/dist/funemployed
BACKEND_JAVA_ROOT=./backend
BACKEND_JAVA_FRONTEND_SLOT=${BACKEND_JAVA_ROOT}/src/main/resources/public

echo "Building Frontend. Using angular cli (ng)"
pushd ${FRONTEND_ROOT}
ng build
popd

echo "Moving frontend into backend /public resources folder"
rm -rf ${BACKEND_JAVA_FRONTEND_SLOT}/*
cp -R ${FRONTEND_BUILD}/* ${BACKEND_JAVA_FRONTEND_SLOT}

echo "Building Backend"
pushd ${BACKEND_JAVA_ROOT}
mvn clean package
popd