package com.ares.game.player.modules.item;

import cfg.item.ItemType;
import com.ares.common.excelconfig.ExcelConfigMgr;
import com.ares.core.exception.UnknownLogicException;
import com.ares.game.player.Player;
import com.ares.game.player.PlayerModule;
import com.ares.game.player.modules.basic.PlayerBasicModule;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerItemModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerItemModule.class);

    private PackItemStore packItemStore = new PackItemStore(getPlayer());
    private int diamondCoin = 0;

    public PlayerItemModule(Player player) {
        super(ProtoInner.GameModuleId.GMI_PlayerItem, player);
    }

    @Override
    public void fromBin(BinServer.PlayerDataBin bin) {
        packItemStore.fromBin(bin.getItemBin().getPackStore());
    }

    @Override
    public void toBin(BinServer.PlayerDataBin.Builder bin) {
        packItemStore.toBin(bin.getItemBinBuilder().getPackStoreBuilder());
    }

    @Override
    public void init() {
        packItemStore.init();
    }

    @Override
    public void notifyAllData() {
        packItemStore.notifyAllData();
    }

    public ProtoItem.UseItemRes useItem(ProtoItem.UseItemReq req) {
        ProtoItem.UseItemRes.Builder builder = ProtoItem.UseItemRes.newBuilder();
        return builder.build();
    }

    public Item createItem(int itemType, int itemId, long guid)
    {
        Item item;

        switch (itemType)
        {
            case ItemType.VIRTUAL_VALUE:
            {
                throw new UnknownLogicException("can't create virtual item");
//                item = MAKE_SHARED<Material>(itemType, itemId);
            }
//            break;
            case ItemType.MATERIAL_VALUE:
            {
                item = new Material(itemId);
            }
            break;
            case ItemType.WEAPON_VALUE:
            {
                item = new Weapon(itemId);
            }
            break;
//            case ITEM_RELIQUARY:
//            {
//                item = MAKE_SHARED<Reliquary>(itemType, itemId);
//            }
//            break;
            default:
            {
                throw new UnknownLogicException("unknown item type:" + itemType + " itemId:" + itemId);
            }
        }

        if (item == null)
        {
            throw new UnknownLogicException("createItem failed, itemType:" + itemType + " itemId:" + itemId);
        }

        if (guid == 0)
        {
            guid = player.getBasicModule().genGuid(PlayerBasicModule.GuidType.GUID_ITEM);
        }
        if (guid == 0)
        {
            throw new UnknownLogicException("genGuid failed, itemType:" + itemType + " itemId:" + itemId);
        }
        item.setGuid(guid);

        return item;
    }

    // 创建合法的道具，对创建出的实例进行初始化操作
    public List<Item> createItem(ItemParam itemParam)
    {
        return createItemBatch(List.of(itemParam));
    }

    // 要么全部成功，要么全部失败
    public List<Item> createItemBatch(List<ItemParam> itemParamList)
    {
        List<Item> itemList = new ArrayList<>();

        for (ItemParam itemParam : itemParamList)
        {
            int itemId = itemParam.itemId;
            int count = itemParam.count;

            if (count == 0)
            {
                log.warn("createItem failed, count is 0, itemId:" + itemId);
                itemList.clear();
                return itemList;
            }

            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemId);
            if (itemConfig == null)
            {
                log.warn("findItemBaseConfig failed, itemId:" + itemId);
                itemList.clear();
                return itemList;
            }
            int itemType = itemConfig.type;

            switch (itemType)
            {
//                case ITEM_VIRTUAL:
                case ItemType.MATERIAL_VALUE:
                {
                    Item item = createItem(itemType, itemId, 0);
                    if (item == null)
                    {
                        log.warn("createItem failed, itemType:" + itemType + " itemId:" + itemId
                                + "count:" + count);
                        itemList.clear();
                        return itemList;
                    }

                    Material material = (Material)item;

                    material.setCount(count);

                    try {
                        item.init(true);
                    }
                    catch (Exception e) {
                        log.warn("item init failed, itemType:" + itemType + " itemId:" + itemId
                                + " count:" + count, e);
                        itemList.clear();
                        return itemList;
                    }

                    itemList.add(item);
                }
                break;
//                case ITEM_RELIQUARY:
                case ItemType.WEAPON_VALUE:
                {
//                    // 防止创建太多Item把服务器卡死
//                    int hard_limit = GET_TXT_CONFIG_MGR.const_value_config_mgr.getItemHardLimitByItemType(itemConfig.item_type);
//                    if (count > hard_limit)
//                    {
//                        LOG_ERROR + "cannot create too many equips, itemType:" + itemType + " count:" + count + " hard_limit:" + hard_limit
//                                + " going to lost " + count - hard_limit + " equips. uid:" + player.getUid();
//                        count = hard_limit;
//                    }
                    for (int idx = 0; idx < count; idx++)
                    {
                        Item item = createItem(itemType, itemId, 0);
                        if (item == null)
                        {
                            log.warn("createItem failed, itemType:" + itemType + " itemId:" + itemId
                                    + "count:" + count);
                            itemList.clear();
                            return itemList;
                        }

                        Material material = (Material)item;

                        material.setCount(count);

                        try {
                            item.init(true);
                        }
                        catch (Exception e) {
                            log.warn("item init failed, itemType:" + itemType + " itemId:" + itemId
                                    + " count:" + count, e);
                            itemList.clear();
                            return itemList;
                        }

                        itemList.add(item);
                    }
                }
                break;
                default:
                {
                    log.warn("createItem failed, itemType:" + itemType + " itemId:" + itemId);
                    itemList.clear();
                    return itemList;
                }
            }
        }

        return itemList;
    }

    public ProtoCommon.StoreType getItemStoreType(long guid) {
        if (packItemStore.findItem(guid) != null)
        {
            return ProtoCommon.StoreType.STORE_PACK;
        }
        return ProtoCommon.StoreType.STORE_NONE;
    }

    public static Map<Integer, ItemParam> mergeItemParam(List<ItemParam> itemParamList)
    {
        Map<Integer, ItemParam> mergedParams = new HashMap<>();
        for (ItemParam itemParam : itemParamList)
        {
            ItemParam mergedParam = mergedParams.computeIfAbsent(itemParam.itemId, k ->new ItemParam());
            mergedParam.itemId = itemParam.itemId;
            mergedParam.count += itemParam.count;
        }

        return mergedParams;
    }


    public Item findItemInPack(long guid)
    {
        return packItemStore.findItem(guid);
    }

    public Material findMaterial(int itemId)
    {
        return packItemStore.findMaterial(itemId);
    }

    public int getItemCount(int itemId)
    {
        return packItemStore.getItemCount(itemId);
    }

    public int checkAddItemByParam(ItemParam itemParam, ActionReason reason)
    {
        return checkAddItemByParamBatch(List.of(itemParam), reason);
    }

    public int checkAddVirtualItem(ItemParam itemParam)
    {
        switch (itemParam.itemId)
        {
            case ProtoInner.VirtualItemId.VIRTUAL_ITEM_DIAMOND_COIN_VALUE:
            {
//                int ret = checkAddHcoin(itemParam.count);
//                if (0 != ret)
//                {
//                    LOG_WARNING + "hcoin add failed, cur_hcoin:" + getHcoin() + " count:" + itemParam.count;
//                    return ret;
//                }
            }
            break;
            default:
            {
                log.warn("unknown virtual item, itemId:" + itemParam.itemId);
                return -1;
            }
        }
        return 0;
    }

    public List<AddItemResult> addVirtualItem(int itemId, int itemCount, ActionReason reason)
    {
        List<AddItemResult> addItemResults = new ArrayList<>();
        switch (itemId)
        {
            case ProtoInner.VirtualItemId.VIRTUAL_ITEM_DIAMOND_COIN_VALUE:
            {
//                if (0 != addHcoin(itemCount, reason))
//                {
//                    LOG_ERROR + "addHcoin failed, count:" + itemCount;
//                }
//                AddItemResult add_result =
//                        {
//                                guid: 0,
//                    itemId: proto::ITEM_VIRTUAL_HCOIN,
//                    add_count: itemCount,
//                    cur_count: getHcoin()
//            };
//                temp_result_vec.emplace_back(std::move(add_result));
            }
            break;
            default:
                log.error("unknown virtual item, itemId:" + itemId);
                break;
        }
        return addItemResults;
    }

    public int checkAddItemByParamBatch(List<ItemParam> itemParamList, ActionReason reason)
    {
        if (itemParamList.isEmpty())
        {
            log.warn("itemParamList is empty");
            return -1;
        }

        int ret = 0;
//        ret = checkOutputLimitBeforeCheckAddItemBatch(itemParamList, reason);
//        if (0 != ret)
//        {
//            LOG_WARNING + "checkOutputLimitBeforeCheckAddItemBatch fail, itemParamList:" + itemParamList + " player:" + player;
//            return ret;
//        }

        List<ItemParam> itemParamListNoVirtual = new ArrayList<>();

        for (ItemParam itemParam : itemParamList)
        {
            cfg.item.Item ItemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemParam.itemId);
            if (ItemConfig == null)
            {
                log.warn("findItemConfig failed, uid:" + player.getUid() + " itemId:" + itemParam.itemId);
                return -1;
            }
            if (ItemConfig.type == ItemType.VIRTUAL_VALUE)
            {
                ret = checkAddVirtualItem(itemParam);
                if (0 != ret)
                {
                    return ret;
                }
                continue;
            }
            itemParamListNoVirtual.add(itemParam);
        }

        return packItemStore.checkAddItemByParamBatch(itemParamListNoVirtual);
    }

    public void addItemByParam(ItemParam itemParam, ActionReason reason)
    {
        addItemByParamBatch(List.of(itemParam), reason);
    }

    public void addItemByParamBatchWithStackLimit(List<ItemParam> itemParamList, ActionReason reason)
    {
        itemParamList.removeIf(itemParam -> {
            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemParam.itemId);
            if (itemConfig == null) {
                log.warn("not find  material_config,item id:" + itemParam.itemId);
                return true;
            }
            int curCount = getPackMaterialCount(itemParam.itemId);
            if (curCount + itemParam.count > itemConfig.stackLimit)
            {
                // 当前数量已经达到堆叠上限 则不添加
                if (curCount >= itemConfig.stackLimit)
                {
                    log.debug("cur >= stackLimit remove item:" + itemParam.itemId + "," + itemParam.count);
                    itemParam.count = 0;
                    return true;
                }
                else
                {
                    log.debug("stackLimit item:" + itemParam.itemId + " " + itemParam.count + " force to " + (itemConfig.stackLimit - curCount));
                    itemParam.count = itemConfig.stackLimit - curCount;
                    return false;
                }
            }
            return false;
        });
        if (itemParamList.isEmpty())
        {
            return;
        }
        addItemByParamBatch(itemParamList, reason);
    }

    public List<AddItemResult> addItemByParamBatch(List<ItemParam> itemParamList, ActionReason reason)
    {
        List<AddItemResult> addItemResults = new ArrayList<>();
//        vector<ItemParam> curItemParamList = checkOutputLimitAndModifyItemParams(itemParamList, reason);
//        curItemParamList = autoUseItemBatch(curItemParamList, temp_result_vec, reason);
        List<ItemParam> curItemParamList = itemParamList;

        List<ItemParam> itemParamListNoVirtual = new ArrayList<>();
        for (ItemParam itemParam : curItemParamList)
        {
            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemParam.itemId);
            if (itemConfig == null)
            {
                log.error("findItemConfig failed, uid:" + player.getUid() + " itemId:" + itemParam.itemId);
                continue;
            }
            // 虚拟道具增加不会失败
            if (itemConfig.type == ItemType.VIRTUAL_VALUE)
            {
                addItemResults.addAll(addVirtualItem(itemParam.itemId, itemParam.count, reason));
            }
            else
            {
                itemParamListNoVirtual.add(itemParam);
            }
        }

        addItemResults.addAll(packItemStore.addItemByParamBatch(itemParamListNoVirtual, reason));
