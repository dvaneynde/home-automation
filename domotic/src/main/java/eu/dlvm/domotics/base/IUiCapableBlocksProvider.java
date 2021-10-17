package eu.dlvm.domotics.base;

import java.util.List;

public interface IUiCapableBlocksProvider {
    IUiCapableBlock findUiCapable(String name);
    List<IUiCapableBlock> getUiCapableBlocks();
}
