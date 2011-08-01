#!/usr/bin/env python2

import glob
import sys
import re
from PIL import Image

if len(sys.argv) != 3:
	print "usage: resize2.py src_dir dst_dir"
	sys.exit(1)

src = sys.argv[1]
dst = sys.argv[2]

def closestPower(value):
	best = 1
	for exp in range(1, 10):
		test = 2 ** exp
		diff = abs(test - value)
		if diff < abs(best - value):
			best = test
	return best

for filename in glob.glob(src + "/*.png"):
	image = Image.open(filename)
	x, y = image.size
	x = closestPower(x)
	y = closestPower(y)
	name = re.sub(".*/", "", filename)
	print "echo resizing %s to %dx%d" % (name, x, y)
	print "convert -geometry %dx%d %s %s/%s" % (x, y, filename, dst, name)
