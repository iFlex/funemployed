import json
from os import listdir
from os.path import isfile, join
import sys

report_path = sys.argv[1]
output_path = sys.argv[2]
report = {}#"all":[]}

onlyfiles = [join(report_path, f) for f in listdir(report_path) if isfile(join(report_path, f))]
for pth in onlyfiles:
    with open(pth,"r") as f:
        data = json.loads(f.read())
        line = 0
        for key in data:
            if key not in report:
                report[key] = []
            report[key].append(data[key])
            
for key in report:
    headers = report[key][0].keys()
    with open(output_path+key+".csv","w") as f:
        f.write(",".join(headers))
        for row in report[key]:
            values = list()
            for ink in headers:
                values.append(row[ink])
            f.write("\n")
            f.write(",".join(str(x) for x in values))

