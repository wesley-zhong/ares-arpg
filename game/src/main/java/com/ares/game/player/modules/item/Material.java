package com.ares.game.player.modules.item;

import cfg.item.ItemType;
import com.ares.core.exception.UnknownLogicException;
import com.ares.game.player.Player;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Material extends Item {
    private int count;

    public Material(int itemId) {
        super(ItemType.MATERIAL, itemId);
    }

    @Override
    public int getItemCount() {
        return count;
    }
 
    @Override
    public ItemType getItemType() {
        return ItemType.MATERIAL;
    }

    @Override
    public int checkConsume() {
        // 材料默认都可以被消耗
        return 0;
    }

    public void fromBin(BinServer.ItemBin bin)
    {
        super.fromBin(bin);

        BinServer.MaterialBin materialBin = bin.getMaterial();
        count = materialBin.getCount();
//        for (auto [time, num] : proto_material_bin.delete_bin().delete_time_num_map())
//        {
//            delete_time_num_map_[time] = num;
//        }
    }

    public void toBin(BinServer.ItemBin.Builder bin)
    {
        super.toBin(bin);

        bin.setMaterial(BinServer.MaterialBin.newBuilder()
                .setCount(count)
                .build());
//        auto& bin_map = *proto_material_bin.mutable_delete_bin().mutable_delete_time_num_map();
//        for (auto [time, num] : delete_time_num_map_)
//        {
//            if (num == 0)
//            {
//                continue;
//            }
//            bin_map[time] = num;
//        }
    }

    public void toClient(ProtoCommon.Item.Builder proto)
    {
        super.toClient(proto);

        proto.setMaterial(ProtoCommon.PbMaterial.newBuilder()
                        .setCount(count)
                        .build());
//        MaterialDeleteExcelConfig* delete_config = GET_TXT_CONFIG_MGR.material_config_mgr.findMaterialDeleteExcelConfig(getItemId());
//        if (delete_config != null)
//        {
//            proto_material.mutable_delete_info().set_has_delete_config(true);
//            switch (delete_config.expire_type)
//            {
//                case data::MaterialExpireType::DelayWeekCountDown:
//                {
//                    uint32_t delay_week = 0;
//                    uint32_t config_time = 0;
//                    if (delete_config.expire_time_param_vec.size() > 1)
//                    {
//                        delay_week = delete_config.expire_time_param_vec[0];
//                        config_time = delete_config.expire_time_param_vec[1];
//                    }
//                    auto& proto_map = *proto_material.mutable_delete_info().mutable_delay_week_count_down_delete().mutable_delete_time_num_map();
//                    for (auto [time, num] : delete_time_num_map_)
//                    {
//                        if (num == 0)
//                        {
//                            continue;
//                        }
//                        proto_map[time] = num;
//                    }
//                    proto_material.mutable_delete_info().mutable_delay_week_count_down_delete().set_config_delay_week(delay_week);
//                    proto_material.mutable_delete_info().mutable_delay_week_count_down_delete().set_config_count_down_time(config_time);
//                    break;
//                }
//                case data::MaterialExpireType::CountDown:
//                {
//                    uint32_t time = 0;
//                    if (!delete_config.expire_time_param_vec.empty())
//                    {
//                        time = delete_config.expire_time_param_vec[0];
//                    }
//                    auto& proto_map = *proto_material.mutable_delete_info().mutable_count_down_delete().mutable_delete_time_num_map();
//                    for (auto [time, num] : delete_time_num_map_)
//                    {
//                        if (num == 0)
//                        {
//                            continue;
//                        }
//                        proto_map[time] = num;
//                    }
//                    proto_material.mutable_delete_info().mutable_count_down_delete().set_config_count_down_time(time);
//                    break;
//                }
//                case data::MaterialExpireType::DateTime:
//                default:
//                {
//                    uint32_t time = 0;
//                    if (!delete_config.expire_time_param_vec.empty())
//                    {
//                        time = delete_config.expire_time_param_vec[0];
//                    }
//                    proto_material.mutable_delete_info().mutable_date_delete().set_delete_time(time);
//                    break;
//                }
//            }
//        }
    }

    public void init(boolean is_first_create)
    {
        super.init(is_first_create);
//
//        MaterialExcelConfig* material_config = GET_TXT_CONFIG_MGR.material_config_mgr.findMaterialExcelConfig(getItemId());
//        if (material_config == null)
//        {
//            LOG_WARNING + "findMaterialExcelConfig failed, item_id:" + getItemId();
//            return -1;
//        }

        // 这里不需要检查数量是否超过了堆叠上限，
        // 因为如果策划如果修改了堆叠上限，在这里检查会导致玩家登陆失败
        // 实际上只需要不能再增加就可以了
    }

//    uint32_t getWeight()
//    {
//        ItemConfig* item_config = getItemConfig();
//        if (item_config == null)
//        {
//            LOG_WARNING + "findItemConfig failed, item_id:" + getItemId();
//            return 0;
//        }
//
//        return item_config.weight * count_;
//    }

    // TODO：材料删除处理split与merge
    // 如果全部split出去，要删掉原来的item
    public Item split(Player player, int count)
    {
        if (count > getItemCount())
        {
            throw new UnknownLogicException("split material failed, item_id:" + getItemId()
                    + " cur_count:" + getItemCount() + " split_count:" + count);
        }

        subCount(count);

        ItemParam item_param = new ItemParam();
        item_param.itemId = getItemId();
        item_param.count = count;

        List<Item> itemList = player.getItemModule().createItem(item_param);
        if (itemList.size() != 1)
        {
            throw new UnknownLogicException("createItem failed, item_id:" + getItemId()
                    + " count:" + count + " itemList size:" + itemList.size());
        }

        return itemList.getFirst();
    }

    public void merge(Item item)
    {
        if (item == null)
        {
            throw new UnknownLogicException("item is null");
        }

        if (item.getItemId() != getItemId())
        {
            throw new UnknownLogicException("can not merge, source item_id:" + getItemId()
                    + " target item_id:" + getItemId());
        }

        Material material = (Material)item;

        // 这边可能还需要和策划商量一下溢出上限的处理
        // 目前先按照不能溢出来处理
        addCount(material.getItemCount());
        material.setCount(0);
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public void addCount(int count)
    {
        long totalAmount = (long)getItemCount() + count;
        if (totalAmount > Integer.MAX_VALUE) {
            log.warn("add material count overflow, item_id:" + getItemId()
                    + " cur_count:" + getItemCount() + " add_count:" + count);
            totalAmount = Integer.MAX_VALUE;
        }
//
//        MaterialExcelConfig* material_config = GET_TXT_CONFIG_MGR.material_config_mgr.findMaterialExcelConfig(getItemId());
//        if (material_config == null)
//        {
//            LOG_WARNING + "findMaterialExcelConfig failed, item_id:" + getItemId();
//            return -1;
//        }
//
//        if (totalAmount > material_config.stack_limit)
//        {
//            LOG_WARNING + "addCount exceed limit, item_id:" + getItemId()
//                    + " cur_count:" + getItemCount() + " add_count:" + count
//                    + " max_count:" + material_config.stack_limit;
//        }

//        tryAddDeleteCount(totalAmount - getItemCount());
        setCount((int)totalAmount);

        notifyItemChange();
    }
//
//    int32_t tryAddDeleteCount(uint32_t count)
//    {
//    MaterialDeleteExcelConfig* delete_config = GET_TXT_CONFIG_MGR.material_config_mgr.findMaterialDeleteExcelConfig(getItemId());
//        if (null == delete_config)
//        {
//            return 0;
//        }
//        uint32_t now = TimeUtils::getNow();
//        uint32_t time_offset = GET_TXT_CONFIG_MGR.const_value_config_mgr.getTimeOffsetSec();
//        switch (delete_config.expire_type)
//        {
//            case data::MaterialExpireType::DelayWeekCountDown:
//            {
//                uint32_t expire_time = 0;
//                uint32_t delay_week = 0;
//                if (delete_config.expire_time_param_vec.size() > 1)
//                {
//                    delay_week = delete_config.expire_time_param_vec[0];
//                    expire_time = delete_config.expire_time_param_vec[1];
//                    // 本周开始时间 + delay周时间
//                    uint32_t count_down_start_time = SAFE_ADD(TimeUtils::getWeekTime(now, time_offset), SAFE_MULTIPLY(604800, delay_week));
//                    SAFE_ADD_TO(expire_time, count_down_start_time);
//                }
//                delete_time_num_map_[expire_time] += count;
//#ifdef HK4E_DEBUG
//                LOG_DEBUG + "[MATERIAL DELETE] material:" + getItemId() + " add delete time:"
//                        + expire_time + " num:" + count + " total:" + delete_time_num_map_;
//#endif
//                break;
//            }
//            case data::MaterialExpireType::CountDown:
//            {
//                uint32_t expire_time = 0;
//                if (delete_config.expire_time_param_vec.size() > 0)
//                {
//                    expire_time = delete_config.expire_time_param_vec[0];
//                }
//                delete_time_num_map_[now + expire_time] += count;
//#ifdef HK4E_DEBUG
//                LOG_DEBUG + "[MATERIAL DELETE] material:" + getItemId() + " add delete time:"
//                        + now + expire_time + " num:" + count + " total:" + delete_time_num_map_;
//#endif
//                break;
//            }
//            default:
//                break;
//        }
//        return 0;
//    }

    public void subCount(int count)
    {
        int realSubCount = count;
        if (getItemCount() < count)
        {
            log.warn("sub material count overflow, item_id:" + getItemId()
                    + " cur_count:" + getItemCount() + " sub_count:" + count);
            realSubCount = getItemCount();
        }
        setCount(getItemCount() - realSubCount);
        // 处理删除, 优先去掉时间早的
//        uint32_t rest_use = realSubCount;
//        for (auto& [_, num] : delete_time_num_map_)
//        {
//            uint32_t u = num > rest_use ? rest_use : num;
//            num -= u;
//            rest_use -= u;
//        }

        // notify
        if (getItemCount() != 0)
        {
            notifyItemChange();
        }
    }
//
//    std::pair<uint32_t, umap<uint32_t, uint32_t>> getDeleteReturn(uint32_t now)
//    {
//        umap<uint32_t, uint32_t> return_item_map;
//        uint32_t delete_num = 0;
//    MaterialDeleteExcelConfig* delete_config = GET_TXT_CONFIG_MGR.material_config_mgr.findMaterialDeleteExcelConfig(getItemId());
//        if (delete_config == null)
//        {
//            return std::make_pair(delete_num, return_item_map);
//        }
//        switch (delete_config.expire_type)
//        {
//            case data::MaterialExpireType::DateTime:
//            {
//                if (delete_config.expire_time_param_vec.empty()
//                        || delete_config.expire_time_param_vec[0] > now)
//                {
//                    break;
//                }
//                delete_num = count_;
//                break;
//            }
//            case data::MaterialExpireType::DelayWeekCountDown:
//            case data::MaterialExpireType::CountDown:
//            {
//                for (auto [time, num] : delete_time_num_map_)
//                {
//                    if (time > now)
//                    {
//                        break;
//                    }
//                    delete_num += num;
//                }
//                break;
//            }
//            default:
//                break;
//        }
//        if (delete_num == 0)
//        {
//            return std::make_pair(delete_num, return_item_map);
//        }
//        // 确保不会溢出
//        delete_num = std::min(delete_num, count_);
//        // 走到这里说明需要回收
//        for (auto& [item_id, convert_ratio] : delete_config.return_item_map)
//        {
//            uint32_t return_num = CommonMiscs::multiplyFloatToUInt(delete_config.round_type, convert_ratio, delete_num);
//            if (return_num != 0)
//            {
//                return_item_map[item_id] += return_num;
//            }
//        }
//        return std::make_pair(delete_num, return_item_map);
//    }

//    uint32_t getNextDeleteTime()
//    {
//    MaterialDeleteExcelConfig* delete_config = GET_TXT_CONFIG_MGR.material_config_mgr.findMaterialDeleteExcelConfig(getItemId());
//        if (delete_config == null)
//        {
//            return std::numeric_limits<uint32_t>::max();
//        }
//        switch (delete_config.expire_type)
//        {
//            case data::MaterialExpireType::DateTime:
//            {
//                if (delete_config.expire_time_param_vec.empty())
//                {
//                    return std::numeric_limits<uint32_t>::max();
//                }
//                return delete_config.expire_time_param_vec[0];
//            }
//            case data::MaterialExpireType::DelayWeekCountDown:
//            case data::MaterialExpireType::CountDown:
//            {
//                for (auto [time, num] : delete_time_num_map_)
//                {
//                    if (num != 0)
//                    {
//                        return time;
//                    }
//                }
//            }
//            default:
//                return std::numeric_limits<uint32_t>::max();
//        }
//        return std::numeric_limits<uint32_t>::max();
//    }

//MaterialExcelConfig* getMaterialConfig()
//    {
//        MaterialExcelConfig* material_config = GET_TXT_CONFIG_MGR.material_config_mgr.findMaterialExcelConfig(getItemId());
//        if (material_config == null)
//        {
//            LOG_WARNING + "findMaterialExcelConfig failed, item_id:" + getItemId();
//            return null;
//        }
//
//        return material_config;
//    }

}
