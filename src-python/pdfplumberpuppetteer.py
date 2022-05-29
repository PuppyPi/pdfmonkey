import sys, json, pdfplumber

def main():
	currentFile = None
	currentTables = None
	
	while True:
		line = sys.stdin.readline()
		gds = json.loads(line)
		
		out = None
		quit = False
		
		if (gds[0] == "open"):
			currentFile = pdfplumber.open(gds[1])
			out = ["okay", len(currentFile.pages)]
		elif (gds[0] == "close"):
			currentFile = None
			out = ["okay"]
		
		elif (gds[0] == "find_tables" or gds[0] == "find_tables,extract_tables" or gds[0] == "extract_tables"):
			if (currentFile == None):
				out = ["error", "No current file"]
			else:
				pageIndex = gds[1]
				p = currentFile.pages[pageIndex]
				error = False
				if (len(gds) > 2):
					if (gds[2] == "crop"):
						p = p.crop(gds[3:7])
					elif (gds[2] == "within_bbox"):
						p = p.within_bbox(gds[3:7])
					else:
						out = ["error", "Unrecognized crop type: "+repr(gds[2])]
						error = True
				
				if (not error):
					if (gds[0] == "find_tables"):
						currentTables = p.find_tables()
						out = ["okay", convertTablesToOutput(currentTables)]
					elif (gds[0] == "find_tables,extract_tables"):
						currentTables = p.find_tables()
						extractions = map(lambda t: t.extract(), currentTables)
						out = ["okay", convertTablesToOutput(currentTables), convertTableExtractionsToOutput(extractions)]
					elif (gds[0] == "extract_tables"):
						extractions = p.extract_tables()
						out = ["okay", convertTableExtractionsToOutput(extractions)]
					else:
						raise AssertionError()
		
		elif (gds[0] == "find_tables[i].extract"):
			if (currentFile == None):
				out = ["error", "No current file"]
			elif (currentTables == None):
				out = ["error", "No current tables"]
			else:
				tableIndex = gds[1]
				extraction = currentTables[tableIndex].extract()
				out = ["okay", convertTableExtractionToOutput(extraction)]
		
		elif (gds[0] == "find_tables.extract"):
			if (currentFile == None):
				out = ["error", "No current file"]
			elif (currentTables == None):
				out = ["error", "No current tables"]
			else:
				extractions = map(lambda t: t.extract(), currentTables)
				out = ["okay", convertTableExtractionsToOutput(extractions)]
		
		elif (gds[0] == "quit"):
			out = ["okay"]
			quit = True
		
		else:
			out = ["error", "Unrecognize operation: "+repr(gds[0])]
		
		
		
		sys.stdout.write(json.dumps(out).replace('\n', '').replace('\r', ''))
		
		if (quit):
			break
#





def convertTableExtractionsToOutput(l):
	return list(map(convertTableExtractionToOutput, l))

def convertTableExtractionToOutput(x):
	return x   #hahaha oh right XD


def convertTablesToOutput(l):
	return list(map(convertTableToOutput, l))

def convertTableToOutput(t):
	return {"bbox": t.bbox, "cells": t.cells}



main()
