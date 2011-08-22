#!/usr/bin/python2

def printarr(name, arr):
	print "private static final float[]", name, "= {",
	for i in range(len(arr)):
		if i != 0:
			print ",",
		print str(round(arr[i], 3)) + "f",
	print "};"

def printhex(index, points):
	print "hexagon[", index, "].setVertices(",
	for i in range(len(points)):
		if i != 0:
			print ",",
		print "vertex[", points[i], "]",
	print ");"

def printedge(index, v1, v2):
	print "edge[", index, "].setVertices(vertex[", v1, "], vertex[", v2, "]);"

radius = 0.5
height = 0.42
width = 0.24
testdist = 0.1
testradius = radius + testdist
testedge = 2 * width + testdist

hexx = []
hexy = []
pointx = []
pointy = []
uniquex = []
uniquey = []
edgex = []
edgey = []

for i in range(3):
	hexx.append(-6 * width)
	hexy.append(-2 * height * (i - 1))

for i in range(4):
	hexx.append(-3 * width)
	hexy.append(-2 * height * (i - 1.5))

for i in range(5):
	hexx.append(0)
	hexy.append(-2 * height * (i - 2))

for i in range(4):
	hexx.append(3 * width)
	hexy.append(-2 * height * (i - 1.5))

for i in range(3):
	hexx.append(6 * width)
	hexy.append(-2 * height * (i - 1))

offx = [ -width, width, radius, width, -width, -radius ]
offy = [ height, height, 0, -height, -height, 0 ]

for i in range(len(hexx)):
	for j in range(6):
		pointx.append(hexx[i] + offx[j])
		pointy.append(hexy[i] + offy[j])

heights = sorted(pointy)
prev = -10
for h in range(len(heights)):
	y = heights[h]
	
	if abs(y - prev) < testdist:
		continue
	
	prev = y
	
	res = []
	for i in range(len(pointy)):
		if abs(pointy[i] - y) < testdist:
			res.append(pointx[i])
	
	positions = sorted(res)
	
	last = -10
	for i in range(len(positions)):
		x = positions[i]
		
		if abs(x - last) < testdist:
			continue
		
		last = x
		
		uniquex.append(x)
		uniquey.append(-y)

for i in range(len(hexx)):
	points = []
	for j in range(len(uniquex)):
		dx = hexx[i] - uniquex[j]
		dy = hexy[i] - uniquey[j]
		dist2 = dx * dx + dy * dy
		if dist2 < testradius * testradius:
			points.append(j)
	printhex(i, points)

print

edge = 0
for i in range(len(uniquex)):
	for j in range(i + 1, len(uniquex)):
		dx = uniquex[i] - uniquex[j]
		dy = uniquey[i] - uniquey[j]
		dist2 = dx * dx + dy * dy
		if dist2 < testedge * testedge:
			printedge(edge, i, j)
			edge += 1
			edgex.append((uniquex[i] + uniquex[j]) / 2)
			edgey.append((uniquey[i] + uniquey[j]) / 2)
			

print

print 
print "hexagons:", len(hexx)
print "total points:", len(pointx)
print "unique points:", len(uniquex)
print "edges:", len(edgex)

print
printarr("HEXAGON_X", hexx)
print
printarr("HEXAGON_Y", hexy)
print
printarr("POINT_X", uniquex)
print
printarr("POINT_Y", uniquey)
print
printarr("EDGE_X", edgex)
print
printarr("EDGE_Y", edgey)
print
