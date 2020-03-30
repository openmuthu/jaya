import cv2
import sys
import pytesseract
import os
import io
from pdf2image import convert_from_path
import numpy as np
import shutil
import argparse
import time, multiprocessing, concurrent.futures
import re

#export PYTHONIOENCODING=UTF-8

# Dependencies
#pip install pdf2image
# pdf2image needs poppler
# To install poppler on mac, use the following command
# brew install poppler
# http://macappstore.org/poppler/

# usage example
# python ocr.py gv01.pdf -sp 3 -np 2

tokenize = re.compile(r'(\d+)|(\D+)').findall
def natural_sortkey(string):          
    return tuple(int(num) if num else alpha for num, alpha in tokenize(string))

def getFilesWithExtInDir(dir, extList):
	retVal = []
	files = os.listdir(dir)
	#print(files)
	for file in files:
		for ext in extList:
			if file.endswith(ext):
				retVal.append(os.path.join(dir, file))
				break
	#print(retVal)
	return sorted(retVal)

def ocr(imgPath, script=None):
	#print("script: {0}".format(script))
	config = ('-l script/Devanagari --oem 1 --psm 3')
	if script != None:	
		config = ('-l script/' + script + ' --oem 1 --psm 3')
	#print("processing file: " + file)
	# Read image from disk
	#im = cv2.imread(file, cv2.IMREAD_COLOR)
	im = cv2.imread(imgPath, 0)

	# Run tesseract OCR on image
	text = pytesseract.image_to_string(im, config=config)
	return text

def getPDFPageCount(pdfPath):
	cmd = "pdfinfo '" + pdfPath + "' | grep 'Pages' | awk '{print $2}'"
	return int(os.popen(cmd).read().strip())

if __name__ == '__main__':
	if len(sys.argv) < 2:
		print('Usage: python ocr.py <image.jpg|images_folder|pdf_file>')
		sys.exit(1)

	parser = argparse.ArgumentParser()
	parser.add_argument("file")
	parser.add_argument("-s", "--script", default="Devanagari", help="Script can be Devanagari|Kannada")
	parser.add_argument("-sp", "--start-page", type=int, default=1, help="starting page number")
	parser.add_argument("-np", "--num-pages", type=int, default=-1, help="number of pages to convert to text, starting from start_page")
	args = parser.parse_args()
	start_page = args.start_page;
	num_pages_to_convert = args.num_pages;

	#print("mark 1")
	# Read image path from command line
	imPath = args.file #sys.argv[1]
	files = []
	out_file_name_suffix = ""
	extList = ["png", "jpg"]
	cleanupFolders = []
	if imPath.endswith('/'):
		imPath = imPath[:-1]
	if imPath.endswith('pdf'):
		imgOutFolder = imPath[:-4]
		if not os.path.exists(imgOutFolder):
			os.makedirs(imgOutFolder)
		else:
			shutil.rmtree(imgOutFolder)
		pageCount = getPDFPageCount(imPath)		
		print("pdf page count: {0}".format(pageCount))
		if start_page < 0 or start_page > pageCount:
			start_page = 1
		if (num_pages_to_convert < 0) or (start_page+num_pages_to_convert > pageCount):
			num_pages_to_convert = pageCount - start_page + 1			
		end_page = start_page + num_pages_to_convert - 1		
		if start_page != 1 or end_page != pageCount:
			out_file_name_suffix = "_{}-{}".format(start_page, end_page)
		batchSize = 100
		batches = np.arange(start_page, end_page, batchSize).tolist()
		batches.append(end_page+1)
		start = time.time()
		for i in range(0, len(batches)-1):
			print("Converting pdf pages to images: {0}-{1}".format(batches[i],  batches[i+1]-1))
			imgOutSubFolder = os.path.join(imgOutFolder, "{0}-{1}".format(batches[i],  batches[i+1]-1))
			if not os.path.exists(imgOutSubFolder):
				os.makedirs(imgOutSubFolder)
			images = convert_from_path(imPath, first_page=batches[i], last_page=batches[i+1]-1, output_folder=imgOutSubFolder, fmt="jpeg", thread_count=multiprocessing.cpu_count())
			#print(images)
			[im.close() for im in images]
			files += getFilesWithExtInDir(imgOutSubFolder, extList)	
		end = time.time()
		print("Conversion of pdf to images took: {0} seconds".format(end-start))
		imPath = imgOutFolder
	elif os.path.isdir(imPath):
		files = getFilesWithExtInDir(imPath, extList)
	else:
		files.append(imPath)
	#print("mark 2")

	start = time.time()

	outputFilePath = os.path.normpath( os.path.join(imPath, "..", os.path.basename(imPath) + out_file_name_suffix + ".txt") )
	print("Writing output file: " + outputFilePath)


	files = sorted(files, key=natural_sortkey)
	#print("files: {0}".format(files))
	scripts = [args.script for i in files]
	with io.open(outputFilePath, 'w+', encoding='utf8') as outputFile:            
		with concurrent.futures.ThreadPoolExecutor(max_workers=multiprocessing.cpu_count()) as executor:
			for img_path,text in zip(files,executor.map(ocr,files, scripts)):
				#print(img_path)
				outputFile.write(text + "\n")

	'''
	#outputFile = open(outputFilePath, "w+")
	with io.open(outputFilePath, 'w+', encoding='utf8') as outputFile:            
		# Uncomment the line below to provide path to tesseract manually
		# pytesseract.pytesseract.tesseract_cmd = '/usr/bin/tesseract'

		numFiles = len(files)

		# Define config parameters.
		# '-l eng'  for using the English language
		# '--oem 1' for using LSTM OCR Engine
		config = ('-l script/Devanagari --oem 1 --psm 3')		
		for (i, file) in enumerate(files):
			#print("processing file: " + file)
			# Read image from disk
			#im = cv2.imread(file, cv2.IMREAD_COLOR)
			im = cv2.imread(file, 0)

			# Run tesseract OCR on image
			text = pytesseract.image_to_string(im, config=config)

			if i % 5 == 1:
				print("Pages converted to text, so far: {0}".format(i))

			# Print recognized text
			#print(text)
			outputFile.write(text + "\n")
	'''				

	end = time.time()
	print("Conversion to text took: {} seconds", end-start)