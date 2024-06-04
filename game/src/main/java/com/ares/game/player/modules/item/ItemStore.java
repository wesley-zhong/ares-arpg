package com.ares.game.player.modules.item;

import cfg.item.ItemType;
import com.ares.common.excelconfig.ExcelConfigMgr;
import com.ares.core.exception.UnknownLogicException;
import com.ares.game.player.Player;
import com.game.protoGen.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Getter
@Slf4j
public abstract class ItemStore {
    private final Map<Long, Item> itemMap = new HashMap<>();     // 物品存储，guid . item
    private final Map<Integer, Set<Long>> itemIdMap = new HashMap<>(); // 物品索引，item_id . guid_set
    private final Player player;    // 玩家引用
    private int totalMaterialNum;   // 当前材料总数量
    private int totalWeaponNum;     // 当前武器总数量
//    private int totalReliquaryNum;  // 当前圣遗物总数量

    public ItemStore(final Player player) {
        this.player = player;
    }

    //  获取存储类型
    public abstract ProtoCommon.StoreType getStoreType();

    /*
     *  序列化、反序列化
     */
    // 反序列化
    public void fromBin(BinServer.ItemStoreBin bin)
    {
        for (BinServer.ItemBin itemBin : bin.getItemListList())
        {
            Item item = player.getItemModule().createItem(itemBin.getItemType(),
                    itemBin.getItemId(), itemBin.getGuid());

            item.fromBin(itemBin);

            emplaceItem(item, false);
        }
    }

    // 序列化
    public void toBin(BinServer.ItemStoreBin.Builder bin)
    {
        for (Item item :itemMap.values())
        {
            item.toBin(bin.addItemListBuilder());
        }
    }

    // 初始化
    public void init()
    {
        for (Item item :itemMap.values())
        {
            item.init(false);
            Set<Long> itemIdSet = itemIdMap.computeIfAbsent(item.getItemId(), k -> new HashSet<>());
            itemIdSet.add(item.getGuid());
        }

        // 如果有存档的道具数量超过了硬上限，需要报错查问题，但不阻塞登录
//        for (data::ItemType item_type : { data::ITEM_MATERIAL, data::ITEM_RELIQUARY, data::ITEM_WEAPON , data::ITEM_FURNITURE})
//        {
//            int cur_num = getTotalNumByItemType(item_type);
//        int hard_limit = GET_TXT_CONFIG_MGR.const_value_config_mgr.getItemHardLimitByItemType(item_type);
//            if (cur_num > hard_limit)
//            {
//                LOG_ERROR + "item count exceed hard limit! item_type:" + item_type + " cur_num:" + cur_num + " hard_limit:" + hard_limit + " uid:" + player.getUid();
//            }
//        }
    }

    // 查找道具
    public Item findItem(long guid)
    {
        return itemMap.get(guid);
    }

    public <T extends Item> T findItem(Class<T> clazz, long guid) {
        Item item = findItem(guid);
        if (clazz.isAssignableFrom(item.getClass())) {
            return (T) item;
        }
        return null;
    }

    // 查找材料
    public Material findMaterial(int itemId)
    {
        Set<Long> itemIdSet = itemIdMap.get(itemId);
        if (itemIdSet == null || itemIdSet.isEmpty()) {
            return null;
        }
        return findItem(Material.class, itemIdSet.iterator().next());
    }

    // 是否有道具
    public boolean hasItem(long guid)
    {
        return itemMap.containsKey(guid);
    }

    // 道具是否存在
    public boolean hasItemById(int itemId)
    {
        Set<Long> itemIdSet = itemIdMap.get(itemId);
        return itemIdSet != null && !itemIdSet.isEmpty();
    }