//        // 检查与处理删除
//        vector<int> id_vec;
//        for (auto result : result_vec)
//        {
//            id_vec.emplace_back(result.itemId);
//        }
//        checkMaterialDelete(TimeUtils::getNow(), id_vec);
//
//        result_vec.insert(result_vec.end(), temp_result_vec.begin(), temp_result_vec.end());

        // 通过result，记录信息在comp上
//        processByAddItemResultVec(result_vec);

        return addItemResults;
    }

    public int checkAddItem(Item item, ActionReason reason)
    {
        return checkAddItemBatch(List.of(item), reason);
    }

    public int checkAddItemBatch(List<Item> itemList, ActionReason reason)
    {
        if (itemList.isEmpty())
        {
            log.warn("itemList is empty, uid:" + player.getUid());
            return -1;
        }

        int ret = 0;
//        ret = checkOutputLimitBeforeCheckAddItemBatch(itemList, reason);
//        if (0 != ret)
//        {
//            LOG_WARNING + "checkOutputLimitBeforeCheckAddItemBatch fail, itemList:" + getItemParamVec(itemList) + " player:" + player;
//            return ret;
//        }

        List<Item> itemListNoVirtual = new ArrayList<>();

        for (Item item : itemList)
        {
            if (packItemStore.findItem(item.getGuid()) != null)
            {
                log.warn("item already exist, uid:" + player.getUid()
                        + " itemId:" + item.getItemId() + " guid:" + item.getGuid());
                return -1;
            }
            if (item.getItemType() == ItemType.VIRTUAL)
            {
                ItemParam param = new ItemParam();
                param.itemId = item.getItemId();
                param.count = item.getItemCount();
                ret = checkAddVirtualItem(param);
                if (0 != ret)
                {
                    return ret;
                }
                continue;
            }

            itemListNoVirtual.add(item);
        }

        return packItemStore.checkAddItemBatch(itemListNoVirtual);
    }

    public List<AddItemResult> addItem(Item item, ActionReason reason)
    {
        return addItemBatch(List.of(item), reason);
    }

    public List<AddItemResult> addItemBatch(List<Item> itemList, ActionReason reason)
    {
//        vector<Item> cur_item_vec = checkOutputLimitAndModifyItemParams(itemList, reason);
//        vector<AddItemResult> temp_result_vec;
//        cur_item_vec = autoUseItemBatch(cur_item_vec, temp_result_vec, reason);
        List<AddItemResult> addItemResults = new ArrayList<>();
        List<Item> curItemList = itemList;

        List<Item> itemListNoVirtual = new ArrayList<>();
        for (Item item : curItemList)
        {
            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(item.getItemId());
            if (itemConfig == null)
            {
                log.error("findItemConfig failed, uid:" + player.getUid() + " itemId:" + item.getItemId());
                continue;
            }
            // 虚拟道具增加不会失败
            if (itemConfig.type == ItemType.VIRTUAL_VALUE)
            {
                addItemResults.addAll(addVirtualItem(item.getItemId(), item.getItemCount(), reason));
            }
            else
            {
                itemListNoVirtual.add(item);
            }
        }

        addItemResults.addAll(packItemStore.addItemBatch(itemListNoVirtual, reason));
        // 检查与处理删除
//        vector<int> id_vec;
//        for (auto result : result_vec)
//        {
//            id_vec.emplace_back(result.itemId);
//        }
//        checkMaterialDelete(TimeUtils::getNow(), id_vec);
//        result_vec.insert(result_vec.end(), temp_result_vec.begin(), temp_result_vec.end());

//        // 通过result，记录信息在comp上
//        processByAddItemResultVec(result_vec);
//
//        // 触发任务和探索事件
//        triggerItemAddEvent(result_vec, true, reason);

        return addItemResults;
    }

    public int checkSubItemByParam(ItemParam itemParam)
    {
        return checkSubItemByParamBatch(List.of(itemParam));
    }

    public int checkSubItemByParamBatch(List<ItemParam> itemParamList)
    {
        if (itemParamList.isEmpty())
        {
            log.error("itemParamList is empty, player:" + player);
            return -1;
        }
        List<ItemParam> itemParamListNoVirtual = new ArrayList<>();
        for (ItemParam itemParam : itemParamList)
        {
            if (0 == itemParam.count)
            {
                log.error("item_count is 0, itemId:" + itemParam.itemId + " player:" + player);
                return -1;
            }
            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemParam.itemId);
            if (itemConfig == null)
            {
                log.warn("findItemConfig failed, uid:" + player.getUid() + " itemId:" + itemParam.itemId);
                return -1;
            }
            if (itemConfig.type == ItemType.VIRTUAL_VALUE)
            {
                switch (itemParam.itemId)
                {
                    case ProtoInner.VirtualItemId.VIRTUAL_ITEM_DIAMOND_COIN_VALUE:
                    {
//                        int ret = checkSubMcoin(itemParam.count);
//                        if (0 != ret)
//                        {
//                            LOG_DEBUG + "checkSubMcoin fails, cur_mcoin:" + getMcoin() + " count:" + itemParam.count + " uid:" + player.getUid();
//                            return ret;
//                        }
//                        break;
                    }
                    default:
                    {
                        log.warn("unknown virtual item, itemId:" + itemParam.itemId);
                        return -1;
                    }
                }
            }

            itemParamListNoVirtual.add(itemParam);
        }

        return packItemStore.checkSubItemByParamBatch(itemParamListNoVirtual);
    }

    public List<SubItemResult> subItemByParam(ItemParam itemParam, SubItemReason reason)
    {
        return subItemByParamBatch(List.of(itemParam), reason);
    }

    public List<SubItemResult> subItemByParamBatch(List<ItemParam> itemParamList, SubItemReason reason)
    {
        List<SubItemResult> subItemResults = new ArrayList<>();
        List<ItemParam> itemParamListNoVirtual = new ArrayList<>();
        for (ItemParam itemParam : itemParamList)
        {
            cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemParam.itemId);
            if (itemConfig == null)
            {
                throw new UnknownLogicException("findItemConfig failed, uid:" + player.getUid() + " itemId:" + itemParam.itemId);
            }
            if (itemConfig.type == ItemType.VIRTUAL_VALUE)
            {
                SubItemResult subResult = new SubItemResult();
                subResult.itemId = itemParam.itemId;
                subResult.subCount = itemParam.count;
                switch (itemParam.itemId)
                {
                    case ProtoInner.VirtualItemId.VIRTUAL_ITEM_DIAMOND_COIN_VALUE:
                    {
//                        if (0 != subMcoin(itemParam.count, reason))
//                        {
//                            LOG_ERROR + "mcoin sub fails, cur_mcoin:" + getMcoin() + " count:" + itemParam.count + " uid:" + player.getUid();
//                            return -1;
//                        }
//                        subResult.cur_count = getMcoin();
//                        finanl_sub_result.emplace_back(subResult);
                    }
                    break;
                    default:
                    {
                        throw new UnknownLogicException("unknown virtual item, itemId:" + itemParam.itemId);
                    }
                }
            }
            itemParamListNoVirtual.add(itemParam);
        }

        subItemResults.addAll(packItemStore.subItemByParamBatch(itemParamListNoVirtual, reason));
