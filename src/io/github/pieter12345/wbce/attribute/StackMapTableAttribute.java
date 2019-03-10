package io.github.pieter12345.wbce.attribute;

import java.io.IOException;
import java.util.ArrayList;

import io.github.pieter12345.wbce.FancyInputStream;
import io.github.pieter12345.wbce.ClassConstantPool;
import io.github.pieter12345.wbce.FancyByteArrayOutputStream;

public class StackMapTableAttribute implements IAttribute {
	
	// Variables & Constants.
	private int attrNameIndex; // Index of the "StackMapTable" string in the constant pool.
	private ArrayList<StackMapFrame> frames;
	
	public StackMapTableAttribute(int attrNameIndex, ArrayList<StackMapFrame> frames) {
		this.attrNameIndex = attrNameIndex;
		this.frames = frames;
	}
	
	public static StackMapTableAttribute fromInputStream(int attrNameIndex, FancyInputStream inStream) throws IOException {
		inStream.readFourByteInt(); // attrInfoLength.
		int frameCount = inStream.readTwoByteInt();
		ArrayList<StackMapFrame> frames = new ArrayList<StackMapFrame>(frameCount);
		for(int i = 0; i < frameCount; i++) {
			frames.add(StackMapFrame.fromInputStream(inStream));
		}
		return new StackMapTableAttribute(attrNameIndex, frames);
	}
	
	@Override
	public byte[] toBytes() {
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
		outStream.writeTwoByteInteger(this.frames.size());
		for(int i = 0; i < this.frames.size(); i++) {
			outStream.write(this.frames.get(i).toBytes());
		}
		byte[] outStreamBytes = outStream.toByteArray();
		
		@SuppressWarnings("resource")
		FancyByteArrayOutputStream outStream2 = new FancyByteArrayOutputStream();
		outStream2.writeTwoByteInteger(this.attrNameIndex);
		outStream2.writeFourByteInteger(outStreamBytes.length);
		outStream2.write(outStreamBytes);
		
		return outStream2.toByteArray();
	}
	
	public StackMapFrame getStackMapFrame(int index) {
		return this.frames.get(index);
	}
	
	public void setStackMapFrame(int index, StackMapFrame frame) {
		this.frames.set(index, frame);
	}
	
	public void addStackMapFrame(StackMapFrame frame) {
		this.frames.add(frame);
	}
	
	public StackMapFrame removeStackMapFrame(int index) {
		return this.frames.remove(index);
	}
	
	public boolean removeStackMapFrame(StackMapFrame frame) {
		return this.frames.remove(frame);
	}
	
	@Override
	public String toString(ClassConstantPool constPool) {
		String framesStr = "";
		for(int i = 0; i < this.frames.size(); i++) {
			framesStr += i + ": " + this.frames.get(i).toString(constPool) + " ";
		}
		return StackMapTableAttribute.class.getSimpleName() + ": {" + framesStr.trim() + "}";
	}
	
	public static abstract interface StackMapFrame {
		
		public static StackMapFrame fromInputStream(FancyInputStream inStream) throws IOException {
			int frameType = inStream.readUnsignedByte(); // In range of [0-255].
			if(frameType >= 0 && frameType <= 63) {
				return SameFrame.fromInputStream(frameType, inStream);
			} else if(frameType >= 64 && frameType <= 127) {
				return SameLocals1StackItemFrame.fromInputStream(frameType, inStream);
			} else if(frameType == 247) {
				return SameLocals1StackItemFrameExtended.fromInputStream(frameType, inStream);
			} else if(frameType >= 248 && frameType <= 250) {
				return ChopFrame.fromInputStream(frameType, inStream);
			} else if(frameType == 251) {
				return SameFrameExtended.fromInputStream(frameType, inStream);
			} else if(frameType >= 252 && frameType <= 254) {
				return AppendFrame.fromInputStream(frameType, inStream);
			} else if(frameType == 255) {
				return FullFrame.fromInputStream(frameType, inStream);
			}
			System.out.println("[DEBUG] [" + StackMapTableAttribute.class.getSimpleName() + "]"
					+ " Found invalid/unimplemented stack frame type: " + frameType);
			return null;
		}
		