    // 获取道具数量
    public int getItemCount(int itemId)
    {
        Set<Long> itemIdSet = itemIdMap.get(itemId);
        if (itemIdSet == null || itemIdSet.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (long guid : itemIdSet)
        {
            Item item = findItem(guid);
            if (item == null)
            {
                log.warn("item is null, guid:" + guid);
                continue;
            }

            count += item.getItemCount();
        }

        return count;
    }

    // 获得材料数量
    public int getMaterialCount(int itemId)
    {
        // TODO: 单独处理材料
        return getItemCount(itemId);
    }

    // 获得材料类型材料的数量
//    int getMaterialCountWithMaterialType(int material_type)
//    {
//        int total_count = 0;
//    umap<int, set<int>>& material_item_ids_map = GET_TXT_CONFIG_MGR.material_config_mgr.material_item_ids_map;
//        auto iter = material_item_ids_map.find(material_type);
//        if (iter != material_item_ids_map.end())
//        {
//            for (int item_id : iter.second)
//            {
//                total_count += getItemCount(item_id);
//            }
//        }
//        LOG_DEBUG + "material_type:" + material_type + " total_count:" + total_count;
//        return total_count;
//    }
//
//    // 丢弃道具
//    public Item dropItem(long guid, int count)
//    {
//        Item dropItem = null;
//        Item item = findItem(guid);
//        if (item == null)
//        {
//            throw new FyLogicException(ProtoErrorCode.ErrCode.RET_ITEM_NOT_EXIST_VALUE, "findItem fails, uid:" + player.getUid() + " guid:" + guid);
//        }
//
//        int ret = item.checkConsume();
//        if (0 != ret)
//        {
//            throw new FyLogicException(ret, "Item cannot be consumed, uid:" + player.getUid()
//                    + " item_id:" + item.getItemId() + " guid:" + item.getGuid());
//        }
//
//        cfg.item.Item itemConfig = item.getItemConfig();
//        if (itemConfig == null)
//        {
//            throw new UnknownLogicException("getItemConfig fails, uid:" + player.getUid()
//                    + " item_id:" + item.getItemId() + " guid:" + item.getGuid());
//        }
//
//        if (!itemConfig.dropable)
//        {
//            throw new FyLogicException(ProtoErrorCode.ErrCode.RET_ITEM_NOT_DROPABLE_VALUE, "item is not dropable, uid:" + player.getUid()
//                    + " item_id:" + item.getItemId() + " guid:" + item.getGuid());
//        }
//
//        SubItemReason reason = new SubItemReason(ProtoInner.ActionReasonType.ACTION_REASON_DROP_ITEM);
//        switch (item.getItemType())
//        {
//            case ItemType.MATERIAL:
//            {
//                // 开关检查
////                if (GET_FEATURE_SWITCH_MGR.isItemSystemClosed())
////                {
////                    LOG_DEBUG + "[FEATURE_SWITCH] ItemSystem closed";
////                    return proto::RET_FEATURE_CLOSED;
////                }
////                if (GET_FEATURE_SWITCH_MGR.isItemClosed(item.getItemId()))
////                {
////                    LOG_DEBUG + "[FEATURE_SWITCH] ItemSystem itemId: " + item.getItemId() + " closed";
////                    return proto::RET_FEATURE_CLOSED;
////                }
//
//                Material material = (Material)item;
//                int itemId = material.getItemId();
//                int oldCount = material.getItemCount();
//                dropItem = material.split(player, count);
//                int cur_count = item.getItemCount();
//                if (cur_count == 0)
//                {
//                    delItem(item.getGuid(), true);
//                }
////                logAddMaterial(itemId, oldCount, cur_count, reason.reason_type, data::ITEM_LIMIT_NONE);
//            }
//            break;
////            case ITEM_RELIQUARY:
////            {
////                // 检查圣遗物开关
////                if (GET_FEATURE_SWITCH_MGR.isReliquarySystemClosed())
////                {
////                    LOG_DEBUG + "[FEATURE_SWITCH] ReliquarySystem closed";
////                    return proto::RET_FEATURE_CLOSED;
////                }
////                if (GET_FEATURE_SWITCH_MGR.isReliquaryClosed(item.getItemId()))
////                {
////                    LOG_DEBUG + "[FEATURE_SWITCH] ReliquarySystem item_id: " + item.getItemId() + " closed";
////                    return proto::RET_FEATURE_CLOSED;
////                }
////
////                Equip equip = dynamic_pointer_cast<Equip>(item);
////                if (equip == null)
////                {
////                    LOG_WARNING + "dynamic_pointer_cast failed, uid:" + player.getUid()
////                            + " item_id:" + item.getItemId() + " guid:" + item.getGuid();
////                    return proto::Retcode::RET_FAIL;
////                }
////
////                // 圣遗物被锁定
////                if (equip.getIsLocked())
////                {
////                    LOG_WARNING + "Reliquary is locked, uid:" + player.getUid()
////                            + " item_id:" + item.getItemId() + " guid:" + item.getGuid();
////                    return proto::Retcode::RET_EQUIP_IS_LOCKED;
////                }
////
////                if (equip.getOwner() != null)
////                {
////                    LOG_WARNING + "weapon is in use, uid:" + player.getUid()
////                            + " item_id:" + item.getItemId() + " guid:" + item.getGuid();
////                    return proto::Retcode::RET_EQUIP_WEARED_CANNOT_DROP;
////                }
////                drop_item = item;
////                delItem(item.getGuid());
////                logAddEquip(item, 0, reason.reason_type, data::ITEM_LIMIT_NONE);
////            }
////            break;
//            case ItemType.WEAPON:
//            {
//                // 检查武器开关
////                if (GET_FEATURE_SWITCH_MGR.isWeaponSystemClosed())
////                {
////                    LOG_DEBUG + "[FEATURE_SWITCH] WeaponSystem closed";
////                    return proto::RET_FEATURE_CLOSED;
////                }
////                if (GET_FEATURE_SWITCH_MGR.isWeaponClosed(item.getItemId()))
////                {
////                    LOG_DEBUG + "[FEATURE_SWITCH] WeaponSystem item_id: " + item.getItemId() + " closed";
////                    return proto::RET_FEATURE_CLOSED;
////                }
//
//                Equip equip = (Equip)item;
//                // 武器被锁定
//                if (equip.isLocked())
//                {
//                    throw new FyLogicException(ProtoErrorCode.ErrCode.RET_EQUIP_IS_LOCKED_VALUE, "Weapon is locked, uid:" + player.getUid()
//                            + " item_id:" + item.getItemId() + " guid:" + item.getGuid());
//                }
//
//                if (equip.getOwner() != null)
//                {
//                    throw new FyLogicException(ProtoErrorCode.ErrCode.RET_EQUIP_WEARED_CANNOT_DROP_VALUE, "weapon is in use, uid:" + player.getUid()
//                            + " item_id:" + item.getItemId() + " guid:" + item.getGuid());
//                }
//                dropItem = item;
//                delItem(item.getGuid(), true);
////                logAddEquip(item, 0, reason.reason_type, data::ITEM_LIMIT_NONE);
//            }
//            break;
//            default:
//            {
//                throw new UnknownLogicException("unsupported item for drop, uid:" + player.getUid()
//                        + " item_id:" + item.getItemId() + " item_type:" + item.getItemType()
//                        + " guid:" + item.getGuid());
//            }
//        }
//
//        return dropItem;
//    }

    // 通知所有道具给客户端
    public void notifyAllItem()
    {
        ProtoItem.PlayerStoreNtf.Builder builder = ProtoItem.PlayerStoreNtf.newBuilder();
        builder.setStoreType(getStoreType());

        for (Item item : itemMap.values()) {
            item.toClient(builder.addItemListBuilder());
        }

        player.sendMessage(ProtoMsgId.MsgId.PLAYER_STORE_NTF, builder.build());
    }

    // 通知最大重量
//    int notifyMaxWeight()
//    {
//        auto notify = MAKE_SHARED<proto::StoreWeightLimitNotify>();
//        notify.set_store_type(getStoreType());
//        notify.set_weight_limit(getMaxWeight());
//        notify.set_material_count_limit(GET_TXT_CONFIG_MGR.const_value_config_mgr.getMaterialItemLimit());
//        notify.set_weapon_count_limit(GET_TXT_CONFIG_MGR.const_value_config_mgr.getWeaponItemLimit());
//        notify.set_reliquary_count_limit(GET_TXT_CONFIG_MGR.const_value_config_mgr.getReliquaryItemLimit());
//        return player.sendMessage(CONST_MESSAGE_PTR(notify));
//    }

    // 通知所有数据给客户端
    public void notifyAllData()
    {
//        notifyMaxWeight();
        notifyAllItem();
    }

    // 清除所有道具
    public void clear()
    {
        itemMap.clear();
        itemIdMap.clear();
        totalMaterialNum = 0;
        totalWeaponNum = 0;
    }

    public Set<Long> getItemGuidSetByItemId(int item_id)
    {
        return itemIdMap.get(item_id);
    }

    // 强制增加武器，加角色时用，防止因为武器数量硬上限导致加角色失败
    public List<AddItemResult> forceAddEquipByAddAvatar(int equip_id, ActionReason reason)
    {
        ItemParam itemParam = new ItemParam();
        itemParam.itemId = equip_id;
        itemParam.count = 1;

        List<Item> itemList = player.getItemModule().createItem(itemParam);
        List<AddItemResult> addItemResults = new ArrayList<>();
        for (Item item : itemList)
        {
            int itemId = item.getItemId();
            cfg.item.Item itemConfig = item.getItemConfig();
            if (itemConfig == null)
            {
                throw new UnknownLogicException("getItemConfig fails, uid:" + player.getUid()
                        + " item_id:" + item.getItemId() + " guid:" + item.getGuid());
            }

            switch (itemConfig.type)
            {
                case ItemType.WEAPON_VALUE:
//                case ITEM_RELIQUARY:
                {
                    emplaceItem(item, true);
                    AddItemResult addResult = new AddItemResult();
                    addResult.guid = item.getGuid();
                    addResult.itemId = itemId;
                    addResult.addCount = 1;
                    addResult.curCount = item.getItemCount();
                    addItemResults.add(addResult);
//                    logAddEquip(item, 1, reason.reason_type, reason.limit_type);
                    break;
                }
                default:
                    log.error("invalid item_type:" + itemConfig.type + " guid:" + item.getGuid() + " uid:" + player.getUid());
                    break;
            }
        }

        return addItemResults;
    }

    // 增加道具检查
    public int checkAddItemByParam(ItemParam itemParam)
    {
        return checkAddItemByParamBatch(List.of(itemParam));
    }

    public int checkAddItemByParamBatch(List<ItemParam> itemParamList)
    {
        Map<Integer, ItemParam> itemParamMap = PlayerItemModule.mergeItemParam(itemParamList);
        // 检查材料是否超出上限
//        int material_item_limit = GET_TXT_CONFIG_MGR.const_value_config_mgr.getMaterialItemLimit();
//        int weapon_item_limit = GET_TXT_CONFIG_MGR.const_value_config_mgr.getWeaponItemLimit();
//        int reliquary_item_limit = GET_TXT_CONFIG_MGR.const_value_config_mgr.getReliquaryItemLimit();
//        int furniture_item_limit = GET_TXT_CONFIG_MGR.const_value_config_mgr.getFurnitureItemLimit();
//        int total_add_material_count = 0;
//        int total_add_weapon_count = 0;
//        int total_add_reliquary_count = 0;
//        int total_add_furniture_count = 0;
        for (ItemParam itemParam : itemParamMap.values())
        {
            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemParam.itemId);
            if (itemConfig == null)
            {
                log.warn("findItemConfig failed, uid:" + player.getUid() + " item_id:" + itemParam.itemId);
                return -1;
            }

            switch (itemConfig.type)
            {
                // 材料检查堆叠上限和格子数
                case ItemType.MATERIAL_VALUE:
                {
//                    MaterialExcelConfig* material_config = dynamic_cast<MaterialExcelConfig*>(item_config);
//                    if (material_config == null)
//                    {
//                        LOG_WARNING + "dynamic_pointer_cast failed, uid:" + player.getUid() + " item_id:" + item_param.item_id;
//                        return -1;
//                    }
//                    int cur_material_count = getItemCount(itemParam.itemId);
//                    if (cur_material_count + item_param.count > material_config.stack_limit)
//                    {
//                        LOG_DEBUG + "material exceed limit, uid:" + player.getUid() + " item_id:" + item_param.item_id + " cur_material_count:" + cur_material_count + " add_count:" + item_param.count;
//                        return proto::RET_ITEM_EXCEED_LIMIT;
//                    }
//                    if (0 == cur_material_count)
//                    {
//                        SAFE_ADD_TO(total_add_material_count, 1);
//                        if (total_material_num_ + 1 > material_item_limit)
//                        {
//                            LOG_DEBUG + "material item count exceed limit, uid:" + player.getUid() + " item_param:" + item_param + " total_material_num:" + total_material_num_ + " material_item_limit:" + material_item_limit;
//                            return proto::RET_ITEM_EXCEED_LIMIT;
//                        }
//                    }
                    break;
                }
                // 武器和圣遗物分别检查总数量
                case ItemType.WEAPON_VALUE:
//                    SAFE_ADD_TO(total_add_weapon_count, item_param.count);
//                    if (total_weapon_num_ + item_param.count > weapon_item_limit)
//                    {
//                        LOG_DEBUG + "weapon item count exceed limit, uid:" + player.getUid() + " item_param:" + item_param + " total_weapon_num:" + total_weapon_num_ + " weapon_item_limit:" + weapon_item_limit;
//                        return proto::RET_ITEM_EXCEED_LIMIT;
//                    }
                    break;
//                case ITEM_RELIQUARY:
//                    SAFE_ADD_TO(total_add_reliquary_count, item_param.count);
//                    if (total_reliquary_num_ + item_param.count > reliquary_item_limit)
//                    {
//                        LOG_DEBUG + "reliquary exceed limit, uid:" + player.getUid() + " item_param:" + item_param + " total_reliquary_num:" + total_reliquary_num_ + " reliquary_item_limit:" + reliquary_item_limit;
//                        return proto::RET_ITEM_EXCEED_LIMIT;
//                    }
//                    break;
                // 虚拟道具或未知类型不能加入背包
                default:
                    log.error("invalid item type, uid:" + player.getUid() + " item_type:" + itemConfig.type + " item_param:" + itemParam);
                    return -1;
            }
        }

        // 检查3种道具是否超过格子数上限
//        if (0 != total_add_material_count && SAFE_ADD(total_material_num_, total_add_material_count) > material_item_limit)
//        {
//            LOG_DEBUG + "total material count exceed limit, uid:" + player.getUid() + " total_add_material_count:" + total_add_material_count + " total_material_num:" + total_material_num_ + " material_item_limit:" + material_item_limit;
//            return proto::RET_ITEM_EXCEED_LIMIT;
//        }
//        if (0 != total_add_weapon_count && SAFE_ADD(total_weapon_num_, total_add_weapon_count) > weapon_item_limit)
//        {
//            LOG_DEBUG + "total weapon count exceed limit, uid:" + player.getUid() + " total_add_weapon_count:" + total_add_weapon_count + " total_weapon_num:" + total_weapon_num_ + " weapon_item_limit:" + weapon_item_limit;
//            return proto::RET_ITEM_EXCEED_LIMIT;
//        }
//        if (0 != total_add_reliquary_count && SAFE_ADD(total_reliquary_num_, total_add_reliquary_count) > reliquary_item_limit)
//        {
//            LOG_DEBUG + "total reliquary count exceed limit, uid:" + player.getUid() + " total_add_reliquary_count:" + total_add_reliquary_count + " total_reliquary_num:" + total_reliquary_num_ + " reliquary_item_limit:" + reliquary_item_limit;
//            return proto::RET_ITEM_EXCEED_LIMIT;
//        }
        return 0;
    }

