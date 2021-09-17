package foundation.icon.btp.bmv;

import foundation.icon.btp.bmv.lib.HexConverter;
import foundation.icon.btp.bmv.lib.mpt.*;
import i.IInstrumentation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import testutils.TestInstrumentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MPTTest {
    private static MerklePatriciaTree mpt;

    @BeforeEach
    public void setup() throws Exception {
        IInstrumentation.attachedThreadInstrumentation.set(new TestInstrumentation());
    }

    @AfterEach
    public void tearDown() throws Exception {
        IInstrumentation.attachedThreadInstrumentation.remove();
    }

    @Test
    public void branchNodeTest() throws MPTException {
        TrieNode.BranchNode node = new TrieNode.BranchNode();
        assertEquals(17, node.getRaw().size());
        assertArrayEquals(null, node.getValue());
        assertArrayEquals(null, node.getBranch(0));
        node.setBranch(1, new byte[][]{new byte[]{32}, new byte[]{98}});
        assertArrayEquals(new byte[][]{new byte[]{32}, new byte[]{98}}, node.getRaw().get(1));
        assertEquals("D380C22062808080808080808080808080808080", HexConverter.bytesToHex(node.encodeRLP()));

        List<byte[][]> branches = new ArrayList<>();
        for (int i = 0; i < 16; i++)
            branches.add(null);

        branches.set(1, new byte[][]{new byte[]{32}, new byte[]{98}});
        branches.set(3, new byte[][]{new byte[]{32}, new byte[]{100}});
        node = TrieNode.BranchNode.fromList(branches);
        assertEquals("D580C2206280C2206480808080808080808080808080", HexConverter.bytesToHex(node.encodeRLP()));
    }

    @Test
    public void trieBasicTest() throws MPTException {
        Trie trie = new Trie();
        trie.put(new byte[]{'a'}, new byte[]{'b'});
        trie.put(new byte[]{'c'}, new byte[]{'d'});
        trie.put(new byte[]{'b'}, new byte[]{'v'});
        assertEquals("9935F35F16F1EADC0481399D5D72AD3583F67656542FFD4FABDFE96D8F014436", HexConverter.bytesToHex(trie.getRoot()));
        trie.put(new byte[]{'m'}, new byte[]{'n'});
        System.out.println(HexConverter.bytesToHex(trie.getRoot()));
    }

    //@Test
    public void trieFromProofsTest() throws MPTException {
        byte[][] proofs = new byte[][]{
                HexConverter.hexStringToByteArray("f851a07dbb96ba3fe5ef48b697cf4b938bdfd0f84c03ab7f6dfd8313dbd9f61f9a0c5f80808080808080a0f59c4e824a9db68fd4e86f4d6fb07306ed615fae2c61efd313bea792033490cf8080808080808080"),
                HexConverter.hexStringToByteArray("f9066830b90664f90661018307f558b9010000000002000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000008000000000000000000000200000000000080000000002000008000000000000000100100000000000000000000000000000000000040000000000000000000000000000000000000010000000000000000000000000000000000000000000000002000000000000000000000000022000000010000004000000000008000000000000000000000010000000000000200002000000000000000000000000000000000000000000000000000000200210000001000000000000000000000000001000000000000000200000800000f90556f89b948c2e5fc5d651129ce7296847dcfac62c646e4e3df863a0ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3efa000000000000000000000000070e789d2f5d469ea30e0525dbfdd5515d6ead30da000000000000000000000000081c0094f73123eebd250ab4ee1e8aa6e82a7ca6fa0000000000000000000000000000000000000000000000000000000000000000af89b948c2e5fc5d651129ce7296847dcfac62c646e4e3df863a08c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925a000000000000000000000000070e789d2f5d469ea30e0525dbfdd5515d6ead30da000000000000000000000000081c0094f73123eebd250ab4ee1e8aa6e82a7ca6fa00000000000000000000000000000000000000000000000000000000000000000f9021a94aafc8eeaee8d9c8bd3262cce3d73e56dee3fb776e1a037be353f216cf7e33639101fd610c542e6a0c0109173fa1c1d8b04d34edb7c1bb901e00000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000000000000000000000000000000000000000003e6274703a2f2f30786638616163332e69636f6e2f637864623061396461303465633132353537326166366233643032356261313138393561613737653765000000000000000000000000000000000000000000000000000000000000000000f7f8f5b8396274703a2f2f307839372e6273632f307841614663384565614545386439433862443332363243434533443733453536446545334642373736b83e6274703a2f2f30786638616163332e69636f6e2f63786462306139646130346563313235353732616636623364303235626131313839356161373765376588546f6b656e42534800b86ef86c00b869f867b3307837306537383964326635643436396561333065303532356462666464353531356436656164333064000000000000000000aa637861616462666536346236613465656262623335616464306439663036363335363964626239613138c7c6834554480a00000000000000000000f901fc9430802e869941c6347a904ca034840a30b1d728baf842a050d22373bb84ed1f9eeb581c913e6d45d918c05f8b1d90f0be168f06a4e6994aa000000000000000000000000070e789d2f5d469ea30e0525dbfdd5515d6ead30db901a00000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000000000000000000000000000000000000000003e6274703a2f2f30786638616163332e69636f6e2f6378616164626665363462366134656562626233356164643064396630363633353639646262396131380000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000034554480000000000000000000000000000000000000000000000000000000000")
        };

        //System.out.println(TrieNode.decodeRaw(proofs));
        var arr = HexConverter.hexStringToByteArray("D716D580C220628080808080C22068808080808080808080");
        //TrieNode.decode(arr);
    }

    @Test
    public void testNibbles() {
        assertArrayEquals(new byte[]{3, 8, 0, 2}, Nibbles.bytesToNibbles(new byte[]{56, 2}));
        assertArrayEquals(new byte[]{18, 64}, Nibbles.nibblesToBytes(new byte[]{0x01, 0x02, 0x03, 0x10}));
        assertArrayEquals(new byte[]{2, 0, 6, 1}, Nibbles.addHexPrefix(new byte[]{6, 1}, true));
        assertArrayEquals(new byte[]{3}, Nibbles.removeHexPrefix(new byte[]{4, 2, 3}));
    }

    @Test
    public void testMatchingNibbleLength1() {
        List<Byte> a = Arrays.asList((byte) 0x00, (byte) 0x01);
        List<Byte> b = Arrays.asList((byte) 0x00, (byte) 0x01, (byte) 0x02);

        int result = Nibbles.matchingNibbleLength(a, b);
        assertEquals(2, result);
    }
}