//        triggerItemSubEvent(finanl_sub_result, reason);

        // 纪录日志
        return subItemResults;
    }

    public int checkSubItem(long guid)
    {
        return checkSubItemBatch(List.of(guid));
    }

    public int checkSubItemBatch(List<Long> guid_vec)
    {
        return packItemStore.checkSubItemBatch(guid_vec);
    }

    public List<SubItemResult> subItem(long guid, SubItemReason reason)
    {
        return subItemBatch(List.of(guid), reason);
    }

    public List<SubItemResult> subItemBatch(List<Long>guid_vec, SubItemReason reason)
    {
        List<SubItemResult> results = packItemStore.subItemBatch(guid_vec, reason);
//        triggerItemSubEvent(results, reason);

        // 纪录日志
        return results;
    }

    public Item forceAddEquipByAddAvatar(int itemId, ActionReason reason)
    {
        List<AddItemResult> results = packItemStore.forceAddEquipByAddAvatar(itemId, reason);

        // 通过result，记录信息在comp上
//        processByAddItemResultVec(results);

        // 触发任务和探索事件，不能触发虚拟道具
//        triggerItemAddEvent(results, false, reason);

        if (results.isEmpty())
        {
            return null;
        }
        else
        {
            return packItemStore.findItem(results.getFirst().guid);
        }
    }

    public int getPackMaterialCount(int itemId)
    {
        cfg.item.Item itemConfig = ExcelConfigMgr.getTables().getTbItem().get(itemId);
        if (itemConfig == null)
        {
            log.warn("findItemConfig failed, uid:" + player.getUid() + " itemId:" + itemId);
            return 0;
        }

        // 增加对虚拟道具的支持
        if (itemConfig.type == ItemType.VIRTUAL_VALUE)
        {
            switch (itemId)
            {
                case ProtoInner.VirtualItemId.VIRTUAL_ITEM_DIAMOND_COIN_VALUE:
                    return diamondCoin;
                default:
                    //LOG_ERROR + "Not Support virtual item: " + itemId;
                    return 0;
            }
        }

        return packItemStore.getMaterialCount(itemId);
    }
}
