./gradlew assembleRelease
mkdir -p builds
cp ./app/build/outputs/aar/app-release.aar ./builds/gatekeeper.aar
