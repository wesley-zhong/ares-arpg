
//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

package cfg.item;

import luban.*;
import com.google.gson.JsonElement;


public final class TbItemMiscConfig {
    private final cfg.ItemMiscConfig _data;

    public final cfg.ItemMiscConfig data() { return _data; }

    public TbItemMiscConfig(JsonElement _buf) {
        int n = _buf.getAsJsonArray().size();
        if (n != 1) throw new SerializationException("table mode=one, but size != 1");
        _data = cfg.ItemMiscConfig.deserialize(_buf.getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject());
    }


    /**
     * 背包容量
     */
     public int getBagCapacity() { return _data.bagCapacity; }

}
