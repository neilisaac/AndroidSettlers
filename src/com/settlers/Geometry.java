package com.settlers;

import android.util.DisplayMetrics;

public class Geometry {

	private static final double MAX_PAN = 2.5;

	private static final double[] HEXAGON_X = { -1.5, -1.5, -1.5, -0.75, -0.75,
			-0.75, -0.75, 0.0, 0.0, 0.0, 0.0, 0.0, 0.75, 0.75, 0.75, 0.75, 1.5,
			1.5, 1.5 };

	private static final double[] HEXAGON_Y = { -0.866, 0.0, 0.866, -1.299,
			-0.433, 0.433, 1.299, -1.732, -0.866, 0.0, 0.866, 1.732, -1.299,
			-0.433, 0.433, 1.299, -0.866, 0.0, 0.866 };

	private static final double[] POINT_X = { -0.25, 0.25, -1.0, -0.5, 0.5,
			1.0, -1.75, -1.25, -0.25, 0.25, 1.25, 1.75, -2.0, -1.0, -0.5, 0.5,
			1.0, 2.0, -1.75, -1.25, -0.25, 0.25, 1.25, 1.75, -2.0, -1.0, -0.5,
			0.5, 1.0, 2.0, -1.75, -1.25, -0.25, 0.25, 1.25, 1.75, -2.0, -1.0,
			-0.5, 0.5, 1.0, 2.0, -1.75, -1.25, -0.25, 0.25, 1.25, 1.75, -1.0,
			-0.5, 0.5, 1.0, -0.25, 0.25 };

	private static final double[] POINT_Y = { -2.165, -2.165, -1.732, -1.732,
			-1.732, -1.732, -1.299, -1.299, -1.299, -1.299, -1.299, -1.299,
			-0.866, -0.866, -0.866, -0.866, -0.866, -0.866, -0.433, -0.433,
			-0.433, -0.433, -0.433, -0.433, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			0.433, 0.433, 0.433, 0.433, 0.433, 0.433, 0.866, 0.866, 0.866,
			0.866, 0.866, 0.866, 1.299, 1.299, 1.299, 1.299, 1.299, 1.299,
			1.732, 1.732, 1.732, 1.732, 2.165, 2.165 };

	private static final double[] EDGE_X = { 0.0, -0.375, 0.375, -0.75, -1.125,
			-0.375, 0.75, 0.375, 1.125, -1.5, -1.875, -1.125, 0.0, -0.375,
			0.375, 1.5, 1.125, 1.875, -1.875, -0.75, -1.125, -0.375, 0.75,
			0.375, 1.125, 1.875, -1.5, -1.875, -1.125, 0.0, -0.375, 0.375, 1.5,
			1.125, 1.875, -1.875, -0.75, -1.125, -0.375, 0.75, 0.375, 1.125,
			1.875, -1.5, -1.875, -1.125, 0.0, -0.375, 0.375, 1.5, 1.125, 1.875,
			-1.875, -0.75, -1.125, -0.375, 0.75, 0.375, 1.125, 1.875, -1.5,
			-1.125, 0.0, -0.375, 0.375, 1.5, 1.125, -0.75, -0.375, 0.75, 0.375,
			0.0 };

	private static final double[] EDGE_Y = { -2.165, -1.949, -1.949, -1.732,
			-1.515, -1.515, -1.732, -1.515, -1.515, -1.299, -1.083, -1.083,
			-1.299, -1.083, -1.083, -1.299, -1.083, -1.083, -0.65, -0.866,
			-0.65, -0.65, -0.866, -0.65, -0.65, -0.65, -0.433, -0.217, -0.217,
			-0.433, -0.217, -0.217, -0.433, -0.217, -0.217, 0.217, 0.0, 0.217,
			0.217, 0.0, 0.217, 0.217, 0.217, 0.433, 0.65, 0.65, 0.433, 0.65,
			0.65, 0.433, 0.65, 0.65, 1.083, 0.866, 1.083, 1.083, 0.866, 1.083,
			1.083, 1.083, 1.299, 1.515, 1.299, 1.515, 1.515, 1.299, 1.515,
			1.732, 1.949, 1.732, 1.949, 2.165 };

	private static final int[] TRADER_EDGE = { 0, 4, 8, 27, 34, 52, 59, 67, 69 };

	private static final int[] TRADER_HEX = { 7, 3, 12, 1, 17, 2, 18, 6, 15 };

	private static final double[] TRADER_OFFSET_X = { 0.0, -0.16, 0.15, -0.15,
			0.15, -0.15, 0.15, 0.0, 0.0 };

