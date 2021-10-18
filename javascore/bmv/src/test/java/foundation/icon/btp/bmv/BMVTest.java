package foundation.icon.btp.bmv;

import com.iconloop.testsvc.Account;
import com.iconloop.testsvc.Score;
import com.iconloop.testsvc.ServiceManager;
import com.iconloop.testsvc.TestBase;
import foundation.icon.btp.bmv.lib.mpt.MPTException;
import foundation.icon.btp.bmv.types.*;
import foundation.icon.ee.io.DataWriter;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.data.TransactionResult;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import score.ByteArrayObjectWriter;
import score.Context;
import scorex.util.ArrayList;
import scorex.util.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(CustomParameterResolver.class)
public class BMVTest extends TestBase {

    final static String RLPn = "RLPn";
    private static final ServiceManager sm = getServiceManager();

    private static final int rootSize = 3;
    private static final int cacheSize = 10;
    private static final String sol_bmc = "8cd1d5d16caf488efc057e4fc3add7c11b01d9b0";
    //    private static final int offset = 55;
    /* private static final String sol_bmc = "8cd1d5d16caf488efc057e4fc3add7c11b01d9b0";
     private static final int offset = 113;*/
    private static final boolean isAllowNewerWitness = true;

    private static Score bmv;
    private static KeyWallet[] accounts;
    private static List<byte[]> validators_pub_keys;
    private static List<byte[]> validators_priv_keys;
    private static String currentBMCAdd;
    private static String currentBMCNet;
    private static String prevBMCAdd;
    private static String prevBMCnet;  //also destination network

    private static String currentBMCBTPAdd;
    private static String prevBMCBTPAdd;

    private static Account[] owners;

    @BeforeEach
    public void setup(int offset) throws Exception {
        accounts = new KeyWallet[5];
        owners = new Account[3];
        for (int i = 0; i < owners.length; i++) {
            owners[i] = sm.createAccount(100);
        }
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = KeyWallet.create();
        }
        currentBMCAdd = owners[0].getAddress().toString();
        currentBMCNet = "0x1.iconee";
        prevBMCAdd = sol_bmc;
        prevBMCnet = "0x1.bsc"; //also destination network

        currentBMCBTPAdd = "btp://" + currentBMCNet + "/" + currentBMCAdd;
        prevBMCBTPAdd = "btp://" + prevBMCnet + "/" + prevBMCAdd;

