import os, sys, codecs

BUFSIZE = 4096
BOMLEN = len(codecs.BOM_UTF8)

#path = sys.argv[1]
path = os.getcwd()
for fpathe, dirs, fs in os.walk(path):
    for f in fs:
        code_file = os.path.join(fpathe, f)
        if os.path.splitext(f)[1] == '.java' or os.path.splitext(f)[1] == '.html':
            print "found code: " + code_file
            with open(code_file, "r+b") as fp:
                chunk = fp.read(BUFSIZE)
                if chunk.startswith(codecs.BOM_UTF8):
                    i = 0
                    chunk = chunk[BOMLEN:]
                    while chunk:
                        fp.seek(i)
                        fp.write(chunk)
                        i += len(chunk)
                        fp.seek(BOMLEN, os.SEEK_CUR)
                        chunk = fp.read(BUFSIZE)
                    fp.seek(-BOMLEN, os.SEEK_CUR)
                    fp.truncate()
                    print "remove BOM on: " + code_file
