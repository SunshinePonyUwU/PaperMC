package org.bukkit.craftbukkit.inventory;

import java.util.Arrays;

import net.minecraft.server.CraftingRecipe;
import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryCrafting;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CraftInventoryCrafting extends CraftInventory implements CraftingInventory {
    private IInventory resultInventory;

    public CraftInventoryCrafting(InventoryCrafting inventory, IInventory resultInventory) {
        super(inventory);
        this.resultInventory = resultInventory;
    }

    public IInventory getResultInventory() {
        return inventory;
    }

    public IInventory getMatrixInventory() {
        return resultInventory;
    }

    @Override
    public int getSize() {
        return getResultInventory().getSize() + getMatrixInventory().getSize();
    }

    @Override
    public void setContents(ItemStack[] items) {
        int resultLen = getResultInventory().getContents().length;
        int len = getMatrixInventory().getContents().length + resultLen;
        if (len > items.length) {
            throw new IllegalArgumentException("Invalid inventory size; expected " + len + " or less");
        }
        setContents(items[0], Arrays.copyOfRange(items, 1, items.length));
    }

    @Override
    public CraftItemStack[] getContents() {
        CraftItemStack[] items = new CraftItemStack[getSize()];
        net.minecraft.server.ItemStack[] mcResultItems = getResultInventory().getContents();

        int i = 0;
        for (i = 0; i < mcResultItems.length; i++ ) {
            items[i] = new CraftItemStack(mcResultItems[i]);
        }

        net.minecraft.server.ItemStack[] mcItems = getMatrixInventory().getContents();

        for (int j = 0; j < mcItems.length; j++) {
            items[i + j] = new CraftItemStack(mcItems[j]);
        }

        return items;
    }

    public void setContents(ItemStack result, ItemStack[] contents) {
        setResult(result);
        setMatrix(contents);
    }

    @Override
    public CraftItemStack getItem(int index) {
        if (index < getResultInventory().getSize()) {
            net.minecraft.server.ItemStack item = getResultInventory().getItem(index);
            return item == null ? null : new CraftItemStack(item);
        } else {
            net.minecraft.server.ItemStack item = getMatrixInventory().getItem(index - getResultInventory().getSize());
            return item == null ? null : new CraftItemStack(item);
        }
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (index < getResultInventory().getSize()) {
            getResultInventory().setItem(index, (item == null ? null : CraftItemStack.createNMSItemStack(item)));
        } else {
            getMatrixInventory().setItem((index - getResultInventory().getSize()), (item == null ? null : CraftItemStack.createNMSItemStack(item)));
        }
    }

    public ItemStack[] getMatrix() {
        CraftItemStack[] items = new CraftItemStack[getSize()];
        net.minecraft.server.ItemStack[] matrix = getMatrixInventory().getContents();

        for (int i = 0; i < matrix.length; i++ ) {
            items[i] = new CraftItemStack(matrix[i]);
        }

        return items;
    }

    public ItemStack getResult() {
        net.minecraft.server.ItemStack item = getResultInventory().getItem(0);
        if(item != null) return new CraftItemStack(item);
        return null;
    }

    public void setMatrix(ItemStack[] contents) {
        if (getMatrixInventory().getContents().length > contents.length) {
            throw new IllegalArgumentException("Invalid inventory size; expected " + getMatrixInventory().getContents().length + " or less");
        }

        net.minecraft.server.ItemStack[] mcItems = getMatrixInventory().getContents();

        for (int i = 0; i < mcItems.length; i++ ) {
            if (i < contents.length) {
                ItemStack item = contents[i];
                if (item == null || item.getTypeId() <= 0) {
                    mcItems[i] = null;
                } else {
                    mcItems[i] = new net.minecraft.server.ItemStack( item.getTypeId(), item.getAmount(), item.getDurability());
                }
            } else {
                mcItems[i] = null;
            }
        }
    }

    public void setResult(ItemStack item) {
        net.minecraft.server.ItemStack[] contents = getResultInventory().getContents();
        if (item == null || item.getTypeId() <= 0) {
            contents[0] = null;
        } else {
            contents[0] = new net.minecraft.server.ItemStack( item.getTypeId(), item.getAmount(), item.getDurability());
        }
    }

    public Recipe getRecipe() {
        CraftingRecipe recipe = ((InventoryCrafting)getInventory()).currentRecipe;
        return recipe == null ? null : recipe.toBukkitRecipe();
    }
}