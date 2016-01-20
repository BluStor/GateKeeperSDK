echo "Building project ..."
./gradlew assembleRelease
echo "Done."
echo "Creating builds directory ..."
mkdir -p builds
echo "Done."
echo "Copying aar file to ./builds/gatekeeper.aar"
cp ./app/build/outputs/aar/app-release.aar ./builds/gatekeeper.aar
echo "Finished."
