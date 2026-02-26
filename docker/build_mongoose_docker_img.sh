mkdir -p docker-builder
cd docker-builder
git clone https://github.com/esl/mongooseim-docker.git --depth=1 .
mkdir -p docker-builder/builds
docker build -f Dockerfile.builder -t mongooseim-builder .
docker run --rm --name mongooseim-builder -h mongooseim-builder -e TARBALL_NAME=mongooseim -v $(pwd)/builds:/builds mongooseim-builder ./build.sh MongooseIM https://github.com/zextras/mongooseim a21c5b77097fe2fff892189ea54511a43e70fd19
if [ -z "$(ls -1 $(pwd)/builds/mongooseim-*.tar.gz 2>/dev/null)" ]; then
    echo "ERROR: MongooseIM build failed, no tar.gz file found in builds/"
    exit 1
fi
cp $(pwd)/builds/mongooseim-*.tar.gz $(pwd)/member/
docker build -f Dockerfile.member -t mongooseim .
rm -rf docker-builder
