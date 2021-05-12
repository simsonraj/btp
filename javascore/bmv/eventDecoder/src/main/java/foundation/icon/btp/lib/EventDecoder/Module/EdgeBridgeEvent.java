package foundation.icon.btp.lib.eventdecoder;

import java.util.Arrays;
import java.util.List;

import foundation.icon.btp.lib.Constant;
import foundation.icon.btp.lib.scale.ScaleReader;
import foundation.icon.btp.lib.utils.ByteSliceInput;
import foundation.icon.btp.lib.utils.Hash;
import foundation.icon.btp.lib.utils.HexConverter;

import score.Context;
import score.ObjectReader;
import scorex.util.ArrayList;
import scorex.util.HashMap;

public class EdgeBridgeEvent {
    public static byte[] decodeEvent(byte subIndex, ByteSliceInput input) {
        switch (subIndex) {
            case (byte)(0x00):
                return transferOverBridge(input);
        }
        return null;
    }

    public static byte[] transferOverBridge(ByteSliceInput input) {
        // this.accountId = input.take(32);
        int startPoint = input.getOffset();
        input.seek(startPoint + 32);
        ScaleReader.readBytes(input); // description
        int descriptionSize = input.getOffset() - startPoint - 32;
        // this.chainId = ScaleReader.readU8(input);
        // this.balance = ScaleReader.readU128(input);
        input.seek(startPoint);
        return input.take(32 + descriptionSize + 1 + 16);
    }
}