		public int getFrameType();
		
		public byte[] toBytes();
		
		public String toString(ClassConstantPool constPool);
		
		public static class SameFrame implements StackMapFrame {
			
			// Variables & Constants.
			private final int frameType;
			
			public SameFrame(int frameType) {
				this.frameType = frameType;
			}
			
			public static SameFrame fromInputStream(int frameType, FancyInputStream inStream) throws IOException {
				return new SameFrame(frameType);
			}
			
			@Override
			public byte[] toBytes() {
				@SuppressWarnings("resource")
				FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
				outStream.writeUnsignedByte(this.frameType);
				return outStream.toByteArray();
			}
			
			@Override
			public int getFrameType() {
				return this.frameType;
			}
			
			@Override
			public String toString(ClassConstantPool constPool) {
				return SameFrame.class.getSimpleName() + ": {frame_type=" + this.frameType + "}";
			}
		}
		
		public static class SameFrameExtended implements StackMapFrame {
			
			// Variables & Constants.
			private final int frameType;
			private int offsetDelta;
			
			public SameFrameExtended(int frameType, int offsetDelta) {
				this.frameType = frameType;
				this.offsetDelta = offsetDelta;
			}
			
			public static SameFrameExtended fromInputStream(int frameType, FancyInputStream inStream) throws IOException {
				int offsetDelta = inStream.readTwoByteInt();
				return new SameFrameExtended(frameType, offsetDelta);
			}
			
			@Override
			public byte[] toBytes() {
				@SuppressWarnings("resource")
				FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
				outStream.writeUnsignedByte(this.frameType);
				outStream.writeTwoByteInteger(this.offsetDelta);
				return outStream.toByteArray();
			}
			
			@Override
			public int getFrameType() {
				return this.frameType;
			}
			
			public int getOffsetDeltaType() {
				return this.offsetDelta;
			}
			
			@Override
			public String toString(ClassConstantPool constPool) {
				return SameFrameExtended.class.getSimpleName() + ": {"
						+ "frame_type=" + this.frameType + ", offset_delta=" + this.offsetDelta + "}";
			}
		}

		public static class ChopFrame implements StackMapFrame {
			
			// Variables & Constants.
			private final int frameType;
			private int offsetDelta;
			
			public ChopFrame(int frameType, int offsetDelta) {
				this.frameType = frameType;
				this.offsetDelta = offsetDelta;
			}
			
			public static ChopFrame fromInputStream(int frameType, FancyInputStream inStream) throws IOException {
				int offsetDelta = inStream.readTwoByteInt();
				return new ChopFrame(frameType, offsetDelta);
			}
			
			@Override
			public byte[] toBytes() {
				@SuppressWarnings("resource")
				FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
				outStream.writeUnsignedByte(this.frameType);
				outStream.writeTwoByteInteger(this.offsetDelta);
				return outStream.toByteArray();
			}
			
			@Override
			public int getFrameType() {
				return this.frameType;
			}
			
			public int getOffsetDeltaType() {
				return this.offsetDelta;
			}
			
			@Override
			public String toString(ClassConstantPool constPool) {
				return ChopFrame.class.getSimpleName() + ": {"
						+ "frame_type=" + this.frameType + ", offset_delta=" + this.offsetDelta + "}";
			}
		}
		
		public static class SameLocals1StackItemFrame implements StackMapFrame {
			
			// Variables & Constants.
			private final int frameType;
			private VerificationTypeInfo veriTypeInfo;
			
			public SameLocals1StackItemFrame(int frameType, VerificationTypeInfo veriTypeInfo) {
				this.frameType = frameType;
				this.veriTypeInfo = veriTypeInfo;
			}
			