    public int checkAddItem(Item item)
    {
        if (item == null)
        {
            throw new UnknownLogicException("item is null");
        }

        ItemParam itemParam = new ItemParam();
        itemParam.itemId = item.getItemId();
        itemParam.count = item.getItemCount();

        return checkAddItemByParam(itemParam);
    }

    public int checkAddItemBatch(List<Item> itemList)
    {
        List<ItemParam> itemParamList = new ArrayList<>();

        for (Item item : itemList)
        {
            if (item == null)
            {
                throw new UnknownLogicException("item is null");
            }

            if (hasItem(item.getGuid()))
            {
                throw new UnknownLogicException("item has been owned, uid:" + player.getUid() + " guid:" + item.getGuid());
            }

            ItemParam itemParam =  new ItemParam();
            itemParam.itemId = item.getItemId();
            itemParam.count = item.getItemCount();
            itemParamList.add(itemParam);
        }

        return checkAddItemByParamBatch(itemParamList);
    }

    public List<AddItemResult> addItemByParam(ItemParam itemParam, ActionReason reason)
    {
        return addItemByParamBatch(List.of(itemParam), reason);
    }

    public List<AddItemResult> addItemByParamBatch(List<ItemParam> itemParamList, ActionReason reason)
    {
        List<Item> itemList = new ArrayList<>();

        for (ItemParam itemParam : itemParamList)
        {
            itemList.addAll(player.getItemModule().createItem(itemParam));
        }

        return addItemBatch(itemList, reason);
    }

