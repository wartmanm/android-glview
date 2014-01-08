package com.github.wartman4404.glview.gl;

import java.nio.ByteBuffer;

import android.util.Log;

public class BufferDumper {
	public static void dumpStuff(ByteBuffer buf, String formatString, PrimitiveType[] types, int count) {
		Object[] formatArgs = new Object[types.length+1];
		int rowSize = 0;
		for (PrimitiveType type: types) {
			rowSize += type.size();
		}
		int rowLimit = buf.remaining() / rowSize;
		if (rowLimit > count)
			rowLimit = count;
		int maxsize = rowLimit * rowSize;
		int maxpos = maxsize + buf.position();
		Log.i("dumpstuff", "got row size: " + rowSize);
		Log.i("dumpstuff", "asked to limit to " + count + " rows, choosing to limit to " + rowLimit + " rows, or 0x" + Integer.toHexString(maxsize) + " bytes");
		ByteBuffer dumpBuffer = buf.duplicate();
		dumpBuffer.order(buf.order());
		if (dumpBuffer.limit() > maxpos) {
			dumpBuffer.limit(maxpos);
		}
		while(dumpBuffer.hasRemaining()) {
			formatArgs[0] = dumpBuffer.position();
			for (int i = 0; i < types.length; i++) {
				formatArgs[i+1] = types[i].read(dumpBuffer);
			}
			Log.i("bufferdump", String.format("0x%04x  " + formatString, formatArgs));
		}
	}
	
	static enum PrimitiveType {
		BYTE,
		CHAR,
		SHORT,
		INT,
		LONG,
		FLOAT,
		DOUBLE;
		Object read(ByteBuffer buf) {
			switch(this) {
			case BYTE:   return buf.get();
			case CHAR:   return buf.getChar();
			case SHORT:  return buf.getShort();
			case INT:    return buf.getInt();
			case LONG:   return buf.getLong();
			case FLOAT:  return buf.getFloat();
			case DOUBLE: return buf.getDouble();
			default: return null;
			}
		}
		int size() {
			switch(this) {
			case BYTE:   return Byte.SIZE/8;
			case CHAR:   return Character.SIZE/8;
			case SHORT:  return Short.SIZE/8;
			case INT:    return Integer.SIZE/8;
			case LONG:   return Long.SIZE/8;
			case FLOAT:  return Float.SIZE/8;
			case DOUBLE: return Double.SIZE/8;
			default: return 0;
			}
		}
	}

	public static void dumpTexturedVertexBuffer(ByteBuffer dataBuffer, int count) {
		PrimitiveType FLOAT = PrimitiveType.FLOAT;
		BufferDumper.dumpStuff(dataBuffer,
				"  C  %6.2f %6.2f %6.2f  N  %6.2f %6.2f %6.2f  UV %6.2f %6.2f ",
				new PrimitiveType[] { FLOAT, FLOAT, FLOAT,  FLOAT, FLOAT, FLOAT,  FLOAT, FLOAT },
				count
				);
	}
	public static void dumpUniformVertexBuffer(ByteBuffer dataBuffer, int count) {
		PrimitiveType FLOAT = PrimitiveType.FLOAT;
		BufferDumper.dumpStuff(dataBuffer,
				"  C  %6.2f %6.2f %6.2f  N  %6.2f %6.2f %6.2f",
				new PrimitiveType[] { FLOAT, FLOAT, FLOAT,  FLOAT, FLOAT, FLOAT },
				count
				);
	}
	public static void dumpNormalMapVertexBuffer(ByteBuffer dataBuffer, int count) {
		PrimitiveType FLOAT = PrimitiveType.FLOAT;
		BufferDumper.dumpStuff(dataBuffer,
				"  C  %6.2f %6.2f %6.2f  N  %6.2f %6.2f %6.2f  UV %6.2f %6.2f  T %6.2f %6.2f %6.2f",
				new PrimitiveType[] { FLOAT, FLOAT, FLOAT,  FLOAT, FLOAT, FLOAT,  FLOAT, FLOAT,  FLOAT, FLOAT, FLOAT},
				count
				);
	}

	public static void dumpIndexBuffer(ByteBuffer dataBuffer, int count) {
		PrimitiveType SHORT = PrimitiveType.SHORT;
		BufferDumper.dumpStuff(dataBuffer, "%4d  %4d  %4d", new PrimitiveType[] { SHORT, SHORT, SHORT }, count);
	}
	
	public static void dumpByType(ByteBuffer vertBuffer, int offset, int count, int type) {
		ByteBuffer tmp = vertBuffer.duplicate();
		tmp.order(vertBuffer.order());
		tmp.position(offset);
		switch (type) {
		case ObjSaver.MATERIAL_UNIFORM_ID:
			dumpUniformVertexBuffer(tmp, count);
			break;
		case ObjSaver.MATERIAL_DIFFUSE_TEXTURED_ID:
			dumpTexturedVertexBuffer(tmp, count);
			break;
		case ObjSaver.MATERIAL_NORMAL_TEXTURED_ID:
			dumpNormalMapVertexBuffer(tmp, count);
			break;
		}
	}
}