        // bmv = spy(new BTPMessageVerifier(bmc,bmcBTPAddress.getNet(),encodedValidators,offset,rootSize,cacheSize,isAllowNewerWitness));
        bmv = sm.deploy(owners[0], BTPMessageVerifier.class, currentBMCAdd, prevBMCnet, offset, rootSize, cacheSize, isAllowNewerWitness);
    }



    /*
        @Test
        @Order(1)
        public void test_handleRelayMessage() throws MPTException {

            //header bytes sample value from verify.js poc:108 "headerEncoded" var
            byte[] headerBytes = Hex.decode("f9025aa0bf25430263c61d1d64f68bcd3695d0973b7c2499cec457d11354098e844d9ceca01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479448948297c3236ec3ea6c95f4eec22fdb18255e55a08cce51d9e20deb11ed161cc68dbcb126eed23523bba3b831c5fc6e57aa13f207a07cd40799a188776553982c23ead78405e8d3afd53337fe67e8b3147c759e488da0984f36aad5db6cd51b4857a9ebb8dbcb9b7bf7e483d6ff852d96ca6092a81620b90100000000020000000002000040000000000000000000000000000000000000000000011000000000000020000020000000000080000000000000000000002000004000000000000000000000080000000021000000001000020004000000008000000800200000000400000000000000000000000000000000000000100000000010100000000000000000000000000000000004000000000000000200000000001200000000000000048000000000100000000000000000000000000000000000002000028000200000000000000000000000000000000000001040000000000002100000000000000000000000000100000400100000000000000000008000000272840186aacc830853fc84610c16a8b861d883010006846765746888676f312e31352e36856c696e757800000011c0aa9e6faed5519244ded947a4b70c80915f5a49cd0338b2731514a054a67becfcb23e04a8266400bd06717db5971c7eca4ca076917a1ab46d9f25059a2d14c9f4273900a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
            BlockHeader header = BlockHeader.fromBytes(headerBytes);
            BlockUpdate bu = new BlockUpdate(header, null, new byte[0][0]);
            List<BlockUpdate> buList = new ArrayList<BlockUpdate>();
            buList.add(bu);

            //witness got from poc verify.js:47 "witness" of transactionProof
            byte[] witness = Hex.decode("f8912880887fffffffffffffff9400000000000000000000000000000000000010008724057de9042f00a4f340fa0100000000000000000000000048948297c3236ec3ea6c95f4eec22fdb18255e5581e5a0abe917502a075c44ad2ddef608112267bb303d61140f69d2dc8ddf3e862b3e9ea0722249d2348e7a82d0a1e1d8697163b23baa4929813faa0920d669550aff88d9");
            BlockWitness bw = BlockWitness.fromBytes(witness);
            BlockProof blockProof = new BlockProof(header, bw);

            //receipts root hash = d5fb9fafd6b0c3d46d6c9e08d9947a70e95400dd0bf21467ea3d3f17ce77651b
            // from  keccak(rlp.encode(resp.receiptProof[0]))
            // rp= mptproofs = needed to prove the Receipt in MPT = rlp.encode(encodedProof).toString('hex') where encodedeProof = rlp.encode(resp.receiptProof)

            byte[] rp = Hex.decode("f90683b853f851a024659e8223cdf8e2147925381730d012b41f4c62d443964757e3ee8f3d9e347a80808080808080a049679fa867c754666b328950663558758d93dfafd36b830cdac96013379a01df8080808080808080b9062bf9062830b90624f906210183084052b9010000000002000000000200000000000000000000000000000000000000000000000001000000000000002000000000000000008000000000000000000000200000400000000000000000000008000000000000000000100002000400000000800000000000000000040000000000000000000000000000000000000010000000001000000000000000000000000000000000000000000000000000020000000000120000000000000004800000000010000000000000000000000000000000000000200002000000000000000000000000000000000000000000000000000000000210000000000000000000000000000000000010000000000000000000800000f90516f89b9448b163bdfea4f745d2be987ebc24ab81ec917c98f863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa000000000000000000000000070e789d2f5d469ea30e0525dbfdd5515d6ead30da00000000000000000000000003cc15e6fe06ca92c5e062463472852b68af526f4a00000000000000000000000000000000000000000000000000000000000000064f89b9448b163bdfea4f745d2be987ebc24ab81ec917c98f863a08c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925a000000000000000000000000070e789d2f5d469ea30e0525dbfdd5515d6ead30da00000000000000000000000003cc15e6fe06ca92c5e062463472852b68af526f4a00000000000000000000000000000000000000000000000000000000000000000f901da9417c44bc2a35829a84bfb53f6d9247273227a744ae1a037be353f216cf7e33639101fd610c542e6a0c0109173fa1c1d8b04d34edb7c1bb901a00000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000001c6274703a2f2f6273632f3078313233343536373831323334353637380000000000000000000000000000000000000000000000000000000000000000000000def8dcb8436274703a2f2f6274703a2f2f6273632f3078313763343462633261333538323961383462666235336636643932343732373332323761373434610000000000000000009c6274703a2f2f6273632f30783132333435363738313233343536373888546f6b656e42534800b86ef86c00b869f867b3307837306537383964326635643436396561333065303532356462666464353531356436656164333064000000000000000000aa687862366235373931626530623565663637303633623363313062383430666238313531346462326664c7c68345544863010000f901fc94503c16557325c0fcdf5fab9e31b7ec29f9690066f842a050d22373bb84ed1f9eeb581c913e6d45d918c05f8b1d90f0be168f06a4e6994aa000000000000000000000000070e789d2f5d469ea30e0525dbfdd5515d6ead30db901a00000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c000000000000000000000000000000000000000000000000000000000000000346274703a2f2f6273632f6878623662353739316265306235656636373036336233633130623834306662383135313464623266640000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000063000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000034554480000000000000000000000000000000000000000000000000000000000");

            // Mocking relay message from actual data
            //relay message
            ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
            relayMsgWriter.beginList(3);
            //blockUpdates
            relayMsgWriter.beginList(2);

            ByteArrayObjectWriter blockUpdateWriter = Context.newByteArrayObjectWriter(RLPn);
            BlockUpdate.writeObject(blockUpdateWriter, bu, headerBytes);
            relayMsgWriter.write(blockUpdateWriter.toByteArray());


            relayMsgWriter.end();
            //blockProof
            ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
            blockProofWrtr.beginList(2);
            blockProofWrtr.write(headerBytes); //block header
            blockProofWrtr.write(witness); // block witness
            blockProofWrtr.end();
            relayMsgWriter.write(blockProofWrtr.toByteArray());
            //receiptProof
            relayMsgWriter.beginList(1);
            ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
            receiptProofWtr.beginList(4);
            receiptProofWtr.write(0);
            receiptProofWtr.write(rp); // receipt proof
            receiptProofWtr.writeNull();
            receiptProofWtr.writeNull();
            receiptProofWtr.end();
            relayMsgWriter.write(receiptProofWtr.toByteArray());
            relayMsgWriter.end();
            relayMsgWriter.end();
            byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());


            bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, _msg);

            byte[] btpMsg = Hex.decode("f864b83d6274703a2f2f6273632f3078386364316435643136636166343838656663303537653466633361646437633131623031643962300000000000000000008362736388546f6b656e4253480096d50293d200905472616e7366657220537563636573730000000000000000000000000000000000000000000000000000");
            BTPMessage.fromBytes(btpMsg);
        }*/
    @ParameterizedTest(name = "should_handleRelayMessage")
    @CsvSource({"55"})
    @Order(1)
    public void should_handleRelayMessage(int offset) {
        //header bytes sample value from verify.js poc:108 "headerEncoded" var

        byte[] headerBytes = Hex.decode("f90259a0762577e92f95731a13473cc53e02fdd689963d993bb5e154b284fa4803e89142a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0997a1add7987e3d4e4498fa04fb343411676b44e4f229c8764ba8007d515a22fa0ce8bc9f9a3bb622d7eb3c2506f6e00a9f0d01d2ae19f2c02b5f39bdb7cb1c717a0d5fb9fafd6b0c3d46d6c9e08d9947a70e95400dd0bf21467ea3d3f17ce77651bb90100010000020000000000000002000800000000000000000000008000800000000000000000800000000000000000000200000000000000000000000000002000000000000000000000000000080000000000000080000000000000000000000000200000000000000040000000840000000020000000800000000000100000000000000000100000000000000000000008000000000000000000000008001000000200000000080000040040000000000000000000000000001000000000000000002000020000000000000000000000000000000000000800000040000000000000100000000000000000010000000000000000000000000000000000000000000038836691b78305177c8460c09b7bb861d883010006846765746888676f312e31362e36856c696e757800000011c0aa9e9868fed7a999be887a6ce9ec962a7cdecfd9169464b9914dfd06a103fc608f6b4132335cd76ba7e3028b1c3c6b89f04fc7ae94c69b8b266b90f24b82db9dc90d00a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
        BlockHeader header = BlockHeader.fromBytes(headerBytes);
        BlockUpdate bu = new BlockUpdate(header, null, new byte[0][0], null);
        List<BlockUpdate> buList = new ArrayList<BlockUpdate>();
        buList.add(bu);

        //header bytes sample value from verify.js poc:108 "headerEncoded" var
        byte[] headerBytes57 = Hex.decode("f90259a0c93b8edba9a9d845138f2ae0fc38d66958251e13fd18d1549d3e7104585fa10ba01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a06bd8ea40af1ed47dc8d1792c33c5b099dd62af2725797a9704413f52483c86e3a0cdad2da57c7a0eff2aa32fae25babcdafe016330f68c09034cd392b1836f0a0da05dec8b2792fc693f00f23e4e4d71676f22ea2126494acfeed621bf9279b1d83fb90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000039836691b7834340568460cb5601b861d883010006846765746888676f312e31362e36856c696e757800000011c0aa9e9868fed7a999be887a6ce9ec962a7cdecfd9169464b9914dfd06a103fc608f6b4132335cd76ba7e3028b1c3c6b89f04fc7ae94c69b8b266b90f24b82db9dc90d00a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
        BlockHeader header57 = BlockHeader.fromBytes(headerBytes57);
        BlockUpdate bu57 = new BlockUpdate(header57, null, new byte[0][0], null);
        List<BlockUpdate> buList57 = new ArrayList<BlockUpdate>();
        buList57.add(bu57);

        //witness got from poc verify.js:47 "witness" of transactionProof
        byte[] witness = Hex.decode("f9024b378504a817c8008347e7c4948cd1d5d16caf488efc057e4fc3add7c11b01d9b080b901e4e995e3de00000000000000000000000000000000000000000000000000000000000000c00000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000014000000000000000000000000053f1aaac3db0557bd6413da6ade72e53d45b77ab00000000000000000000000000000000000000000000000000000000000001a00000000000000000000000000000000000000000000000000000000000000064000000000000000000000000000000000000000000000000000000000000000362736300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008546f6b656e425348000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002a307835343332364232616436413741663733453066384538613434373845313343323643423832393439000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003455448000000000000000000000000000000000000000000000000000000000025a01d1d49798a10c7abb230fcbaedb48867a2e3f690c0671d8841451fb4d7e8c10aa0528cf1e4ea354ab2d01dde65c724a22d3807d6b9b77fad41700ad53a7d4914e2");
        BlockWitness bw = BlockWitness.fromBytes(witness);
        BlockProof blockProof = new BlockProof(header, bw);

        //receipts root hash = d5fb9fafd6b0c3d46d6c9e08d9947a70e95400dd0bf21467ea3d3f17ce77651b
        // from  keccak(rlp.encode(resp.receiptProof[0]))
        // rp= mptproofs = needed to prove the Receipt in MPT = rlp.encode(encodedProof).toString('hex') where encodedeProof = rlp.encode(resp.receiptProof)

        byte[] rp = Hex.decode("f905a5b905a2f9059f822080b90599f90596018305177cb9010001000002000000000000000200080000000000000000000000800080000000000000000080000000000000000000020000000000000000000000000000200000000000000000000000000008000000000000008000000000000000000000000020000000000000004000000084000000002000000080000000000010000000000000000010000000000000000000000800000000000000000000000800100000020000000008000004004000000000000000000000000000100000000000000000200002000000000000000000000000000000000000080000004000000000000010000000000000000001000000000000000000000000000000000000000000f9048bf89b94b27345f8e20bf8cdd839c837b792b5452c282c22f863a08c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925a00000000000000000000000009c0604273c25c268bad67935553d82437387a397a00000000000000000000000009c0604273c25c268bad67935553d82437387a397a00000000000000000000000000000000000000000000000000000000000000064f89b94b27345f8e20bf8cdd839c837b792b5452c282c22f863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa00000000000000000000000009c0604273c25c268bad67935553d82437387a397a000000000000000000000000053f1aaac3db0557bd6413da6ade72e53d45b77aba00000000000000000000000000000000000000000000000000000000000000064f89b94b27345f8e20bf8cdd839c837b792b5452c282c22f863a08c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925a00000000000000000000000009c0604273c25c268bad67935553d82437387a397a00000000000000000000000009c0604273c25c268bad67935553d82437387a397a00000000000000000000000000000000000000000000000000000000000000000f899948cd1d5d16caf488efc057e4fc3add7c11b01d9b0e1a0aa0f21ab61398ccea95cd4e139e0d3c2cf1438e8034b7e8e9701b80bc39d2f56b86000000000000000000000000000000000000000000000000000000000000000380000000000000000000000000000000000000000000000000000000000000000762577e92f95731a13473cc53e02fdd689963d993bb5e154b284fa4803e89142f9013b948cd1d5d16caf488efc057e4fc3add7c11b01d9b0f842a037be353f216cf7e33639101fd610c542e6a0c0109173fa1c1d8b04d34edb7c1ba083d57b2915dae13afb3bdeac7357363ed5e6545fb90a89f09722f0f95f9efa91b8e0000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000066f864b83d6274703a2f2f6273632f3078386364316435643136636166343838656663303537653466633361646437633131623031643962300000000000000000008362736388546f6b656e4253480096d50293d200905472616e7366657220537563636573730000000000000000000000000000000000000000000000000000f8d9949c0604273c25c268bad67935553d82437387a397e1a0356868e4a05430bccb6aa9c954e410ab0792c5a5baa7b973b03e1d4c03fa1366b8a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000105472616e73666572205375636365737300000000000000000000000000000000");

        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(2);

        ByteArrayObjectWriter blockUpdateWriter = Context.newByteArrayObjectWriter(RLPn);
        BlockUpdate.writeObject(blockUpdateWriter, bu, headerBytes);
        relayMsgWriter.write(blockUpdateWriter.toByteArray());

        ByteArrayObjectWriter blockUpdateWriter57 = Context.newByteArrayObjectWriter(RLPn);
        BlockUpdate.writeObject(blockUpdateWriter57, bu57, headerBytes57);
        relayMsgWriter.write(blockUpdateWriter57.toByteArray());

        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes); //block header
        blockProofWrtr.write(witness); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.write(rp); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        relayMsgWriter.write(receiptProofWtr.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());


        bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg));

        byte[] btpMsg = Hex.decode("f864b83d6274703a2f2f6273632f3078386364316435643136636166343838656663303537653466633361646437633131623031643962300000000000000000008362736388546f6b656e4253480096d50293d200905472616e7366657220537563636573730000000000000000000000000000000000000000000000000000");
        BTPMessage.fromBytes(btpMsg);
    }


    /**
     * Scenario 2: Invalid BMC caller is not BMC
     * Given:
     *    call `handleRelayMessage` with caller "caller"
     * When:
     *    registered bmc address: ownerWallet.getAddress() generated when setup
     * Then:
     *    throw error:
     *    message: "Invalid message sender from BMC"
     */
    @ParameterizedTest(name = "should_throwError_when_invalidBMCCaller")
    @CsvSource({"55"})
    @Order(2)
    public void should_throwError_when_invalidBMCCaller(int offset) {
        Account bmvCallerBMC = owners[1];
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(1);
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(bmvCallerBMC, "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid message sender from BMC"));
    }


    /**
     * Scenario 3: Previous BMC is not belong to network that BMV handle
     * Given:
     *    prev: btp://0x3.bsc/8cd1d5d16caf488efc057e4fc3add7c11b01d9b0
     * When:
     *    network that BMV handle: 0x1.bsc
     * Then:
     *    throw error:
     *    message: "Invalid previous BMC"
     */
    @ParameterizedTest(name = "should_throwError_when_invalidPreviousBMC")
    @CsvSource({"55"})
    @Order(3)
    public void should_throwError_when_invalidPreviousBMC(int offset) {
        Account bmvCallerBMC = owners[0];

        String invalidPrevBMCBTPAdd = "btp://" + "0x3.bsc" + "/" + prevBMCAdd;
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(1);
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(bmvCallerBMC, "handleRelayMessage", currentBMCBTPAdd, invalidPrevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid previous BMC"));
    }


    /**
     * Scenario 4: BMC is invalid
     * Given:
     *    bmc: btp://0x1.iconee/caller.getAddress()
     * When:
     *    registered bmc address: ownerWallet.getAddress() generated when setup
     * Then:
     *    throw error:
     *    message: "Invalid current BMC"
     */
    @ParameterizedTest(name = "should_throwError_when_invalidCurrentBMC")
    @CsvSource({"55"})
    @Order(4)
    public void should_throwError_when_invalidCurrentBMC(int offset) {
        Account bmvCallerBMC = owners[0];
        String invlidCurrentBMCBTPAdd = "btp://" + currentBMCNet + "/" + owners[1].getAddress().toString();
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(1);
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(bmvCallerBMC, "handleRelayMessage", invlidCurrentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid Current BMC"));
    }


    /**
     * Scenario 5: Input relay message with invalid base64 format
     * Given:
     *    msg: invalid base64 format
     * When:
     *
     * Then:
     *    throw error:
     *    message: "Failed to decode relay message"
     */
    @ParameterizedTest(name = "should_throwError_when_invalidBase64RelayMsgFormat")
    @CsvSource({"55"})
    @Order(5)
    public void should_throwError_when_invalidBase64RelayMsgFormat(int offset) {
        byte[] invalidMsg = "BaRlDid73RYBFMgqveC8G+gFBBU=".getBytes();// random invalid base64 string
        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(invalidMsg)));
        assertTrue(thrown.getMessage().contains("Failed to decode relay message"));
    }

    /**
     * Scenario 6: Input relay message with empty BlockUpdate and BlockProof
     * Given:
     *    msg: empty BlockUpdate and BlockProof
     * When:
     *
     * Then:
     *    throw error:
     *    message: "Invalid relay message"
     */
    @ParameterizedTest(name = "should_throwError_when_emptyBlockUpdateAndBlockProof")
    @CsvSource({"55"})
    @Order(6)
    public void should_throwError_when_emptyBlockUpdateAndBlockProof(int offset) {
        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);
        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.writeNull(); //block header
        blockProofWrtr.writeNull(); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.writeNull(); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid relay message"));
    }


    /**
     * Scenario 7: If H = current block update height < to MTA.height + 1 (lower)
     * Given:
     *    msg: MTA.height = 55
     * When:
     *    current block update height = 55
     * Then:
     *    throw error:
     *    message: "Invalid block update due to lower height"
     */
    @ParameterizedTest(name = "should_throwError_when_currentBlockUpdateHeightLessThanMTAHeight")
    @CsvSource({"55"})
    @Order(7)
    public void should_throwError_when_currentBlockUpdateHeightLessThanMTAHeight(int offset) {
        // BMV initiated with lastblockhash of 56 & offset of 55
        // but below provided header is for block 55
        // should throw error expected 58, but got 55 (added two block header in the first test case)
        byte[] headerBytes = Hex.decode("f901f7a0767db9089cd42c3aa744c1b2250eef2dee5d9155727955972083da9c074b259ea01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0f46a11da2ed278c0072172cdd8833a45a74ae8fc8c837e23df80b6c37eb38bdea05e4f89922553c9f006e90edae0223fd1876a80410bdf4efd82ab8f4f6a0e343ba0f110b51d14cea1618f3058f1f4c63da35637a3b884a315cfdfd6be72a4399ab4b90100010000020000000000000002000800000000000000000000000000800000000000010000000000000000000000000200000000000000000000000000002000000000000000000000000000480000000000000080000000000000000000000000200000000000000400000000800000000020000000800000000000100000000000000000100000000000000000000008000000000000000000000008000000000200000000080000040040000000000000000000000000401000000000000000002000020000000000000000000000000000000000000800000040000000000002100000000000010000000000000000000000000000000000000000000000008037836691b78307bcdc8460c09b7a80a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
        BlockHeader header = BlockHeader.fromBytes(headerBytes);
        BlockUpdate bu = new BlockUpdate(header, null, new byte[0][0], null);
        byte[] witness = Hex.decode("f9016b368504a817c8008347e7c4949c0604273c25c268bad67935553d82437387a39780b90104d5823df00000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000006400000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000000000000000000000003455448000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000346274703a2f2f6273632f30786133366133326331313465653133303930653335636230383634353961363930663563316638653800000000000000000000000025a08f946648cdbabab82ed320ad466beb0373888cc152f3faacb6596bb9babe8bb1a07b3c367ec935226a0cd37f4f7ab87a22f65713b63446fd0e289b684171c9c44d");

        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter blockUpdateWriter = Context.newByteArrayObjectWriter(RLPn);
        BlockUpdate.writeObject(blockUpdateWriter, bu, headerBytes);
        relayMsgWriter.write(blockUpdateWriter.toByteArray());
        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes); //block header
        blockProofWrtr.write(witness); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.writeNull(); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid block update due to lower height")); //expected 58, but got 55
    }


    /**
     * Scenario 8: If H = current block update height > to MTA.height + 1 (higher)
     * Given:
     *    msg: MTA.height = 55
     * When:
     *    current block update height = 71
     * Then:
     *    throw error:
     *    message: "Invalid block update due to higher height"
     */
    @ParameterizedTest(name = "should_throwError_when_currentBlockUpdateHeightHigherThanMTAHeight")
    @CsvSource({"55"})
    @Order(8)
    public void should_throwError_when_currentBlockUpdateHeightHigherThanMTAHeight(int offset) {
        // BMV initiated with lastblockhash of 56 & offset of 55
        //but below provided header is for block 71
        //should throw error expected 58, but got 71 (added two block header in the first test case)
        byte[] headerBytes = Hex.decode("f901f7a05052dae90a8375b14ce88ee15672b4a85d7683cbbbd1b317ef7fe19bc17ec671a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0cfa269775348a78aa3c6a73d5979bef6690f524523f422d11f448b33d56bd245a0cfca23a177b21a5a1c2bb6d8cfbd3d94ac7654dab1cd6ec25a460f8a24a6ff98a0e444f8aba145829a3a2e1c8a1ff4157052650831baed9e3e80b4610eb55eef9db90100000000020000000000000000000001008000000000000000000000800000001000010001000000000000000000000000000000000000000080000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000020000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000800000000000000000040000002000000000000000000000000000000000000000002000000000000000000000200000000000000000000800000000000000000002000000000000000000000000000000000000000000000000000000000000008047836691b783040bb68460cb560c80a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
        BlockHeader header = BlockHeader.fromBytes(headerBytes);
        BlockUpdate bu = new BlockUpdate(header, null, new byte[0][0], null);
        byte[] witness = Hex.decode("f9014a468504a817c800836691b79463d7fcb1f79b5f854705a94e3e065be8204c3fe680b8e43842888c000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000346274703a2f2f6273632f3078613336613332633131346565313330393065333563623038363435396136393066356331663865380000000000000000000000000000000000000000000000000000000000000000000000000000000000000008546f6b656e42534800000000000000000000000000000000000000000000000026a0bf92b3a7388b79448816821ce41af9ecdbee67ab8356925acdc19dea681ac4bca0478b95423bab25a8cdc4817cacf47958ac74d30e94087f1864d495e8b62daf85");

        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter blockUpdateWriter = Context.newByteArrayObjectWriter(RLPn);
        BlockUpdate.writeObject(blockUpdateWriter, bu, headerBytes);
        relayMsgWriter.write(blockUpdateWriter.toByteArray());
        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes); //block header
        blockProofWrtr.write(witness); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.writeNull(); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid block update due to higher height")); //expected 58, but got 71
    }

    /**
     *
     * ################ Checking Block Proof, witness section when BlockUpdates when valid/invalid ################
     *
     */


    /**
     * Scenario 9: If block witness does not exist
     *             (in case of blockUpdate null & block proof present : block update validation takes precedence)
     * Given:
     *
     * When:
     *    blockUpdate null & block proof with empty block witness
     * Then:
     *    throw error:
     *    message: "Invalid block proof with non-exist block witness"
     */
    @ParameterizedTest(name = "should_throwError_when_blockWitnessDoesntExist")
    @CsvSource({"55"})
    @Order(9)
    public void should_throwError_when_blockWitnessDoesntExist(int offset) {
        byte[] headerBytes = Hex.decode("f901f7a05052dae90a8375b14ce88ee15672b4a85d7683cbbbd1b317ef7fe19bc17ec671a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0cfa269775348a78aa3c6a73d5979bef6690f524523f422d11f448b33d56bd245a0cfca23a177b21a5a1c2bb6d8cfbd3d94ac7654dab1cd6ec25a460f8a24a6ff98a0e444f8aba145829a3a2e1c8a1ff4157052650831baed9e3e80b4610eb55eef9db90100000000020000000000000000000001008000000000000000000000800000001000010001000000000000000000000000000000000000000080000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000020000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000800000000000000000040000002000000000000000000000000000000000000000002000000000000000000000200000000000000000000800000000000000000002000000000000000000000000000000000000000000000000000000000000008047836691b783040bb68460cb560c80a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");

        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);
        //emtpy block update
        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes); //block header
        blockProofWrtr.writeNull(); // empty block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.writeNull(); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid block proof with non-existing block witness"));
    }


    /**
     * Scenario 10: If block witness is not empty but If block height of block proof height > MTA.height
     * Given:
     *    MTA height = 55
     * When:
     *    nonempty block witness & block proof height = 71
     * Then:
     *    throw error:
     *    message: "Invalid block proof with higher height than MTA"
     */
    @ParameterizedTest(name = "should_throwError_when_blockWitnessNotEmptyButBlockUpdateHeightHigherThanMTAHeight")
    @CsvSource({"55"})
    @Order(10)
    public void should_throwError_when_blockWitnessNotEmptyButBlockUpdateHeightHigherThanMTAHeight(int offset) {
        // BMV initiated with lastblockhash of 56 & offset of 55
        // but below provided header is for block 71
        // Invalid block proof with higher height than MTA
        byte[] headerBytes = Hex.decode("f901f7a05052dae90a8375b14ce88ee15672b4a85d7683cbbbd1b317ef7fe19bc17ec671a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0cfa269775348a78aa3c6a73d5979bef6690f524523f422d11f448b33d56bd245a0cfca23a177b21a5a1c2bb6d8cfbd3d94ac7654dab1cd6ec25a460f8a24a6ff98a0e444f8aba145829a3a2e1c8a1ff4157052650831baed9e3e80b4610eb55eef9db90100000000020000000000000000000001008000000000000000000000800000001000010001000000000000000000000000000000000000000080000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000020000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000800000000000000000040000002000000000000000000000000000000000000000002000000000000000000000200000000000000000000800000000000000000002000000000000000000000000000000000000000000000000000000000000008047836691b783040bb68460cb560c80a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");

        byte[] witness = Hex.decode("f9014a468504a817c800836691b79463d7fcb1f79b5f854705a94e3e065be8204c3fe680b8e43842888c000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000346274703a2f2f6273632f3078613336613332633131346565313330393065333563623038363435396136393066356331663865380000000000000000000000000000000000000000000000000000000000000000000000000000000000000008546f6b656e42534800000000000000000000000000000000000000000000000026a0bf92b3a7388b79448816821ce41af9ecdbee67ab8356925acdc19dea681ac4bca0478b95423bab25a8cdc4817cacf47958ac74d30e94087f1864d495e8b62daf85");

        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);
        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes); //block header
        blockProofWrtr.write(witness); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.writeNull(); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid block proof with higher height than MTA"));
    }


    /**
     * Scenario 11: If height of block witness is invalid
     * Given:
     *    MTA height = 58
     * When:
     *    empty block update & fake witness for block 57
     * Then:
     *    throw error:
     *    message: "Invalid block witness"
     */
    @ParameterizedTest(name = "should_throwError_when_invalidBlockWitness")
    @CsvSource({"58"})
    @Order(11)
    public void should_throwError_when_invalidBlockWitness(int offset) {
        // Height of MTA 57
        // provided header is for block 57 , but with fake witness for 57 and empty blockupdate
        // Invalid Witness
        byte[] headerBytes = Hex.decode("f901f7a0c93b8edba9a9d845138f2ae0fc38d66958251e13fd18d1549d3e7104585fa10ba01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a06bd8ea40af1ed47dc8d1792c33c5b099dd62af2725797a9704413f52483c86e3a0cdad2da57c7a0eff2aa32fae25babcdafe016330f68c09034cd392b1836f0a0da05dec8b2792fc693f00f23e4e4d71676f22ea2126494acfeed621bf9279b1d83fb90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008039836691b7834340568460cb560180a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
        byte[] rp = Hex.decode("f901b5b901b2f901af822080b901a9f901a60182aca9b9010000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000040000000000000000000000000000000000000000020000000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000800000000000000000000000000000000020000000000000000004000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000000004000000000000010000000000001000000000000000000000000000000000000000000000000f89df89b94b27345f8e20bf8cdd839c837b792b5452c282c22f863a08c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925a000000000000000000000000054326b2ad6a7af73e0f8e8a4478e13c26cb82949a00000000000000000000000009c0604273c25c268bad67935553d82437387a397a00000000000000000000000000000000000000000000000000000000000000064");

        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);
        //empty blockproof
        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes); //block header
        ByteArrayObjectWriter blockWitnessWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockWitnessWrtr.beginList(6);
        blockWitnessWrtr.write(57);
        blockWitnessWrtr.write(Hex.decode("04a817c800"));//fake witness
        //blockWitnessWrtr.write(witness);
        blockWitnessWrtr.end();
        blockProofWrtr.write(blockWitnessWrtr.toByteArray()); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.write(rp); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid Block Witness"));
    }


    /**
     * Scenario 12: Trying to added next block 58 with invalid receipt proof from (59)
     * Given:
     *    next block is 58
     * When:
     *    invalid receipt proof with wrong sequence from block 59
     * Then:
     *    throw error:
     *    message: "Invalid receipt proofs with wrong sequence"
     *
     */
    @ParameterizedTest(name = "should_throwError_when_invalidReceiptProofSequence")
    @CsvSource({"57"})
    @Order(12)
    public void should_throwError_when_invalidReceiptProofSequence(int offset) {
        byte[] headerBytes58 = Hex.decode("f90259a0863f61eca8bbc436bcb743ec34dd42730c5c8bed632cc11d003855c465eef05aa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a04766f800e9ff4ed9156de0843f3111413ba221c757ab34f603f1e2f88176ad3aa0cc82a70b989f6f11c3907f29c27a817839cacf5676b75e05618dbd3d9f8d116fa006f0937c90cce6c2a61eaed88a8dad075bb39a8f6ccb8b480f7beca0f9cf6054b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003a836691b7834bc8528460cb5602b861d883010006846765746888676f312e31362e36856c696e757800000011c0aa9e9868fed7a999be887a6ce9ec962a7cdecfd9169464b9914dfd06a103fc608f6b4132335cd76ba7e3028b1c3c6b89f04fc7ae94c69b8b266b90f24b82db9dc90d00a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
        BlockHeader header58 = BlockHeader.fromBytes(headerBytes58);
        BlockUpdate bu58 = new BlockUpdate(header58, null, new byte[0][0], null);
        byte[] witness = Hex.decode("f95b08398504a817c800836691b78080b95ab560806040523480156200001157600080fd5b5060405162005a3538038062005a35833981016040819052620000349162000148565b336000908152602081905260408120805460ff191660019081179091558054916200005f8362000290565b9091555050600280546001600160a01b0319166001600160a01b03841617905580516200009490600c906020840190620000a2565b50506000600a555062000300565b828054620000b0906200023a565b90600052602060002090601f016020900481019282620000d457600085556200011f565b82601f10620000ef57805160ff19168380011785556200011f565b828001600101855582156200011f579182015b828111156200011f57825182559160200191906001019062000102565b506200012d92915062000131565b5090565b5b808211156200012d576000815560010162000132565b600080604083850312156200015b578182fd5b82516001600160a01b038116811462000172578283fd5b602084810151919350906001600160401b038082111562000191578384fd5b818601915086601f830112620001a5578384fd5b815181811115620001ba57620001ba620002d1565b604051601f8201601f1916810185018381118282101715620001e057620001e0620002d1565b6040528181528382018501891015620001f7578586fd5b8592505b818310156200021a5783830185015181840186015291840191620001fb565b818311156200022b57858583830101525b80955050505050509250929050565b6002810460018216806200024f57607f821691505b602082108114156200028a577f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b50919050565b6000600019821415620002ca577f4e487b710000000000000000000000000000000000000000000000000000000081526011600452602481fd5b5060010190565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b61572580620003106000396000f3fe608060405234801561001057600080fd5b506004361061010b5760003560e01c80635df45a37116100a2578063b70eeb8d11610071578063b70eeb8d1461023e578063c5975f1d14610251578063c7a6d7fe14610264578063d5823df014610277578063f63327ee1461028a5761010b565b80635df45a37146101dd5780637065cb48146101f257806381d12c58146102055780639fdc7bc4146102295761010b565b8063243b14cf116100de578063243b14cf146101775780632f54bf6e1461019757806330b39a62146101b75780633842888c146101ca5761010b565b80630a823dea14610110578063173652b014610125578063173825d91461014f578063188e785214610162575b600080fd5b61012361011e3660046149bb565b61029d565b005b610138610133366004614805565b610418565b604051610146929190615326565b60405180910390f35b61012361015d3660046147a0565b610431565b61016a6104bc565b6040516101469190614efe565b61018a610185366004614b22565b610693565b604051610146919061531d565b6101aa6101a53660046147a0565b6106b0565b6040516101469190614f71565b6101236101c5366004614a63565b6106d2565b6101236101d8366004614b5c565b61091a565b6101e5610dcc565b6040516101469190614f5e565b6101236102003660046147a0565b610ffc565b610218610213366004614bb2565b611062565b604051610146959493929190614f8f565b610231611228565b6040516101469190614f7c565b61012361024c36600461488d565b6112b6565b61013861025f3660046147ba565b611973565b61012361027236600461484e565b6119fc565b610123610285366004614aac565b611a40565b61012361029836600461492c565b611c20565b61036a600c80546102ad9061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546102d99061559f565b80156103265780601f106102fb57610100808354040283529160200191610326565b820191906000526020600020905b81548152906001019060200180831161030957829003601f168201915b505050505087878080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509293925050611e7d9050565b15156001146103945760405162461bcd60e51b815260040161038b906152f4565b60405180910390fd5b600084815260066020526040902080546103ad9061559f565b151590506103cd5760405162461bcd60e51b815260040161038b90615271565b61040e848484848080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250611ee592505050565b5050505050505050565b6000806104258484612178565b915091505b9250929050565b3360009081526020819052604090205460ff1615156001146104655760405162461bcd60e51b815260040161038b906151a2565b60018054116104865760405162461bcd60e51b815260040161038b906150e8565b6001600160a01b0381166000908152602081905260408120805460ff1916905560018054916104b483615588565b919050555050565b6060600b546001600160401b038111156104e657634e487b7160e01b600052604160045260246000fd5b60405190808252806020026020018201604052801561051957816020015b60608152602001906001900390816105045790505b5090506000805b60095481101561068d5760006001600160a01b031660036009838154811061055857634e487b7160e01b600052603260045260246000fd5b9060005260206000200160405161056f9190614d7e565b908152604051908190036020019020546001600160a01b03161461067b57600981815481106105ae57634e487b7160e01b600052603260045260246000fd5b9060005260206000200180546105c39061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546105ef9061559f565b801561063c5780601f106106115761010080835404028352916020019161063c565b820191906000526020600020905b81548152906001019060200180831161061f57829003601f168201915b505050505083838151811061066157634e487b7160e01b600052603260045260246000fd5b60200260200101819052508180610677906155d4565b9250505b80610685816155d4565b915050610520565b50505b90565b805160208183018101805160078252928201919093012091525481565b6001600160a01b03811660009081526020819052604090205460ff165b919050565b60006001600160a01b0316600384846040516106ef929190614d52565b908152604051908190036020019020546001600160a01b031614156107265760405162461bcd60e51b815260040161038b9061520d565b336000908152600560205260409081902090518291906107499086908690614d52565b90815260200160405180910390206001015410156107795760405162461bcd60e51b815260040161038b90615174565b336000908152600560205260409081902090516107b891839161079f9087908790614d52565b908152604051908190036020019020600101549061230b565b336000908152600560205260409081902090516107d89086908690614d52565b908152602001604051809103902060010181905550600060038484604051610801929190614d52565b9081526040519081900360200181205463095ea7b360e01b82526001600160a01b03169150819063095ea7b39061083e9030908690600401614ee5565b602060405180830381600087803b15801561085857600080fd5b505af115801561086c573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190610890919061482e565b506040516323b872dd60e01b81526001600160a01b038216906323b872dd906108c190309033908790600401614ec1565b602060405180830381600087803b1580156108db57600080fd5b505af11580156108ef573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190610913919061482e565b5050505050565b6060806109268461231e565b909250905060005b600954811015610b565760076009828154811061095b57634e487b7160e01b600052603260045260246000fd5b906000526020600020016040516109729190614d7e565b908152602001604051809103902054600014610af75760086000600a5481526020019081526020016000206040518060600160405280600984815481106109c957634e487b7160e01b600052603260045260246000fd5b9060005260206000200180546109de9061559f565b80601f0160208091040260200160405190810160405280929190818152602001828054610a0a9061559f565b8015610a575780601f10610a2c57610100808354040283529160200191610a57565b820191906000526020600020905b815481529060010190602001808311610a3a57829003601f168201915b50505050508152602001600760098581548110610a8457634e487b7160e01b600052603260045260246000fd5b90600052602060002001604051610a9b9190614d7e565b90815260405160209181900382019020548252600091810182905283546001810185559382529081902082518051939460030290910192610adf92849201906144a8565b50602082015181600101556040820151816002015550505b600760098281548110610b1a57634e487b7160e01b600052603260045260246000fd5b90600052602060002001604051610b319190614d7e565b9081526020016040518091039020600090558080610b4e906155d4565b91505061092e565b50600a54600090815260086020526040902054610b74575050610dc8565b6000610cf4604051806040016040528060006003811115610ba557634e487b7160e01b600052602160045260246000fd5b8152602001610ced6040518060600160405280610bca306001600160a01b03166123a4565b815260200187815260200160086000600a548152602001908152602001600020805480602002602001604051908101604052809291908181526020016000905b82821015610ce25783829060005260206000209060030201604051806060016040529081600082018054610c3d9061559f565b80601f0160208091040260200160405190810160405280929190818152602001828054610c699061559f565b8015610cb65780601f10610c8b57610100808354040283529160200191610cb6565b820191906000526020600020905b815481529060010190602001808311610c9957829003601f168201915b505050505081526020016001820154815260200160028201548152505081526020019060010190610c0a565b5050509152506125ff565b9052612782565b600254600a54604051635fb60ecd60e11b81529293506001600160a01b039091169163bf6c1d9a91610d2f918791600c918790600401614fdd565b600060405180830381600087803b158015610d4957600080fd5b505af1158015610d5d573d6000803e3d6000fd5b5050600a546000818152600860205260409081902090513094507f50d22373bb84ed1f9eeb581c913e6d45d918c05f8b1d90f0be168f06a4e6994a9350610da7928a929091615052565b60405180910390a2600a8054906000610dbf836155d4565b91905055505050505b5050565b6009546060906001600160401b03811115610df757634e487b7160e01b600052604160045260246000fd5b604051908082528060200260200182016040528015610e3057816020015b610e1d614528565b815260200190600190039081610e155790505b50905060005b600954811015610ff85760006001600160a01b0316600360098381548110610e6e57634e487b7160e01b600052603260045260246000fd5b90600052602060002001604051610e859190614d7e565b908152604051908190036020019020546001600160a01b031614610fe657604051806060016040528060098381548110610ecf57634e487b7160e01b600052603260045260246000fd5b906000526020600020018054610ee49061559f565b80601f0160208091040260200160405190810160405280929190818152602001828054610f109061559f565b8015610f5d5780601f10610f3257610100808354040283529160200191610f5d565b820191906000526020600020905b815481529060010190602001808311610f4057829003601f168201915b50505050508152602001600760098481548110610f8a57634e487b7160e01b600052603260045260246000fd5b90600052602060002001604051610fa19190614d7e565b90815260200160405180910390205481526020016000815250828281518110610fda57634e487b7160e01b600052603260045260246000fd5b60200260200101819052505b80610ff0816155d4565b915050610e36565b5090565b3360009081526020819052604090205460ff1615156001146110305760405162461bcd60e51b815260040161038b906151a2565b6001600160a01b0381166000908152602081905260408120805460ff191660019081179091558054916104b4836155d4565b60066020526000908152604090208054819061107d9061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546110a99061559f565b80156110f65780601f106110cb576101008083540402835291602001916110f6565b820191906000526020600020905b8154815290600101906020018083116110d957829003601f168201915b50505050509080600101805461110b9061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546111379061559f565b80156111845780601f1061115957610100808354040283529160200191611184565b820191906000526020600020905b81548152906001019060200180831161116757829003601f168201915b5050505050908060020180546111999061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546111c59061559f565b80156112125780601f106111e757610100808354040283529160200191611212565b820191906000526020600020905b8154815290600101906020018083116111f557829003601f168201915b5050505050908060030154908060040154905085565b600c80546112359061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546112619061559f565b80156112ae5780601f10611283576101008083540402835291602001916112ae565b820191906000526020600020905b81548152906001019060200180831161129157829003601f168201915b505050505081565b60006112f783838080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152506127f892505050565b905060008151600381111561131c57634e487b7160e01b600052602160045260246000fd5b141561134657600061133182602001516128ce565b905061133f818a8a88612b9b565b505061196a565b60028151600381111561136957634e487b7160e01b600052602160045260246000fd5b14156118d3576000848152600860205260409020541515806113a457506000848152600660205260409020805461139f9061559f565b151590505b6113c05760405162461bcd60e51b815260040161038b90615271565b600084815260086020526040812054156113d8575060015b60006113e78360200151612fc6565b9050816116c4576000868152600660205260408120805461148f919061140c9061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546114389061559f565b80156114855780601f1061145a57610100808354040283529160200191611485565b820191906000526020600020905b81548152906001019060200180831161146857829003601f168201915b505050505061300d565b8251909150600114156114b4576114af8783600001518460200151611ee5565b61163a565b600087815260066020526040812060020180546114d09061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546114fc9061559f565b80156115495780601f1061151e57610100808354040283529160200191611549565b820191906000526020600020905b81548152906001019060200180831161152c57829003601f168201915b50505060008b815260066020908152604080832060038101546004909101546001600160a01b038a168552600590935292819020905195965091949093506115b0925084919061159a908790614d62565b908152604051908190036020019020549061230b565b6001600160a01b0385166000908152600560205260409081902090516115d7908690614d62565b908152602001604051809103902060000181905550611616816007856040516116009190614d62565b9081526040519081900360200190205490613216565b6007846040516116269190614d62565b908152604051908190036020019020555050505b6000878152600660205260408120906116538282614549565b611661600183016000614549565b61166f600283016000614549565b506000600382018190556004909101558151602083015160405130927f9b4c002cf17443998e01f132ae99b7392665eec5422a33a1d2dc47308c59b6e2926116ba928c929190615334565b60405180910390a2505b600086815260086020908152604080832080548251818502810185019093528083529192909190849084015b828210156117c857838290600052602060002090600302016040518060600160405290816000820180546117239061559f565b80601f016020809104026020016040519081016040528092919081815260200182805461174f9061559f565b801561179c5780601f106117715761010080835404028352916020019161179c565b820191906000526020600020905b81548152906001019060200180831161177f57829003601f168201915b5050505050815260200160018201548152602001600282015481525050815260200190600101906116f0565b505084519293505050600114156118705760005b815181101561186e5781818151811061180557634e487b7160e01b600052603260045260246000fd5b602002602001015160200151600783838151811061183357634e487b7160e01b600052603260045260246000fd5b60200260200101516000015160405161184c9190614d62565b9081526040519081900360200190205580611866816155d4565b9150506117dc565b505b600087815260086020526040812061188791614588565b8151602083015160405130927f9b4c002cf17443998e01f132ae99b7392665eec5422a33a1d2dc47308c59b6e2926118c2928c929190615334565b60405180910390a25050505061196a565b6003815160038111156118f657634e487b7160e01b600052602160045260246000fd5b1415611902575061196a565b61040e600389898080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152505060408051808201909152600c81526b554e4b4e4f574e5f5459504560a01b602082015289925090506001613222565b50505050505050565b6001600160a01b038216600090815260056020526040808220905182919061199c908590614d62565b90815260200160405180910390206001015460056000866001600160a01b03166001600160a01b03168152602001908152602001600020846040516119e19190614d62565b90815260405190819003602001902054909590945092505050565b611a3b82828080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525061300d92505050565b505050565b600060038686604051611a54929190614d52565b908152604051908190036020019020546001600160a01b0316905080611a8c5760405162461bcd60e51b815260040161038b9061523a565b60008411611aac5760405162461bcd60e51b815260040161038b906152bd565b6040516323b872dd60e01b81526001600160a01b038216906323b872dd90611adc90339030908990600401614ec1565b602060405180830381600087803b158015611af657600080fd5b505af1158015611b0a573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190611b2e919061482e565b506000611b3b8286612178565b33600090815260056020526040908190209051929750909250611b7c91611b65908a908a90614d52565b908152604051908190036020019020548690613216565b33600090815260056020526040908190209051611b9c908a908a90614d52565b90815260408051918290036020908101832093909355601f8601839004830282018301905284815261196a91869086908190840183828082843760009201919091525050604080516020601f8d018190048102820181019092528b815292508b91508a90819084018382808284376000920191909152508a925086915061332d9050565b3360009081526020819052604090205460ff161515600114611c545760405162461bcd60e51b815260040161038b906151a2565b60006001600160a01b031660038888604051611c71929190614d52565b908152604051908190036020019020546001600160a01b031614611ca75760405162461bcd60e51b815260040161038b906151c9565b8060038888604051611cba929190614d52565b908152602001604051809103902060006101000a8154816001600160a01b0302191690836001600160a01b03160217905550604051806080016040528088888080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250505090825250604080516020601f8901819004810282018101909252878152918101919088908890819084018382808284376000920182905250938552505050602080830187905260409283018690526001600160a01b038516825260048152919020825180519192611da2928492909101906144a8565b506020828101518051611dbb92600185019201906144a8565b506040820151600282015560609091015160039091015560098054600181018255600091909152611e0f907f6e1540171b6c0c960b71a7020d9f60077f6af931a8bbf590da0223dacf75c7af0188886145a9565b50600b8054906000611e20836155d4565b91905055508686604051611e35929190614d52565b60405180910390207f3e49fb7efebefffe4fc2e2c193820fac9b11de4bf185e0d14de13e0068c2ac3482604051611e6c9190614ead565b60405180910390a250505050505050565b600081604051602001611e909190614d62565b6040516020818303038152906040528051906020012083604051602001611eb79190614d62565b604051602081830303815290604052805190602001201415611edb57506001611edf565b5060005b92915050565b60008381526006602052604081208054859291611f059161140c9061559f565b600086815260066020526040812060020180549293509091611f269061559f565b80601f0160208091040260200160405190810160405280929190818152602001828054611f529061559f565b8015611f9f5780601f10611f7457610100808354040283529160200191611f9f565b820191906000526020600020905b815481529060010190602001808311611f8257829003601f168201915b50505060008981526006602052604080822060038082015460049092015492519697509095919450919250611fd5908690614d62565b908152604080519182900360209081018320546001600160a01b0389811660009081526005909352929091209116925061203c9184916120369187919061201d908a90614d62565b9081526040519081900360200190206001015490613216565b90613216565b6001600160a01b038616600090815260056020526040908190209051612063908790614d62565b9081526020016040518091039020600101819055506120af8360056000886001600160a01b03166001600160a01b031681526020019081526020016000208660405161159a9190614d62565b6001600160a01b0386166000908152600560205260409081902090516120d6908790614d62565b90815260408051602092819003830190209290925560008b81526006909152908120906121038282614549565b612111600183016000614549565b61211f600283016000614549565b5060006003820181905560049091015560405130907f9b4c002cf17443998e01f132ae99b7392665eec5422a33a1d2dc47308c59b6e290612165908c908c908c90615334565b60405180910390a2505050505050505050565b6001600160a01b0382166000908152600460205260408082208151608081019092528054839283929091829082906121af9061559f565b80601f01602080910402602001604051908101604052809291908181526020018280546121db9061559f565b80156122285780601f106121fd57610100808354040283529160200191612228565b820191906000526020600020905b81548152906001019060200180831161220b57829003601f168201915b505050505081526020016001820180546122419061559f565b80601f016020809104026020016040519081016040528092919081815260200182805461226d9061559f565b80156122ba5780601f1061228f576101008083540402835291602001916122ba565b820191906000526020600020905b81548152906001019060200180831161229d57829003601f168201915b5050505050815260200160028201548152602001600382015481525050905060648160600151856122eb91906154db565b6122f5919061538d565b9150612301848361230b565b9250509250929050565b60006123178284615522565b9392505050565b606080600061234684604051806040016040528060018152602001602f60f81b8152506135a5565b90508060028151811061236957634e487b7160e01b600052603260045260246000fd5b60200260200101518160038151811061239257634e487b7160e01b600052603260045260246000fd5b60200260200101519250925050915091565b604080518082018252601081526f181899199a1a9b1b9c1cb0b131b232b360811b60208201528151603380825260608281019094526001600160a01b0385169291600091602082018180368337019050509050600360fc1b8160008151811061241d57634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a905350600f60fb1b8160018151811061245a57634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a90535060005b60148110156125f6578260048561248c84600c615375565b602081106124aa57634e487b7160e01b600052603260045260246000fd5b1a60f81b6001600160f81b031916901c60f81c60ff16815181106124de57634e487b7160e01b600052603260045260246000fd5b01602001516001600160f81b031916826124f98360026154db565b612504906002615375565b8151811061252257634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a905350828461254683600c615375565b6020811061256457634e487b7160e01b600052603260045260246000fd5b825191901a600f1690811061258957634e487b7160e01b600052603260045260246000fd5b01602001516001600160f81b031916826125a48360026154db565b6125af906003615375565b815181106125cd57634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a905350806125ee816155d4565b915050612474565b50949350505050565b60606000606080606060005b86604001515181101561270c576126c86126538860400151838151811061264257634e487b7160e01b600052603260045260246000fd5b6020026020010151600001516137de565b61268b8960400151848151811061267a57634e487b7160e01b600052603260045260246000fd5b6020026020010151602001516137e9565b6126c38a6040015185815181106126b257634e487b7160e01b600052603260045260246000fd5b6020026020010151604001516137e9565b6137fc565b91506126d38261382b565b94506126e085600061382f565b93506126ec84836138f8565b91506126f883836138f8565b925080612704816155d4565b91505061260b565b506127168261382b565b935061272384600061382f565b925061272f83836138f8565b915061275461274187600001516137de565b61274e88602001516137de565b846137fc565b915061275f8261382b565b935061276c84600061382f565b925061277883836138f8565b9695505050505050565b606060006127c76127b5846000015160038111156127b057634e487b7160e01b600052602160045260246000fd5b6137e9565b6127c2856020015161390d565b6138f8565b905060006127d48261382b565b905060006127e382600061382f565b90506127ef81846138f8565b95945050505050565b61280061461d565b600061281361280e8461396b565b613990565b905060405180604001604052806128518360008151811061284457634e487b7160e01b600052603260045260246000fd5b6020026020010151613abd565b600381111561287057634e487b7160e01b600052602160045260246000fd5b600381111561288f57634e487b7160e01b600052602160045260246000fd5b81526020016128c5836001815181106128b857634e487b7160e01b600052603260045260246000fd5b6020026020010151613b49565b90529392505050565b6128d6614635565b60006128e461280e8461396b565b905060006129198260028151811061290c57634e487b7160e01b600052603260045260246000fd5b6020026020010151613990565b516001600160401b0381111561293f57634e487b7160e01b600052604160045260246000fd5b60405190808252806020026020018201604052801561297857816020015b612965614528565b81526020019060019003908161295d5790505b50905060006129a18360028151811061290c57634e487b7160e01b600052603260045260246000fd5b5190506129ac614528565b60006129d28560028151811061290c57634e487b7160e01b600052603260045260246000fd5b905060005b6129fb8660028151811061290c57634e487b7160e01b600052603260045260246000fd5b51811015612b2e576040518060600160405280612a54612a3485858151811061290c57634e487b7160e01b600052603260045260246000fd5b6000815181106128b857634e487b7160e01b600052603260045260246000fd5b8152602001612a9f612a7f85858151811061290c57634e487b7160e01b600052603260045260246000fd5b60018151811061284457634e487b7160e01b600052603260045260246000fd5b8152602001612aea612aca85858151811061290c57634e487b7160e01b600052603260045260246000fd5b60028151811061284457634e487b7160e01b600052603260045260246000fd5b815250925082858281518110612b1057634e487b7160e01b600052603260045260246000fd5b60200260200101819052508080612b26906155d4565b9150506129d7565b506040518060600160405280612b5e876000815181106128b857634e487b7160e01b600052603260045260246000fd5b8152602001612b87876001815181106128b857634e487b7160e01b600052603260045260246000fd5b815260200194909452509195945050505050565b60208401516040516363d36bff60e11b8152309063c7a6d7fe90612bc3908490600401614f7c565b600060405180830381600087803b158015612bdd57600080fd5b505af1925050508015612bee575060015b612cf557612bfa61564b565b80612c055750612c53565b612c4c600286868080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525088925086915060019050613222565b5050612fc0565b3d808015612c7d576040519150601f19603f3d011682016040523d82523d6000602084013e612c82565b606091505b50612c4c600286868080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525050604080518082019091526016815275125b9d985b1a59081859191c995cdcc8199bdc9b585d60521b602082015288925090506001613222565b604085015160005b8151811015612f52576000828281518110612d2857634e487b7160e01b600052603260045260246000fd5b60200260200101516020015190506000838381518110612d5857634e487b7160e01b600052603260045260246000fd5b60200260200101516000015190506000600382604051612d789190614d62565b908152604051908190036020019020546001600160a01b0316905080612e0e57612e0660028a8a8080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250506040805180820190915260128152712ab73932b3b4b9ba32b932b2102a37b5b2b760711b60208201528c925090506001613222565b505050612f40565b6000600383604051612e209190614d62565b9081526040519081900360200181205463095ea7b360e01b82526001600160a01b03169150819063095ea7b390612e5d9030908890600401614ee5565b602060405180830381600087803b158015612e7757600080fd5b505af1158015612e8b573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190612eaf919061482e565b50806001600160a01b03166323b872dd30612ec98a61300d565b876040518463ffffffff1660e01b8152600401612ee893929190614ec1565b602060405180830381600087803b158015612f0257600080fd5b505af1158015612f16573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190612f3a919061482e565b50505050505b80612f4a816155d4565b915050612cfd565b50612fbd600286868080601f016020809104026020016040519081016040528093929190818152602001838380828437600092018290525060408051808201909152601081526f5472616e73666572205375636365737360801b60208201528a945092509050613222565b50505b50505050565b612fce614656565b6000612fdc61280e8461396b565b9050604051806040016040528061288f8360008151811061284457634e487b7160e01b600052603260045260246000fd5b60008181808060025b602a81101561320b5761302b610100856154b5565b935084818151811061304d57634e487b7160e01b600052603260045260246000fd5b016020015160f81c925084613063826001615375565b8151811061308157634e487b7160e01b600052603260045260246000fd5b016020015160f81c915060616001600160a01b038416108015906130af57506066836001600160a01b031611155b156130c6576130bf6057846154fa565b9250613131565b6041836001600160a01b0316101580156130ea57506046836001600160a01b031611155b156130fa576130bf6037846154fa565b6030836001600160a01b03161015801561311e57506039836001600160a01b031611155b156131315761312e6030846154fa565b92505b6061826001600160a01b03161015801561315557506066826001600160a01b031611155b1561316c576131656057836154fa565b91506131d7565b6041826001600160a01b03161015801561319057506046826001600160a01b031611155b156131a0576131656037836154fa565b6030826001600160a01b0316101580156131c457506039826001600160a01b031611155b156131d7576131d46030836154fa565b91505b816131e38460106154b5565b6131ed9190615353565b6131f79085615353565b9350613204600282615375565b9050613016565b509195945050505050565b60006123178284615375565b600260009054906101000a90046001600160a01b03166001600160a01b031663bf6c1d9a85600c8661329a60405180604001604052808c600381111561327857634e487b7160e01b600052602160045260246000fd5b8152602001610ced60405180604001604052808b81526020018c815250613c06565b6040518563ffffffff1660e01b81526004016132b99493929190614fdd565b600060405180830381600087803b1580156132d357600080fd5b505af11580156132e7573d6000803e3d6000fd5b505050507f356868e4a05430bccb6aa9c954e410ab0792c5a5baa7b973b03e1d4c03fa136683828460405161331e93929190615334565b60405180910390a15050505050565b6060806133398661231e565b60408051600180825281830190925292945090925060009190816020015b61335f614528565b815260200190600190039081613357579050509050604051806060016040528087815260200186815260200185815250816000815181106133b057634e487b7160e01b600052603260045260246000fd5b602002602001018190525060006134256040518060400160405280600060038111156133ec57634e487b7160e01b600052602160045260246000fd5b8152602001610ced6040518060600160405280613411336001600160a01b03166123a4565b8152602001888152602001878152506125ff565b600254600a54604051635fb60ecd60e11b81529293506001600160a01b039091169163bf6c1d9a91613460918891600c918790600401614fdd565b600060405180830381600087803b15801561347a57600080fd5b505af115801561348e573d6000803e3d6000fd5b505050506040518060a001604052806134af336001600160a01b03166123a4565b81526020018481526020018881526020018781526020018681525060066000600a54815260200190815260200160002060008201518160000190805190602001906134fb9291906144a8565b50602082810151805161351492600185019201906144a8565b50604082015180516135309160028401916020909101906144a8565b5060608201516003820155608090910151600490910155600a5460405133917f50d22373bb84ed1f9eeb581c913e6d45d918c05f8b1d90f0be168f06a4e6994a9161357e918c918790615027565b60405180910390a2600a8054906000613596836155d4565b91905055505050505050505050565b606082600060015b600183516135bb9190615522565b8210156136025760006135cf878785613c27565b90508060001914156135e15750613602565b816135eb816155d4565b92506135fa9050816001615375565b9250506135ad565b806001600160401b0381111561362857634e487b7160e01b600052604160045260246000fd5b60405190808252806020026020018201604052801561365b57816020015b60608152602001906001900390816136465790505b50935060009150600090505b600183516136759190615522565b8210156137d5576000613689878785613c27565b9050806000191415613699575082515b60006136a58483615522565b6001600160401b038111156136ca57634e487b7160e01b600052604160045260246000fd5b6040519080825280601f01601f1916602001820160405280156136f4576020820181803683370190505b509050806000855b848110156137875787818151811061372457634e487b7160e01b600052603260045260246000fd5b01602001516001600160f81b031916838361373e816155d4565b94508151811061375e57634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a9053508061377f816155d4565b9150506136fc565b50613793846001615375565b95508188866137a1816155d4565b9750815181106137c157634e487b7160e01b600052603260045260246000fd5b602002602001018190525050505050613667565b50505092915050565b6060611edf8261390d565b6060611edf6137f783613ced565b61390d565b606083838360405160200161381393929190614d0f565b60405160208183030381529060405290509392505050565b5190565b606060378311801561383f575081155b1561388157600061384f84613e83565b9050600061385c8261382b565b9050600061386b82600161382f565b905061387781846138f8565b9350505050611edf565b60378311158015613890575081155b156138c6576138a08360c0615375565b6040516020016138b09190614e95565b6040516020818303038152906040529050611edf565b6138d18360f7615375565b6040516020016138e19190614e95565b604051602081830303815290604052905092915050565b606082826040516020016138e1929190614ce0565b6060808251600114801561394a575060808360008151811061393f57634e487b7160e01b600052603260045260246000fd5b016020015160f81c11155b15613956575081611edf565b61231761396584516080613fd3565b846141c1565b613973614670565b506040805180820190915281518152602082810190820152919050565b606061399b8261423e565b6139b75760405162461bcd60e51b815260040161038b90615295565b60006139c283614278565b90506000816001600160401b038111156139ec57634e487b7160e01b600052604160045260246000fd5b604051908082528060200260200182016040528015613a2557816020015b613a12614670565b815260200190600190039081613a0a5790505b5090506000613a3785602001516142fb565b8560200151613a469190615375565b90506000805b8481101561320b57613a5d83614386565b9150604051806040016040528083815260200184815250848281518110613a9457634e487b7160e01b600052603260045260246000fd5b6020908102919091010152613aa98284615375565b925080613ab5816155d4565b915050613a4c565b805160009015801590613ad257508151602110155b613aee5760405162461bcd60e51b815260040161038b90615147565b6000613afd83602001516142fb565b90506000818460000151613b119190615522565b9050600080838660200151613b269190615375565b90508051915060208310156125f657506020919091036101000a90049392505050565b8051606090613b6a5760405162461bcd60e51b815260040161038b9061511f565b6000613b7983602001516142fb565b90506000818460000151613b8d9190615522565b90506000816001600160401b03811115613bb757634e487b7160e01b600052604160045260246000fd5b6040519080825280601f01601f191660200182016040528015613be1576020820181803683370190505b50905060008160200190506125f6848760200151613bff9190615375565b828561442f565b606060006127c7613c1a84600001516137e9565b6127c285602001516137de565b815160009084908490600114613c4d57634e487b7160e01b600052600160045260246000fd5b835b8251811015613cdf5781600081518110613c7957634e487b7160e01b600052603260045260246000fd5b602001015160f81c60f81b6001600160f81b031916838281518110613cae57634e487b7160e01b600052603260045260246000fd5b01602001516001600160f81b0319161415613ccd579250612317915050565b80613cd7816155d4565b915050613c4f565b506000199695505050505050565b606081613d1b5781604051602001613d059190614e95565b60405160208183030381529060405290506106cd565b60408051602080825281830190925260009160208201818036833701905050905082602082015260005b6020811015613d9957818181518110613d6e57634e487b7160e01b600052603260045260246000fd5b01602001516001600160f81b03191615613d8757613d99565b80613d91816155d4565b915050613d45565b6000613da6826020615522565b6001600160401b03811115613dcb57634e487b7160e01b600052604160045260246000fd5b6040519080825280601f01601f191660200182016040528015613df5576020820181803683370190505b50905060005b81518110156125f6578383613e0f816155d4565b945081518110613e2f57634e487b7160e01b600052603260045260246000fd5b602001015160f81c60f81b828281518110613e5a57634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a90535080613e7b816155d4565b915050613dfb565b606060ff8211613e9e5781604051602001613d059190614e95565b60ff82118015613eb0575061ffff8211155b15613ec65781604051602001613d059190614ded565b61ffff82118015613eda575062ffffff8211155b15613ef05781604051602001613d059190614e05565b62ffffff82118015613f06575063ffffffff8211155b15613f1c5781604051602001613d059190614e1d565b63ffffffff82118015613f34575064ffffffffff8211155b15613f4a5781604051602001613d059190614e35565b64ffffffffff82118015613f64575065ffffffffffff8211155b15613f7a5781604051602001613d059190614e4d565b65ffffffffffff82118015613f96575066ffffffffffffff8211155b15613fac5781604051602001613d059190614e65565b81604051602001613fbd9190614e7d565b6040516020818303038152906040529050919050565b606080603884101561404b57604080516001808252818301909252906020820181803683370190505090506140088385615375565b601f1a60f81b8160008151811061402f57634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a905350612317565b600060015b61405a818761538d565b156140805781614069816155d4565b92506140799050610100826154db565b9050614050565b61408b826001615375565b6001600160401b038111156140b057634e487b7160e01b600052604160045260246000fd5b6040519080825280601f01601f1916602001820160405280156140da576020820181803683370190505b5092506140e78583615375565b6140f2906037615375565b601f1a60f81b8360008151811061411957634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a905350600190505b8181116141b8576101006141498284615522565b614155906101006153e7565b61415f908861538d565b61416991906155ef565b601f1a60f81b83828151811061418f57634e487b7160e01b600052603260045260246000fd5b60200101906001600160f81b031916908160001a905350806141b0816155d4565b915050614135565b50509392505050565b6060806040519050835180825260208201818101602087015b818310156141f25780518352602092830192016141da565b50855184518101855292509050808201602086015b8183101561421f578051835260209283019201614207565b508651929092011591909101601f01601f191660405250905092915050565b805160009061424f575060006106cd565b6020820151805160001a9060c082101561426e576000925050506106cd565b5060019392505050565b8051600090614289575060006106cd565b60008061429984602001516142fb565b84602001516142a89190615375565b90506000846000015185602001516142c09190615375565b90505b808210156142f2576142d482614386565b6142de9083615375565b9150826142ea816155d4565b9350506142c3565b50909392505050565b8051600090811a60808110156143155760009150506106cd565b60b8811080614330575060c08110801590614330575060f881105b1561433f5760019150506106cd565b60c081101561437457614354600160b8615539565b6143619060ff1682615522565b61436c906001615375565b9150506106cd565b614354600160f8615539565b50919050565b80516000908190811a60808110156143a15760019150614428565b60b88110156143c7576143b5608082615522565b6143c0906001615375565b9150614428565b60c08110156143f45760b78103600185019450806020036101000a85510460018201810193505050614428565b60f8811015614408576143b560c082615522565b60f78103600185019450806020036101000a855104600182018101935050505b5092915050565b8061443957611a3b565b602081106144715782518252614450602084615375565b925061445d602083615375565b915061446a602082615522565b9050614439565b60006001614480836020615522565b61448c906101006153e7565b6144969190615522565b84518451821691191617835250505050565b8280546144b49061559f565b90600052602060002090601f0160209004810192826144d6576000855561451c565b82601f106144ef57805160ff191683800117855561451c565b8280016001018555821561451c579182015b8281111561451c578251825591602001919060010190614501565b50610ff892915061468a565b60405180606001604052806060815260200160008152602001600081525090565b5080546145559061559f565b6000825580601f106145675750614585565b601f016020900490600052602060002090810190614585919061468a565b50565b5080546000825560030290600052602060002090810190614585919061469f565b8280546145b59061559f565b90600052602060002090601f0160209004810192826145d7576000855561451c565b82601f106145f05782800160ff1982351617855561451c565b8280016001018555821561451c579182015b8281111561451c578235825591602001919060010190614602565b60408051808201909152600081526060602082015290565b60405180606001604052806060815260200160608152602001606081525090565b604051806040016040528060008152602001606081525090565b604051806040016040528060008152602001600081525090565b5b80821115610ff8576000815560010161468b565b80821115610ff85760006146b38282614549565b50600060018201819055600282015560030161469f565b80356001600160a01b03811681146106cd57600080fd5b60008083601f8401126146f2578081fd5b5081356001600160401b03811115614708578182fd5b60208301915083602082850101111561042a57600080fd5b600082601f830112614730578081fd5b81356001600160401b038082111561474a5761474a61562f565b604051601f8301601f19168101602001828111828210171561476e5761476e61562f565b604052828152848301602001861015614785578384fd5b82602086016020830137918201602001929092529392505050565b6000602082840312156147b1578081fd5b612317826146ca565b600080604083850312156147cc578081fd5b6147d5836146ca565b915060208301356001600160401b038111156147ef578182fd5b6147fb85828601614720565b9150509250929050565b60008060408385031215614817578182fd5b614820836146ca565b946020939093013593505050565b60006020828403121561483f578081fd5b81518015158114612317578182fd5b60008060208385031215614860578182fd5b82356001600160401b03811115614875578283fd5b614881858286016146e1565b90969095509350505050565b60008060008060008060006080888a0312156148a7578283fd5b87356001600160401b03808211156148bd578485fd5b6148c98b838c016146e1565b909950975060208a01359150808211156148e1578485fd5b6148ed8b838c016146e1565b909750955060408a0135945060608a013591508082111561490c578384fd5b506149198a828b016146e1565b989b979a50959850939692959293505050565b600080600080600080600060a0888a031215614946578283fd5b87356001600160401b038082111561495c578485fd5b6149688b838c016146e1565b909950975060208a0135915080821115614980578485fd5b5061498d8a828b016146e1565b90965094505060408801359250606088013591506149ad608089016146ca565b905092959891949750929550565b60008060008060008060008060a0898b0312156149d6578081fd5b88356001600160401b03808211156149ec578283fd5b6149f88c838d016146e1565b909a50985060208b0135915080821115614a10578283fd5b614a1c8c838d016146e1565b909850965060408b0135955060608b0135945060808b0135915080821115614a42578283fd5b50614a4f8b828c016146e1565b999c989b5096995094979396929594505050565b600080600060408486031215614a77578283fd5b83356001600160401b03811115614a8c578384fd5b614a98868287016146e1565b909790965060209590950135949350505050565b600080600080600060608688031215614ac3578081fd5b85356001600160401b0380821115614ad9578283fd5b614ae589838a016146e1565b9097509550602088013594506040880135915080821115614b04578283fd5b50614b11888289016146e1565b969995985093965092949392505050565b600060208284031215614b33578081fd5b81356001600160401b03811115614b48578182fd5b614b5484828501614720565b949350505050565b60008060408385031215614b6e578081fd5b82356001600160401b0380821115614b84578283fd5b614b9086838701614720565b93506020850135915080821115614ba5578283fd5b506147fb85828601614720565b600060208284031215614bc3578081fd5b5035919050565b6000815180845260208085018081965082840281019150828601855b85811015614c30578284038952815160608151818752614c0882880182614c3d565b83890151888a0152604093840151939097019290925250509784019790840190600101614be6565b5091979650505050505050565b60008151808452614c5581602086016020860161555c565b601f01601f19169290920160200192915050565b60008154614c768161559f565b808552602060018381168015614c935760018114614ca757614cd5565b60ff19851688840152604088019550614cd5565b866000528260002060005b85811015614ccd5781548a8201860152908301908401614cb2565b890184019650505b505050505092915050565b60008351614cf281846020880161555c565b835190830190614d0681836020880161555c565b01949350505050565b60008451614d2181846020890161555c565b845190830190614d3581836020890161555c565b8451910190614d4881836020880161555c565b0195945050505050565b6000828483379101908152919050565b60008251614d7481846020870161555c565b9190910192915050565b6000808354614d8c8161559f565b60018281168015614da45760018114614db557614de1565b60ff19841687528287019450614de1565b8786526020808720875b85811015614dd85781548a820152908401908201614dbf565b50505082870194505b50929695505050505050565b60f09190911b6001600160f01b031916815260020190565b60e89190911b6001600160e81b031916815260030190565b60e09190911b6001600160e01b031916815260040190565b60d89190911b6001600160d81b031916815260050190565b60d09190911b6001600160d01b031916815260060190565b60c89190911b6001600160c81b031916815260070190565b60c09190911b6001600160c01b031916815260080190565b60f89190911b6001600160f81b031916815260010190565b6001600160a01b0391909116815260200190565b6001600160a01b039384168152919092166020820152604081019190915260600190565b6001600160a01b03929092168252602082015260400190565b6000602080830181845280855180835260408601915060408482028701019250838701855b82811015614f5157603f19888603018452614f3f858351614c3d565b94509285019290850190600101614f23565b5092979650505050505050565b6000602082526123176020830184614bca565b901515815260200190565b6000602082526123176020830184614c3d565b600060a08252614fa260a0830188614c3d565b8281036020840152614fb48188614c3d565b90508281036040840152614fc88187614c3d565b60608401959095525050608001529392505050565b600060808252614ff06080830187614c3d565b82810360208401526150028187614c69565b9050846040840152828103606084015261501c8185614c3d565b979650505050505050565b60006060825261503a6060830186614c3d565b84602084015282810360408401526127788185614bca565b6000606080835261506581840187614c3d565b60208681860152604085830381870152828754808552838501915083848202860101898852848820885b838110156150d657878303601f190185528883526150af89840183614c69565b600183810154858a015260028401549488019490945294870194926003909201910161508f565b50909c9b505050505050505050505050565b6020808252601b908201527f556e61626c6520746f2072656d6f7665206c617374204f776e65720000000000604082015260600190565b6020808252600e908201526d092dcecc2d8d2c840d8cadccee8d60931b604082015260600190565b60208082526013908201527224b73b30b634b2103ab4b73a10373ab6b132b960691b604082015260600190565b602080825260149082015273496e73756666696369656e742062616c616e636560601b604082015260600190565b6020808252600d908201526c2737903832b936b4b9b9b4b7b760991b604082015260600190565b60208082526024908201527f546f6b656e20776974682073616d65206e616d652065786973747320616c726560408201526330b23c9760e11b606082015260800190565b602080825260139082015272151bdad95b881b9bdd081cdd5c1c1bdc9d1959606a1b604082015260600190565b60208082526017908201527f546f6b656e206973206e6f742072656769737465726564000000000000000000604082015260600190565b6020808252600a908201526924b73b30b634b21029a760b11b604082015260600190565b6020808252600e908201526d135d5cdd0818994818481b1a5cdd60921b604082015260600190565b60208082526019908201527f496e76616c696420616d6f756e74207370656369666965642e00000000000000604082015260600190565b6020808252600f908201526e496e76616c6964207365727669636560881b604082015260600190565b90815260200190565b918252602082015260400190565b6000848252836020830152606060408301526127ef6060830184614c3d565b60006001600160a01b03828116848216808303821115614d0657614d06615603565b6000821982111561538857615388615603565b500190565b60008261539c5761539c615619565b500490565b80825b60018086116153b357506153de565b8187048211156153c5576153c5615603565b808616156153d257918102915b9490941c9380026153a4565b94509492505050565b6000612317600019848460008261540057506001612317565b8161540d57506000612317565b8160018114615423576002811461542d5761545a565b6001915050612317565b60ff84111561543e5761543e615603565b6001841b91508482111561545457615454615603565b50612317565b5060208310610133831016604e8410600b841016171561548d575081810a8381111561548857615488615603565b612317565b61549a84848460016153a1565b8086048211156154ac576154ac615603565b02949350505050565b60006001600160a01b03828116848216811515828404821116156154ac576154ac615603565b60008160001904831182151516156154f5576154f5615603565b500290565b60006001600160a01b038381169083168181101561551a5761551a615603565b039392505050565b60008282101561553457615534615603565b500390565b600060ff821660ff84168082101561555357615553615603565b90039392505050565b60005b8381101561557757818101518382015260200161555f565b83811115612fc05750506000910152565b60008161559757615597615603565b506000190190565b6002810460018216806155b357607f821691505b6020821081141561438057634e487b7160e01b600052602260045260246000fd5b60006000198214156155e8576155e8615603565b5060010190565b6000826155fe576155fe615619565b500690565b634e487b7160e01b600052601160045260246000fd5b634e487b7160e01b600052601260045260246000fd5b634e487b7160e01b600052604160045260246000fd5b60e01c90565b600060443d101561565b57610690565b600481823e6308c379a061566f8251615645565b1461567957610690565b6040513d600319016004823e80513d6001600160401b0381602484011181841117156156a85750505050610690565b828401925082519150808211156156c25750505050610690565b503d830160208284010111156156da57505050610690565b601f01601f191681016020016040529150509056fea2646970667358221220fce13fb4c555db045e3106729edcee6efdc04cd15ea73439999d05af91f208c764736f6c6343000800003300000000000000000000000063d7fcb1f79b5f854705a94e3e065be8204c3fe600000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000008546f6b656e42534800000000000000000000000000000000000000000000000025a0f56b4cca7261eb7562339d1a53e9f7ad16543cd62ebc1f5aef20dfa1c5ea3e16a0599e416dfc57450bf8493d338a270bae701ad1962e922d96a85e905fa18c7d63");
        byte[] rp = Hex.decode("f901b6b901b3f901b0822080b901aaf901a701830c65f3b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000048000000000000000000000000000000000000000000000000020000000000000000000800000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000042000000000000000000000002000000000000000000000000000000000000000000008000000020000000000000000001000000000000000000000000000000000000000000000000f89df89b94f329bb6d3d92514810b507cf1ac977c20206207df863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa00000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000054326b2ad6a7af73e0f8e8a4478e13c26cb82949a000000000000000000000000000000000000000000000021e19e0c9bab2400000");

        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);

        ByteArrayObjectWriter blockUpdateWriter57 = Context.newByteArrayObjectWriter(RLPn);
        BlockUpdate.writeObject(blockUpdateWriter57, bu58, headerBytes58);
        relayMsgWriter.write(blockUpdateWriter57.toByteArray());

        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes58); //block header
        blockProofWrtr.write(witness); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.write(rp); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid receipt proofs with wrong sequence"));
    }

    /**
     * Scenario 13: Adding next block 180 with receipt proof successfully
     */
    @ParameterizedTest(name = "should_handleRelayMessage_when_validReceiptProof")
    @CsvSource({"180"})
    @Order(13)
    public void should_handleRelayMessage_when_validReceiptProof(int offset) {
        var msg = "-Qw_-QLiuQLf-QLcuQJf-QJcoFj4n3ZkdCoYPjCUFp36ydgvqeZf4M3E8Qx-Kkyztcr4oB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoK992IufNyWO2D9HUaGiruhs6agwtGOHW1j_27nZyKfQoPRL3jOXrn89M_sn6aoK6bzqfciJ48fzZCnwdx0fQf61oOG3X5qs980uwpENOKDQRdCRShbrYTglNTZIBQ9FEIFeuQEAAAAAAgAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARAAAAAAAAAAAAAgAAAAAACAAAAAAAAAAAAAACAAAEAAAAAAAAAAAAAACAAAAAAhAAABABAAAAAAAAEAAAAAAAgAIAAQAAQAAAAAAAAAAAAAAAABAAAAAAAAEAAAAAIAEAAAAAAAAAAAAAAAAAAAAAAEAgAAAAAAAAAAAAAACAIAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAAoAAIAAAAAAAAAAAAAAAAAAAAAAAABBAEAAAACAKEAAAAAAAAAAAAAAAAAEAAAQAAAAAAAAAAAAAAIEAAAKCALWEASyN9YMwdTaEYVLAvLhh2IMBAAaEZ2V0aIhnbzEuMTYuNoVsaW51eAAAABHAqp6YaP7XqZm-iHps6eyWKnzez9kWlGS5kU39BqED_GCPa0EyM1zXa6fjAoscPGuJ8E_HrpTGm4sma5DyS4LbnckNAKAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIgAAAAAAAAAALh4TRvmTw6aRmwuZqU0M5KBkng-Kfj6Ib6yEzSZte93D2AAAADo1KUQAJkwiqNlxAVUvImYKvUF2F2pUlFEXV3Uqbs33SWE_ZLTAAAA6NSlEAABd2kg_wsPONeM-VwDPCGt9wRXhRFOOSp1RBeWUuCmEgAAAOjUpRAA-AD5CVW5CVL5CU8AuQbG-QbDuFP4UaA7LGW2CCkiE35Yg5nS5t4faIO0KHJsDq2G2f--zcoyXoCAgICAgICgP2kzSfOdMIxK-J1L-ZSiuAtkOObzBpma4OnXdlO-LxKAgICAgICAgLkGa_kGaDC5BmT5BmEBgwf1WLkBAAAAAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAgAABAAAAAAAAAAAAAAAgAAAAAAAAAAQAQAAAAAAABAAAAAAAAAAAAEAAEAAAAAAAAAAAAAAAAAQAAAAAAABAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAIAAAAAAAAAAAAAAAgCAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAgChAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACBAAD5BVb4m5S6NPPGiTsS_0EVrPG0cSxuJ4Otg_hjoN3yUq0b4sibacKwaPw3jaqVK6fxY8ShFij1Wk31I7PvoAAAAAAAAAAAAAAAAHDnidL11GnqMOBSXb_dVRXW6tMNoAAAAAAAAAAAAAAAAHGhUgu7fmByu_NoKmDHPWO2k2kKoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAK-JuUujTzxok7Ev9BFazxtHEsbieDrYP4Y6CMW-Hl6-x9W9FPcUJ9HoTz3QMUwPeyKR5bIArIx8O5JaAAAAAAAAAAAAAAAABw54nS9dRp6jDgUl2_3VUV1urTDaAAAAAAAAAAAAAAAABxoVILu35gcrvzaCpgxz1jtpNpCqAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPkCGpSq_I7q7o2ci9MmLM49c-Vt7j-3duGgN741PyFs9-M2ORAf1hDFQuagwBCRc_ocHYsE007bfBu5AeAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPmJ0cDovLzB4OTU0YWEzLmljb24vY3hiZTI4MjBhZjRiOTZkNTRjMzMwZDE5YmNiYWU2NmU1MjE0YWZiODA3AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA9_j1uDlidHA6Ly8weDk3LmJzYy8weEFhRmM4RWVhRUU4ZDlDOGJEMzI2MkNDRTNENzNFNTZEZUUzRkI3Nza4PmJ0cDovLzB4OTU0YWEzLmljb24vY3hiZTI4MjBhZjRiOTZkNTRjMzMwZDE5YmNiYWU2NmU1MjE0YWZiODA3iFRva2VuQlNIALhu-GwAuGn4Z7MweDcwZTc4OWQyZjVkNDY5ZWEzMGUwNTI1ZGJmZGQ1NTE1ZDZlYWQzMGQAAAAAAAAAAACqaHgyNzVjMTE4NjE3NjEwZTY1YmE1NzJhYzBhNjIxZGRkMTMyNTUyNDJix8aDRVRICgAAAAAAAAAAAAD5AfyUOryN_wyVuJgjmdrPbtW9e5SkAGj4QqBQ0iNzu4TtH57rWByRPm1F2RjAX4sdkPC-Fo8GpOaZSqAAAAAAAAAAAAAAAABw54nS9dRp6jDgUl2_3VUV1urTDbkBoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA-YnRwOi8vMHg5NTRhYTMuaWNvbi9oeDI3NWMxMTg2MTc2MTBlNjViYTU3MmFjMGE2MjFkZGQxMzI1NTI0MmIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA0VUSAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA-QKC-QJ_ArkCe_kCeKoweEFhRmM4RWVhRUU4ZDlDOGJEMzI2MkNDRTNENzNFNTZEZUUzRkI3NzbhoDe-NT8hbPfjNjkQH9YQxULmoMAQkXP6HB2LBNNO23wbuQHgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAGAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD5idHA6Ly8weDk1NGFhMy5pY29uL2N4YmUyODIwYWY0Yjk2ZDU0YzMzMGQxOWJjYmFlNjZlNTIxNGFmYjgwNwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPf49bg5YnRwOi8vMHg5Ny5ic2MvMHhBYUZjOEVlYUVFOGQ5QzhiRDMyNjJDQ0UzRDczRTU2RGVFM0ZCNzc2uD5idHA6Ly8weDk1NGFhMy5pY29uL2N4YmUyODIwYWY0Yjk2ZDU0YzMzMGQxOWJjYmFlNjZlNTIxNGFmYjgwN4hUb2tlbkJTSAC4bvhsALhp-GezMHg3MGU3ODlkMmY1ZDQ2OWVhMzBlMDUyNWRiZmRkNTUxNWQ2ZWFkMzBkAAAAAAAAAAAAqmh4Mjc1YzExODYxNzYxMGU2NWJhNTcyYWMwYTYyMWRkZDEzMjU1MjQyYsfGg0VUSAoAAAAAAAAAAAAAggC1oFGCfMQGfrJEAwF5anutReQpjUCEibxWVOjL1hMp5LWJAKA8bJvKziim1NtCb8keX3oEvFIKzhW2ihxz6DFE82bPRgIA";
        bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, msg);
    }


    /**
     * Scenario 14: When block update has invalid validator signature
     * Given:
     * When: signature in extraData not valid
     * Then:
     *    throw error:
     *    message: Invalid validator signature
     */
    @ParameterizedTest(name = "should_throwError_when_invalidValidatorSignature")
    @CsvSource({"59"})
    @Order(14)
    public void should_throwError_when_invalidValidatorSignature(int offset) {
        byte[] headerBytes = Hex.decode("f90259a043e2a0a1b168f72d1cd457504746752ecdcf0e993a3af01d5bd79647f66b67daa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479448948297c3236ec3ea6c95f4eec22fdb18255e55a0fc236f0311a4a6b79aff14fdb0a5957087171c3a3f44482b763d505996c3adeda0f0ed92b9d8c41d9ce1299af2d9ff939effc4efecf32236bd9af9816c34f6ef04a0198bcd6b44590c263021c375ade2679e8ff8e8ec62f5acea2bcf5030d97ab764b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000048000000000000000000000000000000000000000000000000020000000000000000000800000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000042000000000000000000000002000000000000000000000000000000000000000000008000000020000000000000000001000000000000000000000000000000000000000000000000003c836691b7830c65f38460cb5602b861d883010006846765746888676f312e31362e36856c696e757800000011c0aa9e6568fed7a999be887a6ce9ec962a7cdecfd9169464b9910303030303fc608f6b4132335cd76ba7e3028b1c3c6b89f04fc7ae94c69b8b266b90f24b82db9d000000a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
        byte[] witness = Hex.decode("f910fb3a8504a817c800836691b78080b910a860806040523480156200001157600080fd5b506040518060400160405280600881526020017f4552433230544b4e0000000000000000000000000000000000000000000000008152506040518060400160405280600381526020017f4554480000000000000000000000000000000000000000000000000000000000815250816003908051906020019062000096929190620001d1565b508051620000ac906004906020840190620001d1565b50620000d89150339050620000c46012600a6200031f565b620000d29061271062000414565b620000de565b620004bb565b6001600160a01b0382166200012a576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401620001219062000277565b60405180910390fd5b6200013860008383620001cc565b80600260008282546200014c9190620002b7565b90915550506001600160a01b038216600090815260208190526040812080548392906200017b908490620002b7565b90915550506040516001600160a01b038316906000907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef90620001c0908590620002ae565b60405180910390a35050565b505050565b828054620001df9062000436565b90600052602060002090601f0160209004810192826200020357600085556200024e565b82601f106200021e57805160ff19168380011785556200024e565b828001600101855582156200024e579182015b828111156200024e57825182559160200191906001019062000231565b506200025c92915062000260565b5090565b5b808211156200025c576000815560010162000261565b6020808252601f908201527f45524332303a206d696e7420746f20746865207a65726f206164647265737300604082015260600190565b90815260200190565b60008219821115620002cd57620002cd6200048c565b500190565b80825b6001808611620002e6575062000316565b818704821115620002fb57620002fb6200048c565b808616156200030957918102915b9490941c938002620002d5565b94509492505050565b600062000330600019848462000337565b9392505050565b600082620003485750600162000330565b81620003575750600062000330565b81600181146200037057600281146200037b57620003af565b600191505062000330565b60ff8411156200038f576200038f6200048c565b6001841b915084821115620003a857620003a86200048c565b5062000330565b5060208310610133831016604e8410600b8410161715620003e7575081810a83811115620003e157620003e16200048c565b62000330565b620003f68484846001620002d2565b8086048211156200040b576200040b6200048c565b02949350505050565b60008160001904831182151516156200043157620004316200048c565b500290565b6002810460018216806200044b57607f821691505b6020821081141562000486577f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b50919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b610bdd80620004cb6000396000f3fe608060405234801561001057600080fd5b50600436106100f55760003560e01c80633950935111610097578063a457c2d711610066578063a457c2d7146101bb578063a9059cbb146101ce578063dd62ed3e146101e1578063f76f8d78146101f4576100f5565b8063395093511461018557806370a082311461019857806395d89b41146101ab578063a3f4df7e146101b3576100f5565b806323b872dd116100d357806323b872dd1461014d5780632e0f2625146101605780632ff2e9dc14610175578063313ce5671461017d576100f5565b806306fdde03146100fa578063095ea7b31461011857806318160ddd14610138575b600080fd5b6101026101fc565b60405161010f91906107a9565b60405180910390f35b61012b610126366004610775565b61028e565b60405161010f919061079e565b6101406102ab565b60405161010f91906109dd565b61012b61015b36600461073a565b6102b1565b610168610353565b60405161010f91906109e6565b610140610358565b610168610373565b61012b610193366004610775565b610378565b6101406101a63660046106ee565b6103c7565b6101026103e6565b6101026103f5565b61012b6101c9366004610775565b610419565b61012b6101dc366004610775565b610494565b6101406101ef366004610708565b6104a8565b6101026104d3565b60606003805461020b90610b56565b80601f016020809104026020016040519081016040528092919081815260200182805461023790610b56565b80156102845780601f1061025957610100808354040283529160200191610284565b820191906000526020600020905b81548152906001019060200180831161026757829003601f168201915b5050505050905090565b60006102a261029b6104f2565b84846104f6565b50600192915050565b60025490565b60006102be8484846105aa565b6001600160a01b0384166000908152600160205260408120816102df6104f2565b6001600160a01b03166001600160a01b031681526020019081526020016000205490508281101561032b5760405162461bcd60e51b8152600401610322906108c7565b60405180910390fd5b610346856103376104f2565b6103418685610b3f565b6104f6565b60019150505b9392505050565b601281565b6103646012600a610a52565b61037090612710610b20565b81565b601290565b60006102a26103856104f2565b8484600160006103936104f2565b6001600160a01b03908116825260208083019390935260409182016000908120918b168152925290205461034191906109f4565b6001600160a01b0381166000908152602081905260409020545b919050565b60606004805461020b90610b56565b6040518060400160405280600881526020016722a92199182a25a760c11b81525081565b600080600160006104286104f2565b6001600160a01b03908116825260208083019390935260409182016000908120918816815292529020549050828110156104745760405162461bcd60e51b815260040161032290610998565b61048a61047f6104f2565b856103418685610b3f565b5060019392505050565b60006102a26104a16104f2565b84846105aa565b6001600160a01b03918216600090815260016020908152604080832093909416825291909152205490565b6040518060400160405280600381526020016208aa8960eb1b81525081565b3390565b6001600160a01b03831661051c5760405162461bcd60e51b815260040161032290610954565b6001600160a01b0382166105425760405162461bcd60e51b81526004016103229061083f565b6001600160a01b0380841660008181526001602090815260408083209487168084529490915290819020849055517f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9259061059d9085906109dd565b60405180910390a3505050565b6001600160a01b0383166105d05760405162461bcd60e51b81526004016103229061090f565b6001600160a01b0382166105f65760405162461bcd60e51b8152600401610322906107fc565b6106018383836106d2565b6001600160a01b0383166000908152602081905260409020548181101561063a5760405162461bcd60e51b815260040161032290610881565b6106448282610b3f565b6001600160a01b03808616600090815260208190526040808220939093559085168152908120805484929061067a9084906109f4565b92505081905550826001600160a01b0316846001600160a01b03167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040516106c491906109dd565b60405180910390a350505050565b505050565b80356001600160a01b03811681146103e157600080fd5b6000602082840312156106ff578081fd5b61034c826106d7565b6000806040838503121561071a578081fd5b610723836106d7565b9150610731602084016106d7565b90509250929050565b60008060006060848603121561074e578081fd5b610757846106d7565b9250610765602085016106d7565b9150604084013590509250925092565b60008060408385031215610787578182fd5b610790836106d7565b946020939093013593505050565b901515815260200190565b6000602080835283518082850152825b818110156107d5578581018301518582016040015282016107b9565b818111156107e65783604083870101525b50601f01601f1916929092016040019392505050565b60208082526023908201527f45524332303a207472616e7366657220746f20746865207a65726f206164647260408201526265737360e81b606082015260800190565b60208082526022908201527f45524332303a20617070726f766520746f20746865207a65726f206164647265604082015261737360f01b606082015260800190565b60208082526026908201527f45524332303a207472616e7366657220616d6f756e7420657863656564732062604082015265616c616e636560d01b606082015260800190565b60208082526028908201527f45524332303a207472616e7366657220616d6f756e74206578636565647320616040820152676c6c6f77616e636560c01b606082015260800190565b60208082526025908201527f45524332303a207472616e736665722066726f6d20746865207a65726f206164604082015264647265737360d81b606082015260800190565b60208082526024908201527f45524332303a20617070726f76652066726f6d20746865207a65726f206164646040820152637265737360e01b606082015260800190565b60208082526025908201527f45524332303a2064656372656173656420616c6c6f77616e63652062656c6f77604082015264207a65726f60d81b606082015260800190565b90815260200190565b60ff91909116815260200190565b60008219821115610a0757610a07610b91565b500190565b80825b6001808611610a1e5750610a49565b818704821115610a3057610a30610b91565b80861615610a3d57918102915b9490941c938002610a0f565b94509492505050565b600061034c6000198484600082610a6b5750600161034c565b81610a785750600061034c565b8160018114610a8e5760028114610a9857610ac5565b600191505061034c565b60ff841115610aa957610aa9610b91565b6001841b915084821115610abf57610abf610b91565b5061034c565b5060208310610133831016604e8410600b8410161715610af8575081810a83811115610af357610af3610b91565b61034c565b610b058484846001610a0c565b808604821115610b1757610b17610b91565b02949350505050565b6000816000190483118215151615610b3a57610b3a610b91565b500290565b600082821015610b5157610b51610b91565b500390565b600281046001821680610b6a57607f821691505b60208210811415610b8b57634e487b7160e01b600052602260045260246000fd5b50919050565b634e487b7160e01b600052601160045260246000fdfea264697066735822122041d364bf84ad9f709951d5788688f4dea6b8f1e4db87cdd05121f24b80338edc64736f6c6343000800003326a07db3d49fb749522cbd7dd1f601f15939eb2553b40ce73a54b5f0c4d18eb502b9a044a0eb6d1eaf16125d1e00d9dae85c8a98bcad37a4cf62504441d4dee52aeec6");
        byte[] rp = Hex.decode("f901b6b901b3f901b0822080b901aaf901a701830c65f3b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000048000000000000000000000000000000000000000000000000020000000000000000000800000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000042000000000000000000000002000000000000000000000000000000000000000000008000000020000000000000000001000000000000000000000000000000000000000000000000f89df89b94f329bb6d3d92514810b507cf1ac977c20206207df863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa00000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000054326b2ad6a7af73e0f8e8a4478e13c26cb82949a000000000000000000000000000000000000000000000021e19e0c9bab2400000");
        BlockUpdate bu60 = new BlockUpdate(BlockHeader.fromBytes(headerBytes), null, new byte[0][0], null);

        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);

        ByteArrayObjectWriter blockUpdateWriter57 = Context.newByteArrayObjectWriter(RLPn);
        BlockUpdate.writeObject(blockUpdateWriter57, bu60, headerBytes);
        relayMsgWriter.write(blockUpdateWriter57.toByteArray());

        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes); //block header
        blockProofWrtr.write(witness); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.write(rp); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid validator signature"));
    }

    /**
     * Scenario 15: When block update has invalid validator signature
     * Given:
     * When: signature in extraData is not belong to validator
     * Then:
     *    throw error:
     *    message: Invalid validator signature
     */
    @ParameterizedTest(name = "should_throwError_when_validatorSignatureDoesntBelongToValidator")
    @CsvSource({"59"})
    @Order(15)
    public void should_throwError_when_validatorSignatureDoesntBelongToValidator(int offset) {}

    /**
     * Scenario 16: When block update has empty validator signature
     * Given:
     * When: empty validator signature
     * Then:
     *    throw error:
     *    message: Invalid validator signature
     */
    @ParameterizedTest(name = "should_throwError_when_emptyValidatorSignature")
    @CsvSource({"59"})
    @Order(16)
    public void should_throwError_when_emptyValidatorSignature(int offset) {
        byte[] headerBytes = Hex.decode("f90259a043e2a0a1b168f72d1cd457504746752ecdcf0e993a3af01d5bd79647f66b67daa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0fc236f0311a4a6b79aff14fdb0a5957087171c3a3f44482b763d505996c3adeda0f0ed92b9d8c41d9ce1299af2d9ff939effc4efecf32236bd9af9816c34f6ef04a0198bcd6b44590c263021c375ade2679e8ff8e8ec62f5acea2bcf5030d97ab764b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000048000000000000000000000000000000000000000000000000020000000000000000000800000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000042000000000000000000000002000000000000000000000000000000000000000000008000000020000000000000000001000000000000000000000000000000000000000000000000003c836691b7830c65f38460cb5602b861d883010006846765746888676f312e31362e36856c696e757800000011c0aa9e0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000000000000000000000000880000000000000000");
        BlockHeader header = BlockHeader.fromBytes(headerBytes);
        BlockUpdate bu = new BlockUpdate(header, null, new byte[0][0], null);

        byte[] witness = Hex.decode("f910fb3a8504a817c800836691b78080b910a860806040523480156200001157600080fd5b506040518060400160405280600881526020017f4552433230544b4e0000000000000000000000000000000000000000000000008152506040518060400160405280600381526020017f4554480000000000000000000000000000000000000000000000000000000000815250816003908051906020019062000096929190620001d1565b508051620000ac906004906020840190620001d1565b50620000d89150339050620000c46012600a6200031f565b620000d29061271062000414565b620000de565b620004bb565b6001600160a01b0382166200012a576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401620001219062000277565b60405180910390fd5b6200013860008383620001cc565b80600260008282546200014c9190620002b7565b90915550506001600160a01b038216600090815260208190526040812080548392906200017b908490620002b7565b90915550506040516001600160a01b038316906000907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef90620001c0908590620002ae565b60405180910390a35050565b505050565b828054620001df9062000436565b90600052602060002090601f0160209004810192826200020357600085556200024e565b82601f106200021e57805160ff19168380011785556200024e565b828001600101855582156200024e579182015b828111156200024e57825182559160200191906001019062000231565b506200025c92915062000260565b5090565b5b808211156200025c576000815560010162000261565b6020808252601f908201527f45524332303a206d696e7420746f20746865207a65726f206164647265737300604082015260600190565b90815260200190565b60008219821115620002cd57620002cd6200048c565b500190565b80825b6001808611620002e6575062000316565b818704821115620002fb57620002fb6200048c565b808616156200030957918102915b9490941c938002620002d5565b94509492505050565b600062000330600019848462000337565b9392505050565b600082620003485750600162000330565b81620003575750600062000330565b81600181146200037057600281146200037b57620003af565b600191505062000330565b60ff8411156200038f576200038f6200048c565b6001841b915084821115620003a857620003a86200048c565b5062000330565b5060208310610133831016604e8410600b8410161715620003e7575081810a83811115620003e157620003e16200048c565b62000330565b620003f68484846001620002d2565b8086048211156200040b576200040b6200048c565b02949350505050565b60008160001904831182151516156200043157620004316200048c565b500290565b6002810460018216806200044b57607f821691505b6020821081141562000486577f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b50919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b610bdd80620004cb6000396000f3fe608060405234801561001057600080fd5b50600436106100f55760003560e01c80633950935111610097578063a457c2d711610066578063a457c2d7146101bb578063a9059cbb146101ce578063dd62ed3e146101e1578063f76f8d78146101f4576100f5565b8063395093511461018557806370a082311461019857806395d89b41146101ab578063a3f4df7e146101b3576100f5565b806323b872dd116100d357806323b872dd1461014d5780632e0f2625146101605780632ff2e9dc14610175578063313ce5671461017d576100f5565b806306fdde03146100fa578063095ea7b31461011857806318160ddd14610138575b600080fd5b6101026101fc565b60405161010f91906107a9565b60405180910390f35b61012b610126366004610775565b61028e565b60405161010f919061079e565b6101406102ab565b60405161010f91906109dd565b61012b61015b36600461073a565b6102b1565b610168610353565b60405161010f91906109e6565b610140610358565b610168610373565b61012b610193366004610775565b610378565b6101406101a63660046106ee565b6103c7565b6101026103e6565b6101026103f5565b61012b6101c9366004610775565b610419565b61012b6101dc366004610775565b610494565b6101406101ef366004610708565b6104a8565b6101026104d3565b60606003805461020b90610b56565b80601f016020809104026020016040519081016040528092919081815260200182805461023790610b56565b80156102845780601f1061025957610100808354040283529160200191610284565b820191906000526020600020905b81548152906001019060200180831161026757829003601f168201915b5050505050905090565b60006102a261029b6104f2565b84846104f6565b50600192915050565b60025490565b60006102be8484846105aa565b6001600160a01b0384166000908152600160205260408120816102df6104f2565b6001600160a01b03166001600160a01b031681526020019081526020016000205490508281101561032b5760405162461bcd60e51b8152600401610322906108c7565b60405180910390fd5b610346856103376104f2565b6103418685610b3f565b6104f6565b60019150505b9392505050565b601281565b6103646012600a610a52565b61037090612710610b20565b81565b601290565b60006102a26103856104f2565b8484600160006103936104f2565b6001600160a01b03908116825260208083019390935260409182016000908120918b168152925290205461034191906109f4565b6001600160a01b0381166000908152602081905260409020545b919050565b60606004805461020b90610b56565b6040518060400160405280600881526020016722a92199182a25a760c11b81525081565b600080600160006104286104f2565b6001600160a01b03908116825260208083019390935260409182016000908120918816815292529020549050828110156104745760405162461bcd60e51b815260040161032290610998565b61048a61047f6104f2565b856103418685610b3f565b5060019392505050565b60006102a26104a16104f2565b84846105aa565b6001600160a01b03918216600090815260016020908152604080832093909416825291909152205490565b6040518060400160405280600381526020016208aa8960eb1b81525081565b3390565b6001600160a01b03831661051c5760405162461bcd60e51b815260040161032290610954565b6001600160a01b0382166105425760405162461bcd60e51b81526004016103229061083f565b6001600160a01b0380841660008181526001602090815260408083209487168084529490915290819020849055517f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9259061059d9085906109dd565b60405180910390a3505050565b6001600160a01b0383166105d05760405162461bcd60e51b81526004016103229061090f565b6001600160a01b0382166105f65760405162461bcd60e51b8152600401610322906107fc565b6106018383836106d2565b6001600160a01b0383166000908152602081905260409020548181101561063a5760405162461bcd60e51b815260040161032290610881565b6106448282610b3f565b6001600160a01b03808616600090815260208190526040808220939093559085168152908120805484929061067a9084906109f4565b92505081905550826001600160a01b0316846001600160a01b03167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040516106c491906109dd565b60405180910390a350505050565b505050565b80356001600160a01b03811681146103e157600080fd5b6000602082840312156106ff578081fd5b61034c826106d7565b6000806040838503121561071a578081fd5b610723836106d7565b9150610731602084016106d7565b90509250929050565b60008060006060848603121561074e578081fd5b610757846106d7565b9250610765602085016106d7565b9150604084013590509250925092565b60008060408385031215610787578182fd5b610790836106d7565b946020939093013593505050565b901515815260200190565b6000602080835283518082850152825b818110156107d5578581018301518582016040015282016107b9565b818111156107e65783604083870101525b50601f01601f1916929092016040019392505050565b60208082526023908201527f45524332303a207472616e7366657220746f20746865207a65726f206164647260408201526265737360e81b606082015260800190565b60208082526022908201527f45524332303a20617070726f766520746f20746865207a65726f206164647265604082015261737360f01b606082015260800190565b60208082526026908201527f45524332303a207472616e7366657220616d6f756e7420657863656564732062604082015265616c616e636560d01b606082015260800190565b60208082526028908201527f45524332303a207472616e7366657220616d6f756e74206578636565647320616040820152676c6c6f77616e636560c01b606082015260800190565b60208082526025908201527f45524332303a207472616e736665722066726f6d20746865207a65726f206164604082015264647265737360d81b606082015260800190565b60208082526024908201527f45524332303a20617070726f76652066726f6d20746865207a65726f206164646040820152637265737360e01b606082015260800190565b60208082526025908201527f45524332303a2064656372656173656420616c6c6f77616e63652062656c6f77604082015264207a65726f60d81b606082015260800190565b90815260200190565b60ff91909116815260200190565b60008219821115610a0757610a07610b91565b500190565b80825b6001808611610a1e5750610a49565b818704821115610a3057610a30610b91565b80861615610a3d57918102915b9490941c938002610a0f565b94509492505050565b600061034c6000198484600082610a6b5750600161034c565b81610a785750600061034c565b8160018114610a8e5760028114610a9857610ac5565b600191505061034c565b60ff841115610aa957610aa9610b91565b6001841b915084821115610abf57610abf610b91565b5061034c565b5060208310610133831016604e8410600b8410161715610af8575081810a83811115610af357610af3610b91565b61034c565b610b058484846001610a0c565b808604821115610b1757610b17610b91565b02949350505050565b6000816000190483118215151615610b3a57610b3a610b91565b500290565b600082821015610b5157610b51610b91565b500390565b600281046001821680610b6a57607f821691505b60208210811415610b8b57634e487b7160e01b600052602260045260246000fd5b50919050565b634e487b7160e01b600052601160045260246000fdfea264697066735822122041d364bf84ad9f709951d5788688f4dea6b8f1e4db87cdd05121f24b80338edc64736f6c6343000800003326a07db3d49fb749522cbd7dd1f601f15939eb2553b40ce73a54b5f0c4d18eb502b9a044a0eb6d1eaf16125d1e00d9dae85c8a98bcad37a4cf62504441d4dee52aeec6");
        byte[] rp = Hex.decode("f901b6b901b3f901b0822080b901aaf901a701830c65f3b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000048000000000000000000000000000000000000000000000000020000000000000000000800000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000042000000000000000000000002000000000000000000000000000000000000000000008000000020000000000000000001000000000000000000000000000000000000000000000000f89df89b94f329bb6d3d92514810b507cf1ac977c20206207df863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa00000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000054326b2ad6a7af73e0f8e8a4478e13c26cb82949a000000000000000000000000000000000000000000000021e19e0c9bab2400000");

        // Mocking relay message from actual data
        //relay message
        ByteArrayObjectWriter relayMsgWriter = Context.newByteArrayObjectWriter(RLPn);
        relayMsgWriter.beginList(3);
        //blockUpdates
        relayMsgWriter.beginList(1);

        ByteArrayObjectWriter blockUpdateWriter57 = Context.newByteArrayObjectWriter(RLPn);
        BlockUpdate.writeObject(blockUpdateWriter57, bu, headerBytes);
        relayMsgWriter.write(blockUpdateWriter57.toByteArray());

        relayMsgWriter.end();
        //blockProof
        ByteArrayObjectWriter blockProofWrtr = Context.newByteArrayObjectWriter(RLPn);
        blockProofWrtr.beginList(2);
        blockProofWrtr.write(headerBytes); //block header
        blockProofWrtr.write(witness); // block witness
        blockProofWrtr.end();
        relayMsgWriter.write(blockProofWrtr.toByteArray());
        //receiptProof
        relayMsgWriter.beginList(1);
        ByteArrayObjectWriter receiptProofWtr = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr.beginList(4);
        receiptProofWtr.write(0);
        receiptProofWtr.write(rp); // receipt proof
        receiptProofWtr.writeNull();
        receiptProofWtr.writeNull();
        receiptProofWtr.end();
        ByteArrayObjectWriter receiptProofWtr1 = Context.newByteArrayObjectWriter(RLPn);
        receiptProofWtr1.beginList(4);
        receiptProofWtr1.write(0);
        receiptProofWtr1.write(receiptProofWtr.toByteArray()); // receipt proof
        receiptProofWtr1.writeNull();
        receiptProofWtr1.writeNull();
        receiptProofWtr1.end();
        relayMsgWriter.write(receiptProofWtr1.toByteArray());
        relayMsgWriter.end();
        relayMsgWriter.end();
        byte[] _msg = Base64.getUrlEncoder().encode(relayMsgWriter.toByteArray());

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, new String(_msg)));
        assertTrue(thrown.getMessage().contains("Invalid validator signature"));    }

    /**
     * Scenario 17: Adding next block 207 with valid signature successfully
     */
    @ParameterizedTest(name = "should_handleRelayMessage_when_validValidatorSignature")
    @CsvSource({"285"})
    @Order(17)
    public void should_handleRelayMessage_when_validValidatorSignature(int offset) {
//        var msg = "-RJA-QijuQLc-QLZuQJc-QJZoJXic3TIpzXBoJgpUlEXxHUxRmu7_HqdlyO5ZEDIZTOuoB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoCYZIGCRkEdGA3P640cXgrwMPTSO6EZoJxkb1HtaqNcjoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhuQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKCAM-EAQ95mACEYWg3Wbhh2IMBAAaEZ2V0aIhnbzEuMTYuNoVsaW51eAAAABHAqp55dzCyV6zsF9PANpUSe1eJv1vs0lrpswHtz7x0rhZL0EoAfhTFUn7Djb7o1VWM5I9g9Ksx_reuJDIty3ttF7ZwAaAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIgAAAAAAAAAALh4TRvmTw6aRmwuZqU0M5KBkng-Kfj6Ib6yEzSZte93D2AAAADo1KUQAJkwiqNlxAVUvImYKvUF2F2pUlFEXV3Uqbs33SWE_ZLTAAAA6NSlEAABd2kg_wsPONeM-VwDPCGt9wRXhRFOOSp1RBeWUuCmEgAAAOjUpRAAuQLf-QLcuQJf-QJcoEhh5BDjjWDMFb2lD8unjdTKSN7llUEbey83inNYX_R8oB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoAE30_KrG6jvbhtiIi41CjEIgfhziI8_we6LpTGaz7-KoNy2e7B_1pkzPOANtfd267n5VISzRTBHQ8IokIV3-CZXoEX1teYf5tlXk2xpA3IT770MhIeqnhZC7nu8C8_j36WruQEAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAhAAAAAAAAAAAAAAAAAAAAAAgAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIAAIAAAAAAAAAAAAAAAAAAAAAAAABBAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAQAAAAAAAAAAAAAAAAAAAKCANCEAQ5qIIMkHOKEYWg3XLhh2IMBAAaEZ2V0aIhnbzEuMTYuNoVsaW51eAAAABHAqp6QoG_YyNrSGsubDOqa1-vwvkxCJNRjQdfZlCKqVauJiFO_H-IHR7N7YUbXwg11o45PZN8MuE4BAPLQqXo4HnnlAKAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIgAAAAAAAAAALh4TRvmTw6aRmwuZqU0M5KBkng-Kfj6Ib6yEzSZte93D2AAAADo1KUQAJkwiqNlxAVUvImYKvUF2F2pUlFEXV3Uqbs33SWE_ZLTAAAA6NSlEAABd2kg_wsPONeM-VwDPCGt9wRXhRFOOSp1RBeWUuCmEgAAAOjUpRAAuQLf-QLcuQJf-QJcoLNGOEK_NooN0hl4A3UbQ6uYlFkM0uH6o7iefaR4HXXmoB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoALgD7j12e93dx9rzsNIBF7qyc2TBSj9pzYA_uZ6QQSMoDjqnCOVrAmLuGrO3n1qvYxzrQghdTXSoXruzgI7wxtKoFszFWZ2Ta64bePtL2hSsDCxy4b-TLr3t8flsF90P8H7uQEAAAAAAgAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARAAAAAAAAAAAAAgAAAAAACAAAAAAAAAAAAAACAAAEAAAAAAAAAAAAAACAAAAAAhAAABABAAAAAAAAEAAAAAAAgAIAAQAAQAAAAAAAAAAAAAAAABAAAAAAAAEAAAAAIAEAAAAAAAAAAAAAAAAAAAAAAEAgAAAAAAAAAAAAAACAIAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAAoAAIAAAAAAAAAAAAAAAAAAAAAAAABBAEAAAACAKEAAAAAAAAAAAAAAAAAEAAAQAAAAAAAAAAAAAAIEAAAKCANGEAQ1bt4MIfRqEYWg3X7hh2IMBAAaEZ2V0aIhnbzEuMTYuNoVsaW51eAAAABHAqp7wc4z6An0gF7oA2jsSuqfKa-fdto2KjVli5saBjTIGXBymjIvjti_j4Ax9gYYRpSfHpClIe7QfbktusI9FfGfnAaAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIgAAAAAAAAAALh4TRvmTw6aRmwuZqU0M5KBkng-Kfj6Ib6yEzSZte93D2AAAADo1KUQAJkwiqNlxAVUvImYKvUF2F2pUlFEXV3Uqbs33SWE_ZLTAAAA6NSlEAABd2kg_wsPONeM-VwDPCGt9wRXhRFOOSp1RBeWUuCmEgAAAOjUpRAA-AD5CZW5CZL5CY8AuQbm-QbjuFP4UaDLwTuKEn_GjjdUZ7wIiOuiDJSPdROPfhnlJHNRyqYZHYCAgICAgICg_PUe4aAwtM6EzIwZWe1lRFQuHsoH9Bhmsz7ZwR6ua1uAgICAgICAgLkGi_kGiDC5BoT5BoEBgwhpcLkBAAAAAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAgAABAAAAAAAAAAAAAAAgAAAAAAAAAAQAQAAAAAAABAAAAAAAAAAAAEAAEAAAAAAAAAAAAAAAAAQAAAAAAABAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAIAAAAAAAAAAAAAAAgCAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAgChAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACBAAD5BXb4m5S6NPPGiTsS_0EVrPG0cSxuJ4Otg_hjoN3yUq0b4sibacKwaPw3jaqVK6fxY8ShFij1Wk31I7PvoAAAAAAAAAAAAAAAAHDnidL11GnqMOBSXb_dVRXW6tMNoAAAAAAAAAAAAAAAAHGhUgu7fmByu_NoKmDHPWO2k2kKoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA3gtrOnZAAA-JuUujTzxok7Ev9BFazxtHEsbieDrYP4Y6CMW-Hl6-x9W9FPcUJ9HoTz3QMUwPeyKR5bIArIx8O5JaAAAAAAAAAAAAAAAABw54nS9dRp6jDgUl2_3VUV1urTDaAAAAAAAAAAAAAAAABxoVILu35gcrvzaCpgxz1jtpNpCqAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPkCOpSq_I7q7o2ci9MmLM49c-Vt7j-3duGgN741PyFs9-M2ORAf1hDFQuagwBCRc_ocHYsE007bfBu5AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPmJ0cDovLzB4OTU0YWEzLmljb24vY3hkNjE3MmJjZjY0MWQ0YThkYjY5Y2RkYjQ3YzZmNGNjYWJkZTRhNGFlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABB_kBBLg5YnRwOi8vMHg5Ny5ic2MvMHhBYUZjOEVlYUVFOGQ5QzhiRDMyNjJDQ0UzRDczRTU2RGVFM0ZCNzc2uD5idHA6Ly8weDk1NGFhMy5pY29uL2N4ZDYxNzJiY2Y2NDFkNGE4ZGI2OWNkZGI0N2M2ZjRjY2FiZGU0YTRhZYhUb2tlbkJTSAG4ffh7ALh4-HazMHg3MGU3ODlkMmY1ZDQ2OWVhMzBlMDUyNWRiZmRkNTUxNWQ2ZWFkMzBkAAAAAAAAAAAAqmh4MDMyY2FhMjg0ODRkZjljMWY5MjkxZmU0MzQ1ZDM2MjdkOWVjMTBhNtbVg0VUSIgNvS_BN6MAAIcjhvJvwQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPkB_JQ6vI3_DJW4mCOZ2s9u1b17lKQAaPhCoFDSI3O7hO0fnutYHJE-bUXZGMBfix2Q8L4Wjwak5plKoAAAAAAAAAAAAAAAAHDnidL11GnqMOBSXb_dVRXW6tMNuQGgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAGAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD5idHA6Ly8weDk1NGFhMy5pY29uL2h4MDMyY2FhMjg0ODRkZjljMWY5MjkxZmU0MzQ1ZDM2MjdkOWVjMTBhNgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADb0vwTejAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI4byb8EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADRVRIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD5AqL5Ap8CuQKb-QKYqjB4QWFGYzhFZWFFRThkOUM4YkQzMjYyQ0NFM0Q3M0U1NkRlRTNGQjc3NuGgN741PyFs9-M2ORAf1hDFQuagwBCRc_ocHYsE007bfBu5AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPmJ0cDovLzB4OTU0YWEzLmljb24vY3hkNjE3MmJjZjY0MWQ0YThkYjY5Y2RkYjQ3YzZmNGNjYWJkZTRhNGFlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABB_kBBLg5YnRwOi8vMHg5Ny5ic2MvMHhBYUZjOEVlYUVFOGQ5QzhiRDMyNjJDQ0UzRDczRTU2RGVFM0ZCNzc2uD5idHA6Ly8weDk1NGFhMy5pY29uL2N4ZDYxNzJiY2Y2NDFkNGE4ZGI2OWNkZGI0N2M2ZjRjY2FiZGU0YTRhZYhUb2tlbkJTSAG4ffh7ALh4-HazMHg3MGU3ODlkMmY1ZDQ2OWVhMzBlMDUyNWRiZmRkNTUxNWQ2ZWFkMzBkAAAAAAAAAAAAqmh4MDMyY2FhMjg0ODRkZjljMWY5MjkxZmU0MzQ1ZDM2MjdkOWVjMTBhNtbVg0VUSIgNvS_BN6MAAIcjhvJvwQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIIA0aA4SYHHbkgD6hj90KzyE2FmnzyhP7A1WS2nKyHYMm5HmQCgpzBhyrX6uKH4Yfjivmd-xV2XlGy5NmfY6vWbp7jUNR8CAA==";
        var  msg = "-Q-9-Q-3uQU6-QU3uQJc-QJZoMfwn1qmLwqc24hh72XrdQRMqvV208AD3icKwJ9ArBkhoB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoCu5aEhGNANHKzDwlvLWN6D1YwpJC9WviPdHjM465qFKoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhuQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKCAR6EAMdGEgCEYW2C8bhh2IMBAAaEZ2V0aIhnbzEuMTYuNoVsaW51eAAAABHAqp7TRG1oP3FtIPiTwViuiygakB9AHC0BZ5eTWszfUtBoilCsAMxi7VmeC2AJEDcoJRtJv4usLCKFGYSteA6ChafgAKAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIgAAAAAAAAAALh4TRvmTw6aRmwuZqU0M5KBkng-Kfj6Ib6yEzSZte93D2AAAADo1KUQAJkwiqNlxAVUvImYKvUF2F2pUlFEXV3Uqbs33SWE_ZLTAAAA6NSlEAABd2kg_wsPONeM-VwDPCGt9wRXhRFOOSp1RBeWUuCmEgAAAOjUpRAAuQJb-QJYoMfwn1qmLwqc24hh72XrdQRMqvV208AD3icKwJ9ArBkhoB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoCu5aEhGNANHKzDwlvLWN6D1YwpJC9WviPdHjM465qFKoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhuQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKCAR6Dx0YSgIRhbYLxuGHYgwEABoRnZXRoiGdvMS4xNi42hWxpbnV4AAAAEcCqntNEbWg_cW0g-JPBWK6LKBqQH0AcLQFnl5NazN9S0GiKUKwAzGLtWZ4LYAkQNyglG0m_i6wsIoUZhK14DoKFp-AAoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAiAAAAAAAAAAAuQU6-QU3uQJc-QJZoICPUg-WkojU0m4MBiJ2O7I8PiU8GSYjf-lpKc-RwlVXoB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoCu5aEhGNANHKzDwlvLWN6D1YwpJC9WviPdHjM465qFKoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhuQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKCAR-EAMZ-zQCEYW2C9Lhh2IMBAAaEZ2V0aIhnbzEuMTYuNoVsaW51eAAAABHAqp7Qt1P49OaWAlblgtNM3dVjEZm4QuQgc6RVnjvRMedA6C25DnIB4AG7v8IzEwxFRfhRGo3s0FblPp7oGld7wRWtAKAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIgAAAAAAAAAALh4TRvmTw6aRmwuZqU0M5KBkng-Kfj6Ib6yEzSZte93D2AAAADo1KUQAJkwiqNlxAVUvImYKvUF2F2pUlFEXV3Uqbs33SWE_ZLTAAAA6NSlEAABd2kg_wsPONeM-VwDPCGt9wRXhRFOOSp1RBeWUuCmEgAAAOjUpRAAuQJb-QJYoICPUg-WkojU0m4MBiJ2O7I8PiU8GSYjf-lpKc-RwlVXoB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoCu5aEhGNANHKzDwlvLWN6D1YwpJC9WviPdHjM465qFKoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhuQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKCAR-Dxn7NgIRhbYL0uGHYgwEABoRnZXRoiGdvMS4xNi42hWxpbnV4AAAAEcCqntC3U_j05pYCVuWC00zd1WMRmbhC5CBzpFWeO9Ex50DoLbkOcgHgAbu_wjMTDEVF-FEajezQVuU-nugaV3vBFa0AoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAiAAAAAAAAAAAuQU6-QU3uQJc-QJZoKnnqLXJwbIJnksXZesa4I5T_Z5B2nzlIKEXHwCakzLBoB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoCu5aEhGNANHKzDwlvLWN6D1YwpJC9WviPdHjM465qFKoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhuQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKCASCEAMW4UACEYW2C97hh2IMBAAaEZ2V0aIhnbzEuMTYuNoVsaW51eAAAABHAqp6rIgvdqJmETumOFspb3nQfASe2oamiX8eFxm0pjqPy3xveVBBzRt4y3bgRrkS_VmJQVqQprv-Pm13U_zAWl-LXAKAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIgAAAAAAAAAALh4TRvmTw6aRmwuZqU0M5KBkng-Kfj6Ib6yEzSZte93D2AAAADo1KUQAJkwiqNlxAVUvImYKvUF2F2pUlFEXV3Uqbs33SWE_ZLTAAAA6NSlEAABd2kg_wsPONeM-VwDPCGt9wRXhRFOOSp1RBeWUuCmEgAAAOjUpRAAuQJb-QJYoKnnqLXJwbIJnksXZesa4I5T_Z5B2nzlIKEXHwCakzLBoB3MTejex116q4W1Z7bM1BrTEkUblIp0E_ChQv1A1JNHlEiUgpfDI27D6myV9O7CL9sYJV5VoCu5aEhGNANHKzDwlvLWN6D1YwpJC9WviPdHjM465qFKoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhoFboHxcbzFWm_4NF5pLA-G5bSOAbmWytwAFiL7XjY7QhuQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKCASCDxbhQgIRhbYL3uGHYgwEABoRnZXRoiGdvMS4xNi42hWxpbnV4AAAAEcCqnqsiC92omYRO6Y4WylvedB8BJ7ahqaJfx4XGbSmOo_LfG95UEHNG3jLduBGuRL9WYlBWpCmu_4-bXdT_MBaX4tcAoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAiAAAAAAAAAAA-ADA";
        bmv.invoke(owners[0], "handleRelayMessage", currentBMCBTPAdd, prevBMCBTPAdd, BigInteger.ZERO, msg);
    }

    @ParameterizedTest(name = "should_verifySignature_when_realData")
    @CsvSource({"55"})
    @Order(1)
    public void should_verifySignature_when_realData(int offset) {
        var headerBytes = "f9025ca09d996196750bf2c1ed51f55fc08afa68c8571cc7700fc34521a843d99a034b7ca01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479496c5d20b2a975c050e4220be276ace4892f4b41aa04db5fbf990816f80792ceb8054d569715f848a80a6cc1a07eed0df5e4c55e1d2a00e7b0c2d8617b29440b1be5538e335ea3234608a449be7f2627de8e92ad1e7e7a05dd941745a4e6236fe79a0b6bb2333e18c9cf4601a548e381ff8a3196dee82d0b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000020100000000000000000000000000000000000200002000000000000000200000000000000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000283a100418401c9c38082d0c68460ed4ef6b861d883010006846765746888676f312e31352e35856c696e75780000001600553d240cc9838de0cd2c0774ba6a18019b8e00d816a611ee48afcb8df7753602277c01eedc1b26f3b8e341ec36862674c1087768bad5a319348f26107d1bed4a415701a00000000000000000000000000000000000000000000000000000000000000000880000000000000000";
        BlockHeader bh = BlockHeader.fromBytes(Hex.decode(headerBytes));
        bh.verifyValidatorSignature(Hex.decode(headerBytes));
    }


    //TODO: Tests for votes
    // 1. votes does not exist - scenario 4 in the test doc

}