    public List<AddItemResult> addItem(Item item, ActionReason reason)
    {
        return addItemBatch(List.of(item), reason);
    }

    public List<AddItemResult> addItemBatch(List<Item> itemList, ActionReason reason)
    {
        List<AddItemResult> addItemResults = new ArrayList<>();
        for (Item item : itemList)
        {
            if (item == null)
            {
                throw new UnknownLogicException("item is null");
            }

            int itemId = item.getItemId();
            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemId);
            if (itemConfig == null)
            {
                throw new UnknownLogicException("findItemConfig failed, uid:" + player.getUid() + " item_id:" + itemId);
            }

            // 检查每种类型的道具的格子数硬上限
//            switch (item_config.item_type)
//            {
//                case ITEM_MATERIAL:
//                    if (total_material_num_ >= GET_TXT_CONFIG_MGR.const_value_config_mgr.getHardMaterialItemLimit())
//                    {
//                        LOG_ERROR + "material exceed hard limit! total_material_num:" + total_material_num_
//                                + " hard_material_item_limit:" + GET_TXT_CONFIG_MGR.const_value_config_mgr.getHardMaterialItemLimit()
//                                + " lost_item:" + *item
//                            + " uid:" + player.getUid();
//                        continue;
//                    }
//                    break;
//                case ITEM_WEAPON:
//                    if (total_weapon_num_ >= GET_TXT_CONFIG_MGR.const_value_config_mgr.getHardWeaponItemLimit())
//                    {
//                        LOG_ERROR + "weapon exceed hard limit! total_weapon_num:" + total_weapon_num_
//                                + " hard_weapon_item_limit:" + GET_TXT_CONFIG_MGR.const_value_config_mgr.getHardWeaponItemLimit()
//                                + " lost_item:" + *item
//                            + " uid:" + player.getUid();
//                        continue;
//                    }
//                    break;
//                case ITEM_RELIQUARY:
//                    if (total_reliquary_num_ >= GET_TXT_CONFIG_MGR.const_value_config_mgr.getHardReliquaryItemLimit())
//                    {
//                        LOG_ERROR + "reliquary exceed hard limit! total_reliquary_num:" + total_reliquary_num_
//                                + " hard_reliquary_item_limit:" + GET_TXT_CONFIG_MGR.const_value_config_mgr.getHardReliquaryItemLimit()
//                                + " lost_item:" + *item
//                            + " uid:" + player.getUid();
//                        continue;
//                    }
//                    break;
//                default:
//                    break;
//            }

            // 检查所有道具的格子数总硬上限
//            if (item_map_.size() >= ConstValueExcelConfigMgr::getMaxItemErrorCount())
//            {
//                LOG_ERROR + "item store map size too large, now_size:" + item_map_.size()
//                        + " limit:" + ConstValueExcelConfigMgr::getMaxItemErrorCount()
//                    + " item:" + *item
//                    + " uid:" + player.getUid();
//                if (item_map_.size() >= ConstValueExcelConfigMgr::getMaxItemCount())
//                {
//                    LOG_ERROR + "item store map size too large, now_size:" + item_map_.size()
//                            + " limit:" + ConstValueExcelConfigMgr::getMaxItemCount()
//                        + " lost_item:" + *item
//                        + " uid:" + player.getUid();
//                    continue;
//                }
//            }

            switch (itemConfig.type)
            {
                case ItemType.MATERIAL_VALUE:
                {
//                    StatLogUtils::ContextHolder holder(proto_log::PLAYER_ACTION_ADD_MATERIAL, player.getBasicModule().getNextTransNo());
                    Material material = findMaterial(itemId);
                    if (material == null)
                    {
//                        material = dynamic_pointer_cast<Material>(item);
//                        if(material == null)
//                        {
//                            LOG_ERROR + "item is not material, uid:" + player.getUid() + " itemId:"  + itemId
//                                    + " guid:" + item.getGuid();
//                        }
//                        else
//                        {
//                            material.tryAddDeleteCount(material.getItemCount());
//                        }
                        emplaceItem(item, true);
                        AddItemResult addResult = new AddItemResult();
                        addResult.guid = item.getGuid();
                        addResult.itemId = item.getItemId();
                        addResult.addCount = item.getItemCount();
                        addResult.curCount = item.getItemCount();
                        addItemResults.add(addResult);
//                        logAddMaterial(itemId, 0, item.getItemCount(), reason.reason_type, reason.limit_type);
                    }
                    else
                    {
                        int oldCount = material.getItemCount();
                        material.addCount(item.getItemCount());;

                        AddItemResult addResult = new AddItemResult();
                        addResult.guid = item.getGuid();
                        addResult.itemId = item.getItemId();
                        addResult.addCount = item.getItemCount();
                        addResult.curCount = material.getItemCount();
                        addItemResults.add(addResult);
//                        logAddMaterial(itemId, oldCount, material.getItemCount(), reason.reason_type, reason.limit_type);
                    }
                }
                break;
                case ItemType.WEAPON_VALUE:
//                case ITEM_RELIQUARY:
                {
                    emplaceItem(item, true);
                    AddItemResult addResult = new AddItemResult();
                    addResult.guid = item.getGuid();
                    addResult.itemId = item.getItemId();
                    addResult.addCount = item.getItemCount();
                    addResult.curCount = item.getItemCount();
                    addItemResults.add(addResult);
//                        logAddEquip(item, 1, reason.reason_type, reason.limit_type);
                }
                break;
                default:
                    log.error("invalid item_type:" + itemConfig.type + " guid:" + item.getGuid() + " uid:" + player.getUid());
                    break;
            }
        }