			public static SameLocals1StackItemFrame fromInputStream(int frameType, FancyInputStream inStream) throws IOException {
				VerificationTypeInfo veriTypeInfo = VerificationTypeInfo.fromInputStream(inStream);
				return new SameLocals1StackItemFrame(frameType, veriTypeInfo);
			}
			
			@Override
			public byte[] toBytes() {
				@SuppressWarnings("resource")
				FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
				outStream.writeUnsignedByte(this.frameType);
				outStream.write(this.veriTypeInfo.toBytes());
				return outStream.toByteArray();
			}
			
			@Override
			public int getFrameType() {
				return this.frameType;
			}
			
			@Override
			public String toString(ClassConstantPool constPool) {
				return SameLocals1StackItemFrame.class.getSimpleName() + ": {"
						+ "frame_type=" + this.frameType
						+ ", verification_type_info=" + this.veriTypeInfo.toString(constPool) + "}";
			}
		}
		
		public static class SameLocals1StackItemFrameExtended implements StackMapFrame {
			
			// Variables & Constants.
			private final int frameType;
			private int offsetDelta;
			private VerificationTypeInfo veriTypeInfo;
			
			public SameLocals1StackItemFrameExtended(int frameType, int offsetDelta, VerificationTypeInfo veriTypeInfo) {
				this.frameType = frameType;
				this.offsetDelta = offsetDelta;
				this.veriTypeInfo = veriTypeInfo;
			}
			
			public static SameLocals1StackItemFrameExtended fromInputStream(int frameType, FancyInputStream inStream) throws IOException {
				int offsetDelta = inStream.readTwoByteInt();
				VerificationTypeInfo veriTypeInfo = VerificationTypeInfo.fromInputStream(inStream);
				return new SameLocals1StackItemFrameExtended(frameType, offsetDelta, veriTypeInfo);
			}
			
			@Override
			public byte[] toBytes() {
				@SuppressWarnings("resource")
				FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
				outStream.writeUnsignedByte(this.frameType);
				outStream.writeTwoByteInteger(this.offsetDelta);
				outStream.write(this.veriTypeInfo.toBytes());
				return outStream.toByteArray();
			}
			
			@Override
			public int getFrameType() {
				return this.frameType;
			}
			
			@Override
			public String toString(ClassConstantPool constPool) {
				return SameLocals1StackItemFrameExtended.class.getSimpleName() + ": {"
						+ "frame_type=" + this.frameType
						+ ", offset_delta=" + this.offsetDelta
						+ ", verification_type_info=" + this.veriTypeInfo.toString(constPool) + "}";
			}
		}
		
		public static class AppendFrame implements StackMapFrame {
			
			// Variables & Constants.
			private final int frameType;
			private int offsetDelta;
			private VerificationTypeInfo[] veriTypeInfoArray;
			
			public AppendFrame(int frameType, int offsetDelta, VerificationTypeInfo[] veriTypeInfoArray) {
				this.frameType = frameType;
				this.offsetDelta = offsetDelta;
				this.veriTypeInfoArray = veriTypeInfoArray;
			}
			
			public static AppendFrame fromInputStream(int frameType, FancyInputStream inStream) throws IOException {
				int offsetDelta = inStream.readTwoByteInt();
				VerificationTypeInfo[] veriTypeInfoArray = new VerificationTypeInfo[frameType - 251];
				for(int i = 0; i < veriTypeInfoArray.length; i++) {
					veriTypeInfoArray[i] = VerificationTypeInfo.fromInputStream(inStream);
				}
				return new AppendFrame(frameType, offsetDelta, veriTypeInfoArray);
			}
			