/*
Sample data from POC
for the inputs
        let blockHash = '0xc32470c2459fd607246412e23b4b4d19781c1fa24a603d47a5bc066be3b5c0af'
        let txHash    = '0xacb81623523bbabccb1638a907686bc2f3229c70e3ab51777bef0a635f3ac03f'
        let res = await verifyReceipt(txHash, blockHash)

res.receipt proof= 0xf901d2f901cf822080b901c9f901c6a0b5e5c57f738b1874e7c9a693db757dc3106fe69009e127199842f80a447ab91382cd1bb9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000020010000000000000000000020000000000000000000000000000000000000020000000000040000000200000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000040002000000000000000000000000000000000000000000000000000000000000000000000f89df89b947c5a0ce9267ed19b22f8cae653f198e3e8daf098f863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa0000000000000000000000000019f484f4320c8fb11d0238b2b03c16fec905527a000000000000000000000000083335e0c01afac5e02ff201ba0f5979ebc4aa93fa000000000000000000000000000000000000000000000000340aad21b3b700000
block hash header = c32470c2459fd607246412e23b4b4d19781c1fa24a603d47a5bc066be3b5c0af
receiptsroot = fe2b38c1f594b5c8cd4173c9baf34fdee48a487bc0550783bcaaa5e0403b2d98
receiptrootfromproof= keccak(rlp.encode(proof[0]))
txIndex/key/path = 0x0 = 80
receiptBufer = 0xf901c6a0b5e5c57f738b1874e7c9a693db757dc3106fe69009e127199842f80a447ab91382cd1bb9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000020010000000000000000000020000000000000000000000000000000000000020000000000040000000200000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000040002000000000000000000000000000000000000000000000000000000000000000000000f89df89b947c5a0ce9267ed19b22f8cae653f198e3e8daf098f863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa0000000000000000000000000019f484f4320c8fb11d0238b2b03c16fec905527a000000000000000000000000083335e0c01afac5e02ff201ba0f5979ebc4aa93fa000000000000000000000000000000000000000000000000340aad21b3b700000

// Block Header got from
 let txHash    = '0xacb81623523bbabccb1638a907686bc2f3229c70e3ab51777bef0a635f3ac03f'
let resp = await receiptProof(txHash)
rlp.ecnode(resp.header)
rlpencodedproof = f901cf822080b901c9f901c6a0b5e5c57f738b1874e7c9a693db757dc3106fe69009e127199842f80a447ab91382cd1bb9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000020010000000000000000000020000000000000000000000000000000000000020000000000040000000200000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000040002000000000000000000000000000000000000000000000000000000000000000000000f89df89b947c5a0ce9267ed19b22f8cae653f198e3e8daf098f863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa0000000000000000000000000019f484f4320c8fb11d0238b2b03c16fec905527a000000000000000000000000083335e0c01afac5e02ff201ba0f5979ebc4aa93fa000000000000000000000000000000000000000000000000340aad21b3b700000

 /* working tested

     byte[] headerBytes=Hex.decode("f9020ca0ee7f2e6ae3e45315714cae00ad10069f57d8a761aae56ffc5dbfaf2f4d231519a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347949435d50503aee35c8757ae4933f7a0ab56597805a01fc9ace601d79087f316f05401494638011f05e109387f1d9cf41bc2abc761cca0990b09909db83c715a24db6ce9c30ac3bdb11de2bece9ab4a371fa5a218dd894a0fe2b38c1f594b5c8cd4173c9baf34fdee48a487bc0550783bcaaa5e0403b2d98b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000010000000002001000000000000000000002000000000000000000000000000000000000002000000000004000000020000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000004000200000000000000000000000000000000000000000000000000000000000000000000087042a2186d4b670833d39538366e62682cd1b8459662eba8c7761746572686f6c652e696fa0843193e7aa8dbeac600b7dbca4a3c7fd8b92db45d6ee662fdd69801f3d0b7bd0883ea603400005917e");
        BlockHeader header = BlockHeader.fromBytes(headerBytes);
        BlockUpdate bu= new BlockUpdate(header, null, new byte[0][0]);
        List<BlockUpdate> buList = new ArrayList<BlockUpdate>();
        buList.add(bu);
        byte[] witness = Hex.decode("f8a902850df847580082cd1b947c5a0ce9267ed19b22f8cae653f198e3e8daf09880b844a9059cbb00000000000000000000000083335e0c01afac5e02ff201ba0f5979ebc4aa93f00000000000000000000000000000000000000000000000340aad21b3b70000025a087e52ad60613b36f6d6dda62ff7aef013fc91340f6511c834a28f742fcfebf34a05e1bd17543f7e875e8872a3a19386e6428b661329e1ebdd38ad3fd14c256b196");
        BlockWitness bw=BlockWitness.fromBytes(witness);
        BlockProof blockProof = new BlockProof(header, bw);
*/