        return addItemResults;
    }

    public int checkSubItemByParam(ItemParam itemParam)
    {
        return checkSubItemByParamBatch(List.of(itemParam));
    }

    public int checkSubItemByParamBatch(List<ItemParam> itemParamList)
    {
//        int now = TimeUtils::getNow();
        Map<Integer, ItemParam> itemParamMap = PlayerItemModule.mergeItemParam(itemParamList);
        // 检查材料是否超出上限
        for (ItemParam itemParam : itemParamMap.values())
        {
            int itemId = itemParam.itemId;
            int count = itemParam.count;

            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemParam.itemId);
            if (itemConfig == null)
            {
                log.warn("findItemConfig failed, uid:" + player.getUid() + " item_id:" + itemParam.itemId);
                return -1;
            }

            switch (itemConfig.type)
            {
                case ItemType.MATERIAL_VALUE:
                {
                    // 开关检查
//                    if (GET_FEATURE_SWITCH_MGR.isItemSystemClosed())
//                    {
//                        LOG_DEBUG + "[FEATURE_SWITCH] ItemSystem closed";
//                        return proto::RET_FEATURE_CLOSED;
//                    }
//                    if (GET_FEATURE_SWITCH_MGR.isItemClosed(itemId))
//                    {
//                        LOG_DEBUG + "[FEATURE_SWITCH] ItemSystem itemId: " + itemId + " closed";
//                        return proto::RET_FEATURE_CLOSED;
//                    }

                    Material material = findMaterial(itemId);
                    if (material == null)
                    {
                        log.debug("findMaterial failed, uid:" + player.getUid() + " itemId:" + itemId);
                        return ProtoErrorCode.ErrCode.RET_ITEM_COUNT_NOT_ENOUGH_VALUE;
                    }

                    if (0 != material.checkConsume())
                    {
                        log.error("checkConsume failed, uid:" + player.getUid() + " itemId:" + itemId);
                        return -1;
                    }
//                    if (now >= material.getNextDeleteTime())
//                    {
//                        LOG_DEBUG + "material is expired, uid:" + player.getUid() + " itemId:" + itemId;
//                        return proto::RET_ITEM_IS_EXPIRED;
//                    }
                    if (material.getItemCount() < count)
                    {
                        log.debug("item not enough, uid:" + player.getUid() + " itemId:" + itemId
                                + " count:" + material.getItemCount());
                        return ProtoErrorCode.ErrCode.RET_ITEM_COUNT_NOT_ENOUGH_VALUE;
                    }
                }
                break;
                case ItemType.WEAPON_VALUE:
//                case ITEM_RELIQUARY:
                {
                    log.warn("unsupported operation to delete equip with itemId: uid:" + player.getUid()
                            + " itemId:" + itemId);
                    return -1;
                }
                default:
                {
                    log.error("need to be implemented");
                    return -1;
                }
            }
        }

        return 0;
    }

    public List<SubItemResult> subItemByParam(ItemParam itemParam, SubItemReason reason)
    {
        return subItemByParamBatch(List.of(itemParam), reason);
    }

    public List<SubItemResult> subItemByParamBatch(List<ItemParam> itemParamList, SubItemReason reason)
    {
        List<SubItemResult> subItemResults = new ArrayList<>();

        for (ItemParam itemParam : itemParamList)
        {
            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemParam.itemId);
            if (itemConfig == null)
            {
                log.warn("findItemConfig failed, uid:" + player.getUid() + " item_id:" + itemParam.itemId);
                continue;
            }

            switch (itemConfig.type)
            {
                case ItemType.MATERIAL_VALUE:
                {
//                    StatLogUtils::ContextHolder holder(proto_log::PLAYER_ACTION_ADD_MATERIAL, player.getBasicModule().getNextTransNo());
                    Material material = findMaterial(itemParam.itemId);
                    if (material == null)
                    {
                        log.error("findMaterial failed, uid:" + player.getUid() + " item_id:" + itemParam.itemId);
                        continue;
                    }

                    int old_count = material.getItemCount();
                    material.subCount(itemParam.count);

                    SubItemResult subResult = new SubItemResult();
                    subResult.guid = material.getGuid();
                    subResult.itemId = material.getItemId();
                    subResult.subCount = itemParam.count;
                    subResult.curCount = material.getItemCount();

//                    logAddMaterial(itemParam.item_id, old_count, material.getItemCount(), reason.reason_type, data::ITEM_LIMIT_NONE);
                    if (material.getItemCount() == 0)
                    {
                        delItem(material.getGuid(), true);
                    }
                    subItemResults.add(subResult);
                }
                break;
                case ItemType.WEAPON_VALUE:
//                case ITEM_RELIQUARY:
                {
                    log.warn("unsupported operation to delete equip with item_id: uid:" + player.getUid()
                            + " item_id:" + itemParam.itemId);
                    continue;
                }
                default:
                {
                    log.error("need to be implemented");
                    continue;
                }
            }
        }

        return subItemResults;
    }

    public int checkSubItem(long guid)
    {
        return checkSubItemBatch(List.of(guid));
    }

    public int checkSubItemBatch(List<Long> longList)
    {
        for (Long guid : longList)
        {
            Item item = findItem(guid);
            if (item == null)
            {
                log.debug("findItem failed, uid:" + player.getUid() + " guid:" + guid);
                return -1;
            }

            switch (item.getItemType())
            {
                case ItemType.WEAPON:
                {
                    // 检查武器开关
//                    if (GET_FEATURE_SWITCH_MGR.isWeaponSystemClosed())
//                    {
//                        LOG_DEBUG + "[FEATURE_SWITCH] WeaponSystem closed";
//                        return proto::RET_FEATURE_CLOSED;
//                    }
//                    if (GET_FEATURE_SWITCH_MGR.isWeaponClosed(item.getItemId()))
//                    {
//                        LOG_DEBUG + "[FEATURE_SWITCH] WeaponSystem item_id: " + item.getItemId() + " closed";
//                        return proto::RET_FEATURE_CLOSED;
//                    }

                    Equip equip = (Equip)item;

                    // 武器被锁定
                    if (equip.isLocked())
                    {
                        log.debug("Weapon is locked, uid:" + player.getUid() + " guid:" + equip.getGuid());
                        return ProtoErrorCode.ErrCode.RET_EQUIP_IS_LOCKED_VALUE;
                    }

                    if (0 != item.checkConsume())
                    {
                        log.debug("item.checkConsume failed, uid:" + player.getUid() + " guid:" + guid
                                + " item_id:" + item.getItemId());
                        return -1;
                    }
                }
                break;
//                case ITEM_RELIQUARY:
//                {
//                    // 检查圣遗物开关
//                    if (GET_FEATURE_SWITCH_MGR.isReliquarySystemClosed())
//                    {
//                        LOG_DEBUG + "[FEATURE_SWITCH] ReliquarySystem closed";
//                        return proto::RET_FEATURE_CLOSED;
//                    }
//                    if (GET_FEATURE_SWITCH_MGR.isReliquaryClosed(item.getItemId()))
//                    {
//                        LOG_DEBUG + "[FEATURE_SWITCH] ReliquarySystem item_id: " + item.getItemId() + " closed";
//                        return proto::RET_FEATURE_CLOSED;
//                    }
//
//                    Equip equip = dynamic_pointer_cast<Equip>(item);
//                    if (equip == null)
//                    {
//                        LOG_WARNING + "dynamic_pointer_cast failed, uid:" + player.getUid()
//                                + " item_id:" + item.getItemId() + " guid:" + item.getGuid();
//                        return proto::Retcode::RET_FAIL;
//                    }
//
//                    // 圣遗物被锁定
//                    if (equip.getIsLocked())
//                    {
//                        LOG_DEBUG + "Reliquary is locked, uid:" + player.getUid() + " guid:" + equip.getGuid();
//                        return proto::RET_EQUIP_IS_LOCKED;
//                    }
//
//                    if (0 != item.checkConsume())
//                    {
//                        LOG_DEBUG + "item.checkConsume failed, uid:" + player.getUid() + " guid:" + guid
//                                + " item_id:" + item.getItemId();
//                        return -1;
//                    }
//                }
//                break;
                default:
                {
                    log.debug("unsupported operation del material using guid, uid:" + player.getUid()
                            + " guid:" + guid + " item_id:" + item.getItemId());
                    return -1;
                }
            }
        }

        return 0;
    }

    public List<SubItemResult> subItem(long guid, SubItemReason reason)
    {
        return subItemBatch(List.of(guid), reason);
    }

    List<SubItemResult> subItemBatch(List<Long> longList, SubItemReason reason)
    {
        List<SubItemResult> subItemResults = new ArrayList<>();
        for (Long guid : longList)
        {
            Item item = findItem(guid);
            if (item == null)
            {
                log.warn("findItem failed, uid:" + player.getUid() + " guid:" + guid);
                continue;
            }

            // *INDENT-OFF* 关闭astyle格式化
            switch (item.getItemType())
            {
                case ItemType.WEAPON:
//                case ITEM_RELIQUARY:
                {
                    SubItemResult sub_result = new SubItemResult();
                    sub_result.guid = item.getGuid();
                    sub_result.itemId = item.getItemId();
                    sub_result.subCount = 1;
                    sub_result.curCount = 0;
                    subItemResults.add(sub_result);
                    delItem(item.getGuid(), true);
//                    logAddEquip(item, 0, reason.reason_type, data::ITEM_LIMIT_NONE);
                    break;
                }
                default:
                    log.warn("unsupported operation del material using guid, uid:" + player.getUid()
                            + " guid:" + guid + " item_id:" + item.getItemId());
                    break;
            }
            // *INDENT-ON* 开启astyle格式化
        }

        return subItemResults;
    }

    public void delItem(long guid, boolean notify)
    {
        Item item = findItem(guid);
        if (item == null)
        {
            log.error("trying to delete not exist item, uid:" + player.getUid() + " guid:" + guid);
            return;
        }

        item.resetItemOwner();
        itemMap.remove(guid);
        Set<Long> guidSet = itemIdMap.get(item.getItemId());
        if (guidSet != null) {
            guidSet.remove(guid);
            if (guidSet.isEmpty()) {
                itemIdMap.remove(item.getItemId());
            }
        }

        ItemType item_type = item.getItemType();
        switch (item_type)
        {
            case ItemType.MATERIAL:
                --totalMaterialNum;
                break;
            case ItemType.WEAPON:
                --totalWeaponNum;
                break;
//            case ITEM_RELIQUARY:
//                --total_reliquary_num_;
//                break;
            default:
                log.warn("invalid item_type:" + item_type + " guid:" + item.getGuid() + " uid:" + player.getUid());
                break;
        }

        if (notify)
        {
            ProtoItem.StoreItemDelNtf.Builder builder = ProtoItem.StoreItemDelNtf.newBuilder();
            builder.setStoreType(getStoreType());
            builder.addGuidList(guid);
            player.sendMessage(ProtoMsgId.MsgId.STORE_ITEM_DEL_NTF, builder.build());
        }
    }

    private void emplaceItem(Item item, boolean notify)
    {
        if (item == null)
        {
            throw new UnknownLogicException("emplaceItem failed pointer null, uid:" + player.getUid());
        }

        ItemType itemType = item.getItemType();
        if (itemMap.putIfAbsent(item.getGuid(), item) != null)
        {
            throw new UnknownLogicException("duplicate item, uid:" + player.getUid() + " guid:" + item.getGuid());
        }

        item.setItemOwner(player);
        itemIdMap.computeIfAbsent(item.getItemId(), k -> new HashSet<>()).add(item.getGuid());

        switch (itemType)
        {
            case ItemType.MATERIAL:
                ++totalMaterialNum;
                break;
            case ItemType.WEAPON:
                ++totalWeaponNum;
                break;
//            case ITEM_RELIQUARY:
//                ++total_reliquary_num_;
//                break;
            default:
                log.warn("invalid itemType:" + itemType + " guid:" + item.getGuid() + " uid:" + player.getUid());
                break;
        }

        if (notify)
        {
            item.notifyItemChange();
        }
    }

    // 根据道具类型获取当前相应道具的总数量