			@Override
			public byte[] toBytes() {
				@SuppressWarnings("resource")
				FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
				outStream.writeUnsignedByte(this.frameType);
				outStream.writeTwoByteInteger(this.offsetDelta);
				for(int i = 0; i < this.veriTypeInfoArray.length; i++) {
					outStream.write(this.veriTypeInfoArray[i].toBytes());
				}
				return outStream.toByteArray();
			}
			
			@Override
			public int getFrameType() {
				return this.frameType;
			}
			
			@Override
			public String toString(ClassConstantPool constPool) {
				String veriTypeInfoStr = "";
				for(int i = 0; i < this.veriTypeInfoArray.length; i++) {
					veriTypeInfoStr += i + ": " + this.veriTypeInfoArray[i].toString(constPool);
				}
				return AppendFrame.class.getSimpleName() + ": {"
						+ "frame_type=" + this.frameType
						+ ", offset_delta=" + this.offsetDelta
						+ ", verification_type_info[]={" + veriTypeInfoStr.trim() + "}}";
			}
		}
		
		public static class FullFrame implements StackMapFrame {
			
			// Variables & Constants.
			private final int frameType;
			private int offsetDelta;
			private VerificationTypeInfo[] veriTypeInfoLocalsArray;
			private VerificationTypeInfo[] veriTypeInfoStackArray;
			
			public FullFrame(int frameType, int offsetDelta, VerificationTypeInfo[] veriTypeInfoLocalsArray, VerificationTypeInfo[] veriTypeInfoStackArray) {
				this.frameType = frameType;
				this.offsetDelta = offsetDelta;
				this.veriTypeInfoLocalsArray = veriTypeInfoLocalsArray;
				this.veriTypeInfoStackArray = veriTypeInfoStackArray;
			}
			
			public static FullFrame fromInputStream(int frameType, FancyInputStream inStream) throws IOException {
				int offsetDelta = inStream.readTwoByteInt();
				int veriTypeInfoLocalsCount = inStream.readTwoByteInt();
				VerificationTypeInfo[] veriTypeInfoLocalsArray = new VerificationTypeInfo[veriTypeInfoLocalsCount];
				for(int i = 0; i < veriTypeInfoLocalsArray.length; i++) {
					veriTypeInfoLocalsArray[i] = VerificationTypeInfo.fromInputStream(inStream);
				}
				int veriTypeInfoStackCount = inStream.readTwoByteInt();
				VerificationTypeInfo[] veriTypeInfoStackArray = new VerificationTypeInfo[veriTypeInfoStackCount];
				for(int i = 0; i < veriTypeInfoStackArray.length; i++) {
					veriTypeInfoStackArray[i] = VerificationTypeInfo.fromInputStream(inStream);
				}
				return new FullFrame(frameType, offsetDelta, veriTypeInfoLocalsArray, veriTypeInfoStackArray);
			}
			
			@Override
			public byte[] toBytes() {
				@SuppressWarnings("resource")
				FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
				outStream.writeUnsignedByte(this.frameType);
				outStream.writeTwoByteInteger(this.offsetDelta);
				outStream.writeTwoByteInteger(this.veriTypeInfoLocalsArray.length);
				for(int i = 0; i < this.veriTypeInfoLocalsArray.length; i++) {
					outStream.write(this.veriTypeInfoLocalsArray[i].toBytes());
				}
				outStream.writeTwoByteInteger(this.veriTypeInfoStackArray.length);
				for(int i = 0; i < this.veriTypeInfoStackArray.length; i++) {
					outStream.write(this.veriTypeInfoStackArray[i].toBytes());
				}
				return outStream.toByteArray();
			}
			
			@Override
			public int getFrameType() {
				return this.frameType;
			}
			