	private static final double[] TRADER_OFFSET_Y = { -0.18, -0.1, -0.1, -0.1,
			-0.1, 0.1, 0.1, 0.16, 0.16 };

	private static final int REFERENCE_WIDTH = 480;
	private static final int REFERENCE_DENSITY = DisplayMetrics.DENSITY_HIGH;
	private static final double TILE_UNIT_FACTOR = 0.96;

	private int unit, width, height;
	private double zoom, scale, cx, cy;
	private int realWidth,realHeight;

	private int xTranslate(double x) {
		return (int) (width * 0.5 + zoom * unit * (x - cx));
	}

	private int yTranslate(double y) {
		return (int) (height * 0.5 + zoom * unit * (y - cy));
	}

	private int getNearest(int x, int y, double[] xArray, double[] yArray,
			int length) {
		int best = -1;
		double dist2 = unit * unit * zoom * zoom / 4;
		for (int i = 0; i < length; i++) {
			double x2 = Math.pow(x - xTranslate(xArray[i]), 2);
			double y2 = Math.pow(y - yTranslate(yArray[i]), 2);
			if (x2 + y2 < dist2) {
				dist2 = x2 + y2;
				best = i;
			}
		}
		return best;
	}

	public Geometry() {
		this.setSize(100, 100);
		this.setZoom(0.0, 0.0, 1.0);
	}

	public void setRealSize(DisplayMetrics screen, int tileSize) {
		int width = screen.widthPixels;
		int height = screen.heightPixels;
		int density = screen.densityDpi;
		int min = (width < height ? width : height);
		
		realWidth = width;
		realHeight = height;

		double reference = REFERENCE_WIDTH / REFERENCE_DENSITY;
		double actual = min / density;
		scale = actual / reference;
		unit = (int) (tileSize * TILE_UNIT_FACTOR);
	}
	
	public int getRealWidth() {
		return realWidth;
	}
	
	public int getRealHeight() {
		return realHeight;
	}

