package com.github.wartman4404.glview.gl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BoundingBox {
	public float minZ;
	public float maxZ;
	public float minY;
	public float maxY;
	public float minX;
	public float maxX; 
	public float depth() {
		return maxZ - minZ;
	}
	public float width() {
		return maxX - minX;
	}
	public float height() {
		return maxY - minY;
	}
	public float centerX() {
		return maxX/2 + minX/2;
	}
	public float centerY() {
		return maxY/2 + minY/2;
	}
	public float centerZ() {
		return maxZ/2 + minZ/2;
	}
	public void addPoint(float x, float y, float z) {
		if (x > maxX) maxX = x;
		if (x < minX) minX = x;
		if (y > maxY) maxY = y;
		if (y < minY) minY = y;
		if (z > maxZ) maxZ = z;
		if (z < minZ) minZ = z;
	}
	public void squarify() {
		float max = depth();
		max = max > width() ? max : width();
		max = max > height() ? max : height();
		float offsetX = (max - width()) / 2;
		float offsetY = (max - height()) / 2;
		float offsetZ = (max - depth()) / 2;
		minX -= offsetX;
		maxX += offsetX;
		minY -= offsetY;
		maxY += offsetY;
		minZ -= offsetZ;
		maxZ += offsetZ;
	}
	public void addBox(BoundingBox other) {
		addPoint(other.minX, other.minY, other.minZ);
		addPoint(other.maxX, other.maxY, other.maxZ);
	}
	public void save(DataOutputStream out) throws IOException {
		out.writeFloat(minX);
		out.writeFloat(maxX);
		out.writeFloat(minY);
		out.writeFloat(maxY);
		out.writeFloat(minZ);
		out.writeFloat(maxZ);
	}
	public BoundingBox(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minZ = minZ;
		this.maxZ = maxZ;
	}
	public BoundingBox(DataInputStream in) throws IOException {
		this(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
	}
	public BoundingBox(float x, float y, float z) {
		this(x, x, y, y, z, z);
	}

	public BoundingBox(BoundingBox that) {
		this.minX = that.minX;
		this.maxX = that.maxX;
		this.minY = that.minY;
		this.maxY = that.maxY;
		this.minZ = that.minZ;
		this.maxZ = that.maxZ;
	}
	public String toString() {
		return String.format("%.2f, %.2f, %.2f to %.2f, %.2f, %.2f", minX, minY, minZ, maxX, maxY, maxZ);
	}
}