			@Override
			public String toString(ClassConstantPool constPool) {
				String veriTypeInfoLocalsStr = "";
				for(int i = 0; i < this.veriTypeInfoLocalsArray.length; i++) {
					veriTypeInfoLocalsStr += i + ": " + this.veriTypeInfoLocalsArray[i].toString(constPool);
				}
				String veriTypeInfoStackStr = "";
				for(int i = 0; i < this.veriTypeInfoStackArray.length; i++) {
					veriTypeInfoStackStr += i + ": " + this.veriTypeInfoStackArray[i].toString(constPool);
				}
				return FullFrame.class.getSimpleName() + ": {"
						+ "frame_type=" + this.frameType
						+ ", offset_delta=" + this.offsetDelta
						+ ", verification_type_info locals[]={" + veriTypeInfoLocalsStr.trim()
						+ ", verification_type_info stack[]={" + veriTypeInfoStackStr.trim() + "}}";
			}
		}
		
		public static enum VerificationTypeInfoType {
			TOP_VARIABLE_INFO               ((byte) 0),
			INTEGER_VARIABLE_INFO           ((byte) 1),
			FLOAT_VARIABLE_INFO             ((byte) 2),
			DOUBLE_VARIABLE_INFO            ((byte) 3),
			LONG_VARIABLE_INFO              ((byte) 4),
			NULL_VARIABLE_INFO              ((byte) 5),
			UNINITIALIZED_THIS_VARIABLE_INFO((byte) 6),
			OBJECT_VARIABLE_INFO            ((byte) 7, true),
			UNINITIALIZED_VARIABLE_INFO     ((byte) 8, true);
			
			
			// Variables & Constants.
			private final byte tag;
			private boolean hasExtraData;
			
			private VerificationTypeInfoType(byte tag) {
				this(tag, false);
			}
			private VerificationTypeInfoType(byte tag, boolean hasExtraData) {
				this.tag = tag;
				this.hasExtraData = hasExtraData;
			}
			
			public static VerificationTypeInfoType forTag(byte tag) {
				for(VerificationTypeInfoType veriTypeInfo : VerificationTypeInfoType.values()) {
					if(veriTypeInfo.getTag() == tag) {
						return veriTypeInfo;
					}
				}
				return null;
			}
			
			public byte getTag() {
				return this.tag;
			}
			
			public boolean hasExtraData() {
				return this.hasExtraData;
			}
		}
		
		public static class VerificationTypeInfo {
			
			// Variables & Constants.
			private final byte tag;
			private Integer extraData; // Will be null if there is no extra data.
			
			private VerificationTypeInfo(byte tag, Integer extraData) {
				this.tag = tag;
				this.extraData = extraData;
			}
			
			public static VerificationTypeInfo fromInputStream(FancyInputStream inStream) throws IOException {
				byte tag = (byte) inStream.readUnsignedByte();
				VerificationTypeInfoType veriTypeInfo = VerificationTypeInfoType.forTag(tag);
				if(veriTypeInfo == null) {
					throw new RuntimeException("IVariableInfo in a StackMapAttribute had an invalid tag: " + tag);
				}
				Integer extraData = null;
				if(veriTypeInfo.hasExtraData()) {
					extraData = inStream.readTwoByteInt();
				}
				return new VerificationTypeInfo(tag, extraData);
			}
			
			public byte[] toBytes() {
				@SuppressWarnings("resource")
				FancyByteArrayOutputStream outStream = new FancyByteArrayOutputStream();
				outStream.writeUnsignedByte(this.tag);
				if(this.extraData != null) {
					outStream.writeTwoByteInteger(this.extraData);
				}
				return outStream.toByteArray();
			}
			
			public byte getTag() {
				return this.tag;
			}
			
			public Integer getExtraData() {
				return this.extraData;
			}
			
			public String toString(ClassConstantPool constPool) {
				String veriTypeInfoStr = VerificationTypeInfoType.forTag(this.tag).name();
				return VerificationTypeInfo.class.getSimpleName() + ": {tag=" + veriTypeInfoStr
						+ (this.extraData != null ? ", data=" + this.extraData : "") + "}";
			}
		}
		
	}
	
}
