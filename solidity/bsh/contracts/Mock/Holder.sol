pragma solidity >=0.5.0 <0.8.0;
pragma experimental ABIEncoderV2;

import "@openzeppelin/contracts/token/ERC1155/ERC1155Holder.sol";
import "../BSHPeriphery.sol";
import "../BSHCore.sol";

contract Holder is ERC1155Holder {
    BSHPeriphery private bshp;
    BSHCore private bshc;

    function addBSHContract(address _bshp, address _bshc) external {
        bshp = BSHPeriphery(_bshp);
        bshc = BSHCore(_bshc);
    }

    function setApprove(address _operator) external {
        bshc.setApprovalForAll(_operator, true);
    }

    function callTransfer(
        string calldata _coinName,
        uint256 _value,
        string calldata _to
    ) external {
        bshc.transfer(_coinName, _value, _to);
    }
}