//    int getTotalNumByItemType(data::ItemType item_type)
//    {
//        switch (item_type)
//        {
//            case ITEM_MATERIAL:
//                return total_material_num_;
//            case ITEM_WEAPON:
//                return total_weapon_num_;
//            case ITEM_RELIQUARY:
//                return total_reliquary_num_;
//            case ITEM_FURNITURE:
//                return total_furniture_num_;
//            default:
//                break;
//        }
//        return 0;
//    }

    // 记录材料产消日志
//    void logAddMaterial(int material_id, int old_count, int new_count, proto::ActionReasonType reason_type, data::ItemLimitType limit_type)
//    {
//        std::shared<proto_log::PlayerLogBodyAddMaterial> add_material_log = MAKE_SHARED<proto_log::PlayerLogBodyAddMaterial>();
//        add_material_log.set_material_id(material_id);
//    data::MaterialExcelConfig* material_excel_config = GET_TXT_CONFIG_MGR.material_config_mgr.findMaterialExcelConfig(material_id);
//        if (null != material_excel_config)
//        {
//            add_material_log.set_material_type(material_excel_config.material_type);
//        }
//        int64_t add_num = static_cast<int64_t>(new_count) - static_cast<int64_t>(old_count);
//        add_material_log.set_add_num(add_num);
//        add_material_log.set_left_num(new_count);
//        add_material_log.set_reason(reason_type);
//
//        std::shared<proto_log::PlayerLogBodyExtAddMaterial> add_material_ext_log = MAKE_SHARED<proto_log::PlayerLogBodyExtAddMaterial>();
//        add_material_ext_log.set_reason_type(reason_type);
//        add_material_ext_log.set_item_limit_type(limit_type);
//
//        player.printStatLog(add_material_log, add_material_ext_log);
//    }

    // 记录武器或圣遗物增减
