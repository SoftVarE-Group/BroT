import os

# traverse root directory, and list directories as dirs and files as files
for root, dirs, files in os.walk("."):
    indent = len(root.split(os.sep))
    curDir = os.path.basename(root)
    #print((indent - 1) * '  ', curDir)
    for file in files:
        if file.endswith(".model.xml"):
            path = os.path.join(root, file)
            print("{0: >5} kB: {1}".format(int(os.path.getsize(path) / 1000.0), path))