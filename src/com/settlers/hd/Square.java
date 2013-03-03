package com.settlers.hd;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Square {

	private static final float[] texture = { 0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f };
	private static final short[] indices = { 0, 1, 2, 3 };

	private FloatBuffer vertexBuffer, textureBuffer, colorBuffer;
	private ShortBuffer indexBuffer;
	private int resource;

	public Square(int resource, float x, float y, float depth, float width, float height) {
		this.resource = resource;
		colorBuffer = null;

		float left = x - width / 2;
		float right = left + width;
		float bottom = y - height / 2;
		float top = bottom + height;

		float vertices[] = { left, bottom, depth, left, top, depth, right, bottom,
				depth, right, top, depth };

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		ByteBuffer tbb = ByteBuffer.allocateDirect(vertices.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		textureBuffer = tbb.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);

		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}
	
	public Square(float[] colors, float x, float y, float depth, float width, float height) {
		textureBuffer = null;
		
		float left = x - width / 2;
		float right = left + width;
		float bottom = y - height / 2;
		float top = bottom + height;

		float vertices[] = { left, bottom, depth, left, top, depth, right, bottom,
				depth, right, top, depth };

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		colorBuffer = cbb.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);

		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	public void render(GL10 gl) {		
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glFrontFace(GL10.GL_CW);
		gl.glCullFace(GL10.GL_BACK);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		if (colorBuffer != null)
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		if (textureBuffer != null)
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, resource);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		if (colorBuffer != null)
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		
		if (textureBuffer != null)
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, indices.length,
				GL10.GL_UNSIGNED_SHORT, indexBuffer);

		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_CULL_FACE);
	}
}
