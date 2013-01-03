package com.settlers.hd;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Line implements Drawable {

	private static final short[] indices = { 0, 1, 2, 3 };

	private FloatBuffer vertexBuffer;
	private ShortBuffer indexBuffer;
	
	private float[] color;

	public Line(float x1, float y1, float x2, float y2, float depth, float width, float[] color) {
		this.color = color;

		float dx = x2 - x1;
		float dy = y2 - y1;
		
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		
		dx *= width / len;
		dy *= width / len;
		
		float vertices[] = {
				x1 - dy, y1 - dx, depth,
				x1 + dy, y1 + dx, depth,
				x2 + dy, y2 + dx, depth,
				x2 - dy, y2 - dx, depth
		};

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}
	
	public void render(GL10 gl) {		
		gl.glColor4f(color[0], color[1], color[2], color[3]);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		gl.glDrawElements(GL10.GL_TRIANGLE_FAN, indices.length,
				GL10.GL_UNSIGNED_SHORT, indexBuffer);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
}