	public double getScaleFactor() {
		return scale;
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	private void clampPan() {
		if (cx > MAX_PAN)
			cx = MAX_PAN;
		else if (cx < -MAX_PAN)
			cx = -MAX_PAN;

		if (cy > MAX_PAN)
			cy = MAX_PAN;
		else if (cy < -MAX_PAN)
			cy = -MAX_PAN;
	}

	public void setZoom(int x, int y, double zoom) {
		cx += (x - width * 0.5) / (unit * this.zoom);
		cy += (y - height * 0.5) / (unit * this.zoom);
		clampPan();
		this.zoom = zoom;
	}

	public void setZoom(double cx, double cy, double zoom) {
		this.cx = cx;
		this.cy = cy;
		clampPan();
		this.zoom = zoom;
	}

	public void translate(double dx, double dy) {
		cx += dx / (unit * zoom);
		cy += dy / (unit * zoom);
		clampPan();
	}

	public void setZoom(Hexagon hexagon, double zoom) {
		this.cx = getOffsetX(hexagon);
		this.cy = getOffsetY(hexagon);
		this.zoom = zoom;
	}

	public double getZoom() {
		return zoom;
	}

	public double getOffsetX(Hexagon hexagon) {
		return HEXAGON_X[hexagon.getId()];
	}

	public double getOffsetY(Hexagon hexagon) {
		return HEXAGON_Y[hexagon.getId()];
	}

	public double getOffsetX(Vertex vertex) {
		return POINT_X[vertex.getIndex()];
	}

	public double getOffsetY(Vertex vertex) {
		return POINT_Y[vertex.getIndex()];
	}

	public double getOffsetX(Edge edge) {
		return EDGE_X[edge.getIndex()];
	}

	public double getOffsetY(Edge edge) {
		return EDGE_Y[edge.getIndex()];
	}

	public int getHexagonX(int index) {
		return xTranslate(HEXAGON_X[index]);
	}

	public int getHexagonY(int index) {
		return yTranslate(HEXAGON_Y[index]);
	}

	public int getEdgeX(int index) {
		return xTranslate(EDGE_X[index]);
	}

	public int getEdgeY(int index) {
		return yTranslate(EDGE_Y[index]);
	}

	public int getVertexX(int index) {
		return xTranslate(POINT_X[index]);
	}

	public int getVertexY(int index) {
		return yTranslate(POINT_Y[index]);
	}

	public int getTraderX(int index) {
		return xTranslate(HEXAGON_X[TRADER_HEX[index]]);
	}

	public int getTraderY(int index) {
		return yTranslate(HEXAGON_Y[TRADER_HEX[index]]);
	}

	public int getTraderEdgeX(int index) {
		return xTranslate(EDGE_X[TRADER_EDGE[index]]);
	}

	public int getTraderEdgeY(int index) {
		return yTranslate(EDGE_Y[TRADER_EDGE[index]]);
	}

	public double getTraderIconOffsetX(int index) {
		return getTraderEdgeX(index) + 1.5 * TRADER_OFFSET_X[index] * unit * zoom;
	}

	public double getTraderIconOffsetY(int index) {
		return getTraderEdgeY(index) + 1.5 * TRADER_OFFSET_Y[index] * unit * zoom;
	}

	public int getNearestHexagon(int x, int y) {
		return getNearest(x, y, HEXAGON_X, HEXAGON_Y, Hexagon.NUM_HEXAGONS);
	}

	public int getNearestEdge(int x, int y) {
		int nearest = getNearest(x, y, EDGE_X, EDGE_Y, Edge.NUM_EDGES);
		return nearest;
	}

	public int getNearestVertex(int x, int y) {
		return getNearest(x, y, POINT_X, POINT_Y, Vertex.NUM_VERTEX);
	}

	public int getUnitSize() {
		return unit;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getMinimalSize() {
		return width < height ? width : height;
	}

	public boolean isZoomed() {
		return (zoom > 1.01);
	}

	public static void setAssociations(Hexagon[] hexagon, Vertex[] vertex,
			Edge[] edge, Trader[] trader) {
		// associate vertices with hexagons
		hexagon[0].setVertices(vertex[6], vertex[7], vertex[12], vertex[13],
				vertex[18], vertex[19]);
		hexagon[1].setVertices(vertex[18], vertex[19], vertex[24], vertex[25],
				vertex[30], vertex[31]);
		hexagon[2].setVertices(vertex[30], vertex[31], vertex[36], vertex[37],
				vertex[42], vertex[43]);
		hexagon[3].setVertices(vertex[2], vertex[3], vertex[7], vertex[8],
				vertex[13], vertex[14]);
		hexagon[4].setVertices(vertex[13], vertex[14], vertex[19], vertex[20],
				vertex[25], vertex[26]);
		hexagon[5].setVertices(vertex[25], vertex[26], vertex[31], vertex[32],
				vertex[37], vertex[38]);
		hexagon[6].setVertices(vertex[37], vertex[38], vertex[43], vertex[44],
				vertex[48], vertex[49]);
		hexagon[7].setVertices(vertex[0], vertex[1], vertex[3], vertex[4],
				vertex[8], vertex[9]);
		hexagon[8].setVertices(vertex[8], vertex[9], vertex[14], vertex[15],
				vertex[20], vertex[21]);
		hexagon[9].setVertices(vertex[20], vertex[21], vertex[26], vertex[27],
				vertex[32], vertex[33]);
		hexagon[10].setVertices(vertex[32], vertex[33], vertex[38], vertex[39],
				vertex[44], vertex[45]);
		hexagon[11].setVertices(vertex[44], vertex[45], vertex[49], vertex[50],
				vertex[52], vertex[53]);
		hexagon[12].setVertices(vertex[4], vertex[5], vertex[9], vertex[10],
				vertex[15], vertex[16]);
		hexagon[13].setVertices(vertex[15], vertex[16], vertex[21], vertex[22],
				vertex[27], vertex[28]);
		hexagon[14].setVertices(vertex[27], vertex[28], vertex[33], vertex[34],
				vertex[39], vertex[40]);
		hexagon[15].setVertices(vertex[39], vertex[40], vertex[45], vertex[46],
				vertex[50], vertex[51]);
		hexagon[16].setVertices(vertex[10], vertex[11], vertex[16], vertex[17],
				vertex[22], vertex[23]);
		hexagon[17].setVertices(vertex[22], vertex[23], vertex[28], vertex[29],
				vertex[34], vertex[35]);
		hexagon[18].setVertices(vertex[34], vertex[35], vertex[40], vertex[41],
				vertex[46], vertex[47]);

		// associate vertices with edges
		edge[0].setVertices(vertex[0], vertex[1]);
		edge[1].setVertices(vertex[0], vertex[3]);
		edge[2].setVertices(vertex[1], vertex[4]);
		edge[3].setVertices(vertex[2], vertex[3]);
		edge[4].setVertices(vertex[2], vertex[7]);
		edge[5].setVertices(vertex[3], vertex[8]);
		edge[6].setVertices(vertex[4], vertex[5]);
		edge[7].setVertices(vertex[4], vertex[9]);
		edge[8].setVertices(vertex[5], vertex[10]);
		edge[9].setVertices(vertex[6], vertex[7]);
		edge[10].setVertices(vertex[6], vertex[12]);
		edge[11].setVertices(vertex[7], vertex[13]);
		edge[12].setVertices(vertex[8], vertex[9]);
		edge[13].setVertices(vertex[8], vertex[14]);
		edge[14].setVertices(vertex[9], vertex[15]);
		edge[15].setVertices(vertex[10], vertex[11]);
		edge[16].setVertices(vertex[10], vertex[16]);
		edge[17].setVertices(vertex[11], vertex[17]);
		edge[18].setVertices(vertex[12], vertex[18]);
		edge[19].setVertices(vertex[13], vertex[14]);
		edge[20].setVertices(vertex[13], vertex[19]);
		edge[21].setVertices(vertex[14], vertex[20]);
		edge[22].setVertices(vertex[15], vertex[16]);
		edge[23].setVertices(vertex[15], vertex[21]);
		edge[24].setVertices(vertex[16], vertex[22]);
		edge[25].setVertices(vertex[17], vertex[23]);
		edge[26].setVertices(vertex[18], vertex[19]);
		edge[27].setVertices(vertex[18], vertex[24]);
		edge[28].setVertices(vertex[19], vertex[25]);
		edge[29].setVertices(vertex[20], vertex[21]);
		edge[30].setVertices(vertex[20], vertex[26]);
		edge[31].setVertices(vertex[21], vertex[27]);
		edge[32].setVertices(vertex[22], vertex[23]);
		edge[33].setVertices(vertex[22], vertex[28]);
		edge[34].setVertices(vertex[23], vertex[29]);
		edge[35].setVertices(vertex[24], vertex[30]);
		edge[36].setVertices(vertex[25], vertex[26]);
		edge[37].setVertices(vertex[25], vertex[31]);
		edge[38].setVertices(vertex[26], vertex[32]);
		edge[39].setVertices(vertex[27], vertex[28]);
		edge[40].setVertices(vertex[27], vertex[33]);
		edge[41].setVertices(vertex[28], vertex[34]);
		edge[42].setVertices(vertex[29], vertex[35]);
		edge[43].setVertices(vertex[30], vertex[31]);
		edge[44].setVertices(vertex[30], vertex[36]);
		edge[45].setVertices(vertex[31], vertex[37]);
		edge[46].setVertices(vertex[32], vertex[33]);
		edge[47].setVertices(vertex[32], vertex[38]);
		edge[48].setVertices(vertex[33], vertex[39]);
		edge[49].setVertices(vertex[34], vertex[35]);
		edge[50].setVertices(vertex[34], vertex[40]);
		edge[51].setVertices(vertex[35], vertex[41]);
		edge[52].setVertices(vertex[36], vertex[42]);
		edge[53].setVertices(vertex[37], vertex[38]);
		edge[54].setVertices(vertex[37], vertex[43]);
		edge[55].setVertices(vertex[38], vertex[44]);
		edge[56].setVertices(vertex[39], vertex[40]);
		edge[57].setVertices(vertex[39], vertex[45]);
		edge[58].setVertices(vertex[40], vertex[46]);
		edge[59].setVertices(vertex[41], vertex[47]);
		edge[60].setVertices(vertex[42], vertex[43]);
		edge[61].setVertices(vertex[43], vertex[48]);
		edge[62].setVertices(vertex[44], vertex[45]);
		edge[63].setVertices(vertex[44], vertex[49]);
		edge[64].setVertices(vertex[45], vertex[50]);
		edge[65].setVertices(vertex[46], vertex[47]);
		edge[66].setVertices(vertex[46], vertex[51]);
		edge[67].setVertices(vertex[48], vertex[49]);
		edge[68].setVertices(vertex[49], vertex[52]);
		edge[69].setVertices(vertex[50], vertex[51]);
		edge[70].setVertices(vertex[50], vertex[53]);
		edge[71].setVertices(vertex[52], vertex[53]);

		// associate vertices with traders
		for (int i = 0; i < TRADER_EDGE.length; i++) {
			edge[TRADER_EDGE[i]].getVertex1().setTrader(trader[i]);
			edge[TRADER_EDGE[i]].getVertex2().setTrader(trader[i]);
		}
	}
}
