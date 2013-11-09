package com.settlers.hd;

public class Geometry {

	public static final int TILE_SIZE = 256;
	public static final int BUTTON_SIZE = 128;

	private static final float MAX_PAN = 2.5f;

	private int width, height;
	private float cx, cy, zoom;
	private float minZoom, maxZoom, highZoom;

	public Geometry() {
		cx = cy = 0;
		width = height = 480;
		zoom = minZoom = maxZoom = highZoom = 1;
	}

	public void setSize(int w, int h) {
		width = w;
		height = h;
		
		float aspect = (float) width / (float) height;
		float minZoomX = 0.5f * (float) width / (5.5f * TILE_SIZE);
		float minZoomY = 0.5f * (float) width / aspect / (5.1f * TILE_SIZE);
		
		minZoom = Math.min(minZoomX, minZoomY);
		highZoom = 2 * minZoom;
		maxZoom = 3 * minZoom;
		
		setZoom(minZoom);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private int getMinimalSize() {
		return width < height ? width : height;
	}
	
	public float getZoom() {
		return zoom;
	}

	public void zoomOut() {
		cx = cy = 0;
		zoom = minZoom;
	}

	public void zoomTo(int userX, int userY) {
		cx = translateScreenX(userX);
		cy = translateScreenY(userY);
		zoom = highZoom;
		translate(0, 0);
	}

	public void toggleZoom(int userX, int userY) {
		if (zoom > (highZoom - 0.01f))
			zoomOut();
		else
			zoomTo(userX, userY);
	}
	
	public void zoomBy(float z) {
		setZoom(zoom * z);
	}
	
	public void setZoom(float z) {
		zoom = z;
		
		if (zoom > maxZoom)
			zoom = maxZoom;
		else if (zoom < minZoom)
			zoom = minZoom;
		
		translate(0, 0);
	}

	public void translate(float dx, float dy) {
		float halfMin = (float) getMinimalSize() / 2.0f;
		
		cx += dx / halfMin;
		cy -= dy / halfMin;
		
		float radius = (float) Math.sqrt(cx * cx + cy * cy);
		float maxRadius = MAX_PAN * (zoom - minZoom) / (maxZoom - minZoom);
		
		if (radius > maxRadius) {
			cx *= maxRadius / radius;
			cy *= maxRadius / radius;
		}
	}

	public float getTranslateX() {
		return cx;
	}

	public float getTranslateY() {
		return cy;
	}

	private float translateScreenX(int x) {
		float halfMin = (width < height ? width : height) / 2f;
		return (float) ((x - width / 2) / halfMin + cx) / zoom;
	}

	private float translateScreenY(int y) {
		float halfMin = (width < height ? width : height) / 2f;
		return (float) ((height / 2 - y) / halfMin + cy) / zoom;
	}

	private int getNearest(int userX, int userY, float[] rx, float[] ry, int length) {
		float x = translateScreenX(userX);
		float y = translateScreenY(userY);
		
		int best = -1;
		double dist2 = zoom * zoom / 4;
		for (int i = 0; i < length; i++) {
			double x2 = Math.pow(x - rx[i], 2);
			double y2 = Math.pow(y - ry[i], 2);
			if (x2 + y2 < dist2) {
				dist2 = x2 + y2;
				best = i;
			}
		}
		return best;
	}

	public int getNearestHexagon(int userX, int userY) {
		return getNearest(userX, userY, HEXAGON_X, HEXAGON_Y, Hexagon.NUM_HEXAGONS);
	}

	public int getNearestEdge(int userX, int userY) {
		return getNearest(userX, userY, EDGE_X, EDGE_Y, Edge.NUM_EDGES);
	}

	public int getNearestVertex(int userX, int userY) {
		return getNearest(userX, userY, POINT_X, POINT_Y, Vertex.NUM_VERTEX);
	}

	public float getHexagonX(int index) {
		return HEXAGON_X[index];
	}

	public float getHexagonY(int index) {
		return HEXAGON_Y[index];
	}

	public float getEdgeX(int index) {
		return EDGE_X[index];
	}

	public float getEdgeY(int index) {
		return EDGE_Y[index];
	}

	public float getVertexX(int index) {
		return POINT_X[index];
	}

	public float getVertexY(int index) {
		return POINT_Y[index];
	}

	public float getTraderX(int index) {
		return HEXAGON_X[TRADER_HEX[index]];
	}

	public float getTraderY(int index) {
		return HEXAGON_Y[TRADER_HEX[index]];
	}

	public float getTraderIconX(int index) {
		return EDGE_X[TRADER_EDGE[index]] + 1.5f * TRADER_OFFSET_X[index];
	}

	public float getTraderIconY(int index) {
		return EDGE_Y[TRADER_EDGE[index]] + 1.5f * TRADER_OFFSET_Y[index];
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

	private static final float[] HEXAGON_X = { -1.44f, -1.44f, -1.44f, -0.72f,
			-0.72f, -0.72f, -0.72f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.72f, 0.72f,
			0.72f, 0.72f, 1.44f, 1.44f, 1.44f };

	private static final float[] HEXAGON_Y = { 0.84f, -0.0f, -0.84f, 1.26f,
			0.42f, -0.42f, -1.26f, 1.68f, 0.84f, -0.0f, -0.84f, -1.68f, 1.26f,
			0.42f, -0.42f, -1.26f, 0.84f, -0.0f, -0.84f };

	private static final float[] POINT_X = { -0.24f, 0.24f, -0.96f, -0.5f,
			0.48f, 0.96f, -1.68f, -1.22f, -0.24f, 0.22f, 1.2f, 1.68f, -1.94f,
			-0.96f, -0.5f, 0.48f, 0.94f, 1.94f, -1.68f, -1.22f, -0.24f, 0.22f,
			1.2f, 1.68f, -1.94f, -0.96f, -0.5f, 0.48f, 0.94f, 1.94f, -1.68f,
			-1.22f, -0.24f, 0.22f, 1.2f, 1.68f, -1.94f, -0.96f, -0.5f, 0.48f,
			0.94f, 1.94f, -1.68f, -1.22f, -0.24f, 0.22f, 1.2f, 1.68f, -0.96f,
			-0.5f, 0.48f, 0.96f, -0.24f, 0.24f };

	private static final float[] POINT_Y = { 2.1f, 2.1f, 1.68f, 1.68f, 1.68f,
			1.68f, 1.26f, 1.26f, 1.26f, 1.26f, 1.26f, 1.26f, 0.84f, 0.84f,
			0.84f, 0.84f, 0.84f, 0.84f, 0.42f, 0.42f, 0.42f, 0.42f, 0.42f,
			0.42f, -0.0f, -0.0f, -0.0f, -0.0f, -0.0f, -0.0f, -0.42f, -0.42f,
			-0.42f, -0.42f, -0.42f, -0.42f, -0.84f, -0.84f, -0.84f, -0.84f,
			-0.84f, -0.84f, -1.26f, -1.26f, -1.26f, -1.26f, -1.26f, -1.26f,
			-1.68f, -1.68f, -1.68f, -1.68f, -2.1f, -2.1f };

	private static final float[] EDGE_X = { 0.0f, -0.37f, 0.36f, -0.73f,
			-1.09f, -0.37f, 0.72f, 0.35f, 1.08f, -1.45f, -1.81f, -1.09f,
			-0.01f, -0.37f, 0.35f, 1.44f, 1.07f, 1.81f, -1.81f, -0.73f, -1.09f,
			-0.37f, 0.71f, 0.35f, 1.07f, 1.81f, -1.45f, -1.81f, -1.09f, -0.01f,
			-0.37f, 0.35f, 1.44f, 1.07f, 1.81f, -1.81f, -0.73f, -1.09f, -0.37f,
			0.71f, 0.35f, 1.07f, 1.81f, -1.45f, -1.81f, -1.09f, -0.01f, -0.37f,
			0.35f, 1.44f, 1.07f, 1.81f, -1.81f, -0.73f, -1.09f, -0.37f, 0.71f,
			0.35f, 1.07f, 1.81f, -1.45f, -1.09f, -0.01f, -0.37f, 0.35f, 1.44f,
			1.08f, -0.73f, -0.37f, 0.72f, 0.36f, 0.0f };

	private static final float[] EDGE_Y = { 2.1f, 1.89f, 1.89f, 1.68f, 1.47f,
			1.47f, 1.68f, 1.47f, 1.47f, 1.26f, 1.05f, 1.05f, 1.26f, 1.05f,
			1.05f, 1.26f, 1.05f, 1.05f, 0.63f, 0.84f, 0.63f, 0.63f, 0.84f,
			0.63f, 0.63f, 0.63f, 0.42f, 0.21f, 0.21f, 0.42f, 0.21f, 0.21f,
			0.42f, 0.21f, 0.21f, -0.21f, -0.0f, -0.21f, -0.21f, -0.0f, -0.21f,
			-0.21f, -0.21f, -0.42f, -0.63f, -0.63f, -0.42f, -0.63f, -0.63f,
			-0.42f, -0.63f, -0.63f, -1.05f, -0.84f, -1.05f, -1.05f, -0.84f,
			-1.05f, -1.05f, -1.05f, -1.26f, -1.47f, -1.26f, -1.47f, -1.47f,
			-1.26f, -1.47f, -1.68f, -1.89f, -1.68f, -1.89f, -2.1f };

	private static final int[] TRADER_EDGE = { 0, 4, 8, 27, 34, 52, 59, 67, 69 };

	private static final int[] TRADER_HEX = { 7, 3, 12, 1, 17, 2, 18, 6, 15 };

	private static final float[] TRADER_OFFSET_X = { 0.0f, -0.16f, 0.15f,
			-0.15f, 0.15f, -0.15f, 0.15f, 0.0f, 0.0f };

	private static final float[] TRADER_OFFSET_Y = { 0.18f, 0.1f, 0.1f, 0.1f,
			0.1f, -0.1f, -0.1f, -0.16f, -0.16f };

}
