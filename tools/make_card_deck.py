import csv
import sys

path_to_csv_export = ""
if len(sys.argv) > 1:
	path_to_csv_export = sys.argv[1]
else:
	path_to_csv_export = input("Path to your CSV export:")

roles_file = "./roles"
traits_file = "./traits"

with open(path_to_csv_export, newline='') as csvfile:
	spamreader = csv.reader(csvfile, delimiter=',', quotechar='|')
	
	jobs =[]
	traits = []

	skipped_frist = False
	for row in spamreader:
		if skipped_frist:
			if len(row[0]) > 0:
				jobs.append(row[0])
			for i in range(1,len(row)):
				if len(row[i]) > 0:
					traits.append(row[i])
		else:
			skipped_frist = True

with open(roles_file, "w") as w:
	w.write("\n".join(jobs))

with open(traits_file, "w") as w:
	w.write("\n".join(traits))
