package ru.simsonic.rscCommonsLibrary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class VarInt
{
	public static int readVarInt(DataInputStream is) throws IOException
	{
		int result = 0;
		int j = 0;
		for(;;)
		{
			final int k = is.readByte();
			result |= (k & 0x7F) << j++ * 7;
			if(j > 5)
				throw new IOException("VarInt too big");
			if((k & 0x80) != 128)
				break;
		}
		return result;
	}
	public static void writeVarInt(DataOutputStream os, int paramInt) throws IOException
	{
		for(;;)
		{
			if((paramInt & 0xFFFFFF80) == 0)
			{
				os.writeByte(paramInt);
				return;
			}
			os.writeByte(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}
}