//    void logAddEquip(Item item, int num_after, proto::ActionReasonType reason_type, data::ItemLimitType limit_type)
//    {
//        if (null == item)
//        {
//            LOG_WARNING + "item is null";
//            return;
//        }
//        switch (item.getItemType())
//        {
//            case ITEM_WEAPON:
//            {
//                Weapon weapon = dynamic_pointer_cast<Weapon>(item);
//                if (null == weapon)
//                {
//                    LOG_WARNING + "weapon is null";
//                    return;
//                }
//                StatLogUtils::ContextHolder holder(proto_log::PLAYER_ACTION_WEAPON_ADD, player.getBasicModule().getNextTransNo());
//                std::shared<proto_log::PlayerLogBodyWeaponAdd> weapon_add_log = MAKE_SHARED<proto_log::PlayerLogBodyWeaponAdd>();
//                weapon.getWeaponLog(*weapon_add_log.mutable_weapon());
//                weapon_add_log.set_weapon_add(num_after == 0 ? -1 : 1);
//                weapon_add_log.set_weapon_num(num_after);
//                weapon_add_log.set_reason_type(reason_type);
//                weapon_add_log.set_item_limit_type(limit_type);
//                player.printStatLog(weapon_add_log);
//                break;
//            }
//            case ITEM_RELIQUARY:
//            {
//                Reliquary reliquary = dynamic_pointer_cast<Reliquary>(item);
//                if (null == reliquary)
//                {
//                    LOG_WARNING + "reliquary is null";
//                    return;
//                }
//                StatLogUtils::ContextHolder holder(proto_log::PLAYER_ACTION_RELIC_ADD, player.getBasicModule().getNextTransNo());
//                std::shared<proto_log::PlayerLogBodyRelicAdd> relic_add_log = MAKE_SHARED<proto_log::PlayerLogBodyRelicAdd>();
//                reliquary.getRelicLog(*relic_add_log.mutable_relic());
//                relic_add_log.set_relic_add(num_after == 0 ? -1 : 1);
//                relic_add_log.set_relic_num(num_after);
//                relic_add_log.set_reason_type(reason_type);
//                relic_add_log.set_item_limit_type(limit_type);
//                player.printStatLog(relic_add_log);
//                break;
//            }
//            default:
//                break;
//        }
//    }
}
