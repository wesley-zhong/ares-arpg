
//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

package cfg;

import luban.*;
import com.google.gson.JsonElement;

public final class Tables
{

    public  interface  IJsonLoader {
        JsonElement load(String file) throws java.io.IOException;
    }

    private final cfg.item.TbItem _tbitem;
    public cfg.item.TbItem getTbItem() { return _tbitem; }
    private final cfg.item.TbItemMiscConfig _tbitemmiscconfig;
    public cfg.item.TbItemMiscConfig getTbItemMiscConfig() { return _tbitemmiscconfig; }
    private final cfg.item.TbItemLimit _tbitemlimit;
    public cfg.item.TbItemLimit getTbItemLimit() { return _tbitemlimit; }

    public Tables(IJsonLoader loader) throws java.io.IOException {
        _tbitem = new cfg.item.TbItem(loader.load("item_tbitem")); 
        _tbitemmiscconfig = new cfg.item.TbItemMiscConfig(loader.load("item_tbitemmiscconfig")); 
        _tbitemlimit = new cfg.item.TbItemLimit(loader.load("item_tbitemlimit")); 
    }
}
